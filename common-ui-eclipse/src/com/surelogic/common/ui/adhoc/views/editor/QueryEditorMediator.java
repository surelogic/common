package com.surelogic.common.ui.adhoc.views.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryType;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.adhoc.dialogs.AddSubQueryDialog;
import com.surelogic.common.ui.adhoc.dialogs.VariableValueDialog;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.common.ui.tooltip.ToolTip;

public final class QueryEditorMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final SashForm f_sash;
  private final Composite f_lhs;
  private final TabFolder f_lhsFolder;
  private final Table f_queryList;
  private final Tree f_queryTree;
  private final Button f_filterTreeCheck;
  private final Menu f_queryActionMenu;
  private final ToolItem f_runQuery;
  private final ToolItem f_newQuery;
  private final ToolItem f_deleteQuery;
  private final PageBook f_rhs;
  private final Label f_noSelectionPane;
  private final Composite f_selectionPane;
  private final Text f_descriptionText;
  private final Text f_idText;
  private final Text f_cdText;
  private final Spinner f_sortHint;
  private final Combo f_type;
  private final Button f_showCheck;
  private final Button f_showAtRootCheck;
  private final TabFolder f_sqlFolder;
  private final StyledText f_sql;
  private final ToolItem f_addSubQuery;
  private final ToolItem f_deleteSubQuery;
  private final Table f_subQueryTable;

  private final Set<AdHocQuery> f_selections = new HashSet<AdHocQuery>();
  private AdHocQuery f_edit = null;
  private boolean f_filterTree = false;

  QueryEditorMediator(AbstractQueryEditorView view, SashForm sash, Composite lhs, TabFolder lhsFolder, Table queryList,
      Tree queryTree, Button filterTreeCheck, Menu queryActionMenu, ToolItem runQuery, ToolItem newQuery, ToolItem deleteQuery,
      PageBook rhs, Label noSelectionPane, Composite selectionPane, Text descriptionText, Text idText, Text cdText,
      Spinner sortHint, Combo type, Button showCheck, Button showAtRootCheck, TabFolder sqlFolder, StyledText sql,
      ToolItem addSubQuery, ToolItem deleteSubQuery, Table subQueryTable) {
    f_manager = view.getManager();
    f_sash = sash;
    f_lhs = lhs;
    f_lhsFolder = lhsFolder;
    f_queryList = queryList;
    f_queryTree = queryTree;
    f_filterTreeCheck = filterTreeCheck;
    f_queryActionMenu = queryActionMenu;
    f_runQuery = runQuery;
    f_newQuery = newQuery;
    f_deleteQuery = deleteQuery;
    f_rhs = rhs;
    f_noSelectionPane = noSelectionPane;
    f_selectionPane = selectionPane;
    f_descriptionText = descriptionText;
    f_idText = idText;
    f_cdText = cdText;
    f_sortHint = sortHint;
    f_type = type;
    f_showCheck = showCheck;
    f_showAtRootCheck = showAtRootCheck;
    f_sqlFolder = sqlFolder;
    f_sql = sql;
    f_addSubQuery = addSubQuery;
    f_deleteSubQuery = deleteSubQuery;
    f_subQueryTable = subQueryTable;
  }

  @Override
  public void init() {
    f_sash.setWeights(new int[] { EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QEDITOR_SASH_LHS_WEIGHT),
        EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QEDITOR_SASH_RHS_WEIGHT) });

    /*
     * When the left-hand-side composite is resized we'll just guess that the
     * sash is involved. Hopefully, this is conservative. This seems to be the
     * only way to do this.
     */
    f_lhs.addListener(SWT.Resize, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final int[] weights = f_sash.getWeights();
        if (weights != null && weights.length == 2) {
          EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QEDITOR_SASH_LHS_WEIGHT, weights[0]);
          EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QEDITOR_SASH_RHS_WEIGHT, weights[1]);
        }
      }
    });

    f_lhsFolder.setSelection(EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QEDITOR_LHS_TAB_SELECTION));
    f_lhsFolder.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QEDITOR_LHS_TAB_SELECTION, f_lhsFolder.getSelectionIndex());
      }
    });

    f_sqlFolder.setSelection(EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QEDITOR_SQL_TAB_SELECTION));
    f_sqlFolder.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QEDITOR_SQL_TAB_SELECTION, f_lhsFolder.getSelectionIndex());
      }
    });

    final Listener selectionListener = new Listener() {
      @Override
      public void handleEvent(final Event event) {
        querySelectionAction(event.widget);
      }
    };
    f_queryList.addListener(SWT.Selection, selectionListener);
    f_queryTree.addListener(SWT.Selection, selectionListener);

    final Listener doubleClickListener = new Listener() {
      @Override
      public void handleEvent(final Event event) {
        runQueryAction();
      }
    };
    f_queryList.addListener(SWT.MouseDoubleClick, doubleClickListener);
    f_queryTree.addListener(SWT.MouseDoubleClick, doubleClickListener);

    f_filterTree = EclipseUtility.getBooleanPreference(CommonCorePreferencesUtility.QEDITOR_FILTER_TREE_CHECK);
    f_filterTreeCheck.setSelection(f_filterTree);
    f_filterTreeCheck.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final boolean filterTree = f_filterTreeCheck.getSelection();
        if (filterTree != f_filterTree) {
          f_filterTree = filterTree;
          EclipseUtility.setBooleanPreference(CommonCorePreferencesUtility.QEDITOR_FILTER_TREE_CHECK, filterTree);
          f_manager.notifyQueryModelChange();
        }
      }
    });

    f_queryActionMenu.addListener(SWT.Show, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        for (final MenuItem item : f_queryActionMenu.getItems()) {
          item.dispose();
        }
        showQueryActionMenu();
      }
    });

    f_runQuery.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        runQueryAction();
      }
    });

    f_newQuery.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final String id = f_manager.generateUnusedId();
        final AdHocQuery query = f_manager.getOrCreateQuery(id);
        query.setDescription("A new query");
        f_selections.clear();
        f_selections.add(query);
        f_manager.notifyQueryModelChange();
      }
    });

    f_deleteQuery.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        deleteQueryAction();
      }
    });

    f_descriptionText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleDescriptionTextChanges();
      }
    });

    f_cdText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleCustomDisplayTextChanges();
      }
    });

    f_sortHint.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleSortHintChanges();
      }
    });

    f_type.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        savePossibleTypeChanges();
      }
    });

    f_showCheck.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final boolean show = f_showCheck.getSelection();
        if (show != f_edit.showInQueryMenu()) {
          /*
           * http://surelogic.com/bugzilla/show_bug.cgi?id=21
           */
          saveAllPossibleTextEditingChanges();
          if (f_edit.setShowInQueryMenu(show)) {
            f_edit.markAsChanged();
          }
        }
      }
    });

    f_showAtRootCheck.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final boolean show = f_showAtRootCheck.getSelection();
        if (show != f_edit.showAtRootOfQueryMenu()) {
          /*
           * http://surelogic.com/bugzilla/show_bug.cgi?id=21
           */
          saveAllPossibleTextEditingChanges();
          if (f_edit.setShowAtRootOfQueryMenu(show)) {
            f_edit.markAsChanged();
          }
        }
      }
    });

    f_sql.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleSqlChanges();
      }
    });

    final Menu cpsMenu = new Menu(f_sql.getShell(), SWT.POP_UP);
    final MenuItem cutItem = new MenuItem(cpsMenu, SWT.PUSH);
    cutItem.setText("Cut");
    cutItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_CUT));
    cutItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        f_sql.cut();
      }
    });
    final MenuItem copyItem = new MenuItem(cpsMenu, SWT.PUSH);
    copyItem.setText("Copy");
    copyItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
    copyItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        f_sql.copy();
      }
    });
    final MenuItem pasteItem = new MenuItem(cpsMenu, SWT.PUSH);
    pasteItem.setText("Paste");
    pasteItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_PASTE));
    pasteItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        f_sql.paste();
      }
    });
    new MenuItem(cpsMenu, SWT.SEPARATOR);
    final MenuItem selectAllItem = new MenuItem(cpsMenu, SWT.PUSH);
    selectAllItem.setText("Select All");
    selectAllItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        f_sql.selectAll();
      }
    });
    cpsMenu.addMenuListener(new MenuAdapter() {
      @Override
      public void menuShown(final MenuEvent e) {
        final boolean hasSelection = f_sql.getSelectionCount() > 0;
        cutItem.setEnabled(hasSelection);
        copyItem.setEnabled(hasSelection);
        final Clipboard c = new Clipboard(f_sql.getDisplay());
        try {
          final boolean hasTextToPaste = null != c.getContents(TextTransfer.getInstance());
          pasteItem.setEnabled(hasTextToPaste);
        } finally {
          c.dispose();
        }
      }
    });
    f_sql.setMenu(cpsMenu);

    f_addSubQuery.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final List<AdHocQuery> available = f_manager.getQueryList();
        available.remove(f_edit);
        available.removeAll(f_edit.getSubQueries());
        if (available.isEmpty()) {
          AddSubQueryDialog.openNoOtherQueries();
        } else {
          final AddSubQueryDialog dialog = new AddSubQueryDialog(EclipseUIUtility.getShell(), available);
          if (Window.OK == dialog.open()) {
            if (f_edit.addSubQueries(dialog.getSelectedQueries())) {
              f_edit.markAsChanged();
            }
          }
        }
      }
    });

    f_deleteSubQuery.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final TableItem[] selected = f_subQueryTable.getSelection();
        boolean changed = false;
        for (final TableItem item : selected) {
          final AdHocQuery subQuery = (AdHocQuery) item.getData();
          if (f_edit.removeSubQuery(subQuery)) {
            changed = true;
          }
        }
        if (changed) {
          f_edit.markAsChanged();
        }
      }
    });

    f_subQueryTable.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final TableItem[] selected = f_subQueryTable.getSelection();
        if (selected == null || selected.length < 1) {
          return;
        }
        final AdHocQuery subQuery = (AdHocQuery) selected[0].getData();
        if (f_edit.setDefaultSubQuery(subQuery)) {
          f_edit.markAsChanged();
        }
      }
    });

    f_manager.addObserver(this);

    notifyQueryModelChange(f_manager);
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
  }

  void setFocus() {
    f_lhs.setFocus();
  }

  void editInEditor(final AdHocQuery queryToEdit) {
    if (f_manager.getQueries().contains(queryToEdit)) {
      f_selections.clear();
      f_selections.add(queryToEdit);

      setQueryListSelections();
      setQueryTreeSelections();
      updateSelectionPane();
    }
  }

  @Override
  public void notifyQueryModelChange(final AdHocManager manager) {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        updateQueryListContents();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  @Override
  public void notifyGlobalVariableValueChange(final AdHocManager manager) {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        updateSelectionPane();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  private void querySelectionAction(final Widget widget) {
    /*
     * Remember what queries are selected.
     */
    final Item[] selections = widget == f_queryList ? f_queryList.getSelection() : f_queryTree.getSelection();
    final Set<AdHocQuery> newSelections = new HashSet<AdHocQuery>();
    for (final Item item : selections) {
      if (item.getData() instanceof AdHocQuery) {
        newSelections.add((AdHocQuery) item.getData());
      }
    }
    /*
     * Did the set of selections change?
     */
    if (!f_selections.equals(newSelections)) {
      f_selections.clear();
      f_selections.addAll(newSelections);
      /*
       * Ensure the selections are the same in the list and the tree.
       */
      if (widget != f_queryList) {
        setQueryListSelections();
      }
      setQueryTreeSelections();

      updateSelectionPane();
    }
  }

  private void setQueryListSelections() {
    final ArrayList<TableItem> items = new ArrayList<TableItem>();
    for (final TableItem item : f_queryList.getItems()) {
      if (item.getData() instanceof AdHocQuery) {
        final AdHocQuery query = (AdHocQuery) item.getData();
        if (f_selections.contains(query)) {
          items.add(item);
        }
      }
    }
    f_queryList.setSelection(items.toArray(new TableItem[items.size()]));
  }

  private void setQueryTreeSelections() {
    final List<TreeItem> items = new ArrayList<TreeItem>();
    for (final TreeItem item : f_queryTree.getItems()) {
      setQueryTreeSelectionsHelper(item, items);
    }
    f_queryTree.setSelection(items.toArray(new TreeItem[items.size()]));
    f_queryTree.showSelection();
  }

  /**
   * Collect items matching selected queries
   */
  private void setQueryTreeSelectionsHelper(final TreeItem item, final List<TreeItem> items) {
    if (item.getData() instanceof AdHocQuery) {
      final AdHocQuery query = (AdHocQuery) item.getData();
      if (f_selections.contains(query)) {
        items.add(item);
      }
      for (final TreeItem subItem : item.getItems()) {
        setQueryTreeSelectionsHelper(subItem, items);
      }
    }
  }

  private void updateQueryListContents() {
    f_queryList.setRedraw(false);
    f_queryTree.setRedraw(false);

    f_queryList.removeAll();
    f_queryTree.removeAll();

    for (final AdHocQuery query : f_manager.getQueryList()) {
      addQueryToList(query);
      addQueryToTree(query, null, new HashSet<AdHocQuery>());
    }

    /*
     * Intersect the selections with the remaining set of queries.
     */
    f_selections.retainAll(f_manager.getQueryList());
    setQueryListSelections();
    setQueryTreeSelections();
    updateSelectionPane();
    f_queryList.setRedraw(true);
    f_queryTree.setRedraw(true);
  }

  private void addQueryToList(final AdHocQuery query) {
    final TableItem item = new TableItem(f_queryList, SWT.NONE);
    item.setText(query.getDescription());
    item.setData(ToolTip.TIP_TEXT, query.getShortMessage());
    item.setImage(getImageForQuery(query));
    item.setData(query);
  }

  private void addQueryToTree(final AdHocQuery query, final TreeItem parent, final Set<AdHocQuery> ancestorSet) {
    /*
     * Exit if the query is an ancestor
     */
    if (ancestorSet.contains(query)) {
      return;
    }
    /*
     * Exit if we are to filter like the query menu.
     */
    if (f_filterTree) {
      if (!query.showInQueryMenu()) {
        return;
      }
      if (parent == null && !query.showAtRootOfQueryMenu()) {
        return;
      }
    }

    final TreeItem item;
    if (parent == null) {
      item = new TreeItem(f_queryTree, SWT.NONE);
    } else {
      item = new TreeItem(parent, SWT.NONE);
    }
    final Set<AdHocQuery> newSet = new HashSet<AdHocQuery>(ancestorSet);
    newSet.add(query);
    // f_queryTree.showItem(item);
    item.setText(query.getDescription());
    item.setImage(getImageForQuery(query));
    item.setData(query);
    item.setData(ToolTip.TIP_TEXT, query.getShortMessage());
    for (final AdHocQuery subQuery : query.getSubQueryList()) {
      addQueryToTree(subQuery, item, newSet);
    }
  }

  private void updateSelectionPane() {
    final boolean oneQuerySelected = f_selections.size() == 1;
    final boolean oneOrMoreQueriesSelected = !f_selections.isEmpty();
    if (oneOrMoreQueriesSelected) {
      f_queryTree.setMenu(f_queryActionMenu);
      f_queryList.setMenu(f_queryActionMenu);
    } else {
      f_queryTree.setMenu(null);
      f_queryList.setMenu(null);
    }
    f_deleteQuery.setEnabled(oneOrMoreQueriesSelected);
    f_runQuery.setEnabled(oneQuerySelected && f_manager.getGlobalVariableValues().containsKey(AdHocManager.DATABASE));

    if (!oneOrMoreQueriesSelected) {
      f_noSelectionPane.setText(I18N.msg("adhoc.query.editor.rhs.noSelection"));
      f_rhs.showPage(f_noSelectionPane);
    } else if (oneOrMoreQueriesSelected && !oneQuerySelected) {
      f_noSelectionPane.setText(I18N.msg("adhoc.query.editor.rhs.multipleSelection"));
      f_rhs.showPage(f_noSelectionPane);
    } else {
      f_rhs.showPage(f_selectionPane);
      final AdHocQuery theOne = f_selections.toArray(new AdHocQuery[1])[0];
      setOnScreenEdit(theOne);
    }
  }

  private void setOnScreenEdit(final AdHocQuery query) {
    f_edit = query;
    if (f_edit != null) {
      f_descriptionText.setText(f_edit.getDescription());
      f_idText.setText(f_edit.getId());
      f_sortHint.setSelection(f_edit.getSortHint());
      f_type.setText(f_edit.getType().toString());

      final boolean show = f_edit.showInQueryMenu();
      f_showCheck.setSelection(show);
      f_showAtRootCheck.setEnabled(show);
      f_showAtRootCheck.setSelection(f_edit.showAtRootOfQueryMenu());

      f_sql.setText(f_edit.getSql());

      f_subQueryTable.setRedraw(false);
      f_subQueryTable.removeAll();
      final List<AdHocQuery> subQueries = query.getSubQueryList();
      for (final AdHocQuery subQuery : subQueries) {
        final TableItem item = new TableItem(f_subQueryTable, SWT.NONE);
        item.setData(subQuery);
        item.setImage(0, getImageForQuery(subQuery));
        item.setText(0, subQuery.getDescription());
        item.setText(1, subQuery.getId());
        item.setData(ToolTip.TIP_TEXT, subQuery.getShortMessage());
        item.setChecked(query.isDefaultSubQuery(subQuery));
      }
      for (final TableColumn c : f_subQueryTable.getColumns()) {
        c.pack();
      }
      f_deleteSubQuery.setEnabled(!subQueries.isEmpty());
      f_subQueryTable.setRedraw(true);
    }
  }

  private void runQueryAction() {
    saveAllPossibleTextEditingChanges();
    final AdHocQuery query = f_edit;
    if (query != null && f_manager.getGlobalVariableValues().containsKey(AdHocManager.DATABASE)) {
      final Map<String, String> variables = f_manager.getGlobalVariableValues();
      if (!query.isCompletelySubstitutedBy(variables)) {
        final VariableValueDialog dialog = new VariableValueDialog(EclipseUIUtility.getShell(), query.getVariables(), null, false);
        if (dialog.open() != Window.OK) {
          // bail out because the user canceled the dialog
          return;
        }
        variables.putAll(dialog.getEnteredValues());
      }
      final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query, variables, null);
      EclipseQueryUtility.scheduleQuery(boundQuery, f_manager.getDataSource().getCurrentAccessKeys());
    }
  }

  private void deleteQueryAction() {
    final boolean multiDelete = f_selections.size() > 1;
    final String title;
    if (multiDelete) {
      title = I18N.msg("adhoc.query.dialog.confirmDelete.title.many");
    } else {
      title = I18N.msg("adhoc.query.dialog.confirmDelete.title.one");
    }
    final String msg;
    if (multiDelete) {
      msg = I18N.msg("adhoc.query.dialog.confirmDelete.msg.many");
    } else {
      msg = I18N.msg("adhoc.query.dialog.confirmDelete.msg.one");
    }
    if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, msg)) {
      for (final AdHocQuery query : f_selections) {
        f_manager.delete(query);
      }
      f_manager.notifyQueryModelChange();
    }
  }

  /**
   * This method check for changes to all the text controls and saves any
   * changes to the model.
   * <p>
   * We need this method because some controls, such as tool bar buttons, don't
   * grab the focus. Typically, we save changes to text controls when they lose
   * the focus.
   */
  private void saveAllPossibleTextEditingChanges() {
    savePossibleSqlChanges();
    savePossibleDescriptionTextChanges();
    savePossibleSortHintChanges();
    savePossibleTypeChanges();
  }

  private void savePossibleDescriptionTextChanges() {
    if (f_edit.setDescription(f_descriptionText.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleCustomDisplayTextChanges() {
    if (f_edit.setCustomDisplayClassName(f_cdText.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleSortHintChanges() {
    final int value = f_sortHint.getSelection();
    if (f_edit.setSortHint(value)) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleTypeChanges() {
    final AdHocQueryType value = AdHocQueryType.valueOf(f_type.getText());
    if (f_edit.setType(value)) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleSqlChanges() {
    if (f_edit.setSql(f_sql.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void showQueryActionMenu() {
    if (f_selections.size() == 1 && f_edit != null) {
      final MenuItem runItem = new MenuItem(f_queryActionMenu, SWT.PUSH);
      runItem.setText(I18N.msg("adhoc.query.editor.lhs.query.run"));
      runItem.setImage(SLImages.getImage(CommonImages.IMG_RUN_DRUM));
      runItem.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(final Event event) {
          runQueryAction();
        }
      });

      new MenuItem(f_queryActionMenu, SWT.SEPARATOR);
    }

    final MenuItem showItem = new MenuItem(f_queryActionMenu, SWT.CHECK);
    showItem.setSelection(false);
    for (final AdHocQuery selectedQuery : f_selections) {
      if (selectedQuery.showInQueryMenu()) {
        showItem.setSelection(true);
      }
    }
    showItem.setText(I18N.msg("adhoc.query.editor.lhs.query.show"));
    showItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final boolean show = showItem.getSelection();
        for (final AdHocQuery selectedQuery : f_selections) {
          if (selectedQuery.setShowInQueryMenu(show)) {
            selectedQuery.markAsChanged();
          }
        }
      }
    });

    final MenuItem showAtRootItem = new MenuItem(f_queryActionMenu, SWT.CHECK);
    showAtRootItem.setSelection(false);
    for (final AdHocQuery selectedQuery : f_selections) {
      if (selectedQuery.showAtRootOfQueryMenu()) {
        showAtRootItem.setSelection(true);
      }
    }
    showAtRootItem.setText(I18N.msg("adhoc.query.editor.lhs.query.showAtRoot"));
    showAtRootItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final boolean showAtRoot = showAtRootItem.getSelection();
        for (final AdHocQuery selectedQuery : f_selections) {
          boolean markAsChanged = false;
          if (showAtRoot) {
            // Query must be showing to show at root
            if (selectedQuery.setShowInQueryMenu(showAtRoot)) {
              markAsChanged = true;
            }
          }
          if (selectedQuery.setShowAtRootOfQueryMenu(showAtRoot)) {
            markAsChanged = true;
          }
          if (markAsChanged) {
            selectedQuery.markAsChanged();
          }
        }
      }
    });

    new MenuItem(f_queryActionMenu, SWT.SEPARATOR);

    final MenuItem deleteItem = new MenuItem(f_queryActionMenu, SWT.PUSH);
    deleteItem.setText(I18N.msg("adhoc.query.editor.lhs.query.delete"));
    deleteItem.setImage(SLImages.getImage(CommonImages.IMG_EDIT_DELETE));
    deleteItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        deleteQueryAction();
      }
    });
  }

  private Image getImageForQuery(AdHocQuery query) {
    return SLImages.getImageForAdHocQuery(query.getType(), query.showAtRootOfQueryMenu(), !query.showInQueryMenu());
  }
}
