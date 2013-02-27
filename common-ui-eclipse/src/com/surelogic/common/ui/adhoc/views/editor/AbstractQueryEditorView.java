package com.surelogic.common.ui.adhoc.views.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryType;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.common.ui.tooltip.ToolTip;

public abstract class AbstractQueryEditorView extends ViewPart {

  private QueryEditorMediator f_mediator = null;

  public abstract AdHocManager getManager();

  public ToolTip constructToolTip(final Shell shell) {
    return new ToolTip(shell);
  }

  @Override
  public void createPartControl(final Composite parent) {
    parent.setLayout(new FillLayout());
    GridData data;

    final SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
    sash.setLayout(new FillLayout());

    /*
     * Left-hand-side shows all the queries that can be selected.
     */

    final Composite lhs = new Composite(sash, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    lhs.setLayout(layout);

    final ToolBar queryToolBar = new ToolBar(lhs, SWT.VERTICAL);
    data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
    queryToolBar.setLayoutData(data);
    final ToolItem runQuery = new ToolItem(queryToolBar, SWT.PUSH);
    runQuery.setImage(SLImages.getImage(CommonImages.IMG_RUN_DRUM));
    runQuery.setToolTipText(I18N.msg("adhoc.query.editor.lhs.tooltip.run"));
    new ToolItem(queryToolBar, SWT.SEPARATOR);
    final ToolItem newQuery = new ToolItem(queryToolBar, SWT.PUSH);
    newQuery.setImage(SLImages.getImage(CommonImages.IMG_EDIT_ADD));
    newQuery.setToolTipText(I18N.msg("adhoc.query.editor.lhs.tooltip.new"));
    final ToolItem deleteQuery = new ToolItem(queryToolBar, SWT.PUSH);
    deleteQuery.setImage(SLImages.getImage(CommonImages.IMG_EDIT_DELETE));
    deleteQuery.setToolTipText(I18N.msg("adhoc.query.editor.lhs.tooltip.delete"));

    final TabFolder lhsFolder = new TabFolder(lhs, SWT.NONE);
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    lhsFolder.setLayoutData(data);

    final TabItem listTab = new TabItem(lhsFolder, SWT.NONE);
    listTab.setText(I18N.msg("adhoc.query.editor.lhs.tabList"));
    final Table queryList = new Table(lhsFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    listTab.setControl(queryList);

    final TabItem treeTab = new TabItem(lhsFolder, SWT.NONE);
    treeTab.setText(I18N.msg("adhoc.query.editor.lhs.tabTree"));
    final Composite treePane = new Composite(lhsFolder, SWT.NONE);
    treePane.setLayout(new GridLayout());
    treeTab.setControl(treePane);
    final Tree queryTree = new Tree(treePane, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    queryTree.setLayoutData(data);
    final Button filterTreeCheck = new Button(treePane, SWT.CHECK);
    filterTreeCheck.setText(I18N.msg("adhoc.query.editor.lhs.filterTreeCheck"));
    data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
    filterTreeCheck.setLayoutData(data);

    final Menu queryActionMenu = new Menu(parent.getShell(), SWT.POP_UP);

    /*
     * Right-hand-side allows editing of the selected query.
     */

    final PageBook rhs = new PageBook(sash, SWT.NONE);

    final Label noSelectionPane = new Label(rhs, SWT.NONE);
    rhs.showPage(noSelectionPane);

    final Composite selectionPane = new Composite(rhs, SWT.NONE);
    layout.numColumns = 2;
    selectionPane.setLayout(layout);

    Label label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.rhs.desc"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text descriptionText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    descriptionText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.rhs.id"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text idText = new Text(selectionPane, SWT.SINGLE | SWT.READ_ONLY);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    idText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.rhs.custom.display"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Text cdText = new Text(selectionPane, SWT.SINGLE);
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    cdText.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.rhs.sortHint"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Spinner sortHint = new Spinner(selectionPane, SWT.BORDER);
    data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    sortHint.setMinimum(-999);
    sortHint.setMaximum(999);
    sortHint.setLayoutData(data);

    label = new Label(selectionPane, SWT.RIGHT);
    label.setText(I18N.msg("adhoc.query.editor.rhs.type"));
    data = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(data);
    final Combo type = new Combo(selectionPane, SWT.READ_ONLY);
    type.setItems(AdHocQueryType.stringValues());
    data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    type.setLayoutData(data);

    final Button showCheck = new Button(selectionPane, SWT.CHECK);
    data = new GridData(SWT.NONE, SWT.CENTER, false, false, 2, 1);
    showCheck.setLayoutData(data);
    showCheck.setText(I18N.msg("adhoc.query.editor.rhs.show"));

    final Button showAtRootCheck = new Button(selectionPane, SWT.CHECK);
    data = new GridData(SWT.NONE, SWT.CENTER, false, false, 2, 1);
    data.horizontalIndent = 16;
    showAtRootCheck.setLayoutData(data);
    showAtRootCheck.setText(I18N.msg("adhoc.query.editor.rhs.showAtRoot"));

    final TabFolder sqlFolder = new TabFolder(selectionPane, SWT.NONE);
    data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    sqlFolder.setLayoutData(data);

    final TabItem sqlTab = new TabItem(sqlFolder, SWT.NONE);
    sqlTab.setText("SQL");
    final StyledText sql = new StyledText(sqlFolder, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
    sqlTab.setControl(sql);
    sql.setFont(JFaceResources.getTextFont());
    sql.addLineStyleListener(new SQLSyntaxHighlighter(sql.getDisplay()));

    final TabItem subQueryTab = new TabItem(sqlFolder, SWT.NONE);
    subQueryTab.setText("Sub-Queries");
    final Composite subQueryPane = new Composite(sqlFolder, SWT.NONE);
    subQueryTab.setControl(subQueryPane);
    layout = new GridLayout();
    layout.numColumns = 2;
    subQueryPane.setLayout(layout);
    final ToolBar subQueryToolBar = new ToolBar(subQueryPane, SWT.VERTICAL);
    data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
    subQueryToolBar.setLayoutData(data);
    final ToolItem addSubQuery = new ToolItem(subQueryToolBar, SWT.PUSH);
    addSubQuery.setImage(SLImages.getImage(CommonImages.IMG_EDIT_ADD));
    addSubQuery.setToolTipText(I18N.msg("adhoc.query.editor.rhs.tooltip.add"));
    final ToolItem deleteSubQuery = new ToolItem(subQueryToolBar, SWT.PUSH);
    deleteSubQuery.setImage(SLImages.getImage(CommonImages.IMG_EDIT_DELETE));
    deleteSubQuery.setToolTipText(I18N.msg("adhoc.query.editor.rhs.tooltip.delete"));

    final Table subQueryTable = new Table(subQueryPane, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    subQueryTable.setLayoutData(data);
    TableColumn col = new TableColumn(subQueryTable, SWT.NONE);
    col.setText("Description");
    col.pack();
    col = new TableColumn(subQueryTable, SWT.NONE);
    col.setText("Identifier");
    col.pack();
    subQueryTable.setHeaderVisible(true);
    subQueryTable.setLinesVisible(true);

    final ToolTip tip = constructToolTip(parent.getShell());
    tip.register(queryList);
    tip.register(queryTree);
    tip.register(subQueryTable);

    f_mediator = new QueryEditorMediator(this, sash, lhs, lhsFolder, queryList, queryTree, filterTreeCheck, queryActionMenu,
        runQuery, newQuery, deleteQuery, rhs, noSelectionPane, selectionPane, descriptionText, idText, cdText, sortHint, type,
        showCheck, showAtRootCheck, sqlFolder, sql, addSubQuery, deleteSubQuery, subQueryTable);
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

  public void editInEditor(final AdHocQuery queryToEdit) {
    if (queryToEdit == null) {
      return;
    }
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        if (f_mediator != null) {
          f_mediator.editInEditor(queryToEdit);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }
}
