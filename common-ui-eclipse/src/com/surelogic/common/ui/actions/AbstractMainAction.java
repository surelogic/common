package com.surelogic.common.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractMainAction extends Action implements IWorkbenchWindowActionDelegate {

    public void dispose() {
        // Nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // Nothing to do
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // Nothing to do
    }

}
