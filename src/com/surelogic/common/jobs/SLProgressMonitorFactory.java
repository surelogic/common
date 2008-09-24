package com.surelogic.common.jobs;

/**
 * Factory interface for creating progress monitor objects.
 */
public interface SLProgressMonitorFactory {
  /**
   * Create a new progress monitor.
   * @param taskName The name of the task requiring the progress monitor.
   * @return A new progress monitor that can be used for the named task.
   */
  public SLProgressMonitor createSLProgressMonitor(final String taskName);
}
