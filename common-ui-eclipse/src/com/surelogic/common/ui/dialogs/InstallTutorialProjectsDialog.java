package com.surelogic.common.ui.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;

public class InstallTutorialProjectsDialog extends TitleAreaDialog {
	private static final int CONTENTS_WIDTH_HINT = 400;
	private final String logo;
	private final String help;
	private final URL[] projects;
	private final boolean[] toInstall;
	private Button showHelp;
	private Tree projectTree;

	/**
	 * Used to open the dialog
	 * 
	 * @param logo
	 *            a {@link CommonImages} location that corresponds to the
	 *            application logo
	 * @param shell
	 *            a shell.
	 * @param href
	 *            the help document location of the tutorial section.
	 * @param projects
	 *            the URL locations of each project to be added. The project
	 *            should be in the form of <tt>Name.zip</tt>, where
	 *            <tt>Name</tt> is the name of the eclipse project to be added.
	 *            Typically these are created with a call to
	 *            {@link ClassLoader#getResource(String)}.
	 */
	public static void open(final Shell shell, final String logo,
			final String href, final URL... projects) {
		if (projects.length == 0)
			throw new IllegalArgumentException(I18N.err(173));

		final InstallTutorialProjectsDialog dialog = new InstallTutorialProjectsDialog(
				shell, logo, href, projects);
		dialog.open();
	}

	/**
	 * Used to open the dialog
	 * 
	 * @param logo
	 *            a {@link CommonImages} location that corresponds to the
	 *            application logo
	 * @param shell
	 *            a shell.
	 * @param href
	 *            the help document location of the tutorial section.
	 * @param projects
	 *            the URL locations of each project to be added. The project
	 *            should be in the form of <tt>Name.zip</tt>, where
	 *            <tt>Name</tt> is the name of the eclipse project to be added.
	 *            Typically these are created with a call to
	 *            {@link ClassLoader#getResource(String)}.
	 */
	public static void open(final Shell shell, final String logo,
			final String href, final List<URL> projects) {
		open(shell, logo, href, projects.toArray(new URL[projects.size()]));
	}

	public InstallTutorialProjectsDialog(final Shell parentShell,
			final String logo, final String href, final URL[] projects) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.logo = logo;
		this.help = href;
		this.projects = projects;
		toInstall = new boolean[projects.length];
		for (int i = 0; i < toInstall.length; i++) {
			toInstall[i] = true;
		}
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(I18N.msg("common.tutorial.dialog.title"));
		newShell.setImage(SLImages.getImage(logo));
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);
		final Composite panel = new Composite(contents, SWT.NONE);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = CONTENTS_WIDTH_HINT;
		panel.setLayoutData(data);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		panel.setLayout(gridLayout);

		projectTree = new Tree(panel, SWT.CHECK | SWT.FULL_SELECTION);
		projectTree.setHeaderVisible(true);
		projectTree.setLinesVisible(true);
		projectTree.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
				false));
		final TreeColumn projectColumn = new TreeColumn(projectTree, SWT.NONE);
		projectColumn.setText(I18N.msg("common.tutorial.dialog.projectCol"));
		for (final URL project : projects) {
			final String name = projectName(project);
			final TreeItem item = new TreeItem(projectTree, SWT.NONE);
			item.setText(name);
			item.setChecked(true);
		}
		projectColumn.pack();
		showHelp = new Button(panel, SWT.CHECK);
		showHelp.setText(I18N.msg("common.tutorial.dialog.checkbox"));
		showHelp.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false));
		showHelp.setSelection(true);

		setTitle(I18N.msg("common.tutorial.dialog.title"));
		setMessage(I18N.msg("common.tutorial.dialog.info"),
				IMessageProvider.INFORMATION);
		Dialog.applyDialogFont(panel);

		return contents;
	}

	@Override
	protected void okPressed() {
		/*
		 * Construct the list of projects to import.
		 */
		final List<URL> projectList = new ArrayList<URL>();
		int i = 0;
		for (final URL project : projects) {
			if (projectTree.getItem(i++).getChecked()) {
				projectList.add(project);
			}
		}
		final boolean showHelpToTheUser = showHelp.getSelection();

		final SLJob job = new AbstractSLJob("Importing tutorial projects") {
			@Override
      public SLStatus run(SLProgressMonitor monitor) {
				monitor.begin(projectList.size() + 1);
				try {
					for (final URL project : projectList) {
						final IProject p = EclipseUtility
								.unzipToWorkspace(project);
						String workingProjectName = project.getPath();
						if (workingProjectName.length() > 5) {
							workingProjectName = workingProjectName
									.substring(5);
						}
						if (workingProjectName.length() > 5) {
							workingProjectName = workingProjectName.substring(
									0, workingProjectName.length() - 4);
						}
						final String projectName = workingProjectName;
						if (p == null) {
							final UIJob uiJob = new SLUIJob() {
								@Override
								public IStatus runInUIThread(
										IProgressMonitor monitor) {
									MessageDialog
											.openInformation(
													EclipseUIUtility.getShell(),
													"Project Exists in Workspace",
													"The project\n\n"
															+ projectName
															+ "\n\nalready exists in your workspace.\n\nPlease delete the existing project from the disk if you wish to re-import this tutorial.");
									return Status.OK_STATUS;
								}
							};
							uiJob.schedule();
						}
						monitor.worked(1);
					}
					if (showHelpToTheUser) {
						final UIJob uiJob = new SLUIJob() {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								PlatformUI.getWorkbench().getHelpSystem()
										.displayHelpResource(help);
								return Status.OK_STATUS;
							}
						};
						uiJob.schedule();
					}
					monitor.worked(1);
				} catch (final Exception e) {
					final int errno = 174;
					return SLStatus
							.createErrorStatus(errno, I18N.err(errno), e);
				} finally {
					monitor.done();
				}
				return SLStatus.OK_STATUS;
			}
		};
		final Job eJob = EclipseUtility.toEclipseJob(job);
		eJob.setUser(true);
		eJob.schedule();
		super.okPressed();
	}

	private static String projectName(final URL url) {
		final String zipPath = url.getPath();
		final int lastSlash = zipPath.lastIndexOf('/');
		final String zipName = zipPath.substring(lastSlash + 1);
		final String name;
		if (zipName.lastIndexOf('.') >= 0) {
			// Remove suffix
			name = zipName.substring(0, zipName.lastIndexOf('.'));
		} else {
			name = zipName;
		}
		return name;
	}

}
