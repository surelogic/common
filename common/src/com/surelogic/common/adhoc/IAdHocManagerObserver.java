package com.surelogic.common.adhoc;

import com.surelogic.ReferenceObject;

/**
 * An interface that lets the implementing object observe an
 * {@link AdHocManager}.
 * <p>
 * It is recommended that implementers override {@link AdHocManagerAdapter}
 * rather than implementing this interface if possible.
 */
@ReferenceObject
public interface IAdHocManagerObserver {

  /**
   * Invoked when something about the set of queries or categories owned by a
   * query manager has changed.
   * <p>
   * This is triggered via a client call to
   * {@link AdHocManager#notifyQueryModelChange()} on a query manager.
   * 
   * @param manager
   *          the observed query manager.
   */
  void notifyQueryModelChange(AdHocManager manager);

  /**
   * Invoked when the set of global variable values is changed.
   * <p>
   * This is triggered via a client call to
   * {@link AdHocManager#setGlobalVariableValues(java.util.Map)}.
   * 
   * @param manager
   *          the observed query manager.
   */
  void notifyGlobalVariableValueChange(AdHocManager manager);

  /**
   * Invoked when something about the set of results owned by a query manager
   * has changed.
   * <p>
   * This is triggered via a client call to
   * {@link AdHocManager#notifyResultModelChange()} on a query manager.
   * 
   * @param manager
   *          the observed query manager.
   */
  void notifyResultModelChange(AdHocManager manager);

  /**
   * Invoked when the selected result is changed. The selected result is the
   * result that should be displayed in the user interface. The selected result
   * may be {@code null} if no result is selected or if there are no results.
   * <p>
   * This is triggered via a client call to
   * {@link AdHocManager#setSelectedResult(AdHocQueryResult)}.
   * 
   * @param result
   *          the selected result or {@code null} if none.
   */
  void notifySelectedResultChange(AdHocQueryResult result);

  /**
   * Invoked when the set of variable values is changed within a result. This
   * occurs when the selected row of the result used to define variable values
   * changes.
   * <p>
   * This is triggered via a client call to
   * {@link AdHocQueryResultSqlData#setSelectedRowIndex(int)}.
   * 
   * @param result
   *          the result whose variable values changed.
   */
  void notifyResultVariableValueChange(AdHocQueryResultSqlData result);

  /**
   * Invoked when the selected query for Querydoc is changed
   * 
   * @param query
   *          the query to show for Querydoc or {@code null} for none.
   */
  void notifyQuerydocValueChange(AdHocQuery query);
}
