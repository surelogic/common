package com.surelogic.common.ui.adhoc.views.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.CommonImages;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;

public abstract class AbstractCategoryEditorView extends ViewPart {

  private CategoryEditorMediator f_mediator = null;

  public abstract AdHocManager getManager();

  @Override
  public void createPartControl(Composite parent) {
    parent.setLayout(new FillLayout());
    GridData data;

    final SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
    sash.setLayout(new FillLayout());

    /*
     * Left-hand-side shows all the categories that can be selected.
     */

    final Composite lhs = new Composite(sash, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    lhs.setLayout(layout);

    final ToolBar queryToolBar = new ToolBar(lhs, SWT.VERTICAL);
    data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
    queryToolBar.setLayoutData(data);
    final ToolItem newCategory = new ToolItem(queryToolBar, SWT.PUSH);
    newCategory.setImage(SLImages.getImage(CommonImages.IMG_EDIT_ADD));
    newCategory.setToolTipText(I18N.msg("adhoc.query.editor.category.tooltip.new"));
    final ToolItem deleteCategory = new ToolItem(queryToolBar, SWT.PUSH);
    deleteCategory.setImage(SLImages.getImage(CommonImages.IMG_EDIT_DELETE));
    deleteCategory.setToolTipText(I18N.msg("adhoc.query.editor.category.tooltip.delete"));

    final Table categoryList = new Table(lhs, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    categoryList.setLayoutData(data);

    /*
     * Right-hand-side allows editing of the selected category.
     */

    final PageBook rhs = new PageBook(sash, SWT.NONE);

    final Label noSelectionPane = new Label(rhs, SWT.NONE);
    rhs.showPage(noSelectionPane);

    final Composite selectionPane = new Composite(rhs, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    selectionPane.setLayout(layout);

    Label label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.category.rhs.desc"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text descriptionText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    descriptionText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.category.rhs.id"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text idText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    idText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.category.rhs.hasData"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text hasDataText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    hasDataText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.category.rhs.noData"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text noDataText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    noDataText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.category.rhs.sortHint"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Spinner sortHint = new Spinner(selectionPane, SWT.BORDER);
    data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    sortHint.setMinimum(-999);
    sortHint.setMaximum(999);
    sortHint.setLayoutData(data);

    f_mediator = new CategoryEditorMediator(this, sash, lhs, categoryList, newCategory, deleteCategory, rhs, noSelectionPane,
        selectionPane, descriptionText, idText, hasDataText, noDataText, sortHint);
    f_mediator.init();
  }

  @Override
  public void setFocus() {
    if (f_mediator != null) {
      f_mediator.setFocus();
    }
  }

  @Override
  public void dispose() {
    if (f_mediator != null) {
      f_mediator.dispose();
      f_mediator = null;
    }
    super.dispose();
  }
}
