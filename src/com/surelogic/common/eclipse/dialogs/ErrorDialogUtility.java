package com.surelogic.common.eclipse.dialogs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.SWTUtility;

/**
 * A helpful utility class to make using the platform {@link ErrorDialog} a bit
 * easier to use.
 */
public final class ErrorDialogUtility {

	/**
	 * Opens an error dialog to report the given status.
	 * <p>
	 * This method is a shortcut for invoking:
	 * 
	 * <pre>
	 * ErrorDialogUtility.open(shell, title, reason, true);
	 * </pre>
	 * 
	 * @param shell
	 *            the shell to use for the dialog, may be {@code null} if the
	 *            platform default is desired (in most cases this default shell
	 *            is fine to use).
	 * @param title
	 *            the title to use for the dialog. If the title is {@code null}
	 *            then the title <i>Unexpected Problem</i> is used for the
	 *            dialog.
	 * @param reason
	 *            the reason the error dialog is being opened.
	 */
	public static void open(Shell shell, String title, IStatus reason) {
		open(shell, title, reason, true);
	}

	/**
	 * Opens an error dialog to report the given status.
	 * 
	 * @param shell
	 *            the shell to use for the dialog, may be {@code null} if the
	 *            platform default is desired (in most cases this default shell
	 *            is fine to use).
	 * @param title
	 *            the title to use for the dialog. If the title is {@code null}
	 *            then the title <i>Unexpected Problem</i> is used for the
	 *            dialog.
	 * @param reason
	 *            the reason the error dialog is being opened.
	 * @param log
	 *            {@code true} if the problem should be logged, {@code false} if
	 *            a log entry should not be created.
	 */
	public static void open(Shell shell, String title, IStatus reason,
			boolean log) {
		if (shell == null)
			shell = SWTUtility.getShell();
		if (title == null)
			title = I18N.msg("common.error.dialog.title");
		final String msg = I18N.msg("common.error.dialog.eclipse.msg");
		ErrorDialog.openError(shell, title, msg, reason);

		if (!log) {
			return;
		}
		/*
		 * Log this to our log.
		 */
		final int severity = reason.getSeverity();
		final Level level;
		if (severity == IStatus.ERROR) {
			level = Level.SEVERE;
		} else if (severity == IStatus.WARNING) {
			level = Level.WARNING;
		} else {
			level = Level.INFO;
		}
		SLLogger.getLogger().log(level, reason.getMessage(),
				reason.getException());
	}

	private ErrorDialogUtility() {
		// no instances
	}
}
