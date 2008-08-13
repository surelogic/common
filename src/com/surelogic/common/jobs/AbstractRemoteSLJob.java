/*
 * Created on Jan 11, 2008
 */
package com.surelogic.common.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.surelogic.common.logging.SLLogger;

public abstract class AbstractRemoteSLJob {
	private static final String CANCEL = "##" + Local.CANCEL;

	/*
	 * public static void main(String[] args) { RemoteJob job = new
	 * RemoteJob(args); job.run(); }
	 */

	@SuppressWarnings("incomplete-switch")
	protected final void run() {
		final TestCode testCode = TestCode.getTestCode(System
				.getProperty(SLJobConstants.TEST_CODE_PROPERTY));
		if (TestCode.NO_TOOL_OUTPUT.equals(testCode)) {
			System.exit(-SLJobConstants.ERROR_NO_OUTPUT_FROM_JOB);
		}
		System.out.println("JVM started");
		System.out.println("Log level: " + SLLogger.LEVEL.get());
		/*
		 * System.out.println("java.system.class.loader = "+System.getProperty("java.system.class.loader"
		 * ));System.out.println("System classloader = "+ClassLoader.
		 * getSystemClassLoader()); final String auxPathFile =
		 * System.getProperty(SierraToolConstants.AUX_PATH_PROPERTY); if
		 * (auxPathFile != null) {
		 * System.out.println(SierraToolConstants.AUX_PATH_PROPERTY
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
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Created reader");

			final Monitor mon = new Monitor(System.out);
			checkInput(br, mon, "Created monitor");

			final SLJob job = init(br, mon);
			checkInput(br, mon, "Initialized job");

			switch (testCode) {
			case SCAN_FAILED:
				outputFailure(System.out, "Testing scan failure",
						new Throwable());
				break;
			case ABNORMAL_EXIT:
				System.exit(-SLJobConstants.ERROR_PROCESS_FAILED);
				break;
			case EXCEPTION:
				throw new Exception("Testing scan exception");
			}
			final SLStatus status = job.run(mon);
			final long end = System.currentTimeMillis();
			processStatus(mon, status);
			checkInput(br, mon, "Done scanning: " + (end - start) + " ms");
		} catch (final Throwable e) {
			outputFailure(System.out, null, e);
			System.exit(-SLJobConstants.ERROR_JOB_FAILED);
		}
	}

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

	private static void outputFailure(final PrintStream out, final String msg,
			final Throwable e) {
		final StackTraceElement[] trace = e.getStackTrace();
		out.println("Caught exception");
		for (final StackTraceElement ste : trace) {
			out.println("\t at " + ste);
		}
		if (msg == null) {
			out.println("##" + Remote.FAILED + ", " + e.getClass().getName()
					+ " : " + e.getMessage());
		} else {
			out.println("##" + Remote.FAILED + ", " + msg + " - "
					+ e.getClass().getName() + " : " + e.getMessage());
		}
		for (final StackTraceElement ste : trace) {
			out.println("\tat " + ste);
		}
	}

	protected static void checkInput(final BufferedReader br,
			final Monitor mon, final String msg) throws IOException {
		System.out.println(msg);
		if (br.ready()) {
			final String line = br.readLine();
			System.out.println("Received: " + line);
			if (CANCEL.equals(line)) {
				mon.setCanceled(true);
			}
		}
		System.out.flush();
	}

	protected static class Monitor implements SLProgressMonitor {
		final PrintStream out;
		boolean cancelled = false;

		private Monitor(final PrintStream out) {
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
			out.println("##" + Remote.WARNING + ", " + msg);
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
		}

		public void worked(final int work) {
			out.println("##" + Remote.WORK + ", " + work);
		}
	}
}
