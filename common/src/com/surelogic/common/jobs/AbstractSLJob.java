package com.surelogic.common.jobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import com.surelogic.NonNull;
import com.surelogic.ThreadSafe;

/**
 * An abstract implementation of {@link SLJob} that provides the job name via
 * its constructor and supports the invocation of subtasks.
 */
@ThreadSafe(implementationOnly = true)
public abstract class AbstractSLJob implements SLJob {

  /**
   * Constructs and instance with the specified job name.
   * 
   * @param name
   *          the name of this job, or {@code null} if the job has no name.
   */
  protected AbstractSLJob(final String name) {
    f_name = name;
  }

  private final String f_name;

  public final String getName() {
    return f_name;
  }

  private final CopyOnWriteArrayList<SLProgressMonitorObserver> f_observers = new CopyOnWriteArrayList<SLProgressMonitorObserver>();

  public final void addObserver(final SLProgressMonitorObserver observer) {
    if (observer != null)
      f_observers.add(observer);
  }

  public final boolean removeObserver(final SLProgressMonitorObserver observer) {
    if (observer != null)
      return f_observers.remove(observer);
    else
      return false;
  }

  @NonNull
  public Collection<SLProgressMonitorObserver> getObservers() {
    return new ArrayList<SLProgressMonitorObserver>(f_observers);
  }

  /**
   * Invokes a job as a subtask of this job. This method should only be called
   * from within the {@link #run(SLProgressMonitor)} method. The subtask is run
   * in the current thread, blocking until the subtask has completed.
   * <p>
   * An example of use is shown below.
   * 
   * <pre>
   * public SLStatus run(SLProgressMonitor monitor) {
   *   monitor.begin(100);
   *   try {
   *     monitor.worked(50);
   *     SLJob job = new PrepSLJob();
   *     // run the subtask
   *     final SLStatus status = invoke(job, monitor, 50);
   *     if (status.getSeverity() != SLSeverity.OK)
   *       return status;
   *     if (monitor.isCanceled())
   *       return SLStatus.CANCEL_STATUS;
   *   } finally {
   *     monitor.done();
   *   }
   *   return SLStatus.OK_STATUS;
   * </pre>
   * 
   * @param job
   *          the subtask.
   * @param monitor
   *          the parent monitor.
   * @param work
   *          the amount of work to be accomplished on the parent monitor.
   * @return the resulting status of the subtask.
   */
  public static SLStatus invoke(SLJob job, SLProgressMonitor monitor, int work) {
    final SLProgressMonitor sub = new SubSLProgressMonitor(monitor, job.getName(), work);
    try {
      return job.run(sub);
    } finally {
      /*
       * It is OK to call done again, just in case.
       */
      sub.done();
    }
  }
}
