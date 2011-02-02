package com.surelogic.common.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

/**
 * Adapts an {@link SLJob} so that it can execute in Eclipse as a SureLogic
 * database job.
 */
public final class SLDatabaseJobWrapper extends DatabaseJob {

	private final SLJob f_job;

	SLJob getWrappedJob() {
		return f_job;
	}

	/**
	 * Constructs a wrapped database job.
	 * 
	 * @param job
	 *            the job to wrap.
	 * @param accessKeys
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 */
	public SLDatabaseJobWrapper(final SLJob job, final String... accessKeys) {
		super(job.getName(), accessKeys);
		f_job = job;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		return run(f_job, monitor);
	}

	static final IStatus run(SLJob job, IProgressMonitor monitor) {
		final SLStatus status = job.run(new SLProgressMonitorWrapper(monitor,
				job.getName()));
		return SLEclipseStatusUtility.convert(status);
	}
}
