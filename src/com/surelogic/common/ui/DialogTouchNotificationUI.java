package com.surelogic.common.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.license.ILicenseObserver;
import com.surelogic.common.ui.dialogs.LowMaximumMemoryDialog;
import com.surelogic.common.ui.dialogs.NoLicenseDialog;
import com.surelogic.common.ui.jobs.SLUIJob;

public class DialogTouchNotificationUI extends
		SLEclipseStatusUtility.LogTouchNotificationUI {

	@Override
	public ILicenseObserver getLicenseObserver() {
		return new NoLicenseDialog(
				new SLEclipseStatusUtility.LogOutputLicenseObserver());
	}

	@Override
	public void notifyLowMemory(final long maxMemoryMB, final long maxPermGenMB) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final LowMaximumMemoryDialog dialog = new LowMaximumMemoryDialog(
						maxMemoryMB, maxPermGenMB);
				dialog.open();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		// go ahead and write it to the log also
		super.notifyLowMemory(maxMemoryMB, maxPermGenMB);
	}
}
