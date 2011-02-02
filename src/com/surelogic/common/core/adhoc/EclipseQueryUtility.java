package com.surelogic.common.eclipse.core.adhoc;

import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.jobs.CancellableAdHocQueryJob;
import com.surelogic.common.adhoc.jobs.CancellableAdHocQueryMonitorJob;
import com.surelogic.common.eclipse.core.jobs.EclipseJob;

/**
 * A utility for queries in Eclipse.
 */
public final class EclipseQueryUtility {

	/**
	 * Submits a cancellable query job in Eclipse.
	 * 
	 * @param boundQuery
	 *            the non-null fully bound query.
	 * @param parent
	 *            the parent result of the new query, may not be {@code null}.
	 */
	public static void scheduleQuery(final AdHocQueryFullyBound boundQuery,
			final AdHocQueryResultSqlData parent) {
		if (parent == null) {
			throw new IllegalArgumentException(
					"Parent may not be null.  Call scheduleQuery(AdHocQueryFullyBound boundQuery, String accessKey) instead if there is now parent query");
		}
		final CancellableAdHocQueryJob job = new CancellableAdHocQueryJob(
				boundQuery, parent);
		final CancellableAdHocQueryMonitorJob monJob = new CancellableAdHocQueryMonitorJob(
				job);
		EclipseJob.getInstance().scheduleDb(job, false, true,
				parent.getAccessKeys());
		EclipseJob.getInstance().schedule(monJob, true, false);
	}

	/**
	 * Submits a cancellable query job in Eclipse.
	 * 
	 * @param boundQuery
	 *            the non-null fully bound query.
	 * @param acessKeys
	 *            the access key for this query. Only one query w/ a given
	 *            access key will be run at a time.
	 */
	public static void scheduleQuery(final AdHocQueryFullyBound boundQuery,
			final String... accessKeys) {
		final CancellableAdHocQueryJob job = new CancellableAdHocQueryJob(
				boundQuery, accessKeys);
		final CancellableAdHocQueryMonitorJob monJob = new CancellableAdHocQueryMonitorJob(
				job);
		EclipseJob.getInstance().scheduleDb(job, false, true, accessKeys);
		EclipseJob.getInstance().schedule(monJob, true, false);
	}

	private EclipseQueryUtility() {
		// no instances
	}
}
