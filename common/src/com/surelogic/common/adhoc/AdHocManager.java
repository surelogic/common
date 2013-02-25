package com.surelogic.common.adhoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ReferenceObject;
import com.surelogic.common.html.SimpleHTMLPrinter;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;

/**
 * Manages ad hoc query capabilities being used by other plug-ins or modules.
 * <p>
 * Observers of this manager are only notified when
 * {@link #notifyQueryModelChange()} is invoked. Clients should call this method
 * after a set of changes has been made to the model (i.e., not after each small
 * change).
 */
@ReferenceObject
public final class AdHocManager {

  /**
   * A variable that gives the name of the current database. This does not
   * necessarily correspond to the schema name or file location of the database.
   * The current application should always set this when a database is
   * available.
   */
  public static final String DATABASE = "SL-DATABASE";

  /**
   * A map from know data sources to their associated query manager.
   */
  private static final ConcurrentMap<IAdHocDataSource, AdHocManager> f_dataSourceToManager = new ConcurrentHashMap<IAdHocDataSource, AdHocManager>();

  /**
   * Creates, if necessary, and returns a manager for the given data source.
   * <p>
   * <i>Implementation Note:</i> This factory remembers managers so clients
   * should pass the same data source each time they desire the corresponding
   * manager.
   * 
   * @param source
   *          the data source for a query manager.
   * @return the query manager.
   * @throws IllegalArgumentException
   *           if the passed data source is {@code null}
   */
  public static AdHocManager getInstance(final IAdHocDataSource source) {
    if (source == null) {
      throw new IllegalArgumentException(I18N.err(44, "source"));
    }
    final AdHocManager ifNeeded = new AdHocManager(source);
    final AdHocManager previous = f_dataSourceToManager.putIfAbsent(source, ifNeeded);
    if (previous == null) {
      ifNeeded.tryToLoadQuerySaveFile();
      return ifNeeded;
    } else {
      return previous;
    }
  }

  /**
   * Invoked before the IDE shuts down to give all the query managers a chance
   * to persist their set of queries.
   */
  public static void shutdown() {
    for (final AdHocManager manager : f_dataSourceToManager.values()) {
      manager.tryToPersistToQuerySaveFile();
    }
    f_dataSourceToManager.clear();
  }

  /**
   * The data source for this manager.
   */
  private final IAdHocDataSource f_source;

  /**
   * Gets the data source for this manager.
   * 
   * @return the data source for this manager.
   */
  public IAdHocDataSource getDataSource() {
    return f_source;
  }

  /**
   * Only called by {@link #getInstance(String)}
   */
  private AdHocManager(final IAdHocDataSource source) {
    assert source != null;
    f_source = source;
  }

  /**
   * Attempts to load the queries owned by this manager from the query save
   * file. If anything goes wrong the data source is notified.
   */
  private void tryToLoadQuerySaveFile() {
    try {
      AdHocPersistence.load(this, f_source.getDefaultQueryUrl());
      final File querySaveFile = f_source.getQuerySaveFile();
      /*
       * If the save file still doesn't exist don't try the load--we'll just use
       * an empty query model.
       */
      if (querySaveFile.exists()) {
        AdHocPersistence.load(this, f_source.getQuerySaveFile());
      }
    } catch (final Exception e) {
      f_source.badQuerySaveFileNotification(e);
    }
  }

  /**
   * Attempts to save the queries owned by this manager to the query save file.
   * If anything goes wrong the data source is notified.
   */
  private void tryToPersistToQuerySaveFile() {
    try {
      AdHocPersistence.exportDiffFile(this, f_source.getQuerySaveFile());
    } catch (final Exception e) {
      f_source.badQuerySaveFileNotification(e);
    }
  }

  /**
   * The set of queries owned by this manager. It is an invariant that for all
   * queries and categories <i>x</i> and <i>y</i> that are elements of this
   * list, !<i>x</i> {@code getId().equals(}<i>y</i>{@code getId())} unless
   * <i>x</i>==<i>y</i>.
   */
  private final Set<AdHocQuery> f_queries = new HashSet<AdHocQuery>();

  /**
   * Generates an identifier that is not used as the identifier for any query or
   * category owned by this manager. This string will conform to the
   * {@link UUID} specification.
   * 
   * @return an identifier that is not used as the identifier for any query
   *         owned by this manager.
   */
  public String generateUnusedId() {
    while (true) {
      final String candidate = UUID.randomUUID().toString();
      if (!contains(candidate)) {
        return candidate;
      }
    }
  }

  /**
   * Checks if this manager contains a query or category identified by the
   * passed id.
   * 
   * @param id
   *          the identifier of a query.
   * @return {@code true} if this manager contains a query or category
   *         identified by the passed id, {@code false} otherwise.
   */
  public boolean contains(final String id) {
    for (final AdHocQuery query : f_queries) {
      if (query.getId().equals(id)) {
        return true;
      }
    }
    for (final AdHocCategory category : f_categories) {
      if (category.getId().equals(id)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets, or creates if necessary, the query identified by the passed id.
   * 
   * @param id
   *          the non-null identifier of a query.
   * @return the query (should never be {@code null});
   */
  @NonNull
  public AdHocQuery getOrCreateQuery(final String id) {
    if (id == null) {
      throw new IllegalArgumentException(I18N.err(44, "id"));
    }
    for (final AdHocQuery query : f_queries) {
      if (query.getId().equals(id)) {
        return query;
      }
    }
    final AdHocQuery query = new AdHocQuery(this, id);
    f_queries.add(query);
    return query;
  }

  /**
   * Gets the query identified by the passed id or returns {@code null} if this
   * manager does not contain such a query.
   * 
   * @param id
   *          the non-null identifier of a query.
   * @return the query, or {@code null} if a query with <tt>id</tt> can't be
   *         found.
   */
  @Nullable
  public AdHocQuery getQueryOrNull(final String id) {
    if (id == null) {
      throw new IllegalArgumentException(I18N.err(44, "id"));
    }
    for (final AdHocQuery query : f_queries) {
      if (query.getId().equals(id)) {
        return query;
      }
    }
    return null;
  }

  /**
   * Checks if this manager contains the passed query.
   * 
   * @param query
   *          a query.
   * @return {@code true} if this manager contains the query, {@code false}
   *         otherwise.
   */
  public boolean contains(final AdHocQuery query) {
    return f_queries.contains(query);
  }

  /**
   * Gets the number of queries owned by this manager.
   * 
   * @return the number of queries owned by this manager.
   */
  public int getQueryCount() {
    return f_queries.size();
  }

  /**
   * Gets the set of top-level queries owned by this manager.
   */
  public Set<AdHocQuery> getTopLevelQueries() {
    final Set<AdHocQuery> queries = new HashSet<AdHocQuery>(f_queries.size());
    for (AdHocQuery q : f_queries) {
      if (q.showAtRootOfQueryMenu()) {
        queries.add(q);
      }
    }
    return queries;
  }

  /**
   * Gets the set of queries owned by this manager.
   * 
   * @return the set of queries owned by this manager.
   */
  public Set<AdHocQuery> getQueries() {
    return new HashSet<AdHocQuery>(f_queries);
  }

  /**
   * Gets a list of the queries owned by this manager sorted by their sort hint
   * and description.
   * 
   * @return a list of the queries owned by this manager sorted by their sort
   *         hint and description.
   */
  public List<AdHocQuery> getQueryList() {
    final ArrayList<AdHocQuery> result = new ArrayList<AdHocQuery>(f_queries);
    Collections.sort(result, AdHocIdentity.BY_HINT_DESCRIPTION);
    return result;
  }

  /**
   * Gets a list of the root visible queries owned by this manager sorted by
   * their description.
   * 
   * @return a list of the root visible queries owned by this manager sorted by
   *         their description.
   * 
   * @see AdHocQuery#showAtRootOfQueryMenu()
   */
  public List<AdHocQuery> getRootQueryList() {
    final List<AdHocQuery> result = getQueryList();
    for (final Iterator<AdHocQuery> i = result.iterator(); i.hasNext();) {
      final AdHocQuery query = i.next();
      if (!query.showAtRootOfQueryMenu()) {
        i.remove();
      }
    }
    return result;
  }

  /**
   * Deletes the query if it is present.
   * <p>
   * The deleted query will no longer be a sub-query for any of the remaining
   * queries.
   * <p>
   * Any results created by running the query will be removed.
   * 
   * @param query
   *          a query.
   * @return {@code true} if any query results were deleted, {@code false}
   *         otherwise.
   */
  public boolean delete(final AdHocQuery query) {
    if (contains(query)) {
      /*
       * Remove our link to the query.
       */
      f_queries.remove(query);
      /*
       * We need to ensure that this query is not a sub-query of any of the
       * remaining queries.
       */
      for (final AdHocQuery q : f_queries) {
        q.removeSubQuery(query);
      }

      /*
       * We need to delete
       */
      boolean resultDeleted = false;
      final Set<AdHocQueryResult> copy = new HashSet<AdHocQueryResult>(f_results);
      for (final AdHocQueryResult result : copy) {
        if (result.getQueryFullyBound().getQuery() == query) {
          result.delete();
          resultDeleted = true;
        }
      }
      return resultDeleted;
    } else {
      return false;
    }
  }

  /**
   * The set of categories owned by this manager. It is an invariant that for
   * all queries and categories <i>x</i> and <i>y</i> that are elements of this
   * list, !<i>x</i> {@code getId().equals(}<i>y</i>{@code getId())} unless
   * <i>x</i>==<i>y</i>.
   */
  private final Set<AdHocCategory> f_categories = new HashSet<AdHocCategory>();

  /**
   * Gets, or creates if necessary, the category identified by the passed id.
   * 
   * @param id
   *          the non-null identifier of a category.
   * @return the query (should never be {@code null});
   */
  @NonNull
  public AdHocCategory getOrCreateCategory(final String id) {
    if (id == null) {
      throw new IllegalArgumentException(I18N.err(44, "id"));
    }
    for (final AdHocCategory category : f_categories) {
      if (category.getId().equals(id)) {
        return category;
      }
    }
    final AdHocCategory category = new AdHocCategory(this, id);
    f_categories.add(category);
    return category;
  }

  /**
   * Gets the category identified by the passed id or returns {@code null} if
   * this manager does not contain such a category.
   * 
   * @param id
   *          the non-null identifier of a category.
   * @return the category, or {@code null} if a category with <tt>id</tt> can't
   *         be found.
   */
  @Nullable
  public AdHocCategory getCategoryOrNull(final String id) {
    if (id == null) {
      throw new IllegalArgumentException(I18N.err(44, "id"));
    }
    for (final AdHocCategory category : f_categories) {
      if (category.getId().equals(id)) {
        return category;
      }
    }
    return null;
  }

  /**
   * Checks if this manager contains the passed category.
   * 
   * @param category
   *          a category.
   * @return {@code true} if this manager contains the category, {@code false}
   *         otherwise.
   */
  public boolean contains(final AdHocCategory category) {
    return f_categories.contains(category);
  }

  /**
   * Gets the number of categories owned by this manager.
   * 
   * @return the number of categories owned by this manager.
   */
  public int getCategoryCount() {
    return f_categories.size();
  }

  /**
   * Gets the set of categories owned by this manager.
   * 
   * @return the set of categories owned by this manager.
   */
  public Set<AdHocCategory> getCategories() {
    return new HashSet<AdHocCategory>(f_categories);
  }

  /**
   * Gets a list of categories owned by this manager sorted by their sort hint
   * and description.
   * 
   * @return a list of categories owned by this manager sorted by their sort
   *         hint and description.
   */
  public List<AdHocCategory> getCategoryList() {
    final ArrayList<AdHocCategory> result = new ArrayList<AdHocCategory>(f_categories);
    Collections.sort(result, AdHocIdentity.BY_HINT_DESCRIPTION);
    return result;
  }

  /**
   * Deletes the category if it is present.
   * 
   * @param category
   *          a category.
   */
  public void delete(final AdHocCategory category) {
    /*
     * Remove our link to the query.
     */
    f_categories.remove(category);
  }

  /**
   * The set of results owned by this manager.
   */
  private final Set<AdHocQueryResult> f_results = new HashSet<AdHocQueryResult>();

  /**
   * Gets the number of results owned by this manager.
   * 
   * @return the number of results owned by this manager.
   */
  public int getResultCount() {
    return f_results.size();
  }

  /**
   * Gets the number of {@link AdHocQueryResultSqlData} results owned by this
   * manager.
   * 
   * @return the number of {@link AdHocQueryResultSqlData} results owned by this
   *         manager.
   */
  public int getSqlDataResultCount() {
    final Set<AdHocQueryResult> results = getResults();
    int count = 0;
    for (final AdHocQueryResult result : results) {
      if (result instanceof AdHocQueryResultSqlData) {
        count++;
      }
    }
    return count;
  }

  /**
   * Determines if this manager owns a lot of {@link AdHocQueryResultSqlData}
   * results. This is simply a heuristic to help prompt the user (via the user
   * interface) that they might want to delete some saved query results.
   * 
   * @return {@true} if this manager owns a lot of
   *         {@link AdHocQueryResultSqlData} results, {@code false} if it does
   *         not.
   */
  public boolean getHasALotOfSqlDataResults() {
    return getSqlDataResultCount() > 10;
  }

  /**
   * Gets the set of results owned by this manager.
   * 
   * @return the set of results know by this manager. This set may be empty.
   */
  public Set<AdHocQueryResult> getResults() {
    return new HashSet<AdHocQueryResult>(f_results);
  }

  /**
   * Gets a list of the results owned by this manager sorted by when they were
   * run on the database.
   * 
   * @return a list of the results owned by this manager sorted by when they
   *         were run on the database.
   * 
   * @see AdHocQueryResultTimeComparator
   */
  public List<AdHocQueryResult> getResultList() {
    final ArrayList<AdHocQueryResult> result = new ArrayList<AdHocQueryResult>(f_results);
    Collections.sort(result, AdHocQueryResultTimeComparator.getInstance());
    return result;
  }

  /**
   * Adds a result to the set of results owned by this manager. This method
   * should only be invoked by
   * {@link AdHocQueryResult#AdHocQueryResult(AdHocManager, AdHocQueryResultSqlData, AdHocQueryFullyBound)}
   * when it sets up its link from the manager to itself.
   * 
   * @param result
   *          a query result.
   */
  void addResult(final AdHocQueryResult result) {
    f_results.add(result);
  }

  /**
   * Removes a result from the set of results owned by this manager. This method
   * should only be called by {@link AdHocQueryResult#delete()} to remove its
   * link from the manager to itself.
   * 
   * @param result
   *          a query result.
   */
  void removeResult(final AdHocQueryResult result) {
    final AdHocQueryResult parent = result.getParent();
    f_results.remove(result);
    if (result == f_selectedResult) {
      f_selectedResult = parent;
      notifySelectedResultChange();
    }
  }

  /**
   * Clears the set of results owned by this manager.
   * <p>
   * This method has the same effect as iterating over the set of results
   * returned by {@link #getResults()} and calling
   * {@link AdHocQueryResult#delete()} on each.
   */
  public void deleteAllResults() {
    final Set<AdHocQueryResult> results = new HashSet<AdHocQueryResult>(f_results);
    for (final AdHocQueryResult result : results) {
      result.delete();
    }
    notifyResultModelChange();
  }

  /**
   * Clears the set of results owned by this manager that point to the given
   * database.
   * <p>
   * This method has the same effect as iterating over the set of results
   * returned by {@link #getResults()} and calling
   * {@link AdHocQueryResult#delete()} on each.
   * 
   * @param A
   *          flashlight database
   */
  public void deleteAllResults(final DBConnection f_database) {
    final Set<AdHocQueryResult> results = new HashSet<AdHocQueryResult>(f_results);
    for (final AdHocQueryResult result : results) {
      if (f_database.equals(result.getDB())) {
        result.delete();
      }
    }
    notifyResultModelChange();
  }

  /**
   * The one query result that is selected and displayed to the user. It is an
   * invariant that {@link #f_results}{@code .contains(f_selectedResult)}.
   */
  private AdHocQueryResult f_selectedResult = null;

  /**
   * Gets the one query result that is selected and displayed to the user.
   * 
   * @return the one query result that is selected and displayed to the user.
   *         May be {@code null} if no result is selected or there are no
   *         results.
   */
  public AdHocQueryResult getSelectedResult() {
    return f_selectedResult;
  }

  /**
   * Sets the one query result that is selected and displayed to the user. May
   * be set to {@code null} to indicate that no result is currently selected.
   * <p>
   * Observers will be notified if the selected result is changed or if the
   * passed result is {@code null}. This exception for {@code null} exists
   * because we want to allow changing what is shown for no result and this is
   * triggered by setting the selected result to {@code null}.
   * 
   * @param result
   *          the one query result that is selected and displayed to the user.
   *          May be set to {@code null} to indicate that no result is currently
   *          selected.
   * @throws IllegalArgumentException
   *           if the passed result is not managed by this manager.
   */
  public void setSelectedResult(final AdHocQueryResult result) {
    if (result != null && !f_results.contains(result)) {
      throw new IllegalArgumentException(I18N.err(126, result));
    }
    boolean notify = result == null;
    if (f_selectedResult != result) {
      f_selectedResult = result;
      notify = true;
    }
    if (notify) {
      notifySelectedResultChange();
    }
  }

  /**
   * Stores this manager's set of global variable definitions.
   */
  private final Map<String, String> f_globalVariableValues = new HashMap<String, String>();

  /**
   * Gets this manager's set of global variable values.
   * <p>
   * Global variables allow code outside of the ad hoc query manager to interact
   * with the query system. For example, setting the value of the <tt>Run</tt>
   * variable via the "Flashlight Runs" view.
   * 
   * @return this manager's set of global variable values. This might be the
   *         empty map, but it will not be {@code null}.
   */
  public Map<String, String> getGlobalVariableValues() {
    return new HashMap<String, String>(f_globalVariableValues);
  }

  /**
   * Sets this manager's set of global variable values. All global variables are
   * cleared if {@code null} is passed to this method.
   * <p>
   * Observers will be notified that the global variable values have changed
   * (if, in fact, they are changed).
   * <p>
   * Global variables allow code outside of the ad hoc query manager to interact
   * with the query system. For example, setting the value of the <tt>Run</tt>
   * variable via the "Flashlight Runs" view.
   * 
   * @param variables
   *          a set of variable values. A value of {@code null} clears the set
   *          of variable values.
   */
  public void setGlobalVariableValues(final Map<String, String> variables) {
    if (!f_globalVariableValues.equals(variables)) {
      // not the same set of variables and values
      f_globalVariableValues.clear();
      if (variables != null) {
        f_globalVariableValues.putAll(variables);
      }
      notifyGlobalVariableValueChange();
    }
  }

  /**
   * Sets a variable value in this manager's set of global variable values. The
   * variable is cleared if {@code null} is passed as the value to this method.
   * <p>
   * Observers will be notified that the global variable values have changed
   * (if, in fact, they are changed).
   * <p>
   * Global variables allow code outside of the ad hoc query manager to interact
   * with the query system. For example, setting the value of the <tt>Run</tt>
   * variable via the "Flashlight Runs" view.
   * 
   * @param variable
   *          a global variable name.
   * @param value
   *          a value or {@code null} to clear the variable.
   */
  public void setGlobalVariableValue(final String variable, final String value) {
    if (variable == null) {
      throw new IllegalArgumentException(I18N.err(44, "variable"));
    }
    final boolean same;
    if (value == null) {
      same = null == f_globalVariableValues.remove(variable);
    } else {
      same = value.equals(f_globalVariableValues.put(variable, value));
    }
    if (!same) {
      notifyGlobalVariableValueChange();
    }
  }

  /**
   * This manager's set of observers.
   */
  private final CopyOnWriteArraySet<IAdHocManagerObserver> f_observers = new CopyOnWriteArraySet<IAdHocManagerObserver>();

  /**
   * Adds an observer of this manager.
   * 
   * @param o
   *          an observer.
   */
  public void addObserver(final IAdHocManagerObserver o) {
    if (o != null) {
      f_observers.add(o);
    }
  }

  /**
   * Removes the passed observer of this manager if it is present.
   * 
   * @param o
   *          an observer.
   */
  public void removeObserver(final IAdHocManagerObserver o) {
    f_observers.remove(o);
  }

  /**
   * Notifies the observers of this manager that something about the set of
   * queries or categories owned by a query manager has changed. Clients should
   * call this method after a set of changes has been made, i.e., not after each
   * small change.
   * <p>
   * Do <i>not</i> call this method holding any locks due to the potential for
   * deadlock.
   */
  public void notifyQueryModelChange() {
    for (final IAdHocManagerObserver o : f_observers) {
      o.notifyQueryModelChange(this);
    }
  }

  /**
   * Notifies the observers of this manager that something about the set of
   * results owned by a query manager has changed. Clients should call this
   * method after a set of changes has been made, i.e., not after each small
   * change.
   * <p>
   * Do <i>not</i> call this method holding any locks due to the potential for
   * deadlock.
   */
  public void notifyResultModelChange() {
    for (final IAdHocManagerObserver o : f_observers) {
      o.notifyResultModelChange(this);
    }
  }

  /**
   * Notifies the observers of this manager that a new result is ready to be
   * displayed.
   * <p>
   * Do <i>not</i> call this method holding any locks due to the potential for
   * deadlock.
   */
  public void notifySelectedResultChange() {
    for (final IAdHocManagerObserver o : f_observers) {
      o.notifySelectedResultChange(f_selectedResult);
    }
  }

  /**
   * Notifies the observers of this manager that the set of global variable
   * values has changed.
   * <p>
   * Do <i>not</i> call this method holding any locks due to the potential for
   * deadlock.
   */
  private void notifyGlobalVariableValueChange() {
    for (final IAdHocManagerObserver o : f_observers) {
      o.notifyGlobalVariableValueChange(this);
    }
  }

  /**
   * Notifies the observers of this manager that the set of variable values
   * within a result has changed.
   * <p>
   * Do <i>not</i> call this method holding any locks due to the potential for
   * deadlock.
   */
  void notifyResultVariableValueChange(final AdHocQueryResultSqlData result) {
    for (final IAdHocManagerObserver o : f_observers) {
      o.notifyResultVariableValueChange(result);
    }
  }

  /**
   * Parses a URL-like string into a query.
   * <p>
   * For example:
   * 
   * <pre>
   * index.html?query=bd72a5e4-42aa-415d-aa72-28d351f629a4&FieldId=1245&Batman=no
   * </pre>
   * 
   * Represents the query with the id
   * <tt>bd72a5e4-42aa-415d-aa72-28d351f629a4</tt> using the variables
   * <tt>FieldIS</tt> and <tt>Batman</tt> with the values <tt>1245</tt> and
   * <tt>no</tt>, respectively. The variables must bind all free variables in
   * the query or {@link IllegalStateException} is thrown.
   * 
   * @param queryUrl
   *          the string to parse.
   * @return a query that has bound all its variables.
   * 
   * @throws IllegalStateException
   *           if the query doesn't exist, its variables are not fully bound, or
   *           <tt>queryUrl</tt> is not well-formed.
   */
  public AdHocQueryFullyBound parseQueryUrl(final String queryUrl) {
    final Map<String, String> variables = getGlobalVariableValues();
    final Map<String, String> parse = SimpleHTMLPrinter.extractParametersFromURL(queryUrl);
    final String idKey = "query";
    if (!parse.containsKey(idKey)) {
      throw new IllegalStateException(I18N.err(211, queryUrl));
    }
    final String id = parse.get(idKey);
    parse.remove(idKey);
    variables.putAll(parse);
    AdHocQuery query = getQueryOrNull(id);
    if (query != null) {
      final AdHocQueryFullyBound boundQuery = new AdHocQueryFullyBound(query, variables, parse);
      return boundQuery;
    } else {
      throw new IllegalStateException(I18N.err(212, id));
    }
  }
}
