package com.surelogic.common.eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;

public final class BalloonUtility {

	private static ToolTip f_lastTip = null;

	private BalloonUtility() {
		// no instances
	}

	public static void showMessage(final String text, final String message) {
		// Get into a UI thread!
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				synchronized (BalloonUtility.class) {
					if (f_lastTip != null) {
						if (!f_lastTip.isDisposed()) {
							f_lastTip.setVisible(false);
							f_lastTip.dispose();
						}
					}
					final IWorkbenchWindow win = PlatformUI.getWorkbench()
					                         .getActiveWorkbenchWindow();
					final Shell shell = win == null ? null : win.getShell();
					if (shell == null) {
						final int errNo = 35;
						final String msg = I18N.err(errNo);
						return SLEclipseStatusUtility.createErrorStatus(errNo, msg,
								new Exception());
					}

					/*
					 * We need to position the balloon relative to the main
					 * Eclipse window. This puts it at the bottom-left a few
					 * pixels in diagonal up from the bottom-left corner.
					 */
					final Rectangle r = shell.getBounds();
					final int x = r.x + 25;
					final int y = r.y + r.height - 3;

					final ToolTip tip = new ToolTip(shell, SWT.BALLOON
							| SWT.ICON_INFORMATION);
					tip.setMessage(message);
					tip.setText(text);
					tip.setLocation(x, y);
					tip.setVisible(true);
					f_lastTip = tip;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
