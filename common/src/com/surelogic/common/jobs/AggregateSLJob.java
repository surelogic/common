package com.surelogic.common.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

/**
 * An implementation of {@link SLJob} that can aggregate the execution of
 * several jobs together into a single job. This is a very handy class for
 * multi-selection in views that trigger one or more jobs.
 */
@ThreadSafe
public final class AggregateSLJob extends AbstractSLJob {

  private final CopyOnWriteArrayList<SLJob> f_jobs;

  public List<SLJob> getAggregatedJobs() {
    return new ArrayList<>(f_jobs);
  }

  public AggregateSLJob(String name, List<SLJob> jobs) {
    super(name);
    if (jobs == null)
      throw new IllegalArgumentException(I18N.err(44, "jobs"));
    if (jobs.isEmpty())
      throw new IllegalArgumentException(I18N.err(155, AggregateSLJob.class.getName()));
    f_jobs = new CopyOnWriteArrayList<>(jobs);
  }

  @Override
  public SLStatus run(SLProgressMonitor monitor) {
    final int perJobWork = 100;
    monitor.begin(f_jobs.size() * perJobWork);
    try {
      for (SLJob job : f_jobs) {
        final SLStatus status = invoke(job, monitor, perJobWork);
        if (status.getSeverity() != SLSeverity.OK)
          return status;
        if (monitor.isCanceled())
          return SLStatus.CANCEL_STATUS;
      }
    } finally {
      monitor.done();
    }
    return SLStatus.OK_STATUS;
  }
}
