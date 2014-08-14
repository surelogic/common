package com.surelogic.common.jobs.remote;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.logging.*;

import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.*;
import com.surelogic.common.CommonJVMPrefs;
import com.surelogic.common.SLUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;

/**
 * This is the job that runs in our JVM, managing the remote JVM
 * 
 * @author Edwin
 */
@Region("LocalState")
@RegionLock("LocalLock is this protects LocalState")
public abstract class AbstractLocalSLJob<C extends ILocalConfig> extends AbstractSLJob {
	public static final String COMMON_PLUGIN_ID = "com.surelogic.common";
	
    protected static final Logger LOG = SLLogger.getLogger();
	private static final int FIRST_LINES = 3;
	
	protected final boolean verbose;
	protected final int work;
	protected final TestCode testCode;
	protected final int memorySize;
	private final int port; // <=0 if just using System.in/out
	protected final SLStatus.Builder status   = new SLStatus.Builder();
	private final Stack<SubSLProgressMonitor> tasks = new Stack<SubSLProgressMonitor>();
	private SLProgressMonitor topMonitor;
	private Process remoteVM;
	@InRegion("LocalState")
	private Thread handlerThread; // Only if using a port
	protected final C config;
	
	protected AbstractLocalSLJob(String name, int work, C config) {
		this(name, work, config, null);
	}
		
	protected AbstractLocalSLJob(String name, int work, C config, Console console) {
		super(name);
		this.config = config;
		this.work  = work;
		if (work <= 0) {
			throw new IllegalArgumentException("work <= 0");
		}
		testCode   = TestCode.getTestCode(config.getTestCode());
		memorySize = config.getMemorySize();
		this.verbose = config.isVerbose();
		this.port = console == null ? -1 : console.getPort();
		if (config.getLogPath() != null) {
		    try {
		    	System.out.println("Creating log file");
                log = new PrintStream(new File(config.getLogPath()));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		} else {
			System.out.println("No log file to open");
		}
	}
	
	public synchronized void setHandlerThread(Thread handler) {
		handlerThread = handler;
	}
	
	protected RemoteSLJobException newException(int number, Object... args) {
		throw new RemoteSLJobException(getName(), number, args);
	}
	
	protected boolean addToPath(Project proj, Path path, String name) {
		return addToPath(proj, path, new File(name), true);
	}
	
	protected boolean addToPath(Project proj, Path path, String name, boolean required) {
		return addToPath(proj, path, new File(name), required);
	}

	protected boolean addToPath(Project proj, Path path, File f,
			boolean required) {
		final boolean exists = f.exists();
		if (!exists) {
			if (required) {
				throw newException(
						RemoteSLJobConstants.ERROR_CODE_MISSING_FOR_JOB,
						f.getAbsolutePath());
			}
		} else if (TestCode.MISSING_CODE.equals(testCode)) {
			throw newException(
					RemoteSLJobConstants.ERROR_CODE_MISSING_FOR_JOB, f.getAbsolutePath());
		} else {
			path.add(new Path(proj, f.getAbsolutePath()));
		}
		return exists;
	}
	
	protected void findJars(Project proj, Path path, String folder) {
		findJars(proj, path, new File(folder));
	}

	protected void findJars(Project proj, Path path, File folder) {
		for (File f : folder.listFiles()) {
			String name = f.getAbsolutePath();
			if (name.endsWith(".jar")) {
				path.add(new Path(proj, name));
			}
		}
	}
	
	class ExceptionalResult {
		final String errorMsg;
		final String nextLine;
		
		ExceptionalResult(String e, String l) {
			errorMsg = e;
			nextLine = l;
		}
	}
	
	private ExceptionalResult copyException(final Remote type, final String msg, final BufferedReader br)
	throws IOException {
		String label;
		if (tasks.isEmpty()) {
			label = getName();
		} else {
			SubSLProgressMonitor mon = tasks.peek();
			label = mon.getName();
		}
		final StringBuilder sb = new StringBuilder(label + ' ' + type.toString().toLowerCase());
		// printErr("Tool got "+type.toString().toLowerCase()+":"+msg);
		sb.append(": ").append(msg).append('\n');

		// Reconstitute stack trace
		final List<StackTraceElement> trace = new ArrayList<StackTraceElement>();				
		final String exception = br.readLine();
		//printErr(exception);
		
		String line = br.readLine();
		while (line != null && line.startsWith("\t")) {
			//printErr(line);
			sb.append(' ').append(line).append('\n');
			
			// \tat pkg.getMethod(Foo.java:99)
			final String[] tokens = line.split("[ (:)]");
			int lastDot = tokens[1].lastIndexOf('.');
			String tdecl, method;
			if (lastDot < 0) {
				tdecl  = "";
				method = tokens[1];
			} else {
				tdecl  = tokens[1].substring(0, lastDot);
				method = tokens[1].substring(lastDot+1);
			}
			try {
				StackTraceElement ste = new StackTraceElement(tdecl, method, tokens[2], Integer.parseInt(tokens[3]));
				trace.add(ste);
			} catch(NumberFormatException nfe) {
				// Ignore this line, since it's not part of the trace
			}
			line = br.readLine();
		}
		/*
		if (line != null) {
			printErr(line);
		}
		*/
		final Exception ex;
		if (!trace.isEmpty()) {
			ex = new Exception(exception);
			ex.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
		} else {
			ex = null;
		}
		final String errMsg = sb.toString();
		final SLStatus child;
		switch (type) {
		case FAILED:
			printErr(Level.SEVERE, errMsg, ex);
			child = SLStatus.createErrorStatus(-1, errMsg, ex);
			break;
		default:
			printErr(Level.WARNING, errMsg, ex);
			child = SLStatus.createWarningStatus(-1, errMsg, ex);
		    break;
		}
		status.addChild(child);
		return new ExceptionalResult(errMsg, line);
	}
	
	public void reportException(Exception e) {
		status.addChild(SLStatus.createErrorStatus(e));		
	}
		
	@Override
  public SLStatus run(final SLProgressMonitor topMonitor) {
		try {
			this.topMonitor = topMonitor;
			
			CommandlineJava cmdj = new CommandlineJava();
			setupJVM(cmdj);
			
			println("Starting process:");
			for (String arg : cmdj.getCommandline()) {
				println("\t" + arg);
			}
			
			ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());
			pb.redirectErrorStream(true);

			remoteVM = pb.start();
			if (port <= 0) {
				// Use stdin/out
				BufferedReader br = new BufferedReader(new InputStreamReader(remoteVM.getInputStream()));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(remoteVM.getOutputStream()));
				handleInput(br, bw);
			} else {
				// Handle stdin to prevent the remote VM from blocking
				final byte[] buf = new byte[1024];	
				int read;
				try {
					while ((read = remoteVM.getInputStream().read(buf)) > 0) {
						write(buf, 0, read);
					}	
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Wait for the handler to finish
				synchronized (this) {
					if (handlerThread != null) {
						handlerThread.join();
					}
				}
			}
		} catch(Exception e) {
			if (e instanceof RemoteSLJobException) {
				if (topMonitor.isCanceled()) {
					return SLStatus.CANCEL_STATUS;
				}
				throw (RemoteSLJobException) e;
			}
			else if (e instanceof CancellationException) {
				return SLStatus.createCancelStatus(e);
			}
			reportException(e);
		}
		if (log != null) {
			System.out.println("Closing log file");
		    log.close();
		} else {
			System.out.println("No log file to close");
		}
		return status.build();
	}

    public void handleInput(BufferedReader br, BufferedWriter outputStream) {			
		try {
			String firstLine = br.readLine();
			while (firstLine != null) {
				// Copy verbose output until we get to the first line
				// from RemoteTool
				if (firstLine.startsWith("[")) {
					// if (!firstLine.endsWith("rt.jar]")) {
					println(firstLine);
					// }
					firstLine = br.readLine();
				} else {
					break;
				}
			}			
			println("First line = " + firstLine);			

			if (firstLine == null) {
				throw newException(RemoteSLJobConstants.ERROR_NO_OUTPUT_FROM_JOB);
			}
			final String[] firstLines = new String[FIRST_LINES];
			int numLines = 1;
			firstLines[0] = firstLine;

			// Copy any output
			final PrintWriter pout = new PrintWriter(outputStream);
			if (TestCode.SCAN_CANCELLED.equals(testCode)) {
				cancel(remoteVM, pout);
			}
			topMonitor.begin(work);
			
			// Used to help detect imminent OOM issues
			int numConsecutiveGCs = 0;
			String line = br.readLine();
			while (line != null) {
				final SLProgressMonitor monitor = 
					tasks.isEmpty() ? topMonitor : tasks.peek();
				
				if (numLines < FIRST_LINES) {
					firstLines[numLines] = line;
					numLines++;
				}
				if (monitor.isCanceled()) {
					cancel(remoteVM, pout);
				}

				if (line.startsWith("##")) {
					StringTokenizer st = new StringTokenizer(line, "#,");
					if (st.hasMoreTokens()) {
						String first = st.nextToken();
						Remote cmd;
						try {
							cmd = Remote.valueOf(first);
						} catch(Exception e) {
							cmd = Remote.OTHER;
						}
						switch (cmd) {
						case TASK:
							println(line);							
							final String task = st.nextToken();
							final String work = st.nextToken();
							// LOG.info(task+": "+work);
							SubSLProgressMonitor mon = new SubSLProgressMonitor(monitor, task, this.work);
							tasks.push(mon);
							mon.begin(Integer.valueOf(work.trim()));
							break;
						case SUBTASK:
							println(line);							
							monitor.subTask(st.nextToken());
							break;
						case SUBTASK_DONE:							
							println(line);							
							monitor.subTaskDone();
							break;
						case WORK:							
							println(line);					
							try {								
								monitor.worked(Integer.valueOf(st.nextToken()
										.trim()));
							} catch (NumberFormatException e) {
								printErr(Level.INFO, "Couldn't parse amount worked: "+line);
								monitor.worked(1);
							}
							break;
						case WARNING:							
							printErr(Level.WARNING, getRest(line));
							break;
						case WARNING_TRACE:
							printErr(Level.WARNING, getRest(line));
							ExceptionalResult w = copyException(cmd, st.nextToken(), br);
							line = w.nextLine;
							continue;
						case FAILED:						
							printErr(Level.SEVERE, getRest(line));
							ExceptionalResult e = copyException(cmd, st.nextToken(), br);
							println(e.nextLine);
							printErr(Level.SEVERE, "Terminating run");
							remoteVM.destroy();
							if (e.errorMsg
									.contains("FAILED:  java.lang.OutOfMemoryError")) {
								throw newException(
										RemoteSLJobConstants.ERROR_MEMORY_SIZE_TOO_SMALL,
										memorySize);
							}
							throw new RuntimeException(e.errorMsg);
						case CANCELLED:
							printErr(Level.WARNING, "Cancelling run: "+getRest(line));
							throw new CancellationException(getRest(line));
						case DONE:							
							println(line);							
							tasks.pop();
							/*
							if (tasks.isEmpty()) {
								monitor.done();
								break loop;
							}
							*/
							break;
						default:
							println(line);
						} // end of switch
					} else {
						println(line);
					} // end of check for more tokens
					
					numConsecutiveGCs = 0;
				} else {
					if (line.startsWith("[Full GC")) {
						numConsecutiveGCs++;
						if (numConsecutiveGCs > 3) {
							printErr(Level.WARNING, "Probably low on memory: "+numConsecutiveGCs+" full GCs");
						}
					} else {
						numConsecutiveGCs = 0;
					}
					println(line);
				} // end of check for ##
				line = br.readLine();
			}
			line = br.readLine();
			if (line != null) {
				println(line);
			}
			// See if the process already died?
			int value = handleExitValue(remoteVM);
			br.close();
			pout.close();
			if (value != 0) {
				examineFirstLines(firstLines);
				throw newException(RemoteSLJobConstants.ERROR_PROCESS_FAILED, value);
			}
		} catch (Exception e) {
			if (e instanceof RemoteSLJobException) {
				throw (RemoteSLJobException) e;
			}
			else if (e instanceof CancellationException) {
				throw (CancellationException) e;
			}
			reportException(e);
		}
	}

    private String getRest(String line) {
    	if (true) {
    		final int comma = line.indexOf(',');
    		return line.substring(comma+1);
    	} 
    	return line;
    }
    
	protected final void setupJVM(CommandlineJava cmdj) {
		if (testCode != null) {
			cmdj.createVmArgument().setValue(
					"-D" + RemoteSLJobConstants.TEST_CODE_PROPERTY + "="
							+ testCode);
		}
		if (TestCode.LOW_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("2m");
		} else if (TestCode.HIGH_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("2048m");
		} else if (TestCode.MAD_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("9999m");
		} else if (memorySize > 0) {
			cmdj.setMaxmemory(memorySize + "m");
		} else {
			if (SLUtility.is64bit) {
				cmdj.setMaxmemory("1500m");
			} else {
				cmdj.setMaxmemory("1024m");
			}
		}
		final Properties prefs = CommonJVMPrefs.getJvmPrefs();
		final String vmArgs = prefs.getProperty(CommonJVMPrefs.VMARGS);
		if (vmArgs != null) {
			for(String arg : vmArgs.split(" ")) {
				cmdj.createVmArgument().setValue(arg);
			}
		} else {
			if (SLUtility.is64bit) {
				cmdj.createVmArgument().setValue("-XX:MaxPermSize=256m");
			} else {
				cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");
			}				
			cmdj.createVmArgument().setValue("-verbosegc");
		}
		if (SLUtility.is64bit && (SystemUtils.JAVA_VENDOR.contains("Sun") || SystemUtils.JAVA_VENDOR.contains("Apple"))) {
		    // TODO do I need to check if I'm running in 64-bit mode?
		    cmdj.createVmArgument().setValue("-XX:+UseCompressedOops");
		}	
		if (XUtil.useExperimental) {
			cmdj.createVmArgument().setValue("-DSureLogicX=true");
		}
		/*
		if (false) {
			cmdj.createVmArgument().setValue("-verbose");
		}
		*/
		cmdj.setClassname(getRemoteClassName());
		
		final Project proj = new Project();
		final Path path = cmdj.createClasspath(proj);
		final ConfigHelper helper = new ConfigHelper(verbose, config);
		setupClassPath(helper, cmdj, proj, path);
		// TODO convert into error if things are really missing
		for (String p : path.list()) {
			if (!new File(p).exists()) {
				println("Does not exist: " + p);
			} else {
				println("Path: " + p);
			}
		}
				
		//cmdj.createArgument().setValue("This is a argument.");
		if (port > 0) {
			cmdj.createVmArgument().setValue("-D"+RemoteSLJobConstants.REMOTE_PORT_PROP+"="+port);
		}
		cmdj.createVmArgument().setValue("-D"+RemoteScanJob.RUN_DIR_PROP+"="+config.getRunDirectory());
		
		finishSetupJVM(verbose, cmdj, proj);
	}
	
	protected String getRemoteClassName() {
		return getRemoteClass().getCanonicalName();
	}
	
	/**
	 * @return The subclass of AbstractRemoteSLJob to be run on the remote JVM
	 */
	protected abstract Class<? extends AbstractRemoteSLJob> getRemoteClass();
	
	/**
	 * Setup the classpath for the remote JVM
	 * @param cmdj 
	 */
	protected abstract void setupClassPath(ConfigHelper util, CommandlineJava cmdj, Project proj, Path path);
	
	/**
	 * Finish setting JVM arguments
	 */
	protected void finishSetupJVM(boolean debug, CommandlineJava cmdj, Project proj) {
		// Nothing to do right now
	}
	
	private void cancel(Process p, final PrintWriter pout) {
		pout.println("##" + Local.CANCEL);
		p.destroy();
		throw newException(RemoteSLJobConstants.ERROR_JOB_CANCELLED);
	}

	private void examineFirstLines(String[] firstLines) {
		for (String line : firstLines) {
			if (line.startsWith("Could not reserve enough space")
					|| line.startsWith("Invalid maximum heap size")) {
				throw newException(
						RemoteSLJobConstants.ERROR_MEMORY_SIZE_TOO_BIG,
						memorySize);
			}
		}
	}
	
	private int handleExitValue(Process p) {
		int value;
		try {
			value = p.exitValue();
			println("Process result after waiting = " + value);			
		} catch (IllegalThreadStateException e) {
			// Not done yet
			final Thread currentThread = Thread.currentThread();
			Thread t = new Thread() {
				@Override
        public void run() {
					// Set to timeout in 1 minute
					try {
						Thread.sleep(60000);
						currentThread.interrupt();
					} catch (InterruptedException e) {
						// Just end
					}
				}
			};

			final long start = System.currentTimeMillis();
			t.start();
			try {
				value = p.waitFor();
				t.interrupt();
			} catch (InterruptedException ie) {
				long time = System.currentTimeMillis() - start;
				printErr(Level.WARNING, 
						 "Timeout waiting for process to exit: " + time	+ " ms");
				throw new RuntimeException(e);

			}
			println("Process result after waiting = " + value);
		}
		return value;
	}
	
	private PrintStream log;
	
	protected final void printErr(Level l, String msg) {
		printErr(l, msg, null);
	}
	
	protected final void printErr(Level l, String msg, Throwable t) {
		if (XUtil.testing) {
			try {
				if (t == null) {
					LOG.log(l, msg);
				} else {
					LOG.log(l, msg, t);
				}	
			} catch(NullPointerException e) {
				 System.out.println(msg);
				 if (t != null) {
					 t.printStackTrace(System.out);
				 }
			}
		} else {
			 System.out.println(msg);
			 if (t != null) {
				 t.printStackTrace(System.out);
			 }
		}
	    if (log != null) {
	        log.println(msg);
	        if (t != null) {
	        	t.printStackTrace(log);
	        }
	    }
	}
	
	protected final void println(String msg) {
		if (verbose) {
			System.out.println(msg);
		}
	    if (log != null) {
	        log.println(msg);
	    }
	}
	
    
    private void write(byte[] buf, int i, int read) {
        System.out.write(buf, i, read);
        if (log != null) {
            log.write(buf, i, read);
        }
    }
}
