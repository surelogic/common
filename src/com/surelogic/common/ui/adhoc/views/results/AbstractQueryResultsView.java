package com.surelogic.common.ui.adhoc.views.results;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.CommonImages;
import com.surelogic.common.Justification;
import com.surelogic.common.XUtil;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.AdHocQueryResultEmpty;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.AdHocQueryResultSqlException;
import com.surelogic.common.adhoc.AdHocQueryResultSqlUpdateCount;
import com.surelogic.common.adhoc.model.AdornedTreeTableModel;
import com.surelogic.common.adhoc.model.Cell;
import com.surelogic.common.adhoc.model.LeafTreeCell;
import com.surelogic.common.adhoc.model.NonLeafColumnSummary;
import com.surelogic.common.adhoc.model.NonLeafTreeCell;
import com.surelogic.common.adhoc.model.TreeCell;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.SWTUtility;
import com.surelogic.common.ui.TableUtility;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.adhoc.views.editor.AbstractQueryEditorView;
import com.surelogic.common.ui.adhoc.views.editor.SQLSyntaxHighlighterSkipFirstLine;
import com.surelogic.common.ui.tooltip.ToolTip;

public abstract class AbstractQueryResultsView extends ViewPart {

	public abstract AdHocManager getManager();

	private Composite f_parent = null;
	private QueryResultNavigator f_navigator = null;
	private AdHocQueryResult f_result = null;
	private ToolTip tooltip;
	private final Action f_collapseAllAction = new Action() {
		@Override
		public void run() {
			if (f_result != null && f_parent != null) {
				for (final Control comp : ((Composite) f_parent.getChildren()[0])
						.getChildren()) {
					if (comp instanceof Composite) {
						for (final Control c : ((Composite) comp).getChildren()) {
							if (c instanceof Tree) {
								final Tree t = (Tree) c;
								for (final TreeItem ti : t.getItems()) {
									ti.setExpanded(false);
								}
								return;
							}
						}
					}
				}
			}
		}
	};

	@Override
	public void createPartControl(final Composite parent) {
		f_parent = parent;

		f_collapseAllAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_COLLAPSE_ALL));
		f_collapseAllAction.setToolTipText(I18N
				.msg("adhoc.query.results.collapseAll"));

		f_navigator = QueryResultNavigator.getInstance(getManager()
				.getDataSource());
		f_navigator.addEnableWhenResultIsATree(f_collapseAllAction);
		f_navigator.init();

		final IActionBars actionBars = getViewSite().getActionBars();

		final IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(f_collapseAllAction);
		toolBar.add(f_navigator.getBackwardAction());
		toolBar.add(f_navigator.getForwardAction());
		toolBar.add(new Separator());
		toolBar.add(f_navigator.getDisposeAction());
		toolBar.add(f_navigator.getDisposeAllAction());

		final IMenuManager menu = actionBars.getMenuManager();
		menu.add(f_navigator.getShowDefinedVariablesAction());
		menu.add(f_navigator.getShowSqlAction());
		menu.add(f_navigator.getExportAction());
		menu.add(new Separator());
		menu.add(f_navigator.getClearSelectionAction());
		menu.add(new Separator());
		menu.add(f_navigator.getDisposeAction());
		menu.add(f_navigator.getDisposeAllAction());

		tooltip = getToolTip(parent.getShell());

		displayNoResults();
	}

	@Override
	public void dispose() {
		if (f_navigator != null) {
			f_navigator.dispose();
		}
	}

	private void disposeViewContents() {
		for (final Control c : f_parent.getChildren()) {
			c.dispose();
		}
	}

	@Override
	public void setFocus() {
		if (f_parent != null) {
			f_parent.setFocus();
		}
	}

	public AdHocQueryResult getResult() {
		return f_result;
	}

	/**
	 * Must be called from the SWT event dispatch thread.
	 */
	public void displayResult(final AdHocQueryResult result) {
		if (result != null) {
			f_result = result;
			if (f_result instanceof AdHocQueryResultEmpty) {
				displayResultEmpty((AdHocQueryResultEmpty) f_result);
			} else if (f_result instanceof AdHocQueryResultSqlData) {
				displayResultSqlData((AdHocQueryResultSqlData) f_result);
			} else if (f_result instanceof AdHocQueryResultSqlException) {
				displayResultSqlException((AdHocQueryResultSqlException) f_result);
			} else if (f_result instanceof AdHocQueryResultSqlUpdateCount) {
				displayResultSqlUpdateCount((AdHocQueryResultSqlUpdateCount) f_result);
			} else {
				throw new IllegalStateException(
						"unknown subtype of AdHocQueryResult "
								+ f_result.getClass().toString());
			}
		} else {
			f_result = null;
			displayNoResults();
		}
	}

	/**
	 * Constructs an outline of this view showing information about the query
	 * whose results are being displayed and sets up the toolbar.
	 * 
	 * @param result
	 *            the non-null result of running a query on the database.
	 * @return
	 */
	private Composite setupResultsPane(final AdHocQueryResult result) {
		assert result != null;

		final AdHocQueryResultSqlData data;
		if (result instanceof AdHocQueryResultSqlData) {
			data = (AdHocQueryResultSqlData) result;
		} else {
			data = null;
		}

		GridData gridData;
		final Composite panel = new Composite(f_parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		final int numColumns = data != null && data.isRowLimited() ? 3 : 1;
		layout.numColumns = numColumns;
		panel.setLayout(layout);

		final Link queryDescription = new Link(panel, SWT.NONE);
		queryDescription.setData(ToolTip.TIP_TEXT, result.getQueryFullyBound()
				.getQuery().getShortMessage());
		tooltip.activateToolTip(queryDescription);
		/*
		 * Add a hyperlink to edit the query if the result was a failure or an
		 * update count. Also always do this if the SureLogic experimental code
		 * flag is on.
		 */
		if (result.getManager().getDataSource().getEditorViewId() != null
				&& (result instanceof AdHocQueryResultSqlException
						|| result instanceof AdHocQueryResultSqlUpdateCount || XUtil
						.useExperimental())) {
			queryDescription.setText(result.toLinkString());
		} else {
			queryDescription.setText(result.toString());
		}
		gridData = new GridData(SWT.DEFAULT, SWT.CENTER, true, false);
		queryDescription.setLayoutData(gridData);
		queryDescription.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				/*
				 * The hyperlink to edit the query.
				 */
				editQueryInQueryEditor(result.getQueryFullyBound().getQuery());
			}
		});

		if (data != null && data.isRowLimited()) {
			final Label warningIcon = new Label(panel, SWT.NONE);
			warningIcon.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
			gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			warningIcon.setLayoutData(gridData);

			final Label limitWarning = new Label(panel, SWT.NONE);
			gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			limitWarning.setLayoutData(gridData);
			final int rows = data.getModel().getRowCount();
			limitWarning.setText(rows + " of "
					+ (rows + data.getNumRowsOmitted()) + " shown");
		}

		final Composite pane = new Composite(panel, SWT.NONE);
		pane.setLayout(new FillLayout());
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, numColumns, 1);
		pane.setLayoutData(gridData);
		return pane;
	}

	private void addLabel(final Composite parent, final String text) {
		final Label label = new Label(parent, SWT.WRAP);
		label.setText(text);
		label.setBackground(label.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
	}

	private void addLineAndThenSQL(final Composite parent, final String text) {
		final StyledText sql = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		sql.setFont(JFaceResources.getTextFont());
		sql.addLineStyleListener(new SQLSyntaxHighlighterSkipFirstLine(sql
				.getDisplay()));
		sql.setText(text);
	}

	private void setupMenu(final Menu menu) {
		menu.addListener(SWT.Show, new Listener() {

			public void handleEvent(final Event event) {
				for (final MenuItem item : menu.getItems()) {
					if (!item.isDisposed()) {
						item.dispose();
					}
				}

				if (!(f_result instanceof AdHocQueryResultSqlData)) {
					return; // bail out
				}

				final AdHocQueryResultSqlData result = (AdHocQueryResultSqlData) f_result;
				final List<AdHocQuery> subQueryList = result
						.getQueryFullyBound().getQuery()
						.getVisibleSubQueryList();
				final Map<String, String> variableValues = result
						.getVariableValues();
				final Listener runSubQuery = new Listener() {
					public void handleEvent(final Event event) {
						if (event.widget.getData() instanceof AdHocQuery) {
							final AdHocQuery query = (AdHocQuery) event.widget
									.getData();
							final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(
									query, variableValues);
							EclipseQueryUtility.scheduleQuery(boundQuery,
									result);
						}
					}
				};
				for (final AdHocQuery query : subQueryList) {
					final MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(query.getDescription());
					item.setData(query);
					item.setImage(SLImages.getImage(CommonImages.IMG_QUERY));
					item.setEnabled(query
							.isCompletelySubstitutedBy(variableValues));
					item.addListener(SWT.Selection, runSubQuery);
				}
				if (!subQueryList.isEmpty()) {
					new MenuItem(menu, SWT.SEPARATOR);
					final MenuItem copy = new MenuItem(menu, SWT.PUSH);
					copy.setText(I18N.msg("adhoc.query.results.copy"));
					copy.addListener(SWT.Selection, new Listener() {
						public void handleEvent(final Event event) {
							copySelection();
						}
					});
				}
			}
		});
	}

	private void displayNoResults() {
		disposeViewContents();

		setupNoResultsPane(f_parent);

		f_parent.layout();
	}

	/**
	 * This method is called when the query result are cleared and controls what
	 * is displayed in the view. By default this method displays a no results
	 * message in a {@link Label}.
	 * <p>
	 * This method is intended to be overridden by subclasses. The contents will
	 * be disposed when the view is changed to display the results of a query.
	 * The {@link Composite#layout()} method is called by the framework after
	 * this method is invoked.
	 * 
	 * @param parent
	 *            the parent composite to populate.
	 */
	protected void setupNoResultsPane(final Composite parent) {
		addLabel(f_parent, I18N.msg("adhoc.query.results.noResults.msg"));
	}

	private void displayResultEmpty(final AdHocQueryResultEmpty result) {
		disposeViewContents();

		final Composite panel = setupResultsPane(result);
		addLabel(panel, result.getEmptyMessage());

		f_parent.layout();
	}

	private void displayResultSqlData(final AdHocQueryResultSqlData data) {
		disposeViewContents();
		final AdornedTreeTableModel model = data.getModel();

		final Composite panel = setupResultsPane(data);
		panel.setLayout(new FillLayout());

		final Menu menu = new Menu(f_parent.getShell(), SWT.POP_UP);
		setupMenu(menu);

		if (model.isPureTable()) {
			final Table table = new Table(panel, SWT.BORDER
					| SWT.FULL_SELECTION);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setMenu(menu);

			// add the columns
			final String[] columnLabels = model.getColumnLabels();
			for (int colI = 0; colI < model.getColumnCount(); colI++) {
				if (model.isColumnVisible(colI)) {
					final String columnLabel = columnLabels[colI];
					final TableColumn column = new TableColumn(table, SWT.NONE);
					column.setText(columnLabel);
					final Justification justification = model
							.getColumnJustification(colI);
					if (justification == Justification.RIGHT) {
						column.setAlignment(SWT.RIGHT);
					} else if (justification == Justification.CENTER) {
						column.setAlignment(SWT.CENTER);
					}
					column.addListener(SWT.Selection,
							TableUtility.SORT_COLUMN_NUMERICALLY_THEN_LEXICALLY);
					column.setMoveable(true);
				}
			}
			if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_SOLARIS) {
				/*
				 * On GTK Eclipse we need to add an extra column because the
				 * table control doesn't allow blank space. What happens if the
				 * table columns are not as wide as the overall control is that
				 * the last column is made too wide. To avoid this display issue
				 * we just add a blank column.
				 */
				new TableColumn(table, SWT.NONE);
			}

			// add the rows
			final Cell[][] cells = model.getRows();
			for (int rowI = 0; rowI < cells.length; rowI++) {
				final Cell[] row = cells[rowI];
				final TableItem item = new TableItem(table, SWT.NONE);
				item.setData(rowI);
				int itemIndex = 0;
				final int columnCount = model.getColumnCount();
				for (int colI = 0; colI < columnCount; colI++) {
					if (model.isColumnVisible(colI)) {
						final Cell cell = row[colI];
						item.setText(itemIndex, cell.getText());
						item.setImage(itemIndex,
								SLImages.getImage(cell.getImageSymbolicName()));
						itemIndex++;
					}
				}
			}

			table.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
					int rowIndex = table.getSelectionIndex();
					if (rowIndex > -1) {
						rowIndex = (Integer) table.getItem(rowIndex).getData();
					}
					data.setSelectedRowIndex(rowIndex);
				}
			});
			table.addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event event) {
					runDefaultQueryOf(data);
				}
			});

			// minimize the column widths
			for (final TableColumn c : table.getColumns()) {
				c.pack();
			}
			// select the previously selected row
			final int selectedRowIndex = data.getSelectedRowIndex();
			if (selectedRowIndex != -1) {
				table.select(selectedRowIndex);
				table.showItem(table.getItem(selectedRowIndex));
			}
			// avoid scroll bar position being to the right
			table.showColumn(table.getColumn(0));
		} else {
			/*
			 * It is a tree or a tree-table
			 */
			final Tree tree = new Tree(panel, SWT.BORDER | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.FULL_SELECTION);
			tree.setHeaderVisible(true);
			tree.setLinesVisible(true);
			tree.setMenu(menu);
			final TreeColumn treePartColumn = new TreeColumn(tree, SWT.NONE);
			treePartColumn.setText(model.getTreePartColumnLabel());
			if (model.isTreeTable()) {
				final String[] columnLables = model.getColumnLabels();
				for (int colI = model.getLastTreeIndex() + 1; colI < model
						.getColumnCount(); colI++) {
					if (model.isColumnVisible(colI)) {
						final TreeColumn column = new TreeColumn(tree, SWT.NONE);
						column.setText(columnLables[colI]);
						final Justification justification = model
								.getColumnJustification(colI);
						if (justification == Justification.RIGHT) {
							column.setAlignment(SWT.RIGHT);
						} else if (justification == Justification.CENTER) {
							column.setAlignment(SWT.CENTER);
						}
					}
				}
				if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_SOLARIS) {
					/*
					 * On GTK Eclipse we need to add an extra column because the
					 * tree control doesn't allow blank space. What happens if
					 * the tree columns are not as wide as the overall control
					 * is that the last column is made too wide. To avoid this
					 * display issue we just add a blank column.
					 */
					new TreeColumn(tree, SWT.NONE);
				}
			}
			for (final TreeCell cell : model.getTreePart()) {
				addTreeCell(cell, tree, null, data);
			}
			tree.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
					boolean noSelectionMade = true;
					if (tree.getSelectionCount() == 1) {
						final TreeItem item = tree.getSelection()[0];
						if (item.getData() instanceof LeafTreeCell) {
							final LeafTreeCell leaf = (LeafTreeCell) item
									.getData();
							final int rowIndex = leaf.getRowIndex();
							data.setSelectedRowIndex(rowIndex);
							noSelectionMade = false;
						} else if (item.getData() instanceof NonLeafTreeCell) {
							final NonLeafTreeCell nonLeaf = (NonLeafTreeCell) item
									.getData();
							data.setSelectedCell(nonLeaf);
							noSelectionMade = false;
						}
					}
					if (noSelectionMade) {
						data.clearSelection();
					}
				}
			});
			tree.addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event event) {
					runDefaultQueryOf(data);
				}
			});

			// minimize the column widths
			for (final TreeColumn c : tree.getColumns()) {
				c.pack();
			}
			// select the previously selected row
			if (tree.getSelectionCount() == 1) {
				final TreeItem selected = tree.getSelection()[0];
				tree.showItem(selected);
			} else {
				if (tree.getItemCount() > 0) {
					tree.showItem(tree.getItems()[0]);
				}
			}
			// avoid scroll bar position being to the right
			if (tree.getColumnCount() > 0) {
				tree.showColumn(tree.getColumn(0));
			}
		}
		f_parent.layout();
	}

	private void runDefaultQueryOf(final AdHocQueryResultSqlData data) {
		final Map<String, String> variableValues = data.getVariableValues();
		final AdHocQuery query = data.getQueryFullyBound().getQuery()
				.getDefaultSubQuery();
		if (query != null) {
			final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(
					query, variableValues);
			EclipseQueryUtility.scheduleQuery(boundQuery, data);
		}
	}

	/**
	 * Helper method for {@link #displayResultSqlData(AdHocQueryResultSqlData)}.
	 */
	private void addTreeCell(final TreeCell cell, final Tree tree,
			final TreeItem parent, final AdHocQueryResultSqlData data) {
		final AdornedTreeTableModel model = data.getModel();
		final TreeItem item;
		if (parent == null) {
			item = new TreeItem(tree, SWT.NONE);
		} else {
			item = new TreeItem(parent, SWT.NONE);
		}
		showToLevel(tree, item, model.getLastTreeIndexInitiallyVisible());
		item.setData(cell);
		if (cell instanceof NonLeafTreeCell) {
			final NonLeafTreeCell nonLeaf = (NonLeafTreeCell) cell;
			item.setText(nonLeaf.getText());
			item.setImage(SLImages.getImage(nonLeaf.getImageSymbolicName()));
			for (final NonLeafColumnSummary columnSummary : nonLeaf
					.getColumnSummaries()) {
				final int itemIndex = columnSummary.getColumnIndex()
						- model.getLastTreeIndex();
				if (itemIndex >= 0) {
					item.setText(itemIndex, columnSummary.getText());
					item.setForeground(itemIndex, tree.getDisplay()
							.getSystemColor(SWT.COLOR_GRAY));
				}
			}
			if (nonLeaf == data.getSelectedCell()) {
				tree.setSelection(item);
			}
			for (final TreeCell child : nonLeaf.getChildren()) {
				addTreeCell(child, tree, item, data);
			}
		} else if (cell instanceof LeafTreeCell) {
			final LeafTreeCell leaf = (LeafTreeCell) cell;
			if (model.isPureTree()) {
				item.setText(leaf.getText());
				item.setImage(SLImages.getImage(leaf.getImageSymbolicName()));
			} else {
				int itemIndex = 0;
				for (int colI = model.getLastTreeIndex(); colI < model
						.getColumnCount(); colI++) {
					if (model.isColumnVisible(colI)) {
						final Cell rowCell = model.getRows()[leaf.getRowIndex()][colI];
						item.setText(itemIndex, rowCell.getText());
						item.setImage(itemIndex, SLImages.getImage(rowCell
								.getImageSymbolicName()));
						itemIndex++;
					}
				}
			}
			if (leaf.getRowIndex() == data.getSelectedRowIndex()) {
				tree.setSelection(item);
			}
		} else {
			throw new AssertionError(
					"TreeItem is neither a leaf or a non-leaf.");
		}
	}

	/**
	 * Helper method for {@link #displayResultSqlData(AdHocQueryResultSqlData)}.
	 */
	private void showToLevel(final Tree tree, final TreeItem item,
			final int level) {
		if (item == null) {
			return;
		}
		if (level <= 0) {
			return;
		}
		TreeItem work = item;
		for (int i = 0; i < level + 1; i++) {
			work = work.getParentItem();
			if (work == null) {
				tree.showItem(item);
				return;
			}
		}
	}

	private void displayResultSqlException(
			final AdHocQueryResultSqlException result) {
		disposeViewContents();

		final Composite panel = setupResultsPane(result);
		final AdHocQuery query = result.getQueryFullyBound().getQuery();
		final SQLException e = result.getSqlException();
		addLineAndThenSQL(panel, I18N.msg("adhoc.query.results.exception.msg",
				e.getMessage(), query.getSql()));

		f_parent.layout();
	}

	private void displayResultSqlUpdateCount(
			final AdHocQueryResultSqlUpdateCount result) {
		disposeViewContents();

		final Composite panel = setupResultsPane(result);
		final int updateCount = result.getUpdateCount();
		addLabel(panel, I18N.msg("adhoc.query.results.update.msg", updateCount));

		f_parent.layout();
	}

	public void copySelection() {
		if (f_result != null && f_result instanceof AdHocQueryResultSqlData) {
			final AdHocQueryResultSqlData result = (AdHocQueryResultSqlData) f_result;
			final StringBuilder b = new StringBuilder();
			final int selected = result.getSelectedRowIndex();
			if (selected != -1) {
				for (final Cell c : result.getModel().getRows()[selected]) {
					b.append(c.getText());
					b.append('\t');
				}
			} else {
				// We have a partial selection, copy what we
				// can.
				final NonLeafTreeCell selectedC = result.getSelectedCell();
				if (selectedC != null) {
					NonLeafTreeCell nltc = selectedC;

					final List<String> cells = new ArrayList<String>();
					while (nltc != null) {
						cells.add(0, nltc.getText());
						nltc = nltc.getParent();
					}
					for (final NonLeafColumnSummary nlcs : selectedC
							.getColumnSummaries()) {
						cells.add(nlcs.getText());
					}
					for (final String str : cells) {
						b.append(str);
						b.append('\t');
					}
				}
			}
			if (b.length() > 0) {
				final Clipboard cb = new Clipboard(SWTUtility.getShell()
						.getDisplay());
				cb.setContents(
						new Object[] {
						// Chop off end tab
						b.substring(0, b.length() - 1) },
						new Transfer[] { TextTransfer.getInstance() });
			}
		}
	}

	/**
	 * Must be called from the SWT thread.
	 * 
	 * @param query
	 *            the query to open in the query editor.
	 */
	private void editQueryInQueryEditor(final AdHocQuery query) {
		final String viewId = query.getManager().getDataSource()
				.getEditorViewId();
		if (viewId != null) {
			final IViewPart view = EclipseUIUtility.showView(viewId);
			if (view instanceof AbstractQueryEditorView) {
				((AbstractQueryEditorView) view).editInEditor(query);
			}
		}
	}

	public ToolTip getToolTip(final Shell shell) {
		return new ToolTip(shell);
	}

}
