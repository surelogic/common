package com.surelogic.common.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.ThreadSafe;

/**
 * A progress monitor that tracks if the job was canceled but does not log
 * progress information.
 */
@ThreadSafe
public final class NullSLProgressMonitor implements SLProgressMonitor {

  @Override
  public void begin() {
    // Do nothing
  }

  @Override
  public void begin(int totalWork) {
    // Do nothing
  }

  @Override
  public void done() {
    // Do nothing
  }

  private final AtomicBoolean f_canceled = new AtomicBoolean(false);

  @Override
  public boolean isCanceled() {
    return f_canceled.get();
  }

  @Override
  public void setCanceled(boolean value) {
    f_canceled.set(value);
  }

  @Override
  public void subTask(String name) {
    // Do nothing
  }

  @Override
  public void subTaskDone() {
    // Do nothing
  }

  @Override
  public void worked(int work) {
    // Do nothing
  }
}
