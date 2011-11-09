package com.surelogic.common.ui.views;

import java.io.*;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;

/**
 * Various code that's proven handy in views
 * 
 * @author Edwin
 */
public abstract class AbstractSLView extends ViewPart {
	protected static final String[] noStrings = new String[0];
	
	protected Clipboard f_clipboard;

	private Action f_doubleClickAction;

	protected Control f_viewerControl;

	@Override
	public void createPartControl(Composite parent) {
		f_clipboard = new Clipboard(getSite().getShell().getDisplay());		
		f_viewerControl = buildViewer(parent);
		makeActions();
		if (getViewer() != null) {
			hookDoubleClickAction(getViewer());
			hookContextMenu(getViewer());
		}
		contributeToActionBars();
	}

	/**
	 * Setup the custom view
	 */
	protected abstract Control buildViewer(Composite parent);

	/**
	 * Enables various functionality if non-null
	 */
	protected StructuredViewer getViewer() {
		return null;
	}

	@Override
	public void setFocus() {
		f_viewerControl.setFocus();
	}

	/********************* Setup methods ******************************/

	private void hookContextMenu(final StructuredViewer viewer) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				AbstractSLView.this.fillContextMenu_private(manager, s);
			}
		});
		Menu menu = menuMgr.createContextMenu(f_viewerControl);
		f_viewerControl.setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu_private(IMenuManager manager, IStructuredSelection s) {
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

	private void hookDoubleClickAction(final StructuredViewer viewer) {
		f_doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				handleDoubleClick((IStructuredSelection) selection);
			}
		};
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				f_doubleClickAction.run();
			}
		});
	}

	protected void handleDoubleClick(IStructuredSelection selection) {
		// Nothing to do yet
	}

	protected void fillContextMenu(IMenuManager manager,
			IStructuredSelection s) {
		// Nothing to do yet
	}

	/********************* Utility methods ******************************/

	protected final void showMessage(String message) {
		MessageDialog.openInformation(f_viewerControl.getShell(), this
				.getClass().getSimpleName(), message);
	}

	/********************* Utility methods to help with persistent state ******************************/

	/**
	 * Create a list if there's something to add
	 */
	protected static LinkedList<String> loadStrings(BufferedReader br, LinkedList<String> strings) throws IOException {		
		String line;
		if (strings != null) {
			strings.clear();
		}
		while ((line = br.readLine()) != null) {
			if (line.length() == 0) {
				break;
			}
			if (strings == null) {
				strings = new LinkedList<String>();
			}
			strings.add(line);
			//System.out.println("Loaded: "+line);
		}
		return strings;
	}

	protected static void saveStrings(PrintWriter pw, LinkedList<String> strings) {
		for(String s : strings) {
			//System.out.println("Saving: "+s);
			pw.println(s); // TODO what if there are newlines?
		}
		pw.println(); // Marker for the end of the list
	}
	
	protected abstract class SelectionAction<T> extends Action {
		protected SelectionAction(String label) {
			super(label);
		}
		@Override
		public void run() {				
			if (getViewer() != null) {
				IStructuredSelection s = (IStructuredSelection) getViewer().getSelection();
				run(s);
			}
		}
		/**
		 * @return true if performed on at least one element
		 */
		protected abstract boolean run(IStructuredSelection s);
		protected abstract boolean run(T elt);
	}
	
	protected abstract class SingleSelectAction<T> extends SelectionAction<T> {
		protected SingleSelectAction(String label) {
			super(label);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean run(IStructuredSelection s) {				
			if (s.size() == 1) {
				return run((T) s.getFirstElement());
			} else {
				// TODO ignore with warning?
			}
			return false;
		}

	}
	
	protected abstract class MultiSelectAction<T> extends SelectionAction<T> {
		protected MultiSelectAction(String label) {
			super(label);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean run(IStructuredSelection s) {				
			if (s.size() > 0) {
				boolean success = false;
				Iterator it = s.iterator();
				while (it.hasNext()) {
					Object o = it.next();
					success |= run((T) o);
				}
				return success;
			}
			return false;
		}
	}
}

