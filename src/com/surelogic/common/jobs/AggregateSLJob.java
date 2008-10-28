package com.surelogic.common.jobs;

import java.util.List;

import com.surelogic.common.i18n.I18N;

/**
 * An implementation of {@link SLJob} that can aggregate the execution of
 * several jobs together into a single job. This is a very handy class for
 * multi-selection in views that trigger one or more jobs.
 */
public final class AggregateSLJob extends AbstractSLJob {

	private final List<SLJob> f_jobs;

	public AggregateSLJob(String name, List<SLJob> jobs) {
		super(name);
		if (jobs == null)
			throw new IllegalArgumentException(I18N.err(44, "jobs"));
		if (jobs.isEmpty())
			throw new IllegalArgumentException(I18N.err(155,
					AggregateSLJob.class.getName()));
		f_jobs = jobs;
	}

	public SLStatus run(SLProgressMonitor monitor) {
		final int perJobWork = 100;
		monitor.begin(f_jobs.size() * perJobWork);
		try {
			for (SLJob job : f_jobs) {
				final SLStatus status = invoke(job, monitor, perJobWork);
				if (status.getSeverity() != SLSeverity.OK)
					return status;
				if (monitor.isCanceled())
					return SLStatus.CANCEL_STATUS;
			}
		} finally {
			monitor.done();
		}
		return SLStatus.OK_STATUS;
	}
}
