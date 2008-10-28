package com.surelogic.common.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Partial implementation of a progress monitor that remembers if it has been
 * canceled. This implementation is thread-safe.
 * <p>
 * Because cancellation is largely orthogonal to progress monitoring this can be
 * a useful building block for progress monitor implementations.
 */
public abstract class CancellableSLProgressMonitor implements SLProgressMonitor {

	private final AtomicBoolean f_canceled = new AtomicBoolean(false);

	public boolean isCanceled() {
		return f_canceled.get();
	}

	public void setCanceled(boolean value) {
		f_canceled.set(value);
	}
}
