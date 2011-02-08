package com.surelogic.common.ui.adhoc.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.CommonImages;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;

/**
 * Dialog to allow the user to select what sub-queries to add to a query.
 */
public class AddSubQueryDialog extends Dialog {

	/**
	 * Informs the user that no other queries exist.
	 */
	public static void openNoOtherQueries() {
		MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N
				.msg("adhoc.query.dialog.addASubQuery.title"), I18N
				.msg("adhoc.query.dialog.addASubQuery.noQueries.msg"));
	}

	private Table f_subQueryTable;

	private final List<AdHocQuery> f_queries = new ArrayList<AdHocQuery>();

	private final Set<AdHocQuery> f_selectedQueries = new HashSet<AdHocQuery>();

	/**
	 * Gets the set of queries that the user selected to become sub-queries.
	 * 
	 * @return the set of queries that the user selected to become sub-queries.
	 *         This set may be empty but it will not be {@code null}. The
	 *         reference returned is the same underlying set used by the dialog,
	 *         i.e., no copy is made.
	 */
	public Set<AdHocQuery> getSelectedQueries() {
		return f_selectedQueries;
	}

	/**
	 * Creates a dialog instance. If {@code queries.isEmpty()} the client should
	 * call {@link #openNoOtherQueries()} rather than constructing an instance
	 * of this dialog.
	 * 
	 * @param parentShell
	 * @param queries
	 */
	public AddSubQueryDialog(Shell parentShell, List<AdHocQuery> queries) {
		super(parentShell);

		if (queries.isEmpty())
			throw new IllegalArgumentException(I18N.err(128));

		f_queries.addAll(queries);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		newShell.setText(I18N.msg("adhoc.query.dialog.addASubQuery.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		directions.setText(I18N.msg("adhoc.query.dialog.addASubQuery.msg"));

		f_subQueryTable = new Table(panel, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.CHECK);
		f_subQueryTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		TableColumn col = new TableColumn(f_subQueryTable, SWT.NONE);
		col.setText(I18N.msg("adhoc.query.dialog.addASubQuery.id"));
		col.pack();
		col = new TableColumn(f_subQueryTable, SWT.NONE);
		col.setText(I18N.msg("adhoc.query.dialog.addASubQuery.description"));
		col.pack();
		f_subQueryTable.setHeaderVisible(true);
		f_subQueryTable.setLinesVisible(true);

		for (AdHocQuery query : f_queries) {
			final TableItem item = new TableItem(f_subQueryTable, SWT.NONE);
			item.setText(0, query.getId());
			item.setImage(0, SLImages.getImage(query.getImageSymbolicName()));
			item.setText(1, query.getDescription());
			item.setData(query);
		}

		for (TableColumn c : f_subQueryTable.getColumns())
			c.pack();

		// add controls to composite as necessary
		return panel;
	}

	@Override
	protected void okPressed() {
		for (TableItem item : f_subQueryTable.getItems()) {
			if (item.getChecked()) {
				f_selectedQueries.add((AdHocQuery) item.getData());
			}
		}
		super.okPressed();
	}
}
