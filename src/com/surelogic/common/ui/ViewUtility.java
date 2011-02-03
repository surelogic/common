package com.surelogic.common.ui;

import java.util.logging.Level;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;

public final class ViewUtility {

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

	/**
	 * Checks if a bundle identified by the given id exists.
	 * 
	 * @param viewId
	 *            a view identifier
	 * @return code true} if the view identified by the given id exists, {@code
	 *         false} otherwise.
	 */
	public static boolean bundleExists(final String id) {
		return Platform.getBundle(id) != null;
	}

	private ViewUtility() {
		// no instances
	}
}
