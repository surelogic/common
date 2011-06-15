package com.surelogic.common.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class JavaProjectSelectionDialog extends Dialog {

	public static class Configuration {
		final String f_label;
		final String f_shellTitle;
		final Image f_shellImage;

		final List<IJavaProject> f_initiallySelectedJavaProjects;
		final String f_alwaysChooseFromDialogPreferenceConstant; // may be null
		final String f_prvUsrSelProjectListPreferenceConstant; // may be null

		public Configuration(final String label, final String shellTitle,
				final Image shellImage,
				final List<IJavaProject> initiallySelectedJavaProjects,
				final String alwaysChooseFromDialogPreferenceConstant,
				final String previousUserSelectedProjectListPreferenceConstant) {
			f_label = label;
			f_shellTitle = shellTitle;
			f_shellImage = shellImage;
			f_initiallySelectedJavaProjects = initiallySelectedJavaProjects;
			f_alwaysChooseFromDialogPreferenceConstant = alwaysChooseFromDialogPreferenceConstant;
			f_prvUsrSelProjectListPreferenceConstant = previousUserSelectedProjectListPreferenceConstant;
		}
	}

	private final Configuration f_configuration;
	private final List<IJavaProject> f_openJavaProjects;
	private Table f_projectTable;
	/**
	 * Aliased and visible to the static call
	 * {@link #getProjects(String, String, Image, List)}.
	 */
	private final List<IJavaProject> f_selectedProjects;

	/**
	 * Opens the dialog and gets projects from the user if necessary based upon
	 * the users preferences.
	 * 
	 * @param config
	 *            the dialog configuration. This contains information about what
	 *            to display and what preferences to access.
	 * @return
	 */
	public static List<IJavaProject> getProjects(final Configuration config) {
		/*
		 * If the set of initially selected Java projects is empty (meaning that
		 * there is no selection in the Package Explorer) or the user always
		 * wants to choose from a dialog then we show the project selection
		 * dialog.
		 */
		final boolean alwaysChooseFromDialog;
		if (config.f_alwaysChooseFromDialogPreferenceConstant != null) {
			alwaysChooseFromDialog = EclipseUtility
					.getBooleanPreference(config.f_alwaysChooseFromDialogPreferenceConstant);
		} else {
			alwaysChooseFromDialog = true;
		}
		if (config.f_initiallySelectedJavaProjects.isEmpty()
				|| alwaysChooseFromDialog) {

			final List<IJavaProject> openJavaProjects = JDTUtility
					.getJavaProjects();

			if (openJavaProjects.isEmpty()) {
				final UIJob job = new SLUIJob() {
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor) {
						final Shell shell = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell();
						final String msg = I18N
								.msg("common.dialog.noJavaProjectsOpen");
						final MessageDialog dialog = new MessageDialog(shell,
								"No Projects Open", config.f_shellImage, msg,
								MessageDialog.INFORMATION,
								new String[] { "OK" }, 0);
						dialog.open();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			} else {
				final List<IJavaProject> mutableProjectList = new ArrayList<IJavaProject>();
				final UIJob job = new SLUIJob() {
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor) {
						final Shell shell = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell();
						final JavaProjectSelectionDialog dialog = new JavaProjectSelectionDialog(
								shell, config, openJavaProjects,
								mutableProjectList);

						if (dialog.open() == Window.CANCEL) {
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				final IStatus status = job.runInUIThread(null);
				if (status == Status.CANCEL_STATUS) {
					return Collections.emptyList();
				} else {
					return mutableProjectList;
				}
			}
		}
		return config.f_initiallySelectedJavaProjects;
	}

	private JavaProjectSelectionDialog(final Shell parentShell,
			final Configuration config,
			final List<IJavaProject> openJavaProjects,
			final List<IJavaProject> mutableProjectList) {
		super(parentShell);
		this.f_configuration = config;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		f_openJavaProjects = openJavaProjects;
		Collections.sort(f_openJavaProjects, new Comparator<IJavaProject>() {
			public int compare(final IJavaProject o1, final IJavaProject o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getProject()
						.getName(), o2.getProject().getName());
			}
		});
		f_selectedProjects = mutableProjectList;
	}

	@Override
	protected final void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(f_configuration.f_shellImage);
		newShell.setText(f_configuration.f_shellTitle);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label label = new Label(panel, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		label.setText(f_configuration.f_label);

		f_projectTable = new Table(panel, SWT.FULL_SELECTION | SWT.CHECK);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		f_projectTable.setLayoutData(data);

		final boolean onlyOne = f_openJavaProjects.size() == 1;
		for (final IJavaProject jp : f_openJavaProjects) {
			final TableItem item = new TableItem(f_projectTable, SWT.NONE);
			item.setText(jp.getElementName());
			item.setImage(SLImages.getImage(CommonImages.IMG_PROJECT));
			item.setData(jp);
			if (f_configuration.f_initiallySelectedJavaProjects.contains(jp)
					|| onlyOne) {
				item.setChecked(true);
				f_selectedProjects.add(jp);
			}
		}

		f_projectTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				setOKState();
			}
		});

		final Composite allNonePanel = new Composite(panel, SWT.NONE);
		allNonePanel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final GridLayout allNoneLayout = new GridLayout();
		allNoneLayout.numColumns = 2;
		allNoneLayout.makeColumnsEqualWidth = true;
		allNonePanel.setLayout(allNoneLayout);
		final Button allButton = new Button(allNonePanel, SWT.PUSH);
		allButton.setText("Select All");
		allButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		allButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				selectAll();
			}
		});
		final Button noneButton = new Button(allNonePanel, SWT.PUSH);
		noneButton.setText("Deselect All");
		noneButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		noneButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				deselectAll();
			}
		});

		if (f_configuration.f_alwaysChooseFromDialogPreferenceConstant != null) {
			final Button check = new Button(panel, SWT.CHECK);
			check
					.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
							false));
			check
					.setText(I18N
							.msg("common.dialog.showDialogEvenWhenProjectsAreSelected"));
			check
					.setSelection(EclipseUtility
							.getBooleanPreference(f_configuration.f_alwaysChooseFromDialogPreferenceConstant));
			check.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
					final boolean show = !EclipseUtility
							.getBooleanPreference(f_configuration.f_alwaysChooseFromDialogPreferenceConstant);
					EclipseUtility
							.setBooleanPreference(
									f_configuration.f_alwaysChooseFromDialogPreferenceConstant,
									show);
					check.setSelection(show);
				}
			});
		}

		return panel;
	}

	@Override
	protected final Control createContents(final Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private final void setOKState() {
		/*
		 * Remember what is checked.
		 */
		f_selectedProjects.clear();
		if (f_projectTable != null && !f_projectTable.isDisposed()) {
			for (final TableItem item : f_projectTable.getItems()) {
				if (item.getChecked()) {
					f_selectedProjects.add((IJavaProject) item.getData());
				}
			}
			/*
			 * Set the state of the OK button.
			 */
			getButton(IDialogConstants.OK_ID).setEnabled(
					!f_selectedProjects.isEmpty());
		}
	}

	private void selectAll() {
		if (f_projectTable != null && !f_projectTable.isDisposed()) {
			for (final TableItem item : f_projectTable.getItems()) {
				item.setChecked(true);
			}
			setOKState();
		}
	}

	private void deselectAll() {
		if (f_projectTable != null && !f_projectTable.isDisposed()) {
			for (final TableItem item : f_projectTable.getItems()) {
				item.setChecked(false);
			}
			setOKState();
		}
	}
}
