package com.surelogic.common.core.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.surelogic.NonNull;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AggregateSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

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
   * Schedule a job that locks the workspace
   */
  public void scheduleWorkspace(@NonNull final SLJob job) {
    scheduleWorkspace(job, false, false, 0);
  }

  public void scheduleWorkspace(@NonNull final SLJob job, final long delay) {
    scheduleWorkspace(job, false, false, delay);
  }

  /**
   * Schedule a job that locks the workspace
   */
  public void scheduleWorkspace(@NonNull final SLJob job, final boolean user, final boolean system) {
    scheduleWorkspace(job, user, system, 0);
  }

  /**
   * Schedule a job that locks the workspace
   */
  public void scheduleWorkspace(@NonNull final SLJob job, final boolean user, final boolean system, final long delay) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));

    final Job eclipseJob = new WorkspaceLockingJob(job.getName()) {
      @Override
      public IStatus runInWorkspace(final IProgressMonitor monitor) {
        return workspaceRunHelper(job, monitor);
      }
    };
    eclipseJob.addJobChangeListener(new SLJobChangeAdapter(job));
    eclipseJob.setUser(user);
    eclipseJob.setSystem(system);
    eclipseJob.schedule(delay);
  }

  private final IStatus workspaceRunHelper(SLJob job, IProgressMonitor monitor) {
    final SLStatus status = job.run(new SLProgressMonitorWrapper(monitor, job.getName()));
    return SLEclipseStatusUtility.convert(status);
  }

  /**
   * Schedules a job with an optional set of access keys.
   * 
   * @param job
   *          the job.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   * @throws IllegalArgumentException
   *           if {@code job==null}.
   */
  public void schedule(@NonNull final SLJob job, final String... accessKeys) {
    schedule(job, false, false, 0, accessKeys);
  }

  /**
   * Schedules a job with an optional set of access keys.
   * 
   * @param job
   *          the job.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   * @throws IllegalArgumentException
   *           if {@code job==null}.
   */
  public void schedule(@NonNull final SLJob job, final long delay, final String... accessKeys) {
    schedule(job, false, false, delay, accessKeys);
  }

  /**
   * Schedules a job with an optional set of access keys.
   * 
   * @param job
   *          the job.
   * @param user
   *          sets whether or not this job has been directly initiated by a UI
   *          end user.
   * @param system
   *          sets whether or not this job is a system job. System jobs are
   *          typically not revealed to users in any UI presentation of jobs.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   * @throws IllegalArgumentException
   *           if {@code job==null}.
   */
  public void schedule(@NonNull final SLJob job, final boolean user, final boolean system, final String... accessKeys) {
    schedule(job, user, system, 0, accessKeys);
  }

  /**
   * Schedules a job with an optional set of access keys.
   * 
   * @param job
   *          the job.
   * @param user
   *          sets whether or not this job has been directly initiated by a UI
   *          end user.
   * @param system
   *          sets whether or not this job is a system job. System jobs are
   *          typically not revealed to users in any UI presentation of jobs.
   * @param delay
   *          a time delay in milliseconds before the job should run
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   * @throws IllegalArgumentException
   *           if {@code job==null}.
   */
  public void schedule(@NonNull final SLJob job, final boolean user, final boolean system, final long delay,
      final String... accessKeys) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));

    final Job eclipseJob = new SLJobWrapper(job, accessKeys);
    eclipseJob.addJobChangeListener(new SLJobChangeAdapter(job));
    eclipseJob.setUser(user);
    eclipseJob.setSystem(system);
    eclipseJob.schedule(delay);
  }

  /**
   * Checks if there is an active {@link SLJob} of the passed type. This method
   * will go through any wrappers and {@link AggregateSLJob} instances to find
   * the job.
   * 
   * @param type
   *          of {@link SLJob} to search for.
   * @return {@code true} if an {@link SLJob} is active of the passed type,
   *         {@code false} otherwise.
   */
  public boolean isActiveOfType(final Class<? extends SLJob> type) {
    if (type == null) {
      return false;
    }
    return !getActiveJobsOfType(type).isEmpty();
  }

  public <T extends SLJob> List<T> getActiveJobsOfType(final Class<T> type) {
    if (type == null) {
      return Collections.emptyList();
    }
    final List<T> result = new ArrayList<T>();
    for (SLJob jobInEclipse : f_jobsPassedToEclipse) {
      System.out.println("Job in eclipse: " + jobInEclipse.getName());
      getThroughAggregateJobByType(jobInEclipse, type, result);
    }
    System.out.println("--- getActiveJobsOfType:" + (result.isEmpty() ? " nothing" : ""));
    for (SLJob job : result) {
      System.out.println("      found " + job.getName());
    }
    return result;
  }

  public List<SLJob> getActiveJobsWithName(final String name) {
    if (name == null) {
      return Collections.emptyList();
    }
    final List<SLJob> result = new ArrayList<SLJob>();
    for (SLJob jobInEclipse : f_jobsPassedToEclipse) {
      getThroughAggregateJobByName(jobInEclipse, name, result);
    }
    return result;
  }

  private <T extends SLJob> void getThroughAggregateJobByType(final SLJob job, final Class<T> type, List<T> mutableResult) {
    if (job instanceof AggregateSLJob) {
      for (final SLJob subJob : ((AggregateSLJob) job).getAggregatedJobs()) {
        getThroughAggregateJobByType(subJob, type, mutableResult);
      }
    } else {
      if (type.isInstance(job)) {
        @SuppressWarnings("unchecked")
        T jobOfInterest = (T) job;
        mutableResult.add(jobOfInterest);
      }
    }
  }

  private void getThroughAggregateJobByName(final SLJob job, final String name, List<SLJob> mutableResult) {
    if (job instanceof AggregateSLJob) {
      for (final SLJob subJob : ((AggregateSLJob) job).getAggregatedJobs()) {
        getThroughAggregateJobByName(subJob, name, mutableResult);
      }
    } else {
      if (name.equals(job.getName()))
        mutableResult.add(job);
    }
  }

  static final CopyOnWriteArraySet<SLJob> f_jobsPassedToEclipse = new CopyOnWriteArraySet<SLJob>();

  /**
   * An Eclipse job change adapter that updates the set of jobs passed to
   * Eclipse.
   * <p>
   * Sadly, Eclipse wraps their own jobs several times so it is not possible to
   * pull an SLJob out of an Eclipse job (well at least without reflection
   * dependent upon the internals of Eclipse).
   */
  final class SLJobChangeAdapter extends JobChangeAdapter {

    final SLJob f_job;

    SLJobChangeAdapter(SLJob job) {
      if (job == null)
        throw new IllegalArgumentException(I18N.err(44, "job"));
      f_job = job;
    }

    @Override
    public void done(IJobChangeEvent event) {
      f_jobsPassedToEclipse.remove(f_job);
    }

    @Override
    public void scheduled(IJobChangeEvent event) {
      f_jobsPassedToEclipse.add(f_job);
    }
  }
}
