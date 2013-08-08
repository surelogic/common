package com.surelogic.common.adhoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
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

  /**
   * Holds an optional name of a class that implements
   * <tt>IQueryResultCustomDisplay</tt> (by extending
   * <tt>AbstractQueryResultCustomDisplay</tt> if possible). This is a UI class
   * and cannot be referred to in common.
   * <p>
   * A value of {@code null} indicates no custom display.
   */
  @Nullable
  private String f_customDisplayClassName = null;

  /**
   * Gets the custom display class name for this query or {@code null}.
   * 
   * @return the custom display class name for this query or {@code null}.
   */
  @Nullable
  public String getCustomDisplayClassName() {
    return f_customDisplayClassName;
  }

  /**
   * Checks if this query uses a custom display class.
   * 
   * @return {@code true} if this query uses a custom display class,
   *         {@code false} otherwise.
   */
  public boolean usesCustomDisplay() {
    return f_customDisplayClassName != null;
  }

  /**
   * Sets the custom display class name for this query.
   * 
   * @param value
   *          a class name that implements <tt>IQueryResultCustomDisplay</tt>
   *          (by extending <tt>AbstractQueryResultCustomDisplay</tt> if
   *          possible) or {@code null} or <tt>""</tt> to clear.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setCustomDisplayClassName(@Nullable String value) {
    if (value == null || "".equals(value)) {
      if (f_customDisplayClassName == null)
        return false;
      f_customDisplayClassName = null;
      return true;
    }
    if (value.equals(f_customDisplayClassName)) {
      return false;
    }
    f_customDisplayClassName = value;
    return true;
  }

  private static final String STARTINFO = "BEGIN-INFO";
  private static final String STOPINFO = "END-INFO";

  /**
   * Gets the Querydoc associated with this query. It has all <tt>--</tt> breaks
   * removed.
   * <p>
   * If no <tt>BEGIN-INFO</tt> </tt>END-INFO</tt> pair exists in the SQL query
   * comments then the description is returned as HTML, such as
   * <tt>&lt;p&gt;&lt;strong&gt;</tt> <i>description</i>
   * <tt>&lt;/p&gt;&lt;/strong&gt;</tt>
   * 
   * @return an HTML description of this query.
   */
  public String getQueryDoc() {
    final String strippedCommentText = SLUtility.extractTextFromWholeLineCommentBlock(f_sql, "--");
    final int start = strippedCommentText.indexOf(STARTINFO);
    final int stop = strippedCommentText.indexOf(STOPINFO);
    if (start == -1 || stop == -1) {
      return "<p><strong>" + getDescription() + "</strong></p>";
    }
    return strippedCommentText.substring(start + STARTINFO.length(), stop).trim();
  }

  private static final String STARTMETA = "BEGIN-META(";
  private static final char STARTMETA_CLOSE = ')';
  private static final String STOPMETA = "END-META";

  /**
   * Extracts meta information within the comments of the passed text, which is
   * assumed to be from an ad hoc query, and returns it as a map of the meta
   * name to its detailed information. This method is intended for use in
   * low-level data access code that no longer has access to an ad hoc query
   * code (such as within a driver implementation).
   * <p>
   * The returned map maps the meta name to a structure describing it.
   * <p>
   * If no <tt>BEGIN-META(</tt><i>name</i><tt>)</tt> </tt>END-META</tt> pair
   * exists in the SQL query comments within the text then an empty map is
   * returned.
   * <p>
   * While multiple meta regions may exist in a query only the <i>last</i> of a
   * given name will appear in the returned map. Therefore, names of meta
   * regions in SQL comments should be unique per query.
   * 
   * @param text
   *          the fully-qualified SQL text (with comments) of an ad hoc query.
   * @return a possibly empty map containing all the meta information in the
   *         passed text.
   */
  @NonNull
  public static HashMap<String, AdHocQueryMeta> getMetaFromString(final String text) {
    final HashMap<String, AdHocQueryMeta> result = new HashMap<String, AdHocQueryMeta>();
    if (text == null)
      return result;
    String strippedCommentText = SLUtility.extractTextFromWholeLineCommentBlock(text, "--");
    while (true) {
      final int start = strippedCommentText.indexOf(STARTMETA);
      final int stop = strippedCommentText.indexOf(STOPMETA);
      if (start == -1 || stop == -1)
        break; // no more
      final String potentialMeta = strippedCommentText.substring(start + STARTMETA.length(), stop);
      final int closeMetaName = potentialMeta.indexOf(STARTMETA_CLOSE);
      if (closeMetaName != -1) {
        final String metaName = potentialMeta.substring(0, closeMetaName);
        if (!"".equals(metaName)) {
          final String metaText = potentialMeta.substring(metaName.length() + 1);
          AdHocQueryMeta meta = new AdHocQueryMeta(metaName, metaText);
          result.put(meta.getName(), meta);
        }
      }
      strippedCommentText = strippedCommentText.substring(stop + STOPMETA.length());
    }
    return result;
  }

  /**
   * Gets the detailed information for a particular meta name within the
   * comments of the passed text which is assumed to be from an ad hoc query.
   * This method is intended for use in low-level data access code that no
   * longer has access to an ad hoc query code (such as within a driver
   * implementation).
   * <p>
   * If no <tt>BEGIN-META(</tt><i>name</i><tt>)</tt> </tt>END-META</tt> pair
   * with the passed name exists in the SQL query comments within the text then
   * {@code null} is returned.
   * 
   * @param text
   *          the fully-qualified SQL text (with comments) of an ad hoc query.
   * @param name
   *          for the meta region.
   * @return the text of the meta, or {@code null} if a meta region with the
   *         passed name does not exist in the query comments.
   * @throws IllegalArgumentException
   *           if <tt>name</tt> is {@code null}.
   */
  @Nullable
  public static AdHocQueryMeta getMetaFromStringWithName(final String text, final String name) {
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    return getMetaFromString(text).get(name);
  }

  /**
   * The SQL text of this query.
   */
  private String f_sql = "";

  /**
   * Holds meta information about this query. It is obtained from the query
   * comments so it needs to be update any time {@link #f_sql} changes.
   */
  private final Map<String, AdHocQueryMeta> f_nameToMeta = new HashMap<String, AdHocQueryMeta>();

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
    f_nameToMeta.clear();
    f_nameToMeta.putAll(getMetaFromString(sql));
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

  private boolean f_noDefaultSubQuery = false;

  /**
   * Gets if this query is intended to <i>never</i> have a default sub-query.
   * 
   * @return {@code true} if this query should never have a default sub-query,
   *         {@code false} otherwise.
   */
  public boolean noDefaultSubQuery() {
    return f_noDefaultSubQuery;
  }

  /**
   * Sets if this query is intended to <i>never</i> have a default sub-query.
   * 
   * @param value
   *          {@code true} if this query should never have a default sub-query,
   *          {@code false} otherwise.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setNoDefaultSubQuery(boolean value) {
    if (value != f_noDefaultSubQuery) {
      f_noDefaultSubQuery = value;
      return true;
    } else {
      return false; // no change
    }
  }

  /**
   * The set of queries that can be executed based upon a selected row of the
   * result of this query.
   */
  private final List<AdHocSubQuery> f_subQueries = new ArrayList<AdHocSubQuery>();

  /**
   * Adds a sub-query to this query if it is a query managed by the same query
   * manager as this query. A sub-query is a query that can be executed based
   * upon a selected row of the result of this query.
   * 
   * @param subQuery
   *          a sub-query.
   * @param priorityAsDefaultSubQuery
   *          a priority for this query to be the default sub-query. The highest
   *          query able to be run gets chosen as the default.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addSubQuery(final AdHocQuery subQuery, final int priorityAsDefaultSubQuery) {
    if (subQuery == null || !f_manager.contains(subQuery)) {
      return false;
    }
    return AdHocSubQuery.addHelper(f_subQueries, new AdHocSubQuery(subQuery, priorityAsDefaultSubQuery));
  }

  /**
   * Adds all the passed queries as sub-queries to this query with default
   * priority to be the default sub-query.
   * 
   * @param queries
   *          a collection of queries.
   * @return {@code true} if the set of sub-queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addSubQueries(final Collection<AdHocQuery> queries) {
    boolean modified = false;
    for (AdHocQuery query : queries) {
      if (addSubQuery(query, AdHocSubQuery.DEFAULT_PRIORITY)) {
        modified = true;
      }
    }
    return modified;
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
    return AdHocSubQuery.removeHelper(f_subQueries, subQuery);
  }

  /**
   * Removes all sub-queries form this query.
   */
  public void clearSubQueries() {
    f_subQueries.clear();
  }

  /**
   * Gets a copy of the sub-query aggregates for this query. A sub-query is a
   * query that can be executed based upon a selected row of the result of this
   * query.
   * <p>
   * This is a rather raw copy and is not sorted in any particular order. In
   * particular, it should be processed before being displayed in the user
   * interface.
   * 
   * @return the sub-query aggregates for this query.
   */
  public ArrayList<AdHocSubQuery> getSubQueryList() {
    return new ArrayList<AdHocSubQuery>(f_subQueries);
  }

  /**
   * Gets a copy of the sub-query aggregates for this query removing queries
   * that are not intended to be displayed in any query menu. A sub-query is a
   * query that can be executed based upon a selected row of the result of this
   * query.
   * <p>
   * This is a rather raw copy and is not sorted in any particular order. In
   * particular, it should be processed before being displayed in the user
   * interface.
   * 
   * @return the visible sub-query aggregates for this query.
   * 
   * @see #showInQueryMenu()
   */
  public ArrayList<AdHocSubQuery> getVisibleSubQueryList() {
    final ArrayList<AdHocSubQuery> result = getSubQueryList();
    for (final Iterator<AdHocSubQuery> i = result.iterator(); i.hasNext();) {
      final AdHocSubQuery item = i.next();
      if (!item.getQuery().showInQueryMenu()) {
        i.remove();
      }
    }
    return result;
  }

  public HashSet<AdHocQuery> getSubQueries() {
    return AdHocSubQuery.querySetHelper(f_subQueries);
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
  public boolean isCompletelySubstitutedBy(@Nullable final Map<String, String> variableValues) {
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
  public String getSql(@NonNull final Map<String, String> variableValues) {
    if (variableValues == null)
      throw new IllegalArgumentException(I18N.err(44, "variableValues"));

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
