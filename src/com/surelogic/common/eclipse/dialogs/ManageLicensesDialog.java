package com.surelogic.common.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.surelogic.common.CommonImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;

/**
 * A dialog to manage SureLogic tool licenses in Eclipse.
 */
public final class ManageLicensesDialog extends TitleAreaDialog {

	/**
	 * Used to open the license management dialog.
	 * 
	 * @param shell
	 *            a shell.
	 */
	public static void open(final Shell shell) {
		final ManageLicensesDialog dialog = new ManageLicensesDialog(shell);
		dialog.open();
	}

	private static final int CONTENTS_WIDTH_HINT = 600;

	private ManageLicensesMediator f_mediator = null;

	public ManageLicensesDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO));
		newShell.setText(I18N.msg("common.manage.licenses.dialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);

		final Composite panel = new Composite(contents, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = CONTENTS_WIDTH_HINT;
		panel.setLayoutData(data);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(I18N.msg("common.manage.licenses.dialog.info"));
		info.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2,
				1));

		final Table licenseTable = new Table(panel, SWT.MULTI
				| SWT.FULL_SELECTION);
		licenseTable
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		licenseTable.setHeaderVisible(true);
		licenseTable.setLinesVisible(true);

		final TableColumn product = new TableColumn(licenseTable, SWT.DEFAULT);
		product.setText(I18N
				.msg("common.manage.licenses.dialog.column.product"));

		final TableColumn activated = new TableColumn(licenseTable, SWT.DEFAULT);
		activated.setText(I18N
				.msg("common.manage.licenses.dialog.column.activated"));

		final TableColumn type = new TableColumn(licenseTable, SWT.DEFAULT);
		type.setText(I18N.msg("common.manage.licenses.dialog.column.type"));

		final TableColumn expired = new TableColumn(licenseTable, SWT.DEFAULT);
		expired.setText(I18N
				.msg("common.manage.licenses.dialog.column.expired"));

		final TableColumn issuedTo = new TableColumn(licenseTable, SWT.DEFAULT);
		issuedTo.setText(I18N
				.msg("common.manage.licenses.dialog.column.issuedTo"));

		final TableColumn id = new TableColumn(licenseTable, SWT.DEFAULT);
		id.setText(I18N.msg("common.manage.licenses.dialog.column.id"));

		final Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false,
				false));
		final RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		buttonPanel.setLayout(rl);

		final Button installFromFileButton = new Button(buttonPanel, SWT.PUSH);
		installFromFileButton.setText(I18N
				.msg("common.manage.licenses.dialog.installFromFile"));

		final Button installFromClipboardButton = new Button(buttonPanel,
				SWT.PUSH);
		installFromClipboardButton.setText(I18N
				.msg("common.manage.licenses.dialog.installFromClipboard"));

		final Button activateButton = new Button(buttonPanel, SWT.PUSH);
		activateButton.setText(I18N
				.msg("common.manage.licenses.dialog.activate"));
		activateButton.setEnabled(false);

		final Button renewButton = new Button(buttonPanel, SWT.PUSH);
		renewButton.setText(I18N.msg("common.manage.licenses.dialog.renew"));
		renewButton.setEnabled(false);

		final Button uninstallButton = new Button(buttonPanel, SWT.PUSH);
		uninstallButton.setText(I18N
				.msg("common.manage.licenses.dialog.uninstall"));
		uninstallButton.setEnabled(false);

		setTitle(I18N.msg("common.manage.licenses.dialog.msg.title"));
		setMessage(I18N.msg("common.manage.licenses.dialog.msg"),
				IMessageProvider.INFORMATION);
		Dialog.applyDialogFont(panel);

		f_mediator = new ManageLicensesMediator(licenseTable,
				installFromFileButton, installFromClipboardButton,
				activateButton, renewButton, uninstallButton);
		f_mediator.init();

		return contents;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	@Override
	public boolean close() {
		if (f_mediator != null)
			f_mediator.dispose();
		return super.close();
	}
}
