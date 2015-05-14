package com.surelogic.common.ui.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;

public abstract class AbstractProjectSelectedMenuHandler extends
		AbstractHandler {

	protected abstract void runActionOn(List<IJavaProject> selectedProjects);

	@Override
	final public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ISelection ideSelection;
		if (window == null) {
			ideSelection = null;
		} else {
			final IWorkbenchPage page = window.getActivePage();
			ideSelection = page == null ? null : page.getSelection();	
		}
		if (ideSelection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) ideSelection;
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
					.getRoot();
			final IJavaModel javaModel = JavaCore.create(root);
			final List<IJavaProject> selectedProjects = new ArrayList<>();
			for (Object selection : structuredSelection.toArray()) {
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
		return null;
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

	protected JavaProjectSelectionDialog.Configuration getDialogInfo(
			List<IJavaProject> selectedProjects) {
		return null;
	}
}
