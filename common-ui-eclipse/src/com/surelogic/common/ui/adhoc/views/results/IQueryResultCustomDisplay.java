package com.surelogic.common.ui.adhoc.views.results;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;

/**
 * Interface to define a custom display for a particular query. Implementations
 * must provide a zero-argument constructor.
 * <p>
 * All calls to this interface from the {@link AbstractQueryResultsView} are
 * made from the SWT event dispatch thread.
 * 
 */
public interface IQueryResultCustomDisplay extends ILifecycle {

  /**
   * Invoked to draw a custom display for the passed query result.
   * <p>
   * Is always called from the SWT event dispatch thread.
   * 
   * @param result
   *          the data query resulted in.
   * @param panel
   *          a panel to place what is to be displayed within setup, by default,
   *          with a {@link FillLayout} (can be changed).
   */
  void displayResult(AdHocQueryResultSqlData result, Composite panel);
}
