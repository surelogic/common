package com.surelogic.common.core.adhoc;

import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.jobs.CancellableAdHocQueryJob;
import com.surelogic.common.adhoc.jobs.CancellableAdHocQueryMonitorJob;
import com.surelogic.common.core.EclipseUtility;

/**
 * A utility for queries in Eclipse.
 */
public final class EclipseQueryUtility {

  /**
   * Submits a cancellable query job in Eclipse.
   * 
   * @param boundQuery
   *          the non-null fully bound query.
   * @param parent
   *          the parent result of the new query, may not be {@code null}.
   */
  public static void scheduleQuery(final AdHocQueryFullyBound boundQuery, final AdHocQueryResultSqlData parent) {
    if (parent == null) {
      throw new IllegalArgumentException(
          "Parent may not be null.  Call scheduleQuery(AdHocQueryFullyBound boundQuery, String accessKey) instead if there is now parent query");
    }
    final CancellableAdHocQueryJob query = new CancellableAdHocQueryJob(boundQuery, parent);
    final Job job = EclipseUtility.toEclipseJob(query, parent.getAccessKeys());
    job.setSystem(true);
    final Job monJob = EclipseUtility.toEclipseJob(new CancellableAdHocQueryMonitorJob(query));
    monJob.setUser(true);
    job.schedule();
    monJob.schedule();
  }

  /**
   * Submits a cancellable query job in Eclipse.
   * 
   * @param boundQuery
   *          the non-null fully bound query.
   * @param acessKeys
   *          the access key for this query. Only one query w/ a given access
   *          key will be run at a time.
   */
  public static void scheduleQuery(final AdHocQueryFullyBound boundQuery, final String... accessKeys) {
    final CancellableAdHocQueryJob query = new CancellableAdHocQueryJob(boundQuery, accessKeys);
    final Job job = EclipseUtility.toEclipseJob(query, accessKeys);
    job.setSystem(true);
    final Job monJob = EclipseUtility.toEclipseJob(new CancellableAdHocQueryMonitorJob(query));
    monJob.setUser(true);
    job.schedule();
    monJob.schedule();
  }

  private EclipseQueryUtility() {
    // no instances
  }
}
