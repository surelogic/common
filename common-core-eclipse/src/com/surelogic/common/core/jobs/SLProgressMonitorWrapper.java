package com.surelogic.common.core.jobs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitorObserver;

/**
 * Adapts an Eclipse status progress monitor object to the IDE independent
 * {@link SLProgressMonitor} interface.
 */
public final class SLProgressMonitorWrapper implements SLProgressMonitor {

  /**
   * The wrapped progress monitor.
   */
  protected final IProgressMonitor f_monitor;

  /**
   * The name the job being monitored.
   */
  private final String f_name;

  private boolean f_started = false;

  /**
   * Stack of names of the tasks that contain the currently running sub task.
   * Used to return the progress monitor name to the previous subtask when a
   * subtask is finished. The name of the currently running subtask is found in
   * {@link #f_currentSubTask}.
   */
  private final LinkedList<String> f_enclosingSubTasks = new LinkedList<>();

  /**
   * The name of the currently running subtask, or {@code ""} if no subtask is
   * running.
   */
  private String f_currentSubTask = "";

  /**
   * Progress monitor observers
   */
  private final CopyOnWriteArrayList<SLProgressMonitorObserver> f_observers = new CopyOnWriteArrayList<>();

  private void notifyObservers(int percentage) {
    // System.out.println("percent done = " + percentage + "%");
    for (SLProgressMonitorObserver o : f_observers)
      o.notifyPercentComplete(percentage);
  }

  /**
   * The total amount of work, -1 is UNKNOWN
   */
  private int f_totalWork = IProgressMonitor.UNKNOWN;

  /**
   * Amount of total work done so far.
   */
  private int f_worked = 0;

  /**
   * Creates a new wrapper around the given monitor with no progress monitor
   * observers.
   * 
   * @param monitor
   *          the progress monitor to forward to.
   * @param name
   *          the task name.
   */
  public SLProgressMonitorWrapper(IProgressMonitor monitor, String name) {
    this(monitor, name, null);
  }

  /**
   * Creates a new wrapper around the given monitor.
   * 
   * @param monitor
   *          the progress monitor to forward to.
   * @param name
   *          the task name.
   * @param observers
   *          a collection of progress monitor observers. May be {@code null} to
   *          indicate none.
   */
  public SLProgressMonitorWrapper(IProgressMonitor monitor, String name, @Nullable Collection<SLProgressMonitorObserver> observers) {
    if (monitor == null)
      throw new IllegalArgumentException(I18N.err(44, "monitor"));
    f_monitor = monitor;
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    f_name = name;
    if (observers != null)
      f_observers.addAll(observers);
  }

  @Override
  public void begin() {
    if (f_started) {
      throw new IllegalStateException(I18N.err(118));
    }
    f_started = true;
    f_monitor.beginTask(f_name, IProgressMonitor.UNKNOWN);
    notifyObservers(-1);
  }

  @Override
  public void begin(int totalWork) {
    if (f_started) {
      throw new IllegalStateException(I18N.err(118));
    }
    f_started = true;
    f_totalWork = totalWork;
    f_monitor.beginTask(f_name, totalWork);
    notifyObservers(0);
  }

  @Override
  public void done() {
    while (!f_enclosingSubTasks.isEmpty()) {
      subTaskDone();
    }
    f_monitor.done();
    notifyObservers(100);
  }

  @Override
  public boolean isCanceled() {
    return f_monitor.isCanceled();
  }

  @Override
  public void setCanceled(boolean value) {
    f_monitor.setCanceled(value);
  }

  @Override
  public void subTask(String name) {
    if (!f_started) {
      throw new IllegalStateException(I18N.err(119, "subTask"));
    }
    f_enclosingSubTasks.addFirst(f_currentSubTask);
    f_currentSubTask = name;
    f_monitor.subTask(name);
  }

  @Override
  public void subTaskDone() {
    // restore the previous sub task name
    f_currentSubTask = f_enclosingSubTasks.removeFirst();
    f_monitor.subTask(f_currentSubTask);
  }

  @Override
  public void worked(int work) {
    if (!f_started) {
      throw new IllegalStateException(I18N.err(119, "worked"));
    }
    f_monitor.worked(work);
    if (work > 0) {
      f_worked += work;
      int percentage = (int) (((double) f_worked / (double) f_totalWork) * 100.0);
      if (percentage < 1)
        percentage = 1;
      if (percentage > 99)
        percentage = 99;
      notifyObservers(percentage);
    }
  }
}
