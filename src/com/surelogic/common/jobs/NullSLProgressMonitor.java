package com.surelogic.common.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

public final class NullSLProgressMonitor implements SLProgressMonitor {

	private final AtomicBoolean f_canceled = new AtomicBoolean(false);

	public void beginTask(String name, int totalWork) {
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

	public void worked(int work) {
		// Do nothing
	}
}
