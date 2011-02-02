package com.surelogic.common.eclipse.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.eclipse.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

/**
 * Adapts an {@link SLJob} so that it can execute in Eclipse as job.
 */
public final class SLJobWrapper extends Job {

	private final SLJob f_job;

	SLJob getWrappedJob() {
		return f_job;
	}

	public SLJobWrapper(final SLJob job) {
		super(job.getName());
		f_job = job;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final SLStatus status = f_job.run(new SLProgressMonitorWrapper(monitor,
				f_job.getName()));
		return SLEclipseStatusUtility.convert(status);
	}
}
