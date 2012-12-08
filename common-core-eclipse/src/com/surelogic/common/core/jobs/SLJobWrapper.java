package com.surelogic.common.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.NonNull;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

/**
 * Adapts an {@link SLJob} so that it can execute in Eclipse as job.
 */
public final class SLJobWrapper extends AbstractEclipseAccessKeysJob {

  @NonNull
  private final SLJob f_job;

  /**
   * Gets the wrapped IDE independent job.
   * 
   * @return the wrapped IDE independent job.
   */
  SLJob getWrappedJob() {
    return f_job;
  }

  /**
   * Wraps an {@link SLJob} in a {@link Job} with an optional set of access
   * keys. Jobs with the same access keys will proceed in serial order.
   * 
   * @param job
   *          an IDE independent job.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   */
  public SLJobWrapper(@NonNull final SLJob job, String... accessKeys) {
    super(job.getName(), accessKeys);
    f_job = job;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    final SLStatus status = f_job.run(new SLProgressMonitorWrapper(monitor, f_job.getName()));
    return SLEclipseStatusUtility.convert(status);
  }
}
