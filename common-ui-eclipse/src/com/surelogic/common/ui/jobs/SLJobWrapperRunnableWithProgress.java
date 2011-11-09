package com.surelogic.common.ui.jobs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

public class SLJobWrapperRunnableWithProgress implements IRunnableWithProgress {

	private final SLJob f_job;
	private volatile SLStatus f_result;

	public SLJobWrapperRunnableWithProgress(SLJob job) {
		if (job == null)
			throw new IllegalArgumentException(I18N.err(44));
		f_job = job;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		SLProgressMonitorWrapper slMonitor = new SLProgressMonitorWrapper(
				monitor, f_job.getName());
		final SLStatus result = f_job.run(slMonitor);
		f_result = result;
		if (result == SLStatus.CANCEL_STATUS)
			throw new InterruptedException();
		if (result != SLStatus.OK_STATUS)
			throw new InvocationTargetException(result.getException(), result
					.getMessage());
	}

	public SLStatus getResultAsSLStatus() {
		return f_result; // may be null
	}
}
