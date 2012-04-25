package com.surelogic.common.ui.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.i18n.I18N;

public final class ChangeDataDirectoryDialog extends TitleAreaDialog {

	private static final int CONTENTS_WIDTH_HINT = 400;

	private final File f_existing;
	private final String f_title;
	private final String f_information;
	private final Image f_icon;

	private File f_newDataDirectory = null;

	public File getNewDataDirectory() {
		return f_newDataDirectory;
	}

	private boolean f_moveOldToNew = true;

	public boolean moveOldToNew() {
		return f_moveOldToNew;
	}

	public boolean isValidChangeToDataDirectory() {
		if (f_newDataDirectory == null)
			return false;
		if (f_newDataDirectory.equals(f_existing))
			return false;

		return true;
	}

	private Mediator f_mediator;

	public ChangeDataDirectoryDialog(Shell shell, File existing, String title,
			Image icon, String information) {
		super(shell);
		if (existing == null)
			throw new IllegalArgumentException(I18N.err(44, "existing"));
		f_existing = existing;
		f_title = title == null ? I18N
				.msg("common.change.data.directory.dialog.title") : title;
		f_icon = icon;
		f_information = information == null ? I18N
				.msg("common.change.data.directory.dialog.information")
				: information;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (f_icon != null)
			newShell.setImage(f_icon);
		newShell.setText(f_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);

		final Composite panel = new Composite(contents, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = CONTENTS_WIDTH_HINT;
		panel.setLayoutData(data);

		panel.setLayout(new GridLayout());

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(f_information);
		info.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		final Group dataGroup = new Group(panel, SWT.NONE);
		dataGroup
				.setText(I18N.msg("common.change.data.directory.dialog.group"));
		data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		data.verticalIndent = 10;
		dataGroup.setLayoutData(data);
		dataGroup.setLayout(new GridLayout(3, false));

		final Label currentLabel = new Label(dataGroup, SWT.RIGHT);
		currentLabel.setText(I18N
				.msg("common.change.data.directory.dialog.current"));
		currentLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));

		final Label currentPathLabel = new Label(dataGroup, SWT.NONE);
		currentPathLabel.setText(f_existing.getAbsolutePath());
		currentPathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		final Label newLabel = new Label(dataGroup, SWT.RIGHT);
		newLabel.setText(I18N.msg("common.change.data.directory.dialog.new"));
		newLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Text newText = new Text(dataGroup, SWT.SINGLE);
		newText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Button browseButton = new Button(dataGroup, SWT.PUSH);
		browseButton.setText(I18N
				.msg("common.change.data.directory.dialog.new.button"));
		browseButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
				false));

		final Button moveButton = new Button(panel, SWT.CHECK);
		moveButton
				.setText(I18N.msg("common.change.data.directory.dialog.move"));
		moveButton.setSelection(true);
		moveButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
				false));

		setTitle(I18N.msg("common.change.data.directory.dialog.msg.title"));
		setMessage(I18N.msg("common.change.data.directory.dialog.msg"),
				IMessageProvider.INFORMATION);
		Dialog.applyDialogFont(panel);

		f_mediator = new Mediator(newText, browseButton, moveButton);
		f_mediator.init();

		return contents;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		if (f_mediator != null)
			f_mediator.updateState();
	}

	@Override
	protected void okPressed() {
		if (f_mediator != null)
			f_mediator.okPressed();
		super.okPressed();
	}

	private final class Mediator {

		private final Text f_newText;
		private final Button f_browseButton;
		private final Button f_moveButton;

		Mediator(Text newText, Button browseButton, Button moveButton) {
			f_newText = newText;
			f_browseButton = browseButton;
			f_moveButton = moveButton;
		}

		public void init() {
			f_browseButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					final DirectoryDialog dd = new DirectoryDialog(
							f_browseButton.getShell());
					dd.setMessage(I18N
							.msg("common.change.data.directory.dialog.new.dirBrowse.desc"));
					dd.setText(I18N
							.msg("common.change.data.directory.dialog.new.dirBrowse.title"));
					final String path = dd.open();
					f_newText.setText(path);
				}
			});

			f_newText.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					updateState();
				}
			});
		}

		private void updateState() {
			final Button ok = getButton(IDialogConstants.OK_ID);
			final boolean hasDestination = getDestinationFile() != null;
			ok.setEnabled(hasDestination);
		}

		private void okPressed() {
			f_moveOldToNew = f_moveButton.getSelection();
			f_newDataDirectory = new File(f_newText.getText());
		}

		private File getDestinationFile() {
			final String path = f_newText.getText();
			if ("".equals(path))
				return null;
			return new File(path);
		}
	}
}
