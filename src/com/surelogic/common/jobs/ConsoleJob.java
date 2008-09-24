package com.surelogic.common.jobs;

/**
 * Class for submitting jobs to a "console." The jobs are not meant to be
 * interactive, and thus the console is really some king of output-only
 * abstraction as encapsulated by the {@link SLProgressMonitor} used by jobs
 * submitted to this object. In reality, this progress monitor is further
 * abstracted away by using a {@link SLProgressMonitorFactory} that generates a
 * new progress monitor for each submitted job.  For example, the class
 * {@link PrintWriterSLProgressMonitor} is meant to be used with this class.
 * 
 * <p>Jobs are executed serially.
 */
/* 
 * TODO: Use ExecuteServices and Executors.newSingleThreadExecutor() plus futures
 */
public final class ConsoleJob {
  private final SLProgressMonitorFactory pmFactory;
  
  public ConsoleJob(final SLProgressMonitorFactory pmf) {
    pmFactory = pmf;
  }
  
  public SLStatus submitJob(final SLJob slJob) {
    final SLProgressMonitor monitor = pmFactory.createSLProgressMonitor(slJob.getName());
    final SLStatus status = slJob.run(monitor);
    return status;
  }
}
