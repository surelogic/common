package com.surelogic.common.eclipse.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AggregateSLJob;
import com.surelogic.common.jobs.SLJob;

/**
 * A singleton to submit jobs within Eclipse.
 */
public final class EclipseJob {

	private static final EclipseJob INSTANCE = new EclipseJob();

	public static EclipseJob getInstance() {
		return INSTANCE;
	}

	private EclipseJob() {
		// singleton
	}

	/**
	 * Schedules a database job.
	 * 
	 * @param job
	 *            the job that uses the database.
	 * @throws IllegalArgumentException
	 *             if {@code job==null}.
	 */
	public void scheduleDb(final SLJob job) {
		schedule(job, false, false);
	}

	/**
	 * Schedules a database job.
	 * 
	 * @param job
	 *            the job that uses the database.
	 * @param user
	 *            sets whether or not this job has been directly initiated by a
	 *            UI end user.
	 * @param system
	 *            sets whether or not this job is a system job. System jobs are
	 *            typically not revealed to users in any UI presentation of
	 *            jobs.
	 * @param accessKeys
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 * @throws IllegalArgumentException
	 *             if {@code job==null}.
	 */
	public void scheduleDb(final SLJob job, final boolean user,
			final boolean system, final String... accessKeys) {
		if (job == null) {
			throw new IllegalArgumentException(I18N.err(44, "job"));
		}
		final Job eclipseJob = new SLDatabaseJobWrapper(job, accessKeys);
		eclipseJob.setUser(user);
		eclipseJob.setSystem(system);
		eclipseJob.schedule();
	}

	public void scheduleWorkspace(final SLJob job) {
		scheduleWorkspace(job, false, false);
	}

	/**
	 * Schedule a job that locks the workspace
	 */
	public void scheduleWorkspace(final SLJob job, boolean user, boolean system) {
		if (job == null) {
			throw new IllegalArgumentException(I18N.err(44, "job"));
		}
		final Job eclipseJob = new WorkspaceLockingJob(job.getName()) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				return SLDatabaseJobWrapper.run(job, monitor);
			}
		};
		eclipseJob.setUser(user);
		eclipseJob.setSystem(system);
		eclipseJob.schedule();
	}

	/**
	 * Schedules a job.
	 * 
	 * @param job
	 *            the job.
	 * @throws IllegalArgumentException
	 *             if {@code job==null}.
	 */
	public void schedule(final SLJob job) {
		schedule(job, false, false);
	}

	/**
	 * Schedules a job.
	 * 
	 * @param job
	 *            the job.
	 * @param user
	 *            sets whether or not this job has been directly initiated by a
	 *            UI end user.
	 * @param system
	 *            sets whether or not this job is a system job. System jobs are
	 *            typically not revealed to users in any UI presentation of
	 *            jobs.
	 * @throws IllegalArgumentException
	 *             if {@code job==null}.
	 */
	public void schedule(final SLJob job, final boolean user,
			final boolean system) {
		if (job == null) {
			throw new IllegalArgumentException(I18N.err(44, "job"));
		}
		final Job eclipseJob = new SLJobWrapper(job);
		eclipseJob.setUser(user);
		eclipseJob.setSystem(system);
		eclipseJob.schedule();
	}

	/**
	 * Checks if there is an active {@link SLJob} of the passed type. This
	 * method will go through any wrappers and {@link AggregateSLJob} instances
	 * to find the job.
	 * 
	 * @param type
	 *            of {@link SLJob} to search for.
	 * @return {@code true} if an {@link SLJob} is active of the passed type,
	 *         {@code false} otherwise.
	 */
	public boolean isActiveOfType(final Class<? extends SLJob> type) {
		if (type == null) {
			return false;
		}
		final IJobManager manager = Job.getJobManager();
		for (final Job job : manager.find(null)) {
			final SLJob slJob;
			if (job instanceof SLDatabaseJobWrapper) {
				slJob = ((SLDatabaseJobWrapper) job).getWrappedJob();
			} else if (job instanceof SLJobWrapper) {
				slJob = ((SLJobWrapper) job).getWrappedJob();
			} else {
				slJob = null;
			}
			if (slJob != null) {
				return checkIsActiveThroughAggregateJob(slJob, type);
			}
		}
		return false;
	}

	/**
	 * Helper method to find an active {@link SLJob} of the passed type through
	 * {@link AggregateSLJob} instances.
	 * 
	 * @param job
	 *            the @link SLJob} that might be an {@link AggregateSLJob}
	 *            instance.
	 * @param type
	 *            of {@link SLJob} to search for.
	 * @return {@code true} if an {@link SLJob} is active of the passed type,
	 *         {@code false} otherwise.
	 */
	private boolean checkIsActiveThroughAggregateJob(final SLJob job,
			final Class<? extends SLJob> type) {
		if (job instanceof AggregateSLJob) {
			for (final SLJob subJob : ((AggregateSLJob) job)
					.getAggregatedJobs()) {
				if (checkIsActiveThroughAggregateJob(subJob, type)) {
					return true;
				}
			}
			return false;
		} else {
			return type.isInstance(job);
		}
	}
}
