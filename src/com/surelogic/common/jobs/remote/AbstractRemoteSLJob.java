/*
 * Created on Jan 11, 2008
 */
package com.surelogic.common.jobs.remote;

import java.io.*;
import java.net.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.*;

public abstract class AbstractRemoteSLJob {	
	private static final String CANCEL = "##" + Local.CANCEL;
	private InputStream in;
	protected PrintStream out;
	
	/*
	 * public static void main(String[] args) { RemoteJob job = new
	 * RemoteJob(args); job.run(); }
	 */

	@SuppressWarnings("incomplete-switch")
	protected final void run() {
		// Setup streams
		final String port = System.getProperty(RemoteSLJobConstants.REMOTE_PORT_PROP);
		/*
		System.out.println("port = "+port);
		if (true) {
			throw new IllegalStateException();
		}		
		*/
		Socket socket = null;
		if (port == null) {
			in  = System.in;
			out = System.out;
		} else {		
			try {
				socket = new Socket("localhost", Integer.parseInt(port));
				in = socket.getInputStream();
				out = new PrintStream(socket.getOutputStream());
			} catch (Exception e) {
			    e.printStackTrace();
				System.exit(-RemoteSLJobConstants.ERROR_PROCESS_FAILED);
			}
		} 
		
		final TestCode testCode = TestCode.getTestCode(System
				.getProperty(RemoteSLJobConstants.TEST_CODE_PROPERTY));
		if (TestCode.NO_TOOL_OUTPUT.equals(testCode)) {
			System.exit(-RemoteSLJobConstants.ERROR_NO_OUTPUT_FROM_JOB);
		}
		out.println("JVM started: "+System.getProperty("java.version"));
		synchronized (SLLogger.class) {
			out.println("Log level: " + SLLogger.LEVEL.get());
		}
		/*
		 * out.println("java.system.class.loader = "+System.getProperty("java.system.class.loader"
		 * ));out.println("System classloader = "+ClassLoader.
		 * getSystemClassLoader()); final String auxPathFile =
		 * System.getProperty(SierraToolConstants.AUX_PATH_PROPERTY); if
		 * (auxPathFile != null) {
		 * out.println(SierraToolConstants.AUX_PATH_PROPERTY
		 * +"="+auxPathFile); File auxFile = new File(auxPathFile); if
		 * (auxFile.exists()) { // No longer needed after creating the system
		 * ClassLoader auxFile.delete(); } }
		 */

		final long start = System.currentTimeMillis();
		/*
		 * try { Logger LOG = SLLogger.getLogger("sierra"); } catch(Throwable t)
		 * { t.printStackTrace(); }
		 */

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			out.println("Created reader");

			final Monitor mon = new Monitor(br, out);
			checkInput(br, mon, "Created monitor");
			
			SLLogger.addHandler(new LogHandler(mon));
			checkInput(br, mon, "Created log handler");
			
			final SLJob job = init(br, mon);
			checkInput(br, mon, "Initialized job");
			if (job == null) {
				mon.failed("Null job");
				System.exit(-RemoteSLJobConstants.ERROR_JOB_FAILED);
			}
			
			switch (testCode) {
			case SCAN_FAILED:
				outputFailure(out, "Testing scan failure",
						new Throwable());
				break;
			case ABNORMAL_EXIT:
				System.exit(-RemoteSLJobConstants.ERROR_PROCESS_FAILED);
				break;
			case EXCEPTION:
				throw new Exception("Testing scan exception");
			}
			final SLStatus status = job.run(mon);
			final long end = System.currentTimeMillis();
			processStatus(mon, status);
			checkInput(br, mon, "Scanning complete (" + (end - start) + " ms)");

			if (socket == null) {
				out.println("No socket to close");
			}
			out.println("Closing std streams");
			System.out.close();
			System.in.close();
			
			if (socket != null) {
				out.println("Closing socket streams");
				out.println();
				out.flush();
				out.close();
				in.close();
				socket.close();
				System.exit(0);
			}
			System.exit(0);
		} catch (final Throwable e) {
			outputFailure(out, null, e);
			System.exit(-RemoteSLJobConstants.ERROR_JOB_FAILED);
		}
	}

	/**
	 * Do any setup before running the job
	 * e.g. reading system properties to initialize state
	 * 
	 * @return The initialized job to run
	 */
	protected abstract SLJob init(BufferedReader br, Monitor mon)
			throws Throwable;

	private static void processStatus(final Monitor mon, final SLStatus status) {
		// Only look at the leaves
		if (status.getChildren().isEmpty()) {
			if (status.getSeverity() == SLSeverity.OK) {
				return; // Nothing to do
			}
			if (status.getException() == null) {
				mon.error(status.getMessage());
			} else {
				mon.error(status.getMessage(), status.getException());
			}
		} else {
			for (final SLStatus c : status.getChildren()) {
				processStatus(mon, c);
			}
		}
	}

	private void outputFailure(final PrintStream out, final String msg,
			final Throwable e) {
		final StackTraceElement[] trace = e.getStackTrace();
		/*
		out.println("Caught exception");
		for (final StackTraceElement ste : trace) {
			out.println("\t at " + ste);
		}
		*/
		if (msg == null) {
			out.println("##" + Remote.FAILED + ", " + e.getClass().getName()
					+ " : " + e.getMessage());
			out.println(e.getClass().getName() + " : " + e.getMessage());
		} else {
			out.println("##" + Remote.FAILED + ", " + msg + " - "
					+ e.getClass().getName() + " : " + e.getMessage());
		}
		for (final StackTraceElement ste : trace) {
			out.println("\tat " + ste);
		}
		cleanup();
	}

	/**
	 * Used to cleanup when done
	 */
	protected void cleanup() {
		// Nothing to do yet
	}
	
	protected void checkInput(final BufferedReader br,
			final Monitor mon, final String msg) throws IOException {
		out.println(msg);
		checkIfCancelled(br, mon);
		out.flush();
	}

	private void checkIfCancelled(final BufferedReader br, final Monitor mon)
			throws IOException {
		if (br.ready()) {
			final String line = br.readLine();
			out.println("Received: " + line);
			if (CANCEL.equals(line)) {
				mon.setCanceled(true);
			}
		}
	}

	protected class Monitor implements SLProgressMonitor {
		private final BufferedReader br;
		public final PrintStream out;
		boolean cancelled = false;

		private Monitor(BufferedReader br, final PrintStream out) {
			this.br = br;
			this.out = out;
		}

		public void begin() {
			throw new IllegalStateException(
					"begin() can't be used in this context");
		}

		public void begin(final int totalWork) {
			out.println("##" + Remote.TASK + ", Scan, " + totalWork);
		}

		public void done() {
			out.println("##" + Remote.DONE);
		}

		public void error(final String msg) {
			out.println("##" + Remote.WARNING + ", " + msg);
		}

		public void error(final String msg, final Throwable t) {
			out.println("##" + Remote.WARNING_TRACE + ", " + msg);
			t.printStackTrace(out);
		}

		public void failed(final String msg) {
			setCanceled(true);
			final Throwable t = new Throwable();
			outputFailure(out, msg, t);
		}

		public void failed(final String msg, final Throwable t) {
			setCanceled(true);
			outputFailure(out, msg, t);
		}

		public Throwable getFailureTrace() {
			return null;
		}

		public void internalWorked(final double work) {
			// Do nothing
		}

		public boolean isCanceled() {
			return cancelled;
		}

		public void setCanceled(final boolean value) {
			cancelled = value;
		}

		public void setTaskName(final String name) {
			// TODO Auto-generated method stub

		}

		public void subTask(final String name) {
			out.println("##" + Remote.SUBTASK + ", " + name);
			checkIfCancelled();
		}
		
		public void subTaskDone() {
			out.println("##" + Remote.SUBTASK_DONE);
			checkIfCancelled();
		}

		public void worked(final int work) {
			out.println("##" + Remote.WORK + ", " + work);
			checkIfCancelled();
		}
		
		private void checkIfCancelled() {
			try {
				AbstractRemoteSLJob.this.checkIfCancelled(br, this);
			} catch(IOException e) {
				failed("Problem while checking if cancelled", e);
			}
		}
	}
	
	protected class LogHandler extends Handler {
		private final Monitor monitor;
		
		protected LogHandler(Monitor mon) {
			monitor = mon;
		}

		@Override
		public void publish(LogRecord record) {
			if (record == null) {
				return;
			}
			if (!isLoggable(record)) {
				return;
			}
			final String message = record.getMessage() == null ? "" : record
					.getMessage();
			final StringBuilder b = new StringBuilder();
			SLFormatter.formatMsgTail(b, message, record.getSourceClassName(),
					record.getSourceMethodName());
			
			final String msg  = b.toString();
			final Level level = record.getLevel();
			if (level == Level.SEVERE) {
				if (record.getThrown() != null) {
					monitor.failed(msg, record.getThrown());
				} else {
					monitor.failed(msg);
				}
			}
			else if (level == Level.WARNING) {
				if (record.getThrown() != null) {
					monitor.error(msg, record.getThrown());
				} else {
					monitor.error(msg);
				}
			}			
		}
		
		@Override
		public void close() throws SecurityException {
			// nothing to do
		}

		@Override
		public void flush() {
			// nothing to do
		}
	}
}
