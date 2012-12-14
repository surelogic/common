package com.surelogic.common.jobs;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.surelogic.Borrowed;
import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;

/**
 * An object that can act a a hub for job progress notifications. This object
 * can be created for a particular {@link SLJob} and then many
 * {@link SLProgressMonitorObserver} instances can be added later. It also
 * remembers the progress on the job correctly if it was constructed prior to
 * the job being scheduled.
 */
public final class SLJobProgressTracker {

  public final int NOT_STARTED = -100;
  public final int UNKNOWN = -1;

  final AtomicInteger f_percentage = new AtomicInteger(NOT_STARTED);

  final SLProgressMonitorObserver f_monitor = new SLProgressMonitorObserver() {
    public void notifyPercentComplete(final int percentage) {
      f_percentage.set(percentage);
      for (SLProgressMonitorObserver o : f_observers)
        o.notifyPercentComplete(percentage);
    }
  };

  public SLJobProgressTracker(@NonNull @Borrowed final SLJob job) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));

    job.addObserver(f_monitor);
  }

  public int getPercentage() {
    return f_percentage.get();
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
}
