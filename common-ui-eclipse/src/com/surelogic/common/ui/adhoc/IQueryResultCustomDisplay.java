package com.surelogic.common.ui.adhoc;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.ui.adhoc.views.results.AbstractQueryResultsView;

/**
 * Interface to define a custom display for a particular query. Implementations
 * must provide a zero-argument constructor.
 * <p>
 * All calls to this interface from the {@link AbstractQueryResultsView} are
 * made from the SWT event dispatch thread.
 * <p>
 * A new instance is created and the
 * {@link #displayResult(AdHocQueryResultSqlData, Composite)} method followed by
 * the {@link #dispose()} method are invoked each time the query is run. Objects
 * are not reused across query runs.
 * <p>
 * It is highly recommended that implementations subclass
 * {@link AbstractQueryResultCustomDisplay} rather than implement this interface
 * directly because that class helps the implementation execute sub-queries
 * correctly.
 * 
 * @see AbstractQueryResultCustomDisplay
 */
public interface IQueryResultCustomDisplay {

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

  /**
   * Actions required after use.
   */
  void dispose();
}
