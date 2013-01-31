package com.surelogic.common.jobs;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.surelogic.Borrowed;
import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * An object that can act a a hub for job progress notifications. This object
 * can be created for a particular {@link SLJob} and then many
 * {@link SLProgressMonitorObserver} instances can be added later. It also
 * remembers the progress on the job correctly if it was constructed prior to
 * the job being scheduled.
 */
public final class SLJobTracker {

  public final int NOT_STARTED = -2;
  public final int INDETERMINATE = -1;

  final AtomicInteger f_percentageOrState = new AtomicInteger(NOT_STARTED);

  final SLProgressMonitorObserver f_monitor = new SLProgressMonitorObserver() {
    @Override
    public void notifyPercentComplete(int percentage) {
      if (percentage < -1) { // -1 is indeterminate
        SLLogger.getLogger().log(Level.WARNING, I18N.err(297, percentage, 0));
        percentage = 0;
      }
      if (percentage > 100) {
        SLLogger.getLogger().log(Level.WARNING, I18N.err(297, percentage, 100));
        percentage = 100;
      }
      f_percentageOrState.set(percentage);
      for (SLProgressMonitorObserver o : f_observers)
        o.notifyPercentComplete(percentage);
    }
  };

  public SLJobTracker(@NonNull @Borrowed final SLJob job) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));

    job.addObserver(f_monitor);
  }

  /**
   * Gets if the tracked job has started.
   * 
   * @return {@code true} if the tracked job has started, {@code false} if it
   *         has not started.
   */
  public boolean isStarted() {
    return f_percentageOrState.get() != NOT_STARTED;
  }

  /**
   * Gets the percentage of work accomplished by the tracked job or if it has
   * not started or if it has indeterminate amount of work.
   * 
   * @return an integer <tt>[0, 100]</tt> indicating the percent complete of the
   *         work or <tt>-2</tt> (constant {@link #NOT_STARTED}) or <tt>-1</tt>
   *         (constant {@link #INDETERMINATE}). A value of <tt>0</tt> indicates
   *         started. A value of <tt>-1</tt> indicates started but with an
   *         indeterminate amount of work&mdash;the only other call will be
   *         <tt>100</tt> when finished. A value of <tt>100</tt> indicates
   *         finished. A value of <tt>-2</tt> indicates not started.
   */
  public int getPercentageOrState() {
    return f_percentageOrState.get();
  }

  /**
   * Gets if the tracked job is finished.
   * 
   * @return {@code true} if the tracked job is finished, {@code false}
   *         otherwise.
   */
  public boolean isFinished() {
    return f_percentageOrState.get() == 100;
  }

  final CopyOnWriteArrayList<SLProgressMonitorObserver> f_observers = new CopyOnWriteArrayList<SLProgressMonitorObserver>();

  /**
   * Adds a progress monitor observer.
   * 
   * @param observer
   *          an observer of {@link SLJob} progress.
   */
  public final void addObserver(final SLProgressMonitorObserver observer) {
    if (observer != null)
      f_observers.add(observer);
  }

  /**
   * Removes a progress monitor observer.
   * 
   * @param observer
   *          an observer of {@link SLJob} progress.
   * @return {@code true} if the list of observers contained the specified
   *         observer.
   */
  public final boolean removeObserver(final SLProgressMonitorObserver observer) {
    if (observer != null)
      return f_observers.remove(observer);
    else
      return false;
  }

  /**
   * Removes all progress monitor observers. There will be no progress monitor
   * observers after this call returns.
   */
  public final void clearObservers() {
    f_observers.clear();
  }
}
