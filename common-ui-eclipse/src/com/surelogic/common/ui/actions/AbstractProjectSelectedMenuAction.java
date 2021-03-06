package com.surelogic.common.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.common.ui.handlers.AbstractProjectSelectedMenuHandler;

/**
 * Use {@link AbstractProjectSelectedMenuHandler} and the Eclipse command API.
 */
@Deprecated
public abstract class AbstractProjectSelectedMenuAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

  protected abstract void runActionOn(List<IJavaProject> selectedProjects);

  private IStructuredSelection f_currentSelection = null;

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // Nothing to do
  }

  @Override
  public final void run(IAction action) {
    /*
     * Beware the action parameter may be null.
     */
    if (f_currentSelection != null) {
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IJavaModel javaModel = JavaCore.create(root);
      final List<IJavaProject> selectedProjects = new ArrayList<>();
      for (Object selection : f_currentSelection.toArray()) {
        final IJavaProject javaProject;
        outer: if (selection instanceof IJavaProject) {
          javaProject = (IJavaProject) selection;
        } else if (selection instanceof IProject) {
          IProject p = (IProject) selection;
          try {
            for (IJavaProject jp : javaModel.getJavaProjects()) {
              if (p.equals(jp.getProject())) {
                javaProject = jp;
                break outer;
              }
            }
          } catch (JavaModelException e) {
            // Do nothing
          }
          continue;
        } else
          continue;

        selectedProjects.add(javaProject);
      }
      runHelper(selectedProjects);
    } else {
      runHelper(Collections.<IJavaProject> emptyList());
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      f_currentSelection = (IStructuredSelection) selection;
    } else {
      f_currentSelection = null;
    }
  }

  @Override
  public void dispose() {
    // Nothing to do
  }

  @Override
  public void init(IWorkbenchWindow window) {
    // Nothing to do
  }

  protected static List<String> getNames(final List<IJavaProject> projects) {
    List<String> names = new ArrayList<>();
    for (IJavaProject jp : projects) {
      names.add(jp.getElementName());
    }
    return names;
  }

  public void run() {
    run((IAction) null);
  }

  public void run(List<IJavaProject> projects) {
    if (projects == null || projects.isEmpty()) {
      return;
    }
    runHelper(projects);
  }

  private void runHelper(final List<IJavaProject> selectedProjects) {
    List<IJavaProject> projects = selectedProjects;
    // Need a dialog?
    final JavaProjectSelectionDialog.Configuration info = getDialogInfo(projects);
    if (info != null) {
      projects = JavaProjectSelectionDialog.getProjects(info);
    }
    runActionOn(projects);
  }

  protected JavaProjectSelectionDialog.Configuration getDialogInfo(List<IJavaProject> selectedProjects) {
    return null;
  }
}
