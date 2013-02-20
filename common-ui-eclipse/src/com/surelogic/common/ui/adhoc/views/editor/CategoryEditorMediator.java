package com.surelogic.common.ui.adhoc.views.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.PageBook;

import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;

public final class CategoryEditorMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final SashForm f_sash;
  private final Composite f_lhs;
  private final Table f_categoryList;
  private final ToolItem f_newCategory;
  private final ToolItem f_deleteCategory;
  private final PageBook f_rhs;
  private final Label f_noSelectionPane;
  private final Composite f_selectionPane;
  private final Text f_descriptionText;
  private final Text f_idText;
  private final Text f_hasDataText;
  private final Text f_noDataText;

  CategoryEditorMediator(AbstractCategoryEditorView view, SashForm sash, Composite lhs, Table categoryList, ToolItem newCategory,
      ToolItem deleteCategory, PageBook rhs, Label noSelectionPane, Composite selectionPane, Text descriptionText, Text idText,
      Text hasDataText, Text noDataText) {
    f_manager = view.getManager();
    f_sash = sash;
    f_lhs = lhs;
    f_categoryList = categoryList;
    f_newCategory = newCategory;
    f_deleteCategory = deleteCategory;
    f_rhs = rhs;
    f_noSelectionPane = noSelectionPane;
    f_selectionPane = selectionPane;
    f_descriptionText = descriptionText;
    f_idText = idText;
    f_hasDataText = hasDataText;
    f_noDataText = noDataText;
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
        final AdHocQuery query = f_manager.get(id);
        query.setDescription("A new query");
//        f_selections.clear();
//        f_selections.add(query);
        f_manager.notifyQueryModelChange();
      }
    });

    f_deleteCategory.addListener(SWT.Selection, new Listener() {
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

    f_idText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(final FocusEvent e) {
        savePossibleIdTextChanges();
      }
    });

    f_manager.addObserver(this);

    notifyQueryModelChange(f_manager);
  }

  public void setFocus() {
    f_lhs.setFocus();
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
  }

  void categorySelectionAction() {
    // TODO Auto-generated method stub

  }

  void deleteQueryAction() {
    // TODO Auto-generated method stub

  }

  void savePossibleDescriptionTextChanges() {
    // TODO Auto-generated method stub

  }

  void savePossibleIdTextChanges() {
    // TODO Auto-generated method stub
  }
}
