package com.surelogic.common.adhoc.jobs;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

public class CancellableAdHocQueryMonitorJob extends AbstractSLJob {
	private final CancellableAdHocQueryJob f_job;

	public CancellableAdHocQueryMonitorJob(final CancellableAdHocQueryJob job) {
		super(job.getName());
		f_job = job;
	}

	public SLStatus run(final SLProgressMonitor monitor) {
		monitor.begin();
		while (true) {
			if (monitor.isCanceled()) {
				f_job.cancel();
				return SLStatus.CANCEL_STATUS;
			} else if (f_job.isDone()) {
				monitor.done();
				return SLStatus.OK_STATUS;
			} else {
				synchronized (this) {
					try {
						wait(100);
					} catch (final InterruptedException e) {
						// Do nothing
					}
				}
			}
		}
	}

}
