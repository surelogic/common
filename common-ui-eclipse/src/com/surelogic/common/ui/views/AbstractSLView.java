package com.surelogic.common.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * A helper view that calls various setup methods.
 * <p>
 * <i>Implementation note:</i> This class did a lot more in the past, however,
 * it registered listeners for features that most implementers did not need or
 * want so that has been moved to those implementations to avoid slowing down
 * all views.
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
		// default is to do nothing
	}

	@Override
	public void setFocus() {
		getCurrentControl().setFocus();
	}

	/*
	 * Setup methods
	 */

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Avoid using actions, use commands instead and hook handlers using
	 * {@link AbstractSLView#hookHandlersToCommands(IHandlerService)}.
	 */
	@Deprecated
	protected void fillLocalPullDown(IMenuManager manager) {
		// default is to do nothing
	}

	/**
	 * Avoid using actions, use commands instead and hook handlers using
	 * {@link AbstractSLView#hookHandlersToCommands(IHandlerService)}.
	 */
	@Deprecated
	protected void fillLocalToolBar(IToolBarManager manager) {
		// default is to do nothing
	}

	/**
	 * Avoid using actions, use commands instead and hook handlers using
	 * {@link AbstractSLView#hookHandlersToCommands(IHandlerService)}.
	 */
	@Deprecated
	protected void makeActions() {
		// default is to do nothing
	}

	/**
	 * This method may be overridden to hook handlers for the view into the
	 * view's commands: it's menu and toolbar. For example,
	 * 
	 * <pre>
	 * hs.activateHandler(
	 * 		&quot;com.surelogic.jsure.client.eclipse.command.XMLExplorerView.collapseAll&quot;,
	 * 		new AbstractHandler() {
	 * 			&#064;Override
	 * 			public Object execute(ExecutionEvent event)
	 * 					throws ExecutionException {
	 * 				attemptCollapseAll();
	 * 				return null;
	 * 			}
	 * 		});
	 * </pre>
	 * 
	 * Adds a new handler given the below XML setup for a toolbar item in the
	 * <tt>XMLExplorerView</tt> view.
	 * 
	 * <pre>
	 *   &lt;extension point="org.eclipse.ui.menus"&gt;
	 *      &lt;menuContribution
	 *           allPopups="false"
	 *           locationURI="menu:com.surelogic.jsure.client.eclipse.views.xml.XMLExplorerView"&gt;
	 *        &lt;command
	 *                 commandId="com.surelogic.jsure.client.eclipse.command.FindXMLForType"
	 *                 icon="platform:/plugin/com.surelogic.common/lib/images/open_xml_type.gif"
	 *                 label="Open Library Annotations..."
	 *                 mnemonic="L"
	 *                 style="push"
	 *                 tooltip="Open the library annotations for a type">
	 *           &lt;/command&gt;
	 *     &lt;/menuContribution&gt;
	 *  &lt;/extension&gt;
	 * </pre>
	 * 
	 * @param handlerService
	 *            provides services related to activating and deactivating
	 *            handlers within the workbench.
	 */
	protected void hookHandlersToCommands(IHandlerService hs) {
		// default is to do nothing
	}
}
