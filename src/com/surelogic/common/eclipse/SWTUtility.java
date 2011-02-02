package com.surelogic.common.eclipse;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.Justification;
import com.surelogic.common.i18n.I18N;

/**
 * Utility to help with SWT stuff.
 */
public final class SWTUtility {
	// From http://blog.zvikico.com/2008/07/eclipse-gracefully-loading-a-plugin.html
	private static Shell findStartedShell() {
		Shell result = null;
		final Display currentDisplay = Display.getCurrent();
		if (currentDisplay != null) {
			result = currentDisplay.getActiveShell();
			if ((result != null) && (result.getMenuBar() == null)) {
				result = null;
			}
		}
		return result;
	}
	
	public static void startup(final IRunnableWithProgress startupOp) throws InvocationTargetException, InterruptedException {
		final Shell activeShell = findStartedShell();
		if (activeShell != null) {
			final ProgressMonitorDialog progressMonitorDialog =
				new ProgressMonitorDialog(activeShell);
			progressMonitorDialog.run(false, false, startupOp);
		} else {
			startupOp.run(new NullProgressMonitor());
		}
	}
	
	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return window == null ? null : window.getShell();
	}

	/**
	 * Adapts an IDE independent justification to Eclipse.
	 * 
	 * @param value
	 *            the justification.
	 * @return the Eclipse justification.
	 */
	public static int adaptJustification(final Justification value) {
		switch (value) {
		case RIGHT:
			return SWT.RIGHT;
		case LEFT:
			return SWT.LEFT;
		case CENTER:
			return SWT.CENTER;
		default:
			throw new AssertionError(I18N.err(100, value.toString()));
		}
	}

	private SWTUtility() {
		// no instances
	}
}
