package com.surelogic.common.jobs;

/**
 * An observer of {@link SLJob} progress.
 * 
 * @see SLJob#addObserver(SLProgressMonitorObserver)
 * @see SLJob#removeObserver(SLProgressMonitorObserver)
 */
public interface SLProgressMonitorObserver {

  /**
   * Notifies this observer that the percent complete has changed.
   * <p>
   * Do not assume any thread context for this call.
   * <p>
   * This method is called often as a job makes progress so make your
   * implementation return quickly to avoid slowing job performance.
   * 
   * @param percentage
   *          an integer <tt>[0, 100]</tt> indicating the percent complete of
   *          the work. A value of <tt>0</tt> indicates started. A value of
   *          <tt>-1</tt> indicates started but with an indeterminate amount of
   *          work&mdash;the only other call will be <tt>100</tt> when finished.
   *          A value of <tt>100</tt> indicates finished.
   */
  void notifyPercentComplete(int percentage);
}
