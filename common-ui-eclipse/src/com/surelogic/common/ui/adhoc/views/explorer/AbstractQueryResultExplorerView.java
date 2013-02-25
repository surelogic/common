package com.surelogic.common.ui.adhoc.views.explorer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.tooltip.ToolTip;

public abstract class AbstractQueryResultExplorerView extends ViewPart {

  private QueryResultExplorerMediator f_mediator = null;

  public abstract AdHocManager getManager();

  /**
   * Constructs a tool tip object for use by a view.
   * 
   * @param shell
   *          a shell
   * @return an object to invoke
   *         {@link ToolTip#activateToolTip(org.eclipse.swt.widgets.Control)}
   *         on.
   */
  public ToolTip getToolTip(Shell shell) {
    return new ToolTip(shell);
  }

  @Override
  public void createPartControl(final Composite parent) {
    final FillLayout layout = new FillLayout();
    parent.setLayout(layout);
    final Tree queryHistoryTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

    // init() called by the mediator
    final QueryResultNavigator navigator = QueryResultNavigator.getInstance(getManager().getDataSource());

    final IActionBars actionBars = getViewSite().getActionBars();

    final IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(navigator.getBackwardAction());
    toolBar.add(navigator.getForwardAction());
    toolBar.add(new Separator());
    toolBar.add(navigator.getDisposeAction());
    toolBar.add(navigator.getDisposeAllAction());

    final IMenuManager menu = actionBars.getMenuManager();
    menu.add(navigator.getShowDefinedVariablesAction());
    menu.add(navigator.getShowSqlAction());
    menu.add(navigator.getExportAction());
    menu.add(new Separator());
    menu.add(navigator.getClearSelectionAction());
    menu.add(new Separator());
    menu.add(navigator.getDisposeAction());
    menu.add(navigator.getDisposeAllAction());

    final ToolTip tip = getToolTip(parent.getShell());
    tip.activateToolTip(queryHistoryTree);

    f_mediator = new QueryResultExplorerMediator(this, queryHistoryTree, navigator);
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
