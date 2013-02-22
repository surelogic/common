package com.surelogic.common.ui.adhoc.views.menu;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.tooltip.ToolTip;

public abstract class AbstractQueryMenuView extends ViewPart {

  private QueryMenuMediator f_mediator = null;

  public abstract AdHocManager getManager();

  public ToolTip getToolTip(Shell shell) {
    return new ToolTip(shell);
  }

  /**
   * Gets the message for this view to display when no database is selected to
   * query. Intended to be overridden by subclasses to provide a more helpful
   * message.
   * 
   * @return a non-null message to display when no database is selected to
   *         query.
   */
  public String getNoDatabaseMessage() {
    return I18N.msg("adhoc.query.menu.label.noDatabaseSelected");
  }

  /**
   * Indicates that a particular query on the database will result in no data.
   * Intended to be overridden by subclasses to provide an answer.
   * <p>
   * This method is used to provide visual indication in the user interface that
   * the query will not result in data.
   * <p>
   * The default implementation returns {@code false}.
   * 
   * @param query
   *          the query.
   * @return {@code true} if the query when run on the database will result in
   *         no data, {@code false} otherwise.
   */
  protected boolean queryResultWillBeEmpty(final AdHocQuery query) {
    return false;
  }

  @Override
  public void createPartControl(final Composite parent) {
    parent.setLayout(new FillLayout());

    final PageBook pageBook = new PageBook(parent, SWT.NONE);

    final Label noRunSelected = new Label(pageBook, SWT.NONE);

    final ScrolledComposite sc = new ScrolledComposite(pageBook, SWT.NONE);
    final Composite content = new Composite(sc, SWT.NONE);
    // content.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_RED));
    sc.setExpandHorizontal(true);
    sc.setContent(content);
    final GridLayout gl = new GridLayout();
    gl.marginHeight = gl.marginWidth = 0;
    gl.horizontalSpacing = gl.verticalSpacing = 2;
    content.setLayout(gl);
    // sc.addControlListener(new ControlAdapter() {
    // @Override
    // public void controlResized(ControlEvent e) {
    // content.setSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    // }
    // });

    // init() called by the mediator
    final QueryResultNavigator navigator = QueryResultNavigator.getInstance(getManager().getDataSource());

    final IActionBars actionBars = getViewSite().getActionBars();

    final IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(navigator.getClearSelectionAction());

    final IMenuManager menu = actionBars.getMenuManager();
    menu.add(navigator.getClearSelectionAction());

    f_mediator = new QueryMenuMediator(this, pageBook, noRunSelected, sc, content, navigator);
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

  public void runRootQuery(final String id) {
    f_mediator.runRootQuery(id);
  }
}
