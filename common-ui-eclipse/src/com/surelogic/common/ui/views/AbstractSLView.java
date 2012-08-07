package com.surelogic.common.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * A helper view that calls various setup methods.
 */
public abstract class AbstractSLView extends ViewPart {

	private Control f_viewerControl;

	@Override
	public void createPartControl(Composite parent) {
		f_viewerControl = buildViewer(parent);
		makeActions();
		if (f_viewerControl != null && getViewer() != null) {
			setupViewer(getViewer());
		}
		contributeToActionBars();
	}

	/**
	 * Setup the custom view
	 * 
	 * @return null if we need to call getViewer() to get the control
	 */
	protected abstract Control buildViewer(Composite parent);

	protected Control getCurrentControl() {
		if (f_viewerControl != null) {
			return f_viewerControl;
		}
		if (getViewer() != null) {
			return getViewer().getControl();
		}
		return null;
	}

	/**
	 * Enables various functionality if non-null
	 */
	protected StructuredViewer getViewer() {
		return null;
	}

	protected void setupViewer(StructuredViewer viewer) {
		hookContextMenu(viewer);
	}

	@Override
	public void setFocus() {
		getCurrentControl().setFocus();
	}

	/*
	 * Setup methods
	 */

	private void hookContextMenu(final StructuredViewer viewer) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection s = (IStructuredSelection) viewer
						.getSelection();
				AbstractSLView.this.fillContextMenu_private(manager, s);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu_private(IMenuManager manager,
			IStructuredSelection s) {
		fillContextMenu(manager, s);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillGlobalActionHandlers(bars);
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillGlobalActionHandlers(IActionBars bars) {
		// Nothing to do yet
	}

	protected abstract void fillLocalPullDown(IMenuManager manager);

	protected abstract void fillLocalToolBar(IToolBarManager manager);

	protected abstract void makeActions();

	protected void fillContextMenu(IMenuManager manager, IStructuredSelection s) {
		// Nothing to do yet
	}
}
