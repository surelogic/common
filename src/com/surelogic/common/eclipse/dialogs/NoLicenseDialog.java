package com.surelogic.common.eclipse.dialogs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.SLUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.ILicenseObserver;
import com.surelogic.common.ui.SWTUtility;

public final class NoLicenseDialog implements ILicenseObserver {

	private static final NoLicenseDialog INSTANCE = new NoLicenseDialog();

	public static final NoLicenseDialog getInstance() {
		return INSTANCE;
	}

	private NoLicenseDialog() {
		// singleton
	}

	public void notifyNoLicenseFor(final String subject) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final String title = I18N
						.msg("common.manage.licenses.dialog.noLicense.title");
				final String msg = I18N.msg(
						"common.manage.licenses.dialog.noLicense.msg", subject);
				final Shell shell = SWTUtility.getShell();
				if (MessageDialog.openQuestion(shell, title, msg)) {
					ManageLicensesDialog.open(shell);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * Holds the set of license subject that the user has been notified are
	 * close to their expiration date. This set should <i>only</i> be accessed
	 * within the UI thread.
	 */
	private final Set<String> f_alreadyNotified = new HashSet<String>();

	public void notifyExpiration(final String subject, final Date expiration) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				/*
				 * Only notify the user once about a particular license.
				 */
				if (!f_alreadyNotified.contains(subject)) {
					f_alreadyNotified.add(subject);
					final String title = I18N
							.msg("common.manage.licenses.dialog.expiration.title");
					final String msg = I18N.msg(
							"common.manage.licenses.dialog.expiration.msg",
							subject, SLUtility.toStringDay(expiration));
					final Shell shell = SWTUtility.getShell();
					if (MessageDialog.openQuestion(shell, title, msg)) {
						ManageLicensesDialog.open(shell);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
