package com.surelogic.common.ui.adhoc.views.menu;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.Pair;
import com.surelogic.common.adhoc.AdHocCategory;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.common.ui.tooltip.ToolTip;

public final class QueryMenuMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AbstractQueryMenuView f_view;
  private final AdHocManager f_manager;
  private final PageBook f_pageBook;
  private final Label f_noRunSelected;
  private final ScrolledComposite f_sc;
  private final Composite f_content;
  private final QueryResultNavigator f_navigator;

  private final Listener f_runQueryListener = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final AdHocQuery query = getSelectionOrNull(event.widget);
      if (query != null)
        runQuery(query);
    }
  };

  public QueryMenuMediator(final AbstractQueryMenuView view, final PageBook pageBook, final Label noRunSelected,
      final ScrolledComposite sc, final Composite content, final QueryResultNavigator navigator) {
    f_view = view;
    f_manager = view.getManager();
    f_pageBook = pageBook;
    f_noRunSelected = noRunSelected;
    f_noRunSelected.setText(view.getNoDatabaseMessage());
    f_noRunSelected.setBackground(f_noRunSelected.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    f_sc = sc;
    f_content = content;
    f_navigator = navigator;

    // TODO
    // final Table queryMenu = new Table(pageBook, SWT.BORDER |
    // SWT.FULL_SELECTION);
    // final ToolTip tip = getToolTip(parent.getShell());
    // tip.activateToolTip(queryMenu);
    // f_queryMenu.addListener(SWT.MouseDoubleClick, runQueryListener);

  }

  @Override
  public void init() {
    f_navigator.init();

    f_manager.addObserver(this);

    updateQueryMenu();
  }

  private void addContextMenuTo(final Table queryTable) {
    final Menu menu = new Menu(queryTable.getShell(), SWT.POP_UP);
    final MenuItem runQuery = new MenuItem(menu, SWT.PUSH);
    runQuery.setImage(SLImages.getImage(CommonImages.IMG_RUN_DRUM));
    runQuery.setText(I18N.msg("adhoc.query.menu.run"));
    runQuery.addListener(SWT.Selection, f_runQueryListener);
    menu.addListener(SWT.Show, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        boolean menuItemEnabled = false;
        if (queryTable.getSelectionCount() == 1) {
          final TableItem item = queryTable.getSelection()[0];
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
    queryTable.setMenu(menu);
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
    f_navigator.init();
  }

  void setFocus() {
    f_sc.setFocus();
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
  public void notifyResultVariableValueChange(final AdHocQueryResultSqlData result) {
    generalRefresh();
  }

  private Pair<Map<String, String>, Map<String, String>> getVariableValues() {
    final Map<String, String> all;
    final Map<String, String> top;
    final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
    if (selectedResult instanceof AdHocQueryResultSqlData) {
      final AdHocQueryResultSqlData result = (AdHocQueryResultSqlData) selectedResult;
      all = result.getVariableValues();
      top = result.getTopVariableValues();
    } else {
      all = f_manager.getGlobalVariableValues();
      top = Collections.emptyMap();
    }
    return new Pair<Map<String, String>, Map<String, String>>(all, top);
  }

  private void updateQueryMenu() {
    f_content.setRedraw(false);
    // clear out old widget contents
    for (Control child : f_content.getChildren())
      child.dispose();

    final Map<String, String> variableValues = getVariableValues().first();
    final boolean hasDatabaseAccess = variableValues.containsKey(AdHocManager.DATABASE);
    if (hasDatabaseAccess) {
      f_pageBook.showPage(f_sc);

      final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
      if (selectedResult == null) {
        /*
         * top-level query
         */
        final List<AdHocQuery> rootQueries = f_manager.getRootQueryList();
        final List<AdHocCategory> categories = f_manager.getCategoryList();

        // TODO SORT CATS

        for (AdHocCategory category : categories) {
          final List<AdHocQuery> catQueries = category.getQueryList();
          catQueries.retainAll(rootQueries);
          final Label catLabel = new Label(f_content, SWT.NONE);
          catLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.HEADER_FONT));
          catLabel.setText(category.getDescription());
          GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
          catLabel.setLayoutData(data);

          final Label catMsg = new Label(f_content, SWT.WRAP);
          data = new GridData(SWT.FILL, SWT.FILL, true, false);
          data.widthHint = 150; // needed for wrap to work at all
          catMsg.setLayoutData(data);
          catMsg.setForeground(catMsg.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
          if (catQueries.isEmpty()) {
            catMsg.setText(category.getNoDataText());
          } else {
            catMsg.setText(category.getHasDataText());
            addQueryMenu(catQueries, selectedResult, variableValues);
            rootQueries.removeAll(catQueries);
          }
        }
        // add in the rest
        addQueryMenu(rootQueries, selectedResult, variableValues);
      } else {
        /*
         * sub-query
         */
        addQueryMenu(selectedResult.getQueryFullyBound().getQuery().getVisibleSubQueryList(), selectedResult, variableValues);
      }

      /*
       * Handle the case where no queries exist
       */
      boolean noChildren = f_content.getChildren().length == 0;
      if (noChildren) {
        final Label msg = new Label(f_content, SWT.NONE);
        msg.setText(I18N.msg("adhoc.query.menu.label.noQuery"));
        final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        msg.setLayoutData(data);
      }

      f_content.setSize(f_content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    } else {
      f_pageBook.showPage(f_noRunSelected);
    }

    f_content.setRedraw(true);
  }

  private void addQueryMenu(List<AdHocQuery> queries, AdHocQueryResult selectedResult, Map<String, String> variableValues) {
    final Table tm = new Table(f_content, SWT.NO_SCROLL | SWT.FULL_SELECTION);
    final ToolTip tip = f_view.getToolTip(tm.getShell());
    tip.activateToolTip(tm);
    final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    tm.setLayoutData(data);
    addContextMenuTo(tm);
    tm.addListener(SWT.MouseDoubleClick, f_runQueryListener);

    // TODO A SORT THAT MAKES SENSE

    for (AdHocQuery query : queries) {
      final TableItem item = new TableItem(tm, SWT.NONE);
      item.setText(query.getDescription());
      // item.setData(ToolTip.TIP_TEXT, query.getShortMessage());
      if (query.isCompletelySubstitutedBy(variableValues)) {
        final boolean grayscale = query.resultIsKnownToBeEmpty();
        final boolean decorateAsDefault = selectedResult != null
            && selectedResult.getQueryFullyBound().getQuery().isDefaultSubQuery(query);
        item.setImage(SLImages.getImageForAdHocQuery(query.getType(), decorateAsDefault, grayscale));
        item.setData(query);
      } else {
        item.setForeground(tm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
      }
    }
  }

  @Nullable
  private AdHocQuery getSelectionOrNull(Widget w) {
    AdHocQuery result = null;
    if (w instanceof Table) {
      final Table t = (Table) w;
      if (t.getSelectionCount() == 1) {
        final TableItem item = t.getSelection()[0];
        if (item.getData() instanceof AdHocQuery) {
          result = (AdHocQuery) item.getData();
        }
      }
    }
    return result;
  }

  private void runQuery(final AdHocQuery query) {
    final Pair<Map<String, String>, Map<String, String>> variableValues = getVariableValues();
    final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query, variableValues.first(), variableValues.second());
    final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
    if (selectedResult instanceof AdHocQueryResultSqlData) {
      EclipseQueryUtility.scheduleQuery(boundQuery, (AdHocQueryResultSqlData) selectedResult);
    } else {
      EclipseQueryUtility.scheduleQuery(boundQuery, f_manager.getDataSource().getCurrentAccessKeys());
    }
  }

  void runRootQuery(final String id) {
    for (final AdHocQuery q : f_manager.getRootQueryList()) {
      if (q.getId().equals(id)) {
        runQuery(q);
        return;
      }
    }
  }
}
