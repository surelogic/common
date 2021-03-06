package com.surelogic.common.adhoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;

/**
 * Abstract base class representing a query result.
 */
public abstract class AdHocQueryResult {

  /**
   * An empty array of {@link AdHocQueryResult} objects.
   */
  public static final AdHocQueryResult[] EMPTY = new AdHocQueryResult[0];

  @NonNull
  private final AdHocManager f_manager;

  /**
   * Gets the query manager that owns this query result.
   * 
   * @return the query manager that owns this query result.
   */
  @NonNull
  public AdHocManager getManager() {
    return f_manager;
  }

  private final AdHocQueryResultSqlData f_parent;

  /**
   * The parent result, a row of which was used as variables for this query.
   * 
   * @return the parent result of this query, or {@code null} if none.
   */
  public AdHocQueryResultSqlData getParent() {
    return f_parent;
  }

  private final DBConnection f_db;

  /**
   * The data source used to construct this query.
   * 
   * @return a {@link DBConnection}. May not be null.
   */
  public DBConnection getDB() {
    return f_db;
  }

  @NonNull
  private final Set<AdHocQueryResult> f_children = new HashSet<>();

  protected void addChild(final AdHocQueryResult child) {
    f_children.add(child);
  }

  protected void removeChild(final AdHocQueryResult child) {
    f_children.remove(child);
  }

  public final boolean hasChildren() {
    return !f_children.isEmpty();
  }

  /**
   * Gets the set of query results run from data obtained from this query
   * result.
   * 
   * @return the set of query results run from data obtained from this query
   *         result.
   */
  public final Set<AdHocQueryResult> getChildren() {
    return new HashSet<>(f_children);
  }

  /**
   * Gets a list of query results run from data obtained from this query result
   * sorted by when they were run on the database.
   * 
   * @return a list of query results run from data obtained from this query
   *         result sorted by when they were run on the database.
   * 
   * @see AdHocQueryResultTimeComparator
   */
  public final List<AdHocQueryResult> getChildrenList() {
    final ArrayList<AdHocQueryResult> result = new ArrayList<>(f_children);
    Collections.sort(result, AdHocQueryResultTimeComparator.getInstance());
    return result;
  }

  @NonNull
  private final AdHocQueryFullyBound f_query;

  /**
   * Gets the fully bound query
   * 
   * @return the fully bound query.
   */
  @NonNull
  public AdHocQueryFullyBound getQueryFullyBound() {
    return f_query;
  }

  private final Date f_timeQueryWasRun = new Date();

  /**
   * Gets the time that the query was run on the database to create this result.
   * 
   * @return the time that the query was run on the database to create this
   *         result.
   */
  public Date getTimeQueryWasRun() {
    return f_timeQueryWasRun;
  }

  public AdHocQueryResult(final AdHocManager manager, final AdHocQueryResultSqlData parent, final AdHocQueryFullyBound query,
      final DBConnection datasource) {
    if (manager == null) {
      throw new IllegalArgumentException(I18N.err(44, "manager"));
    }
    f_manager = manager;
    f_parent = parent;
    if (query == null) {
      throw new IllegalArgumentException(I18N.err(44, "query"));
    }
    f_query = query;

    if (f_parent != null) {
      f_parent.addChild(this);
    }
    f_manager.addResult(this);
    if (datasource == null) {
      throw new IllegalArgumentException(I18N.err(44, "datasource"));
    }
    f_db = datasource;
  }

  /**
   * Deletes this query result as well as all its children.
   * <p>
   * <i>Implementation Note:</i> This operation will clean up references from
   * the query manager to this query result. It will not, however, invoke
   * {@link AdHocManager#notifyResultModelChange()}. That call should be made
   * from client code.
   */
  public void delete() {
    /*
     * Make a copy of the set of children because we are going to delete them
     * and we don't want to throw a concurrent modification exception.
     */
    final Set<AdHocQueryResult> children = new HashSet<>(f_children);
    for (final AdHocQueryResult child : children) {
      child.delete();
    }
    if (!f_children.isEmpty()) {
      throw new IllegalStateException("deleting all child results of a query result failed");
    }
    if (f_parent != null) {
      f_parent.removeChild(this);
    }
    f_manager.removeResult(this);
  }

  /**
   * Gets the symbolic name from {@link CommonImages} for the image that should
   * be displayed in the user interface for this query result.
   * 
   * @return the symbolic name from {@link CommonImages} for the image that
   *         should be displayed in the user interface for this query result.
   */
  public abstract String getImageSymbolicName();

  /**
   * Data associated with this result. A place where custom result displays may
   * store data associated with this query result.
   */
  @Nullable
  private Object f_data = null;

  /**
   * Sets data associated with this result.
   * <p>
   * May be used by custom query result displays.
   * 
   * @param value
   *          data associated with this result, or {@code null} to clear.
   */
  public final void setData(@Nullable Object value) {
    f_data = value;
  }

  /**
   * Gets data associated with this result.
   * <p>
   * May be used by custom query result displays.
   * 
   * @return data associated with this result, or {@code null} if none.
   */
  @Nullable
  public Object getData() {
    return f_data;
  }

  /**
   * Gets the rows returned or other descriptive information about this result
   * as a string.
   * <p>
   * Implementations may decide what should be returned.
   * 
   * @return the rows returned or other descriptive information about this
   *         result as a string.
   */
  @NonNull
  public abstract String getRowCountInformationAsHumanReadableString();

  @Override
  public String toString() {
    return f_query.getQuery().getDescription() + " at " + SLUtility.toStringDayHMS(f_timeQueryWasRun);
  }

  public String toLinkString() {
    return "<A HREF=\"edit\">" + f_query.getQuery().getDescription() + "</A>";
  }
}
