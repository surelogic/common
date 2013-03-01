package com.surelogic.common.ui.adhoc.views.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
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
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseColorUtility;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class QueryMenuMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final PageBook f_pageBook;
  private final Label f_noRunSelected;
  private final ScrolledComposite f_sc;
  private final Composite f_content;
  private final QueryResultNavigator f_navigator;
  private final Action f_showEmptyQueriesAction;
  private boolean f_showEmptyQueries;

  /**
   * This listener is used on double-click and the context menu to actually run
   * a query.
   */
  private final Listener f_runQueryListener = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final AdHocQuery query = getSelectionOrNull(event.widget);
      if (query != null)
        runQuery(query);
    }
  };

  private final Listener f_showQuerydocListener = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final AdHocQuery query = getSelectionOrNull(event.widget);
      final String viewId = f_manager.getDataSource().getQueryDocViewId();
      if (query != null && viewId != null)
        EclipseUIUtility.showView(viewId);
    }
  };

  /**
   * This listener maintains a single selection across all the category tables
   * shown in the menu.
   */
  private final Listener f_oneSelectionListener = new Listener() {
    @Override
    public void handleEvent(Event event) {
      for (Table t : getTables()) {
        if (t != event.widget)
          t.deselectAll();
      }
      final AdHocQuery query = getSelectionOrNull(event.widget);
      if (query != null)
        f_manager.setQuerydoc(query);
    }
  };

  /**
   * This listener is used to detect return events to launch a query.
   */
  private final Listener f_traverseListener = new Listener() {
    @Override
    public void handleEvent(Event event) {
      if (event.detail == SWT.TRAVERSE_RETURN) {
        f_runQueryListener.handleEvent(event);
      }
    }
  };

  /**
   * This handles up and down arrow movements within the tables so that the
   * keyboard can be used as if a categorized menu was one big table.
   */
  private final Listener f_keyDownListener = new Listener() {
    @Override
    public void handleEvent(Event event) {
      if (event.keyCode == SWT.ARROW_DOWN) {
        Table currentTable = (Table) event.widget;
        if (currentTable.getSelectionIndex() == currentTable.getItemCount() - 1) {
          // move up to next table and select the last item
          Table last = null;
          for (final Table current : getTables()) {
            if (last != null) {
              if (currentTable == last) {
                if (current.setFocus()) {
                  final Table finalLast = last;
                  currentTable.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                      // do later - Table, by default, has other behavior
                      current.setSelection(0);
                      finalLast.deselectAll();
                    }
                  });
                }
                break;
              }
            }
            last = current;
          }
        }
      } else if (event.keyCode == SWT.ARROW_UP) {
        Table currentTable = (Table) event.widget;
        if (currentTable.getSelectionIndex() == 0) {
          // move up to next table and select the last item
          Table last = null;
          for (final Table current : getTables()) {
            if (currentTable == current) {
              if (last != null) {
                if (last.setFocus()) {
                  final Table finalLast = last;
                  currentTable.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                      // do later - Table, by default, has other behavior
                      finalLast.setSelection(finalLast.getItemCount() - 1);
                      current.deselectAll();
                    }
                  });
                }
              }
              break;
            }
            last = current;
          }
        }
      }
    }
  };

  public QueryMenuMediator(final AbstractQueryMenuView view, final PageBook pageBook, final Label noRunSelected,
      final ScrolledComposite sc, final Composite content, final QueryResultNavigator navigator, final Action showEmptyQueriesAction) {
    f_manager = view.getManager();
    f_pageBook = pageBook;
    f_noRunSelected = noRunSelected;
    f_noRunSelected.setText(view.getNoDatabaseMessage());
    f_noRunSelected.setBackground(f_noRunSelected.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    f_sc = sc;
    f_content = content;
    f_navigator = navigator;
    f_showEmptyQueriesAction = showEmptyQueriesAction;
  }

  @Override
  public void init() {
    f_navigator.init();

    f_showEmptyQueries = EclipseUtility.getBooleanPreference(CommonCorePreferencesUtility.QMENU_SHOW_EMPTY_QUERIES);
    f_showEmptyQueriesAction.setChecked(f_showEmptyQueries);

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
    if (f_manager.getDataSource().getQueryDocViewId() != null) {
      final MenuItem showQuerydoc = new MenuItem(menu, SWT.PUSH);
      showQuerydoc.setImage(SLImages.getImage(CommonImages.IMG_FILE));
      showQuerydoc.setText(I18N.msg("adhoc.query.menu.querydoc"));
      showQuerydoc.addListener(SWT.Selection, f_showQuerydocListener);
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
          showQuerydoc.setEnabled(menuItemEnabled);
        }
      });
    }
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

  void notifyShowEmptyQueriesValueChange(final boolean value) {
    if (value != f_showEmptyQueries) {
      f_showEmptyQueries = value;
      EclipseUtility.setBooleanPreference(CommonCorePreferencesUtility.QMENU_SHOW_EMPTY_QUERIES, value);
      updateQueryMenu();
    }
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
        final List<AdHocQuery> rootQueriesNotInACategory = new ArrayList<AdHocQuery>(rootQueries);
        final List<AdHocCategory> categories = f_manager.getCategoryList();

        final List<AdHocCategory> emptyCategories = new ArrayList<AdHocCategory>();

        for (AdHocCategory category : categories) {
          final List<AdHocQuery> catQueries = category.getQueryList();
          catQueries.retainAll(rootQueries); // show later
          rootQueriesNotInACategory.removeAll(catQueries);
          if (catQueries.isEmpty() || noResults(catQueries)) {
            emptyCategories.add(category);
          } else {
            addCategoryTitleAndMessage(category, true);
            addQueryMenu(catQueries, selectedResult, variableValues);
          }
        }
        if (willAnyQueriesBeListed(rootQueriesNotInACategory)) {
          // miscellaneous queries not in a category
          if (!categories.isEmpty()) {
            // only show miscellaneous title if another category was shown
            addTitle(I18N.msg("adhoc.query.menu.label.misc.cat"), true);
          }
          // add in the rest
          addQueryMenu(rootQueriesNotInACategory, selectedResult, variableValues);
        }
        for (AdHocCategory category : emptyCategories) {
          addCategoryTitleAndMessage(category, false);
        }
      } else {
        /*
         * sub-query
         */
        List<AdHocQuery> subQueries = selectedResult.getQueryFullyBound().getQuery().getVisibleSubQueryList();
        if (!subQueries.isEmpty())
          addQueryMenu(subQueries, selectedResult, variableValues);
      }

      /*
       * Handle the case where no queries exist
       */
      boolean noChildren = f_content.getChildren().length == 0;
      if (noChildren) {
        final Label message = new Label(f_content, SWT.NONE);
        message.setText(I18N.msg("adhoc.query.menu.label.noQuery"));
        message.setForeground(EclipseColorUtility.getQueryMenuGrayColor());
        message.setBackground(f_noRunSelected.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        message.setLayoutData(data);
      }

      f_content.setSize(f_content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    } else {
      f_pageBook.showPage(f_noRunSelected);
    }

    f_content.setRedraw(true);
  }

  private void addQueryMenu(List<AdHocQuery> queries, AdHocQueryResult selectedResult, Map<String, String> variableValues) {
    final Table tm = new Table(f_content, SWT.NO_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
    final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    tm.setLayoutData(data);
    addContextMenuTo(tm);
    tm.addListener(SWT.MouseDoubleClick, f_runQueryListener);
    tm.addListener(SWT.Selection, f_oneSelectionListener);
    tm.addListener(SWT.Traverse, f_traverseListener);
    tm.addListener(SWT.KeyDown, f_keyDownListener);

    for (AdHocQuery query : queries) {
      if (query.isCompletelySubstitutedBy(variableValues)) {
        boolean emptyResult = query.resultIsKnownToBeEmpty();
        if (!emptyResult || f_showEmptyQueries) {
          final TableItem item = new TableItem(tm, SWT.NONE);
          item.setText(query.getDescription());
          final boolean decorateAsDefault = selectedResult != null
              && selectedResult.getQueryFullyBound().getQuery().isDefaultSubQuery(query);
          item.setImage(SLImages.getImageForAdHocQuery(query.getType(), decorateAsDefault, emptyResult));
          if (emptyResult)
            item.setForeground(EclipseColorUtility.getQueryMenuGrayColor());
          item.setData(query);
        }
      } else {
        final TableItem item = new TableItem(tm, SWT.NONE);
        item.setText(query.getDescription());
        item.setForeground(EclipseColorUtility.getQueryMenuGrayColor());
      }
    }
  }

  /**
   * Gets if none of the passed queries will return results.
   * 
   * @param catQueries
   *          a list of queries.
   * @return {@code true} if none of the passed queries will return results,
   *         {@code false} otherwise.
   */
  public boolean noResults(List<AdHocQuery> queries) {
    boolean result = true;
    for (AdHocQuery query : queries)
      result &= query.resultIsKnownToBeEmpty();
    return result;
  }

  public boolean willAnyQueriesBeListed(List<AdHocQuery> queries) {
    if (queries.isEmpty())
      return false;
    if (noResults(queries)) {
      return f_showEmptyQueries;
    } else
      return true;
  }

  public void addCategoryTitleAndMessage(final AdHocCategory category, boolean hasData) {
    addTitle(category.getDescription(), hasData);

    final Label message = new Label(f_content, SWT.WRAP);
    final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
    data.widthHint = 150; // needed for wrap to work at all
    message.setLayoutData(data);
    message.setForeground(hasData ? EclipseColorUtility.getQueryMenuSubtleColor() : EclipseColorUtility.getQueryMenuGrayColor());
    message.setBackground(f_noRunSelected.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    if (hasData) {
      message.setText(category.getHasDataText());
    } else {
      message.setText(category.getNoDataText());
    }
  }

  public void addTitle(final String text, boolean hasData) {
    final Label title = new Label(f_content, SWT.NONE);
    title.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.BANNER_FONT));
    title.setBackground(f_noRunSelected.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    title.setText(text);
    if (!hasData)
      title.setForeground(EclipseColorUtility.getQueryMenuGrayColor());
    final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
    title.setLayoutData(data);
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
    } else {
      // return the first selection
      for (Table current : getTables()) {
        if (current.getSelectionCount() == 1) {
          final TableItem item = current.getSelection()[0];
          if (item.getData() instanceof AdHocQuery) {
            result = (AdHocQuery) item.getData();
          }
        }
      }
    }
    return result;
  }

  private List<Table> getTables() {
    final List<Table> result = new ArrayList<Table>();
    for (Control child : f_content.getChildren()) {
      if (child instanceof Table)
        result.add((Table) child);
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
}
