package com.surelogic.common.jobs.remote;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	protected AbstractLocalSLJob(String name, int work, ILocalConfig config) {
		this(name, work, config, -1);
	}
		
	protected AbstractLocalSLJob(String name, int work, ILocalConfig config, int port) {
		super(name);
		this.work  = work;
		if (work <= 0) {
			throw new IllegalArgumentException("work <= 0");
		}
		testCode   = TestCode.getTestCode(config.getTestCode());
		memorySize = config.getMemorySize();
		this.verbose = config.isVerbose();
		this.port = port;
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
	
	private String copyException(Remote type, String msg, BufferedReader br)
	throws IOException {
		String label;
		if (tasks.isEmpty()) {
			label = getName();
		} else {
			SubSLProgressMonitor mon = tasks.peek();
			label = mon.getName();
		}
		StringBuilder sb = new StringBuilder(label + ' ' + type.toString().toLowerCase());
		System.out.println("Sierra tool "+type.toString().toLowerCase()+":"+msg);
		sb.append(": ").append(msg).append('\n');

		String line = br.readLine();
		while (line != null && line.startsWith("\t")) {
			System.out.println(line);
			sb.append(' ').append(line).append('\n');
			line = br.readLine();
		}
		if (line != null) {
			System.out.println(line);
		}
		final String errMsg = sb.toString();
		final SLStatus child;
		switch (type) {
		case FAILED:
			child = SLStatus.createErrorStatus(-1, errMsg);
			break;
		default:
			child = SLStatus.createWarningStatus(-1, errMsg);
		    break;
		}
		status.addChild(child);
		return errMsg;
	}
	
	static class Streams {
		final InputStream in;
		final OutputStream out;
		
		Streams(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}
	}
	
	private Streams getStreams(Process p) throws Exception {
		if (port > 0) {
			// TODO wait until the process has really started up?
			int retry = 3;
			do {
				retry--;
				try {
					Thread.sleep(100);
					Socket s = new Socket("localhost", port);					
					startOutputProcessingThread(p);
					return new Streams(s.getInputStream(), s.getOutputStream());
				} catch(Exception e) {
					if (retry <= 0) {
						// Try to read the first part of the stream, because something failed
						final byte[] buf = new byte[1024];	
						final int read = p.getInputStream().read(buf);
						System.out.write(buf, 0, read);
						System.out.println();
						System.out.flush();
						throw e;
					}
				}
			} while (retry > 0);
		}
		return new Streams(p.getInputStream(), p.getOutputStream());
	}
	
	/**
	 * Starts a thread to process the output from p
	 * (needed to keep it from blocking)
	 */
	private void startOutputProcessingThread(final Process p) {			
		Thread t = new Thread(new Runnable() {
			public void run() {
				final byte[] buf = new byte[1024];	
				int read;
				try {
					while ((read = p.getInputStream().read(buf)) > 0) {
						System.out.write(buf, 0, read);
					}	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public SLStatus run(final SLProgressMonitor topMonitor) {
		try {
			final boolean debug = true;//verbose && LOG.isLoggable(Level.FINE);
			CommandlineJava cmdj = new CommandlineJava();
			setupJVM(debug, cmdj);

			if (debug) {
				System.out.println("Starting process:");
				for (String arg : cmdj.getCommandline()) {
					System.out.println("\t" + arg);
				}
			}
			ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());
			pb.redirectErrorStream(true);

			Process p = pb.start();
			Streams s = getStreams(p);
			BufferedReader br = new BufferedReader(new InputStreamReader(s.in));
			String firstLine = br.readLine();
			if (debug) {
				while (firstLine != null) {
					// Copy verbose output until we get to the first line
					// from RemoteTool
					if (firstLine.startsWith("[")) {
						// if (!firstLine.endsWith("rt.jar]")) {
						System.out.println(firstLine);
						// }
						firstLine = br.readLine();
					} else {
						break;
					}
				}
			}
			if (verbose) {
				System.out.println("First line = " + firstLine);
			}

			if (firstLine == null) {
				throw newException(RemoteSLJobConstants.ERROR_NO_OUTPUT_FROM_JOB);
			}
			final String[] firstLines = new String[FIRST_LINES];
			int numLines = 1;
			firstLines[0] = firstLine;

			// Copy any output
			final PrintStream pout = new PrintStream(s.out);
			if (TestCode.SCAN_CANCELLED.equals(testCode)) {
				cancel(p, pout);
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
					cancel(p, pout);
				}

				if (line.startsWith("##")) {
					StringTokenizer st = new StringTokenizer(line, "#,");
					if (st.hasMoreTokens()) {
						String first = st.nextToken();
						Remote cmd   = Remote.valueOf(first);
						switch (cmd) {
						case TASK:
							if (verbose) {
								System.out.println(line);
							}
							final String task = st.nextToken();
							final String work = st.nextToken();
							// LOG.info(task+": "+work);
							SubSLProgressMonitor mon = new SubSLProgressMonitor(monitor, task, 1);
							tasks.push(mon);
							mon.begin(Integer.valueOf(work.trim()));
							break;
						case SUBTASK:
							monitor.subTask(st.nextToken());
							break;
						case WORK:
							monitor.worked(Integer.valueOf(st.nextToken()
									.trim()));
							break;
						case WARNING:
							if (verbose) {
								System.out.println(line);
							}
							copyException(cmd, st.nextToken(), br);
							break;
						case FAILED:
							if (verbose) {
								System.out.println(line);
							}
							String msg = copyException(cmd, st.nextToken(), br);
							System.out.println("Terminating run");
							p.destroy();
							if (msg
									.contains("FAILED:  java.lang.OutOfMemoryError")) {
								throw newException(
										RemoteSLJobConstants.ERROR_MEMORY_SIZE_TOO_SMALL,
										memorySize);
							}
							throw new RuntimeException(msg);
						case DONE:
							if (verbose) {
								System.out.println(line);
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
								System.out.println(line);
							}
						}
					} else if (verbose) {
						System.out.println(line);
					}
				} else if (verbose) {
					System.out.println(line);
				}
				line = br.readLine();
			}
			line = br.readLine();
			if (line != null) {
				System.out.println(line);
			}
			// See if the process already died?
			int value = handleExitValue(p);
			br.close();
			pout.close();
			if (value != 0) {
				examineFirstLines(firstLines);
				throw newException(RemoteSLJobConstants.ERROR_PROCESS_FAILED, value);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// FIX status not built if exception thrown
		return status.build();
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
		cmdj.setClassname(getRemoteClass().getCanonicalName());
		
		final Project proj = new Project();
		final Path path = cmdj.createClasspath(proj);
		setupClassPath(debug, cmdj, proj, path);
		// TODO convert into error if things are really missing
		if (debug) {
			for (String p : path.list()) {
				if (!new File(p).exists()) {
					System.out.println("Does not exist: " + p);
				} else if (debug) {
					System.out.println("Path: " + p);
				}
			}
		}		
		//cmdj.createArgument().setValue("This is a argument.");
		if (port > 0) {
			cmdj.createVmArgument().setValue("-D"+RemoteSLJobConstants.REMOTE_PORT_PROP+"="+port);
		}
		finishSetupJVM(debug, cmdj, proj);
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
	
	private void cancel(Process p, final PrintStream pout) {
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
				System.out.println("Process result after waiting = " + value);
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
				throw new RuntimeException(
						"Timeout waiting for process to exit: " + time
								+ " ms");
			}
			System.out.println("Process result after waiting = " + value);
		}
		return value;
	}
}
