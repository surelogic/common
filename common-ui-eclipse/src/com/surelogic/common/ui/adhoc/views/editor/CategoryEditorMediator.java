package com.surelogic.common.ui.adhoc.views.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocCategory;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class CategoryEditorMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final SashForm f_sash;
  private final Composite f_lhs;
  private final Table f_categoryList;
  private final Menu f_categoryActionMenu;
  private final ToolItem f_newCategory;
  private final ToolItem f_deleteCategory;
  private final PageBook f_rhs;
  private final Label f_noSelectionPane;
  private final Composite f_selectionPane;
  private final Text f_descriptionText;
  private final Text f_idText;
  private final Text f_hasDataText;
  private final Text f_noDataText;
  private final Spinner f_sortHint;
  private final Table f_queryTable;

  private final Set<AdHocCategory> f_selections = new HashSet<AdHocCategory>();
  private AdHocCategory f_edit = null;

  CategoryEditorMediator(AbstractCategoryEditorView view, SashForm sash, Composite lhs, Table categoryList,
      Menu categoryActionMenu, ToolItem newCategory, ToolItem deleteCategory, PageBook rhs, Label noSelectionPane,
      Composite selectionPane, Text descriptionText, Text idText, Text hasDataText, Text noDataText, Spinner sortHint,
      Table queryTable) {
    f_manager = view.getManager();
    f_sash = sash;
    f_lhs = lhs;
    f_categoryList = categoryList;
    f_categoryActionMenu = categoryActionMenu;
    f_newCategory = newCategory;
    f_deleteCategory = deleteCategory;
    f_rhs = rhs;
    f_noSelectionPane = noSelectionPane;
    f_selectionPane = selectionPane;
    f_descriptionText = descriptionText;
    f_idText = idText;
    f_hasDataText = hasDataText;
    f_noDataText = noDataText;
    f_sortHint = sortHint;
    f_queryTable = queryTable;
  }

  @Override
  public void init() {
    f_sash.setWeights(new int[] { EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QCEDITOR_SASH_LHS_WEIGHT),
        EclipseUtility.getIntPreference(CommonCorePreferencesUtility.QCEDITOR_SASH_RHS_WEIGHT) });

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
          EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QCEDITOR_SASH_LHS_WEIGHT, weights[0]);
          EclipseUtility.setIntPreference(CommonCorePreferencesUtility.QCEDITOR_SASH_RHS_WEIGHT, weights[1]);
        }
      }
    });

    f_categoryActionMenu.addListener(SWT.Show, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        for (final MenuItem item : f_categoryActionMenu.getItems()) {
          item.dispose();
        }
        showCategoryActionMenu();
      }
    });

    f_categoryList.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        categorySelectionAction();
      }
    });

    f_newCategory.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final String id = f_manager.generateUnusedId();
        final AdHocCategory category = f_manager.getOrCreateCategory(id);
        category.setDescription("A new category");
        f_selections.clear();
        f_selections.add(category);
        f_manager.notifyQueryModelChange();
      }
    });

    f_deleteCategory.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        deleteCategoryAction();
      }
    });

    f_descriptionText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleDescriptionTextChanges();
      }
    });

    f_hasDataText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleHasDataTextChanges();
      }
    });

    f_noDataText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleNoDataTextChanges();
      }
    });

    f_sortHint.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleSortHintChanges();
      }
    });

    f_queryTable.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final TableItem[] selected = f_queryTable.getSelection();
        if (selected != null && selected.length > 0) {
          final AdHocQuery query = (AdHocQuery) selected[0].getData();
          if (query != null)
            f_manager.setQuerydoc(query); // show Querydoc
        }

        boolean changed = false;
        for (TableItem item : f_queryTable.getItems()) {
          final AdHocQuery query = (AdHocQuery) item.getData();
          if (item.getChecked()) {
            changed |= f_edit.addQuery(query);
          } else {
            changed |= f_edit.removeQuery(query);
          }
        }
        if (changed)
          f_edit.markAsChanged();
      }
    });

    f_manager.addObserver(this);

    notifyQueryModelChange(f_manager);
  }

  public void setFocus() {
    f_lhs.setFocus();
  }

  @Override
  public void notifyQueryModelChange(final AdHocManager manager) {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        updateCategoryListContents();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  private void showCategoryActionMenu() {
    final MenuItem deleteItem = new MenuItem(f_categoryActionMenu, SWT.PUSH);
    deleteItem.setText(I18N.msg("adhoc.query.editor.category.delete"));
    deleteItem.setImage(SLImages.getImage(CommonImages.IMG_EDIT_DELETE));
    deleteItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        deleteCategoryAction();
      }
    });
  }

  private void updateCategoryListContents() {
    f_categoryList.setRedraw(false);

    f_categoryList.removeAll();

    for (final AdHocCategory category : f_manager.getCategoryList()) {
      addCategoryToList(category);
    }

    /*
     * Intersect the selections with the remaining set of queries.
     */
    f_selections.retainAll(f_manager.getCategoryList());
    setCategoryListSelections();
    updateSelectionPane();

    f_categoryList.setRedraw(true);
  }

  private void addCategoryToList(final AdHocCategory category) {
    final TableItem item = new TableItem(f_categoryList, SWT.NONE);
    item.setText(category.getDescription());
    item.setData(category);
  }

  private void setCategoryListSelections() {
    final ArrayList<TableItem> items = new ArrayList<TableItem>();
    for (final TableItem item : f_categoryList.getItems()) {
      if (item.getData() instanceof AdHocCategory) {
        final AdHocCategory query = (AdHocCategory) item.getData();
        if (f_selections.contains(query)) {
          items.add(item);
        }
      }
    }
    f_categoryList.setSelection(items.toArray(new TableItem[items.size()]));
  }

  private void updateSelectionPane() {
    final boolean oneQuerySelected = f_selections.size() == 1;
    final boolean oneOrMoreQueriesSelected = !f_selections.isEmpty();
    if (oneOrMoreQueriesSelected) {
      f_categoryList.setMenu(f_categoryActionMenu);
    } else {
      f_categoryList.setMenu(null);
    }
    f_deleteCategory.setEnabled(oneOrMoreQueriesSelected);

    if (!oneOrMoreQueriesSelected) {
      f_noSelectionPane.setText(I18N.msg("adhoc.query.editor.category.rhs.noSelection"));
      f_rhs.showPage(f_noSelectionPane);
    } else if (oneOrMoreQueriesSelected && !oneQuerySelected) {
      f_noSelectionPane.setText(I18N.msg("adhoc.query.editor.category.rhs.multipleSelection"));
      f_rhs.showPage(f_noSelectionPane);
    } else {
      f_rhs.showPage(f_selectionPane);
      final AdHocCategory theOne = f_selections.toArray(new AdHocCategory[1])[0];
      setOnScreenEdit(theOne);
    }
  }

  private void setOnScreenEdit(final AdHocCategory category) {
    f_edit = category;
    if (f_edit != null) {
      f_descriptionText.setText(f_edit.getDescription());
      f_idText.setText(f_edit.getId());
      f_hasDataText.setText(f_edit.getHasDataText());
      f_noDataText.setText(f_edit.getNoDataText());
      f_sortHint.setSelection(f_edit.getSortHint());

      f_queryTable.setRedraw(false);
      f_queryTable.removeAll();
      for (final AdHocQuery query : f_manager.getQueryList()) {
        if (query.showAtRootOfQueryMenu()) {
          final TableItem item = new TableItem(f_queryTable, SWT.NONE);
          item.setData(query);
          item.setImage(0, getImageForQuery(query));
          item.setText(0, query.getDescription());
          item.setChecked(f_edit.contains(query));
        }
      }
      for (final TableColumn c : f_queryTable.getColumns()) {
        c.pack();
      }
      f_queryTable.setRedraw(true);
    }
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
  }

  void categorySelectionAction() {
    /*
     * Remember what categories are selected.
     */
    final Item[] selections = f_categoryList.getSelection();
    final Set<AdHocCategory> newSelections = new HashSet<AdHocCategory>();
    for (final Item item : selections) {
      if (item.getData() instanceof AdHocCategory) {
        newSelections.add((AdHocCategory) item.getData());
      }
    }
    /*
     * Did the set of selections change?
     */
    if (!f_selections.equals(newSelections)) {
      f_selections.clear();
      f_selections.addAll(newSelections);

      updateSelectionPane();
    }
  }

  void deleteCategoryAction() {
    final boolean multiDelete = f_selections.size() > 1;
    final String title;
    if (multiDelete) {
      title = I18N.msg("adhoc.query.dialog.category.confirmDelete.title.many");
    } else {
      title = I18N.msg("adhoc.query.dialog.category.confirmDelete.title.one");
    }
    final String msg;
    if (multiDelete) {
      msg = I18N.msg("adhoc.query.dialog.category.confirmDelete.msg.many");
    } else {
      msg = I18N.msg("adhoc.query.dialog.category.confirmDelete.msg.one");
    }
    if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, msg)) {
      for (final AdHocCategory category : f_selections) {
        f_manager.delete(category);
      }
      f_manager.notifyQueryModelChange();
    }
  }

  private void savePossibleDescriptionTextChanges() {
    if (f_edit.setDescription(f_descriptionText.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleHasDataTextChanges() {
    if (f_edit.setHasDataText(f_hasDataText.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleNoDataTextChanges() {
    if (f_edit.setNoDataText(f_noDataText.getText())) {
      f_edit.markAsChanged();
    }
  }

  private void savePossibleSortHintChanges() {
    final int value = f_sortHint.getSelection();
    if (f_edit.setSortHint(value)) {
      f_edit.markAsChanged();
    }
  }

  private Image getImageForQuery(AdHocQuery query) {
    return SLImages.getImageForAdHocQuery(query.getType(), query.showAtRootOfQueryMenu(), !query.showInQueryMenu());
  }
}
