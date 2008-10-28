package com.surelogic.common.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

public final class NullSLProgressMonitor implements SLProgressMonitor {

	private static final SLProgressMonitorFactory FACTORY = new SLProgressMonitorFactory() {
		public SLProgressMonitor createSLProgressMonitor(final String taskName) {
			return new NullSLProgressMonitor();
		}
	};

	public static SLProgressMonitorFactory getFactory() {
		return FACTORY;
	}

	private final AtomicBoolean f_canceled = new AtomicBoolean(false);

	public void begin() {
		// Do nothing
	}

	public void begin(int totalWork) {
		// Do nothing
	}

	public void done() {
		// Do nothing

	}

	public boolean isCanceled() {
		return f_canceled.get();
	}

	public void setCanceled(boolean value) {
		f_canceled.set(value);
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
