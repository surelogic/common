package com.surelogic.common.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.ThreadSafe;

/**
 * A progress monitor that tracks if the job was canceled but does not log
 * progress information.
 */
@ThreadSafe
public final class NullSLProgressMonitor implements SLProgressMonitor {

  public void begin() {
    // Do nothing
  }

  public void begin(int totalWork) {
    // Do nothing
  }

  public void done() {
    // Do nothing
  }

  private final AtomicBoolean f_canceled = new AtomicBoolean(false);

  public boolean isCanceled() {
    return f_canceled.get();
  }

  public void setCanceled(boolean value) {
    f_canceled.set(value);
  }

  public void subTask(String name) {
    // Do nothing
  }

  public void subTaskDone() {
    // Do nothing
  }

  public void worked(int work) {
    // Do nothing
  }
}
