package com.surelogic.common.jobs.remote;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.SLUtility;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;

/**
 * This is the job that runs in our JVM, managing the remote JVM
 * 
 * @author Edwin
 */
public abstract class AbstractLocalSLJob extends AbstractSLJob {
    protected static final Logger LOG = SLLogger.getLogger();
	private static final int FIRST_LINES = 3;
	
	protected final boolean verbose;
	protected final int work;
	protected final TestCode testCode;
	protected final int memorySize;
	private final int port; // <=0 if just using System.in/out
	protected final SLStatus.Builder status   = new SLStatus.Builder();
	private Stack<SubSLProgressMonitor> tasks = new Stack<SubSLProgressMonitor>();
	private SLProgressMonitor topMonitor;
	private Process remoteVM;
	private Thread handlerThread; // Only if using a port
	
	protected AbstractLocalSLJob(String name, int work, ILocalConfig config) {
		this(name, work, config, null);
	}
		
	protected AbstractLocalSLJob(String name, int work, ILocalConfig config, Console console) {
		super(name);
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
                log = new PrintStream(new File(config.getLogPath()));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
	}
	
	public synchronized void setHandlerThread(Thread handler) {
		handlerThread = handler;
	}
	
	protected RemoteSLJobException newException(int number, Object... args) {
		throw new RemoteSLJobException(number, args);
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
	
	private String copyException(final Remote type, final String msg, final BufferedReader br)
	throws IOException {
		String label;
		if (tasks.isEmpty()) {
			label = getName();
		} else {
			SubSLProgressMonitor mon = tasks.peek();
			label = mon.getName();
		}
		final StringBuilder sb = new StringBuilder(label + ' ' + type.toString().toLowerCase());
		println("Sierra tool "+type.toString().toLowerCase()+":"+msg);
		sb.append(": ").append(msg).append('\n');

		// Reconstitute stack trace
		final List<StackTraceElement> trace = new ArrayList<StackTraceElement>();				
		final String exception = br.readLine();
		String line = br.readLine();
		while (line != null && line.startsWith("\t")) {
			println(line);
			sb.append(' ').append(line).append('\n');
			line = br.readLine();
			
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
		}
		if (line != null) {
			println(line);
		}
		
		final Exception ex = new Exception(exception);
		ex.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
		
		final String errMsg = sb.toString();
		final SLStatus child;
		switch (type) {
		case FAILED:
			child = SLStatus.createErrorStatus(-1, errMsg, ex);
			break;
		default:
			child = SLStatus.createWarningStatus(-1, errMsg, ex);
		    break;
		}
		status.addChild(child);
		return errMsg;
	}
	
	final boolean debug = true;//verbose && LOG.isLoggable(Level.FINE);
	
	public void reportException(Exception e) {
		status.addChild(SLStatus.createErrorStatus(e));		
	}
	
	public SLStatus run(final SLProgressMonitor topMonitor) {
		try {
			this.topMonitor = topMonitor;
			
			CommandlineJava cmdj = new CommandlineJava();
			setupJVM(debug, cmdj);

			if (debug) {
				println("Starting process:");
				for (String arg : cmdj.getCommandline()) {
					println("\t" + arg);
				}
			}
			ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());
			pb.redirectErrorStream(true);

			remoteVM = pb.start();
			if (port < 0) {
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
			reportException(e);
		}
		if (log != null) {
		    log.close();
		}
		return status.build();
	}

    public void handleInput(BufferedReader br, BufferedWriter outputStream) {			
		try {
			String firstLine = br.readLine();
			if (debug) {
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
			}
			if (verbose) {
				println("First line = " + firstLine);
			}

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
						Remote cmd   = Remote.valueOf(first);
						switch (cmd) {
						case TASK:
							if (verbose) {
								println(line);
							}
							final String task = st.nextToken();
							final String work = st.nextToken();
							// LOG.info(task+": "+work);
							SubSLProgressMonitor mon = new SubSLProgressMonitor(monitor, task, this.work);
							tasks.push(mon);
							mon.begin(Integer.valueOf(work.trim()));
							break;
						case SUBTASK:
							if (verbose && !line.contains("Uniqueness")) {
								println(line);
							}
							monitor.subTask(st.nextToken());
							break;
						case SUBTASK_DONE:
							if (verbose) {
								println(line);
							}
							monitor.subTaskDone();
							break;
						case WORK:
							if (verbose) {
								println(line);
							}
							monitor.worked(Integer.valueOf(st.nextToken()
									.trim()));
							break;
						case WARNING:
							if (verbose) {
								println(line);
							}
							copyException(cmd, st.nextToken(), br);
							break;
						case FAILED:
							if (verbose) {
								println(line);
							}
							String msg = copyException(cmd, st.nextToken(), br);
							println("Terminating run");
							remoteVM.destroy();
							if (msg
									.contains("FAILED:  java.lang.OutOfMemoryError")) {
								throw newException(
										RemoteSLJobConstants.ERROR_MEMORY_SIZE_TOO_SMALL,
										memorySize);
							}
							throw new RuntimeException(msg);
						case DONE:
							if (verbose) {
								println(line);
							}
							tasks.pop();
							/*
							if (tasks.isEmpty()) {
								monitor.done();
								break loop;
							}
							*/
							break;
						default:
							if (verbose) {
								println(line);
							}
						}
					} else if (verbose) {
						println(line);
					}
				} else if (verbose) {
					println(line);
				}
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
			reportException(e);
		}
	}

	protected final void setupJVM(boolean debug, CommandlineJava cmdj) {
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
		if (SLUtility.is64bit) {
			cmdj.createVmArgument().setValue("-XX:MaxPermSize=256m");
		} else {
			cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");
		}
		/*
		if (false) {
			cmdj.createVmArgument().setValue("-verbose");
		}
		*/
		cmdj.setClassname(getRemoteClassName());
		
		final Project proj = new Project();
		final Path path = cmdj.createClasspath(proj);
		setupClassPath(debug, cmdj, proj, path);
		// TODO convert into error if things are really missing
		if (debug) {
			for (String p : path.list()) {
				if (!new File(p).exists()) {
					println("Does not exist: " + p);
				} else if (debug) {
					println("Path: " + p);
				}
			}
		}		
		//cmdj.createArgument().setValue("This is a argument.");
		if (port > 0) {
			cmdj.createVmArgument().setValue("-D"+RemoteSLJobConstants.REMOTE_PORT_PROP+"="+port);
		}
		finishSetupJVM(debug, cmdj, proj);
	}
	
	protected String getRemoteClassName() {
		return getRemoteClass().getCanonicalName();
	}
	
	/**
	 * @return The subclass of AbstractRemoteSLJob to be run on the remote JVM
	 */
	protected abstract Class<?> getRemoteClass();
	
	/**
	 * Setup the classpath for the remote JVM
	 * @param cmdj 
	 */
	protected abstract void setupClassPath(boolean debug, CommandlineJava cmdj, Project proj, Path path);
	
	/**
	 * Finish setting JVM arguments
	 */
	protected abstract void finishSetupJVM(boolean debug, CommandlineJava cmdj, Project proj);
	
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
			if (verbose) {
				println("Process result after waiting = " + value);
			}
		} catch (IllegalThreadStateException e) {
			// Not done yet
			final Thread currentThread = Thread.currentThread();
			Thread t = new Thread() {
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
				println("Timeout waiting for process to exit: " + time
								+ " ms");
				throw new RuntimeException(e);

			}
			println("Process result after waiting = " + value);
		}
		return value;
	}
	
	private PrintStream log;
	
	protected final void println(String msg) {
	    System.out.println(msg);
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
