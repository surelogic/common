package com.surelogic.common.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

/**
 * An action on a single IProject
 * 
 * @author Edwin
 */
public abstract class AbstractSingleProjectAction 
implements IViewActionDelegate, IObjectActionDelegate {
	protected IProject project;
	
	@Override
  public void init(IViewPart view) {
		// Nothing to do right now
	}

	@Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do right now
	}
	
	@Override
  public final void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object obj = (((IStructuredSelection) selection).getFirstElement());
			if (obj != null) {
				project = (IProject) ((IAdaptable) obj)
						.getAdapter(IProject.class);
			} else {
				project = null;
			}
		}
	}
}
