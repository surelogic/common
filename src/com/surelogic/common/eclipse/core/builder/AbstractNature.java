package com.surelogic.common.eclipse.core.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.surelogic.common.logging.SLLogger;

public abstract class AbstractNature implements IProjectNature {
	private static final Logger LOG = SLLogger.getLogger("com.surelogic.common.eclipse.builder");
	private final String builderId;
	
	/**
	 * the project this nature is being managed for
	 */
	private IProject project;

	protected AbstractNature(String id) {
		builderId = id;
	}
	
	/**
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public final void setProject(IProject project) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("setProject() called for project " + project.getName());
		}
		this.project = project;
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public final IProject getProject() {
		if (LOG.isLoggable(Level.FINE))
			LOG.fine("getProject() called");
		return project;
	}
	
	/**
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		if (LOG.isLoggable(Level.FINE))
			LOG.fine("configure() called");
		if (project == null) {
			LOG.log(Level.SEVERE,
					"the project is strangely null -- this should not happen");
			return;
		}
		addBuilderToProject(project);
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		if (project == null) {
			LOG.log(Level.SEVERE,
					"the project is strangely null -- this should not happen");
			return;
		}
		removeBuilderFromProject(project);
	}

	/**
	 * Checks if a specific builder exists within a project's builder list.
	 * 
	 * @param commands
	 *            a list of builders which we need to check if
	 *            <code>builderId</code> is contained within
	 * @param builderId
	 *            the builder we want to look for within <code>commands</code>
	 * @return <code>true</code> if the builder is listed in
	 *         <code>commands</code>, <code>false</code> otherwise
	 */
	public static boolean hasBuilder(ICommand[] commands, String builderId) {
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the builder to a project.
	 * 
	 * @param project
	 *            the project to add the builder to
	 * @throws CoreException
	 *             if we are unable to get a {@link IProjectDescription} for the
	 *             project (which is how project builders are managed)
	 */
	private void addBuilderToProject(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		if (!hasBuilder(commands, builderId)) {
			// add builder to project
			ICommand command = desc.newCommand();
			command.setBuilderName(builderId);
			ICommand[] newCommands = new ICommand[commands.length + 1];
			// Add it at the end of all the other builders (e.g., after Java
			// builder)
			System.arraycopy(commands, 0, newCommands, 0, commands.length);
			newCommands[newCommands.length - 1] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}

	/**
	 * Removes the builder from a project.
	 * 
	 * @param project
	 *            the project to remove the builder from
	 * @throws CoreException
	 *             if we are unable to get a {@link IProjectDescription} for the
	 *             project (which is how project builders are managed)
	 */
	private void removeBuilderFromProject(IProject project)
			throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		if (hasBuilder(commands, builderId)) {
			// remove builder from the project
			ICommand[] newCommands = new ICommand[commands.length - 1];
			int newCommandsIndex = 0;
			for (int i = 0; i < commands.length; ++i) {
				if (!commands[i].getBuilderName().equals(
						builderId)) {
					newCommands[newCommandsIndex++] = commands[i];
				}
			}
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}
}
