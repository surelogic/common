package com.surelogic.common.adhoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;

/**
 * Represents an SQL query and its associated meta-data. Each instance is
 * created and owned by one and only one {@link AdHocManager}. Queries are
 * identified by a string that is assigned by its query manager upon creation
 * but can later be changed. A query contains a string description that can be
 * changed as desired.
 * <p>
 * Queries may contain variables which are represented by an name surrounded by
 * a '?', e.g., <tt>?RUN?</tt>. Thus the query
 * 
 * <pre>
 * select * from TABLE
 * where Run='?RUN?'
 * and Id=?ID?
 * </pre>
 * 
 * uses the variables <tt>RUN</tt> and <tt>ID</tt> (notice that <tt>Run</tt>
 * represents a string and is thus single quoted). Except for variables, queries
 * are expected to be valid SQL.
 * <p>
 * To substitute variable values into a query use the
 * {@link #getSql(Properties)} method. The (possibly empty) set of variables
 * used in a query can be obtained by calling the {@link #getVariables()}
 * method.
 * <p>
 * Queries define a set of sub-queries. A sub-query is a query that can be
 * executed based upon a selected row of the result of this query.
 */
public final class AdHocQuery implements AdHocIdentity {

  AdHocQuery(final AdHocManager manager, final String id) {
    assert manager != null;
    f_manager = manager;
    assert id != null;
    f_id = id;
  }

  /**
   * The manager that owns this query.
   */
  private final AdHocManager f_manager;

  /**
   * Gets the query manager that created and owns this query.
   * 
   * @return the query manager that created and owns this query.
   */
  public AdHocManager getManager() {
    return f_manager;
  }

  /**
   * The identifier for this query.
   */
  private String f_id;

  @NonNull
  public String getId() {
    return f_id;
  }

  /**
   * Sets the identifier for this query if the new identifier is unused. The
   * identifier is unused if it is not the identifier for another query or
   * category managed by this query's manager, or more formally
   * {@code !getManager().contains(id)}. If the new identifier is already used
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param id
   *          an identifier for this query.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   * @throws IllegalArgumentException
   *           if <tt>id</tt> is being used by another query or category.
   */
  public boolean setId(final String id) {
    if (id == null || f_id.equals(id)) {
      return false;
    }
    if (f_manager.contains(id)) {
      throw new IllegalArgumentException(I18N.err(215, id));
    }
    f_id = id;
    return true;
  }

  /**
   * A description of this query.
   */
  private String f_description = "";

  @NonNull
  public String getDescription() {
    return f_description;
  }

  /**
   * Sets the description of this query. This method has no effect if the
   * description passed is null.
   * 
   * @param description
   *          non-null description of this query.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setDescription(final String description) {
    if (description == null || description.equals(f_description)) {
      return false;
    }
    f_description = description;
    return true;
  }

  /**
   * A value to help sort this query with other queries. May be negative, zero,
   * or positive.
   */
  private int f_sortHint = 0;

  public int getSortHint() {
    return f_sortHint;
  }

  /**
   * Sets a number to help sort this query with other queries.
   * 
   * @param value
   *          a value. May be negative, zero, or positive.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setSortHint(final int value) {
    if (value == f_sortHint)
      return false;
    else {
      f_sortHint = value;
      return true;
    }
  }

  /**
   * The type of result this query returns. This can be used to help the display
   * indicate queries that return informational results from ones that return
   * probable program errors or other types of more specific information. By
   * default it is an informational result.
   */
  @NonNull
  private AdHocQueryType f_type = AdHocQueryType.INFORMATION;

  /**
   * Gets the type of result this query returns. This can be used to help the
   * display indicate queries that return informational results from ones that
   * return probable program errors or other types of more specific information.
   * 
   * @return type of result this query returns.
   */
  @NonNull
  public AdHocQueryType getType() {
    return f_type;
  }

  /**
   * Sets the type of result this query returns.
   * 
   * @param value
   *          a value, ignored if {@code null}.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setType(final AdHocQueryType value) {
    if (value != null && value != f_type) {
      f_type = value;
      return true;
    } else
      return false;
  }

  private static final String STARTINFO = "BEGIN-INFO";
  private static final String STOPINFO = "END-INFO";

  /**
   * Gets a short informational message associated with this query, such as
   * could be displayed in a tool tip.
   * 
   * @return a short informational message associated with this query, such as
   *         could be displayed in a tool tip.
   */
  public String getShortMessage() {
    final int start = f_sql.indexOf(STARTINFO);
    final int stop = f_sql.indexOf(STOPINFO);
    if (start == -1 || stop == -1) {
      return getDescription();
    }
    return f_sql.substring(start + STARTINFO.length(), stop).replace("--", "");
  }

  /**
   * The SQL text of this query.
   */
  private String f_sql = "";

  /**
   * Gets the SQL text of this query.
   * 
   * @return the SQL text of this query.
   */
  public String getSql() {
    return f_sql;
  }

  /**
   * Sets the SQL text of this query.
   * 
   * @param text
   *          non-null SQL text.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setSql(final String sql) {
    if (sql == null || sql.equals(f_sql)) {
      return false;
    }
    f_sql = sql;
    return true;
  }

  /**
   * An increasing number that reflects how many changes have been made state of
   * this object. This value is only changed each time the software is released.
   * It cannot be changed by users of the software.
   * <p>
   * If the state of this query has been changed by the user this is indicated
   * by {@link #isChanged()}.
   */
  private long f_revision = 0;

  /**
   * Gets the revision of this query.
   * <p>
   * The revision is a number that reflects how many changes have been made
   * state of this object. This value is only changed each time the software is
   * released. It cannot be changed by users of the software.
   * <p>
   * If the state of this query has been changed by the user this is indicated
   * by {@link #isChanged()}.
   * 
   * @return the revision of this query.
   */
  public long getRevision() {
    return f_revision;
  }

  /**
   * Sets the revision of this query.
   * 
   * @param value
   *          the revision of this query.
   * 
   */
  public void setRevision(final long value) {
    f_revision = value;
  }

  /**
   * This flag indicates that something about the state of this query has
   * changed from its current revision number.
   */
  private boolean f_changed;

  /**
   * Flags if something about the state of this query has changed from its
   * current revision number.
   * 
   * @return {@code true} if something about the state of this query has changed
   *         from its current revision number, {@code false} otherwise.
   */
  public boolean isChanged() {
    return f_changed;
  }

  /**
   * Sets the value of the changed flag. A value of {@code true} indicates that
   * something about the state of this query has changed from its current
   * revision number.
   * 
   * @param value
   *          the new for the changed flag.
   */
  public void setChanged(final boolean value) {
    f_changed = value;
  }

  /**
   * Marks that something about the state of this query has changed from its
   * current revision number. It then notifies the manager of a query model
   * change by a call to {@link AdHocManager#notifyQueryModelChange()}.
   * <p>
   * This method has the same effect as the below code:
   * 
   * <pre>
   * query.SetChanged(true);
   * query.getManager().notifyQueryModelChange();
   * </pre>
   */
  public void markAsChanged() {
    f_changed = true;
    f_manager.notifyQueryModelChange();
  }

  /**
   * The set of queries that can be executed based upon a selected row of the
   * result of this query.
   */
  private final Set<AdHocQuery> f_subQueries = new HashSet<AdHocQuery>();

  /**
   * Sets the set of queries that can be executed based upon a selected row of
   * the result of this query.
   * 
   * @param subQueries
   *          a set of queries.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean setSubQueries(final Set<AdHocQuery> subQueries) {
    if (subQueries == null || f_subQueries.equals(subQueries)) {
      return false;
    }
    f_subQueries.clear();
    f_subQueries.addAll(subQueries);
    return true;
  }

  /**
   * Adds a sub-query to this query if it is a query managed by the same query
   * manager as this query. A sub-query is a query that can be executed based
   * upon a selected row of the result of this query.
   * 
   * @param subQuery
   *          a sub-query.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addSubQuery(final AdHocQuery subQuery) {
    if (subQuery == null || !f_manager.contains(subQuery)) {
      return false;
    }
    return f_subQueries.add(subQuery);
  }

  /**
   * Adds the sub-queries to this query if, for each sub-query, it is a query
   * managed by the same query manager as this query. A sub-query is a query
   * that can be executed based upon a selected row of the result of this query.
   * 
   * @param subQueries
   *          a collection of sub-queries.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addSubQueries(final Collection<AdHocQuery> subQueries) {
    boolean val = false;
    for (AdHocQuery q : subQueries) {
      val |= addSubQuery(q);
    }
    return val;
  }

  /**
   * Removes a sub-query from this query if it is present. A sub-query is a
   * query that can be executed based upon a selected row of the result of this
   * query.
   * 
   * @param subQuery
   *          a sub-query.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean removeSubQuery(final AdHocQuery subQuery) {
    return f_subQueries.remove(subQuery);
  }

  /**
   * Removes all sub-queries form this query.
   */
  public void clearSubQueries() {
    f_subQueries.clear();
  }

  /**
   * Gets a copy of the set of sub-queries for this query. A sub-query is a
   * query that can be executed based upon a selected row of the result of this
   * query.
   * 
   * @return the set of sub-queries for this query.
   */
  public Set<AdHocQuery> getSubQueries() {
    return new HashSet<AdHocQuery>(f_subQueries);
  }

  /**
   * Returns a list of the sub-queries for this query sorted by their sort hint
   * and description. The default sub-query, obtained from
   * {@link #getDefaultSubQuery()}, if any, is always first in the returned
   * list.
   * 
   * @return a list of the sub-queries for this query sorted by their sort hint
   *         and description with the default-sub query, if any, first.
   */
  public List<AdHocQuery> getSubQueryList() {
    final LinkedList<AdHocQuery> result = new LinkedList<AdHocQuery>(f_subQueries);
    Collections.sort(result, AdHocIdentity.BY_HINT_DESCRIPTION);
    if (f_defaultSubQuery != null) {
      result.remove(f_defaultSubQuery);
      result.addFirst(f_defaultSubQuery);
    }
    return result;
  }

  /**
   * Returns a list of the sub-queries that should be displayed in the query
   * menu for this query sorted per {@link #getSubQueryList()}.
   * 
   * @return a list of the sub-queries that should be displayed in the query
   *         menu for this query.
   * 
   * @see #showInQueryMenu()
   */
  public List<AdHocQuery> getVisibleSubQueryList() {
    final List<AdHocQuery> result = getSubQueryList();
    for (final Iterator<AdHocQuery> i = result.iterator(); i.hasNext();) {
      final AdHocQuery query = i.next();
      if (!query.showInQueryMenu()) {
        i.remove();
      }
    }
    return result;
  }

  /**
   * A default queries that can be executed based upon a selected row of the
   * result of this query. It must be true that
   * <tt>f_subQueries.contains(f_defaultSubQuery)</tt>.
   */
  private AdHocQuery f_defaultSubQuery = null;

  /**
   * Gets the default sub-query for this query. If there is not a specific
   * default sub-query set then the first visible sub-query is returned
   * (obtained via {@link #getVisibleSubQueryList()). If no sub-queries exist
   * for this query then {@code null} is returned.
   * 
   * @return a sub-query or {@code null}.
   */
  public AdHocQuery getDefaultSubQuery() {
    if (f_defaultSubQuery != null) {
      return f_defaultSubQuery;
    }
    List<AdHocQuery> subs = getVisibleSubQueryList();
    if (subs.isEmpty()) {
      return null;
    } else {
      return subs.get(0);
    }
  }

  /**
   * Gets if a query is the default sub-query. This is method has the same
   * effect as <tt>getDefaultSubQuery() == query</tt>.
   * 
   * @param query
   *          a query.
   * @return {@code true} if <tt>query</tt> is the default sub-query,
   *         {@code false} otherwise.
   */
  public boolean isDefaultSubQuery(final AdHocQuery query) {
    return getDefaultSubQuery() == query;
  }

  /**
   * Sets the default sub-query for this query.
   * 
   * @param subQuery
   *          a sub-query, may be {@code null}.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   * @throws IllegalArgumentException
   *           if the passed query is not contained in the set of sub-queries
   *           for this query.
   */
  public boolean setDefaultSubQuery(final AdHocQuery subQuery) {
    if (f_defaultSubQuery == subQuery) {
      return false;
    }
    if (subQuery == null) {
      f_defaultSubQuery = null;
      return true;
    }
    if (!f_manager.contains(subQuery)) {
      return false;
    }
    if (!f_subQueries.contains(subQuery)) {
      throw new IllegalArgumentException(I18N.err(216, subQuery.getId(), getId()));
    }
    f_defaultSubQuery = subQuery;
    return true;
  }

  /**
   * A hint to the user interface if this query should be shown in the query
   * menu.
   */
  private boolean f_showInQueryMenu = false;

  /**
   * Gets if this query should be shown in the query menu.
   * 
   * @return {@code true} if this query should be shown in the query menu,
   *         {@code false} otherwise.
   */
  public boolean showInQueryMenu() {
    return f_showInQueryMenu;
  }

  /**
   * Sets if this query should be shown in the query menu.
   * 
   * @param value
   *          {@code true} if this query should be shown in the query menu,
   *          {@code false} otherwise.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setShowInQueryMenu(final boolean value) {
    if (value == f_showInQueryMenu) {
      return false;
    }
    if (value) {
      f_showInQueryMenu = value;
    } else {
      f_showAtRootOfQueryMenu = f_showInQueryMenu = value;
    }
    return true;
  }

  /**
   * A hint to the user interface if this query should be shown at the root
   * level of any query menu. A value of {@code true} should be acted upon if
   * {@link #f_showAtRootOfQueryMenu} is {@code true}.
   */
  private boolean f_showAtRootOfQueryMenu = false;

  /**
   * Gets if this query should be shown at the root level of the query menu.
   * <p>
   * A value of {@code true} should be acted upon only if
   * {@link #showInQueryMenu()} is {@code true}.
   * 
   * @return {@code true} if this query should be shown at the root level of the
   *         query menu, {@code false} otherwise.
   */
  public boolean showAtRootOfQueryMenu() {
    return f_showAtRootOfQueryMenu;
  }

  /**
   * Sets if this query should be shown at the root level of the query menu
   * <p>
   * A value of {@code true} should be acted upon only if
   * {@link #showInQueryMenu()} is {@code true}.
   * 
   * @param value
   *          {@code true} if this query should be shown at the root level of
   *          the query menu, {@code false} otherwise.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setShowAtRootOfQueryMenu(final boolean value) {
    if (value == f_showAtRootOfQueryMenu) {
      return false;
    }
    f_showAtRootOfQueryMenu = value;
    return true;
  }

  /**
   * Checks if the passed set of variable values would allow a complete
   * substitution of the variables in this query.
   * 
   * @param variableValues
   *          the defined values for variables, or {@code null} for no defined
   *          values.
   * @return {@code true} if the passed set of variable values would allow a
   *         complete substitution of the variables in this query, {@code false}
   *         otherwise.
   */
  public boolean isCompletelySubstitutedBy(final Map<String, String> variableValues) {
    final Set<String> work = getVariables();
    if (variableValues == null) {
      return work.isEmpty();
    }
    work.removeAll(variableValues.keySet());
    return work.isEmpty();
  }

  /**
   * Replaces all variables in the SQL text of this query with the passed values
   * and returns the SQL text of the resulting query.
   * 
   * @param variableValues
   *          the defined values for variables.
   * @return the text of the query with as many substitutions made as possible.
   */
  public String getSql(final Map<String, String> variableValues) {
    if (variableValues == null) {
      throw new IllegalArgumentException(I18N.err(44, "variableValues"));
    }
    final StringBuilder b = new StringBuilder();
    final BufferedReader r = new BufferedReader(new StringReader(f_sql));

    String line;
    try {
      while ((line = r.readLine()) != null) {
        int comment = line.indexOf("--");
        int q1 = line.indexOf('?');
        int q2 = line.indexOf('?', q1 + 1);
        // Keep replacing any variables before the comment
        while (q1 != -1 && q2 != -1 && (comment == -1 || q1 < comment && q2 < comment)) {
          String var = line.substring(q1 + 1, q2);
          final String value = variableValues.get(var);
          line = line.replace('?' + var + '?', value == null ? "" : value);
          q1 = line.indexOf('?');
          q2 = line.indexOf('?', q1 + 1);
          comment = line.indexOf("--");
        }
        b.append(line);
        b.append('\n');
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return b.toString();
  }

  /**
   * Returns the set of variables found in this query's SQL text. The returned
   * set is a copy and may be freely mutated by the caller.
   * 
   * @return the set of variables found in this query's SQL text or the empty
   *         set if no variable are used.
   */
  public Set<String> getVariables() {
    final String sql = f_sql;
    final Set<String> variableSet = new HashSet<String>();
    final BufferedReader sr = new BufferedReader(new StringReader(sql));
    try {
      String line;
      while ((line = sr.readLine()) != null) {
        int comment = line.indexOf("--");
        if (comment != -1) {
          if (comment == 0) {
            continue;
          } else {
            line = line.substring(0, comment);
          }
        }
        int q1 = line.indexOf('?');
        while (q1 != -1) {
          int q2 = line.indexOf('?', q1 + 1);
          if (q2 != -1) {
            final String key = line.substring(q1 + 1, q2);
            if (key != null && key.length() > 0) {
              variableSet.add(key);
            }
            q1 = line.indexOf('?', q2 + 1);
          } else {
            break;
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return variableSet;
  }

  /**
   * Indicates that this particular query on the current data source will result
   * in no data.
   * <p>
   * If the answer is not known {@code false} is returned.
   * 
   * @return {@code true} if this query when run on the current data source is
   *         known to result in no data, {@code false} otherwise.
   */
  public boolean resultIsKnownToBeEmpty() {
    return f_manager.getDataSource().queryResultWillBeEmpty(this);
  }

  @Override
  public String toString() {
    return "[AdHocQuery: id=" + f_id + " description= " + f_description + " sql=\"" + f_sql + "]";
  }
}
