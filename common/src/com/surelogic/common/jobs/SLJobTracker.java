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

  public final int NOT_STARTED = -100;
  public final int INDETERMINATE = -1;

  final AtomicInteger f_percentage = new AtomicInteger(NOT_STARTED);

  final SLProgressMonitorObserver f_monitor = new SLProgressMonitorObserver() {
    public void notifyPercentComplete(int percentage) {
      if (percentage < -1) { // -1 is indeterminate
        SLLogger.getLogger().log(Level.WARNING, I18N.err(297, percentage, 0));
        percentage = 0;
      }
      if (percentage > 100) {
        SLLogger.getLogger().log(Level.WARNING, I18N.err(297, percentage, 100));
        percentage = 100;
      }
      f_percentage.set(percentage);
      for (SLProgressMonitorObserver o : f_observers)
        o.notifyPercentComplete(percentage);
    }
  };

  public SLJobTracker(@NonNull @Borrowed final SLJob job) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));

    job.addObserver(f_monitor);
  }

  public boolean isStarted() {
    return f_percentage.get() != NOT_STARTED;
  }

  public int getPercentage() {
    return f_percentage.get();
  }

  public boolean isFinished() {
    return f_percentage.get() == 100;
  }

  final CopyOnWriteArrayList<SLProgressMonitorObserver> f_observers = new CopyOnWriteArrayList<SLProgressMonitorObserver>();

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

  public final void clearObservers() {
    f_observers.clear();
  }
}
