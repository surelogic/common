package com.surelogic.common.ui.adhoc;

import java.util.Map;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.i18n.I18N;

/**
 * A base implementation of {@link IQueryResultCustomDisplay} that makes running
 * sub-queries much simpler. Implementations should subclass this rather than
 * implement {@link IQueryResultCustomDisplay} whenever possible.
 */
public abstract class AbstractQueryResultCustomDisplay implements IQueryResultCustomDisplay {

  @Override
  public void dispose() {
    // do nothing, implementations may override
  }

  @Nullable
  private AdHocQueryResultSqlData f_result = null;

  /**
   * Returns the query result. Will be {@code null} only during the call to
   * {@link #init()}&mdash;it must be non-null after that point.
   * 
   * @return the query result or {@code null}.
   */
  public final AdHocQueryResultSqlData getResult() {
    return f_result;
  }

  @Override
  public final void displayResult(AdHocQueryResultSqlData result, Composite panel) {
    if (result == null)
      throw new IllegalArgumentException(I18N.err(44, "result"));
    f_result = result;
    displayResult(panel);
  }

  /**
   * Invoked to draw a custom display for the query result which is obtained by
   * invoking.
   * <p>
   * Is always called from the SWT event dispatch thread.
   * 
   * @param panel
   *          a panel to place what is to be displayed within setup, by default,
   *          with a {@link FillLayout} (can be changed).
   */
  protected abstract void displayResult(Composite panel);

  /**
   * Called to schedule a sub-query to this query result. This method can only
   * be invoked after {@link #init()} has been called.
   * 
   * @param query
   *          a query to run in the context of this result.
   */
  protected final void scheduleQuery(@NonNull AdHocQuery query) {
    scheduleQuery(query, null);
  }

  /**
   * Called to schedule a sub-query to this query result. This method can only
   * be invoked after {@link #init()} has been called.
   * 
   * @param query
   *          a query to run in the context of this result.
   * @param extraVariables
   *          extra variable definitions, in addition to any selected row on the
   *          result returned by {@link #getResult()}, that should be defined.
   */
  protected final void scheduleQuery(@NonNull AdHocQuery query, @Nullable Map<String, String> extraVariables) {
    if (query == null)
      throw new IllegalArgumentException(I18N.err(44, "query"));
    final AdHocQueryResultSqlData result = getResult();
    Map<String, String> all = result.getVariableValues();
    Map<String, String> top = result.getTopVariableValues();
    if (extraVariables != null) {
      all.putAll(extraVariables);
      top.putAll(extraVariables);
    }
    final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query, all, top);
    EclipseQueryUtility.scheduleQuery(boundQuery, getResult());
  }
}
