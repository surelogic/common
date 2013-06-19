package com.surelogic.common.ui.adhoc;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.core.adhoc.EclipseQueryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;

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

  protected final void addSubQueriesToMenu(@NonNull Menu menu, @Nullable Map<String, String> extraVariables) {
    if (menu == null)
      throw new IllegalArgumentException(I18N.err(44, "menu"));

    final AdHocQueryResultSqlData result = getResult();
    final AdHocQuery resultQuery = result.getQueryFullyBound().getQuery();
    final List<AdHocQuery> subQueryList = resultQuery.getVisibleSubQueryList();
    final Map<String, String> variableValues = result.getVariableValues();
    final Map<String, String> topVariableValues = result.getTopVariableValues();
    if (extraVariables != null) {
      variableValues.putAll(extraVariables);
      topVariableValues.putAll(extraVariables);
    }
    final Listener runSubQuery = new Listener() {
      @Override
      public void handleEvent(final Event event) {
        if (event.widget.getData() instanceof AdHocQuery) {
          final AdHocQuery query = (AdHocQuery) event.widget.getData();
          final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query, variableValues, topVariableValues);
          EclipseQueryUtility.scheduleQuery(boundQuery, result);
        }
      }
    };
    for (final AdHocQuery query : subQueryList) {
      final MenuItem item = new MenuItem(menu, SWT.PUSH);
      item.setText(query.getDescription());
      item.setData(query);
      final boolean decorateAsDefault = resultQuery.isDefaultSubQuery(query);
      item.setImage(SLImages.getImageForAdHocQuery(query.getType(), decorateAsDefault, false));
      item.setEnabled(query.isCompletelySubstitutedBy(variableValues));
      item.addListener(SWT.Selection, runSubQuery);
    }
  }
}
