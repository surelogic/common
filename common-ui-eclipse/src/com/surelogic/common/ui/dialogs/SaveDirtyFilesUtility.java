package com.surelogic.common.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.surelogic.common.ui.EclipseUIUtility;

public class SaveDirtyFilesUtility {
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
			ListSelectionDialog dlg = new ListSelectionDialog(EclipseUIUtility.getShell(),
					new AdaptableList(resources),
					new WorkbenchContentProvider(),
					new WorkbenchLabelProvider(),
			        "Select the resources to save:");
			dlg.setInitialSelections(resources);
			dlg.setTitle(config.title);
			if (dlg.open() == IDialogConstants.CANCEL_ID) {
				return null;
			}
			Object[] objs = dlg.getResult();
			resources = new IResource[objs.length];
			for (int i = 0; i < objs.length; i++) {
				resources[i] = (IResource) objs[i];
			}
		}
		return resources;
	}
}
