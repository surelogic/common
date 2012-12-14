package com.surelogic.common.jobs;

/**
 * An observer of {@link SLJob} progress.
 */
public interface SLProgressMonitorObserver {

  /**
   * Notifies this observer that the percent complete has changed.
   * 
   * @param percentage
   *          an integer <tt>[0, 100]</tt> indicating the percent complete of
   *          the work. A value of <tt>0</tt> indicates started. A value of
   *          <tt>100</tt> indicates finished.
   */
  void notifyPercentComplete(int percentage);
}
