package com.surelogic.common.jobs;

/**
 * A progress monitor that tracks if the job was canceled but does not log
 * progress information.
 */
public class NullSLProgressMonitor extends CancellableSLProgressMonitor {

	/**
	 * A single factory instance for {@link NullSLProgressMonitor} instances.
	 */
	private static final SLProgressMonitorFactory FACTORY = new SLProgressMonitorFactory() {
		public SLProgressMonitor createSLProgressMonitor(final String taskName) {
			return new NullSLProgressMonitor();
		}
	};

	/**
	 * Gets a factory for {@link NullSLProgressMonitor} instances.
	 * 
	 * @return a factory for {@link NullSLProgressMonitor} instances.
	 */
	public static SLProgressMonitorFactory getFactory() {
		return FACTORY;
	}

	public void begin() {
		// Do nothing
	}

	public void begin(int totalWork) {
		// Do nothing
	}

	public void done() {
		// Do nothing
	}

	public void subTask(String name) {
		// Do nothing
	}

	public void subTaskDone() {
		// Do nothing
	}

	public void worked(int work) {
		// Do nothing
	}
}
