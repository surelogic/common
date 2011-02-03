package com.surelogic.common.eclipse.adhoc.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.adhoc.views.editor.SQLSyntaxHighlighter;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SWTUtility;

/**
 * Dialog to show the SQL that produced a query result.
 */
public final class ShowSqlDialog extends Dialog {

	/**
	 * Opens a dialog to show the SQL that produced a query result.
	 * 
	 * @param sql
	 *            the SQL query.
	 */
	public static void open(String sql) {
		Dialog dialog = new ShowSqlDialog(SWTUtility.getShell(), sql);
		dialog.open();
	}

	final String f_sql;

	protected ShowSqlDialog(Shell parentShell, String sql) {
		super(parentShell);
		if (sql == null)
			throw new IllegalArgumentException(I18N.err(44, "sql"));
		f_sql = sql;

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(I18N.msg("adhoc.query.dialog.showSql.title"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		directions.setText(I18N.msg("adhoc.query.dialog.showSql.msg"));

		final StyledText sql = new StyledText(panel, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		sql.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sql.setFont(JFaceResources.getTextFont());
		sql.addLineStyleListener(new SQLSyntaxHighlighter(sql.getDisplay()));
		sql.setText(f_sql);

		return panel;
	}
}
