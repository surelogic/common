package com.surelogic.common.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.surelogic.common.Justification;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class EclipseUIUtility {

	/**
	 * Gets the Eclipse {@link IPreferenceStore} for the SureLogic Eclipse-based
	 * tools. This is usable in the Eclipse UI, most notably in preference
	 * dialogs.
	 * <p>
	 * These are the same preferences returned by
	 * {@link EclipseUtility#getPreferences()}, however, the
	 * {@link IEclipsePreferences} returned by that method cannot be used in
	 * preference dialogs. It should, however, be used in all other code to
	 * avoid Eclipse UI dependencies.
	 * <p>
	 * These preferences are persisted within per-workspace.
	 * 
	 * @return the SureLogic Eclipse preferences usable in preference dialogs.
	 * 
	 * @see EclipseUtility#getPreferences()
	 * @see EclipseUtility#PREFERENCES_NODE
	 */
	public static IPreferenceStore getPreferences() {
		return new ScopedPreferenceStore(new InstanceScope(),
				EclipseUtility.PREFERENCES_NODE);
	}

	/**
	 * Determines if the passed perspective id is the currently opened
	 * perspective in the workbench.
	 * 
	 * @param id
	 *            the identifier of a perspective.
	 * @return {@code true} if the passed perspective id is the currently opened
	 *         perspective in the workbench, {@code false} otherwise.
	 */
	public static boolean isPerspectiveOpen(final String id) {
		IWorkbenchWindow win = getIWorkbenchWindow();
		if (win != null) {
			IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor desc = page.getPerspective();
				if (desc != null) {
					if (desc.getId().equals(id))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the active workbench window, or {@code null} if none can be found.
	 * 
	 * @return the active workbench window, or {@code null} if none can be
	 *         found.
	 */
	private static IWorkbenchWindow getIWorkbenchWindow() {
		IWorkbenchWindow win = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (win == null) {
			for (IWorkbenchWindow w : PlatformUI.getWorkbench()
					.getWorkbenchWindows()) {
				if (w != null) {
					return w;
				}
			}
		}
		return win;
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is given focus.
	 * <P>
	 * This method must be called from a UI thread or it will throw a
	 * {@link NullPointerException}. *
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @return the shown view or {@code null}.
	 */
	public static IViewPart showView(final String viewId) {
		final IWorkbenchWindow window = getIWorkbenchWindow();
		if (window == null)
			return null;
		final IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;
		try {
			final IViewPart view = page.showView(viewId);
			return view;
		} catch (PartInitException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Unable to open the view identified by " + viewId + ".", e);
		}
		return null;
	}

	/**
	 * Shows the view identified by the given view id and secondary id.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param secondaryId
	 *            the secondary id to use, or {@code null} for no secondary id
	 * @param mode
	 *            the activation mode. Must be
	 *            {@link IWorkbenchPage#VIEW_ACTIVATE},
	 *            {@link IWorkbenchPage#VIEW_VISIBLE} or
	 *            {@link IWorkbenchPage#VIEW_CREATE}
	 * 
	 * @return the shown view or {@code null}.
	 */
	public static IViewPart showView(final String viewId,
			final String secondaryId, final int mode) {
		final IWorkbenchWindow window = getIWorkbenchWindow();
		if (window == null)
			return null;
		final IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;
		try {
			final IViewPart view = page.showView(viewId, secondaryId, mode);
			return view;
		} catch (PartInitException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Unable to open the view identified by " + viewId + " "
							+ secondaryId + ".", e);
		}
		return null;
	}

	/**
	 * Shows the perspective identified by the given id.
	 * 
	 * @param perspectiveId
	 *            the perspective to show.
	 */
	public static void showPerspective(final String perspectiveId) {
		IPerspectiveDescriptor p = PlatformUI.getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().setPerspective(p);
		} catch (NullPointerException e) {
			// ignore
		}
	}

	/*
	 * Adapted from the below article
	 * http://blog.zvikico.com/2008/07/eclipse-gracefully-loading-a-plugin.html
	 */

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

	public static void startup(final IRunnableWithProgress startupOp)
			throws InvocationTargetException, InterruptedException {
		final Shell activeShell = findStartedShell();
		if (activeShell != null) {
			final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
					activeShell);
			progressMonitorDialog.run(false, false, startupOp);
		} else {
			startupOp.run(new NullProgressMonitor());
		}
	}

	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
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

	/**
	 * Open the file with the given path with an Eclipse editor
	 */
	public static boolean openInEditor(String path) {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
		if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			final IWorkbenchWindow window = getIWorkbenchWindow();
			if (window != null) {
				final IWorkbenchPage page =  window.getActivePage();
				try {
					IEditorPart p = IDE.openEditorOnFileStore(page, fileStore);
					return p != null;
				} catch (PartInitException e) {
					/* some code */
				}
			}
		}
		return false;
	}
	
	private EclipseUIUtility() {
		// utility
	}
}
