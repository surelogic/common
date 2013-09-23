package com.surelogic.common.ui.adhoc.views.explorer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;

public abstract class AbstractQueryResultExplorerView extends ViewPart {

  private QueryResultExplorerMediator f_mediator = null;

  public abstract AdHocManager getManager();

  /**
   * Provides a label for this view for a particular query result. May be
   * overridden by implementations to provide custom information.
   * <p>
   * The default implementation invokes {@link AdHocQueryResult#toString()} on
   * the passed result.
   * 
   * @param result
   *          a query result.
   * @return a label for the passed query result.
   */
  @NonNull
  public String getLabelFor(@NonNull AdHocQueryResult result) {
    return result.toString();
  }

  /**
   * Gives a string to search for in the label for a query result that marks
   * where to begin using the subtle text color. The dividing text is drawn
   * using the subtle text color. May be overridden by implementations to
   * provide custom information which should match what is being returned by
   * {@link #getLabelFor(AdHocQueryResult)}.
   * <p>
   * The default implementation returns <tt>" at "</tt>.
   * 
   * @return the text to begin using subtle text color or {@code null} to not
   *         use subtle text color at all.
   */
  @Nullable
  public String getWhereToStartSubtleTextColor() {
    return " at ";
  }

  @Override
  public void createPartControl(final Composite parent) {
    final FillLayout layout = new FillLayout();
    parent.setLayout(layout);

    final TreeViewer queryHistoryTree = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

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
