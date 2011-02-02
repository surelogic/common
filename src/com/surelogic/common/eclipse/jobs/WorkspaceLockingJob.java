package com.surelogic.common.eclipse.jobs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Locks the workspace, as it does an atomic operation on it
 * 
 * @author Edwin
 */
public class WorkspaceLockingJob extends WorkspaceJob {
	public WorkspaceLockingJob(String name) {
		super(name);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		setRule(workspace.getRoot());
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		return Status.OK_STATUS;
	}
}
