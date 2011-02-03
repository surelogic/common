package com.surelogic.common.eclipse.adhoc.views.menu;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.eclipse.adhoc.views.QueryResultNavigator;
import com.surelogic.common.CommonImages;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.tooltip.ToolTip;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.PageBook;
import com.surelogic.common.ui.SLImages;

public final class QueryMenuMediator extends AdHocManagerAdapter implements
		ILifecycle {

	private final AbstractQueryMenuView f_view;
	private final AdHocManager f_manager;
	private final PageBook f_pageBook;
	private final Label f_noRunSelected;
	private final Table f_queryMenu;
	private final QueryResultNavigator f_navigator;

	private AdHocQuery f_selectedQuery = null;

	public QueryMenuMediator(final AbstractQueryMenuView view,
			final PageBook pageBook, final Label noRunSelected,
			final Table queryMenu, final QueryResultNavigator navigator) {
		f_view = view;
		f_manager = view.getManager();
		f_pageBook = pageBook;
		f_noRunSelected = noRunSelected;
		f_noRunSelected.setText(view.getNoDatabaseMessage());
		f_noRunSelected.setBackground(f_noRunSelected.getDisplay()
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		f_queryMenu = queryMenu;
		f_navigator = navigator;
	}

	public void init() {
		f_navigator.init();

		f_queryMenu.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				final AdHocQuery query = getQueryMenuSelection();
				setSelectedQuery(query);
			}
		});

		final Listener runQueryListener = new Listener() {
			public void handleEvent(final Event event) {
				runQueryAction();
			}
		};
		f_queryMenu.addListener(SWT.MouseDoubleClick, runQueryListener);

		final Menu menu = new Menu(f_queryMenu.getShell(), SWT.POP_UP);
		final MenuItem runQuery = new MenuItem(menu, SWT.PUSH);
		runQuery.setImage(SLImages.getImage(CommonImages.IMG_RUN_DRUM));
		runQuery.setText(I18N.msg("adhoc.query.menu.run"));
		runQuery.addListener(SWT.Selection, runQueryListener);
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(final Event event) {
				boolean menuItemEnabled = false;
				if (f_queryMenu.getSelectionCount() == 1) {
					final TableItem item = f_queryMenu.getSelection()[0];
					/*
					 * If there is data then the query can be run.
					 */
					if (item.getData() != null) {
						menuItemEnabled = true;
					}
				}
				runQuery.setEnabled(menuItemEnabled);
			}
		});
		f_queryMenu.setMenu(menu);

		f_manager.addObserver(this);

		updateQueryMenu();
	}

	public void dispose() {
		f_manager.removeObserver(this);
		f_navigator.init();
	}

	void setFocus() {
		f_queryMenu.setFocus();
	}

	private final UIJob f_generalRefreshJob = new SLUIJob() {
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			updateQueryMenu();
			return Status.OK_STATUS;
		}
	};

	private void generalRefresh() {
		// schedule to run in the UI thread
		f_generalRefreshJob.schedule();
	}

	@Override
	public void notifyQueryModelChange(final AdHocManager manager) {
		generalRefresh();
	}

	@Override
	public void notifyGlobalVariableValueChange(final AdHocManager manager) {
		generalRefresh();
	}

	@Override
	public void notifyResultModelChange(final AdHocManager manager) {
		generalRefresh();
	}

	@Override
	public void notifySelectedResultChange(final AdHocQueryResult result) {
		generalRefresh();
	}

	@Override
	public void notifyResultVariableValueChange(
			final AdHocQueryResultSqlData result) {
		generalRefresh();
	}

	private Map<String, String> getVariableValues() {
		final Map<String, String> result;
		final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
		if (selectedResult instanceof AdHocQueryResultSqlData) {
			result = ((AdHocQueryResultSqlData) selectedResult)
					.getVariableValues();
		} else {
			result = f_manager.getGlobalVariableValues();
		}
		return result;
	}

	private void updateQueryMenu() {
		f_queryMenu.setRedraw(false);
		f_queryMenu.removeAll();

		final Map<String, String> variableValues = getVariableValues();

		final AdHocQueryResult selectedResult = f_manager.getSelectedResult();

		boolean atLeastOneQueryIsShown = false;
		final List<AdHocQuery> queriesThatMightBeAbleToBeRun;
		if (selectedResult == null) {
			queriesThatMightBeAbleToBeRun = f_manager.getRootQueryList();
		} else {
			queriesThatMightBeAbleToBeRun = selectedResult.getQueryFullyBound()
					.getQuery().getVisibleSubQueryList();
		}
		final boolean hasDatabaseAccess = variableValues
				.containsKey(AdHocManager.DATABASE);
		for (final AdHocQuery query : queriesThatMightBeAbleToBeRun) {
			atLeastOneQueryIsShown = true;
			final TableItem item = new TableItem(f_queryMenu, SWT.NONE);
			item.setText(query.getDescription());
			item.setData(ToolTip.TIP_TEXT, query.getShortMessage());
			if (hasDatabaseAccess
					&& query.isCompletelySubstitutedBy(variableValues)) {
				if (f_view.queryResultWillBeEmpty(query)) {
					item.setImage(SLImages
							.getImage(CommonImages.IMG_QUERY_EMPTY));
				} else {
					if (selectedResult != null
							&& selectedResult.getQueryFullyBound().getQuery()
									.isDefaultSubQuery(query))
						item.setImage(SLImages
								.getImage(CommonImages.IMG_QUERY_DEFAULT));
					else
						item
								.setImage(SLImages
										.getImage(CommonImages.IMG_QUERY));
				}
				item.setData(query);
			} else {
				item.setForeground(f_queryMenu.getDisplay().getSystemColor(
						SWT.COLOR_GRAY));
			}
		}

		f_selectedQuery = null;

		f_queryMenu.setRedraw(true);
		if (!atLeastOneQueryIsShown) {
			final TableItem item = new TableItem(f_queryMenu, SWT.NONE);
			item.setText(I18N.msg("adhoc.query.menu.label.noQuery"));
			item.setForeground(f_queryMenu.getDisplay().getSystemColor(
					SWT.COLOR_GRAY));
		}

		if (selectedResult != null
				|| f_manager.getGlobalVariableValues().containsKey(
						AdHocManager.DATABASE)) {
			f_pageBook.showPage(f_queryMenu);
		} else {
			f_pageBook.showPage(f_noRunSelected);
		}
	}

	private void setSelectedQuery(final AdHocQuery query) {
		if (f_selectedQuery != query) {
			f_selectedQuery = query;
		}
	}

	private AdHocQuery getQueryMenuSelection() {
		AdHocQuery result = null;
		if (f_queryMenu.getSelectionCount() == 1) {
			final TableItem item = f_queryMenu.getSelection()[0];
			if (item.getData() instanceof AdHocQuery) {
				result = (AdHocQuery) item.getData();
			}
		}
		return result;
	}

	private void runQueryAction() {
		if (f_queryMenu.getSelectionCount() == 1) {
			final TableItem item = f_queryMenu.getSelection()[0];
			if (item.getData() instanceof AdHocQuery) {
				final AdHocQuery query = (AdHocQuery) item.getData();
				runQuery(query);
			}
		}
	}

	private void runQuery(final AdHocQuery query) {
		final Map<String, String> variableValues = getVariableValues();
		final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query,
				variableValues);
		final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
		if (selectedResult instanceof AdHocQueryResultSqlData) {
			EclipseQueryUtility.scheduleQuery(boundQuery,
					(AdHocQueryResultSqlData) selectedResult);
		} else {
			EclipseQueryUtility.scheduleQuery(boundQuery, f_manager
					.getDataSource().getCurrentAccessKeys());
		}
	}

	public void runRootQuery(final String id) {
		for (final AdHocQuery q : f_manager.getRootQueryList()) {
			if (q.getId().equals(id)) {
				runQuery(q);
				return;
			}
		}
	}
}
