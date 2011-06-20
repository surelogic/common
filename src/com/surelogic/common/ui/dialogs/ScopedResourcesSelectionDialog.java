package com.surelogic.common.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.internal.ui.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.surelogic.common.ui.EclipseUIUtility;

/**
 * Copied from:
 *    org.eclipse.debug.internal.ui.launchConfigurations.SaveScopeResourcesHandler
 *    
 * Opens a resizable dialog listing possible files to save, the user can select none, some or all of the files before pressing OK.
 */
@SuppressWarnings("restriction")
public class ScopedResourcesSelectionDialog extends AbstractDebugCheckboxSelectionDialog {
	private final String SETTINGS_ID = "com.surelogic.common.ui.dialogs.SCOPED_SAVE_SELECTION_DIALOG";
	Button fSavePref;
	Object fInput;
	final IStructuredContentProvider fContentProvider;
	final ILabelProvider fLabelProvider;
	final Config fConfig; 
	
	public ScopedResourcesSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider, 
			ILabelProvider labelProvider, Config config) {
		super(parentShell);
		fConfig = config;
		fInput = input;
		fContentProvider = contentProvider;
		fLabelProvider = labelProvider;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setShowSelectAllButtons(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getContentProvider()
	 */
	protected IContentProvider getContentProvider() {
		return fContentProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
	 */
	protected IBaseLabelProvider getLabelProvider() {
		return fLabelProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return SETTINGS_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SELECT_RESOURCES_TO_SAVE_DIALOG;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return "Select resources to save:";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		super.addCustomFooterControls(parent);
		fSavePref = new Button(parent, SWT.CHECK);
		fSavePref.setText(fConfig.alwaysSaveMsg);
		fSavePref.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getCheckBoxTableViewer().setAllChecked(fSavePref.getSelection());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#okPressed()
	 */
	protected void okPressed() {
		fConfig.setAlwaysSavePref(fSavePref.getSelection());
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		// Override to remove listener that affects the ok button
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog#isValid()
	 */
	protected boolean isValid() {
		return true;
	}
	
	//private static final IResource[] noResources = new IResource[0];
	
	public static class Config {
		final String title;
		final String alwaysSaveMsg;
		private boolean alwaysSavePref;
		
		public Config(String title, String alwaysSave, boolean alwaysSavePref) {
			this.title = title;
			this.alwaysSaveMsg = alwaysSave;
			this.alwaysSavePref = alwaysSavePref;
		}

		public void setAlwaysSavePref(boolean pref) {
			alwaysSavePref = pref;
		}
		
		public boolean getAlwaysSavePref() {
			return alwaysSavePref;
		}
	}
	
	/**
	 * @return false if cancelled
	 */
	public static boolean saveDirtyResources(IProject[] projects, Config config) {
		final IResource[] resources = showSaveDialog(projects, config);
		if (resources != null) {
			IDE.saveAllEditors(resources, false);
			return true;
		} else {
			return false;
		}		
	}
	
	public static IResource[] showSaveDialog(IProject[] projects, Config config) {
		IResource[] resources = EclipseUIUtility.getScopedDirtyResources(projects);
		if (!config.getAlwaysSavePref() && resources.length > 0) {
			ScopedResourcesSelectionDialog lsd = new ScopedResourcesSelectionDialog(DebugUIPlugin.getShell(),
					new AdaptableList(resources),
					new WorkbenchContentProvider(),
					new WorkbenchLabelProvider(), config);
			lsd.setInitialSelections(resources);
			lsd.setTitle(config.title);
			if(lsd.open() == IDialogConstants.CANCEL_ID) {
				return null;
			}
			Object[] objs = lsd.getResult();
			resources = new IResource[objs.length];
			for (int i = 0; i < objs.length; i++) {
				resources[i] = (IResource) objs[i];
			}
		}
		return resources;
	}
}