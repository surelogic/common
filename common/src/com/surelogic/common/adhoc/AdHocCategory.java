package com.surelogic.common.adhoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.surelogic.common.i18n.I18N;

/**
 * Represents a group of ad hoc queries. Each instance is created and owned by
 * one and only one {@link AdHocManager}.
 */
public final class AdHocCategory implements AdHocObject {

  AdHocCategory(final AdHocManager manager, final String id) {
    assert manager != null;
    f_manager = manager;
    assert id != null;
    f_id = id;
  }

  /**
   * The manager that owns this category.
   */
  private final AdHocManager f_manager;

  /**
   * Gets the query manager that created and owns this category.
   * 
   * @return the query manager that created and owns this category.
   */
  public AdHocManager getManager() {
    return f_manager;
  }

  /**
   * The identifier for this category.
   */
  private String f_id;

  /**
   * Gets the identifier for this category.
   * 
   * @return the identifier for this category.
   */
  public String getId() {
    return f_id;
  }

  /**
   * Sets the identifier for this category if the new identifier is unused. The
   * identifier is unused if it is not the identifier for another query or
   * category managed by this category's manager, or more formally
   * {@code !getManager().contains(id)}. If the new identifier is already used
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param id
   *          an identifier for this category.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   * @throws IllegalArgumentException
   *           if <tt>id</tt> is being used by another query or category.
   */
  public boolean setId(final String id) {
    if (id == null || f_id.equals(id)) {
      return false;
    }
    if (f_manager.contains(id)) {
      throw new IllegalArgumentException(I18N.err(303, id));
    }
    f_id = id;
    return true;
  }

  /**
   * A description of this query.
   */
  private String f_description = "";

  /**
   * Gets the description of this query.
   * 
   * @return the description of this query.
   */
  public String getDescription() {
    return f_description;
  }

  /**
   * Sets the description of this query. This method has no effect if the name
   * passed is the empty string.
   * 
   * @param description
   *          non-null description of this query.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setDescription(final String description) {
    if (description == null || description.equals(f_description) || "".equals(description)) {
      return false;
    }
    f_description = description;
    return true;
  }

  /**
   * The text for this category if it has data returned by any of its queries.
   */
  private String f_hasDataText = "";

  /**
   * Gets the text for this category if it has data returned by any of its
   * queries.
   * 
   * @return the text for this category if it has data returned by any of its
   *         queries.
   */
  public String getHasDataText() {
    return f_hasDataText;
  }

  /**
   * Sets the text for this category if it has data returned by any of its
   * queries.
   * 
   * @param text
   *          non-null text.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setHasDataText(final String value) {
    if (value == null || value.equals(f_hasDataText)) {
      return false;
    }
    f_hasDataText = value;
    return true;
  }

  /**
   * The text for this category if no data is returned by any of its queries.
   */
  private String f_noDataText = "";

  /**
   * Gets the text for this category if no data is returned by any of its
   * queries.
   * 
   * @return the text for this category if no data is returned by any of its
   *         queries.
   */
  public String getNoDataText() {
    return f_noDataText;
  }

  /**
   * Sets the text for this category if no data is returned by any of its
   * queries.
   * 
   * @param text
   *          non-null text.
   * @return {@code true} if the value was changed, {@code false} otherwise.
   */
  public boolean setNoDataText(final String value) {
    if (value == null || value.equals(f_noDataText)) {
      return false;
    }
    f_noDataText = value;
    return true;
  }

  /**
   * A value to help sort this category with other categories. May be negative,
   * zero, or positive.
   */
  private int f_sortHint = 0;

  /**
   * Gets a number to help sort this category with other categories.
   * 
   * @return a value. May be negative, zero, or positive.
   */
  public int getSortHint() {
    return f_sortHint;
  }

  /**
   * Sets a number to help sort this category with other categories.
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
   * An increasing number that reflects how many changes have been made state of
   * this object. This value is only changed each time the software is released.
   * It cannot be changed by users of the software.
   * <p>
   * If the state of this query has been changed by the user this is indicated
   * by {@link #isChanged()}.
   */
  private long f_revision = 0;

  /**
   * Gets the revision of this category.
   * <p>
   * The revision is a number that reflects how many changes have been made
   * state of this object. This value is only changed each time the software is
   * released. It cannot be changed by users of the software.
   * <p>
   * If the state of this query has been changed by the user this is indicated
   * by {@link #isChanged()}.
   * 
   * @return the revision of this category.
   */
  public long getRevision() {
    return f_revision;
  }

  /**
   * Sets the revision of this category.
   * 
   * @param value
   *          the revision of this category.
   * 
   */
  public void setRevision(final long value) {
    f_revision = value;
  }

  /**
   * This flag indicates that something about the state of this category has
   * changed from its current revision number.
   */
  private boolean f_changed;

  /**
   * Flags if something about the state of this category has changed from its
   * current revision number.
   * 
   * @return {@code true} if something about the state of this category has
   *         changed from its current revision number, {@code false} otherwise.
   */
  public boolean isChanged() {
    return f_changed;
  }

  /**
   * Sets the value of the changed flag. A value of {@code true} indicates that
   * something about the state of this category has changed from its current
   * revision number.
   * 
   * @param value
   *          the new for the changed flag.
   */
  public void setChanged(final boolean value) {
    f_changed = value;
  }

  /**
   * Marks that something about the state of this category has changed from its
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
   * The set of queries in this category.
   */
  private final Set<AdHocQuery> f_queries = new HashSet<AdHocQuery>();

  /**
   * Checks if this category contains the passed query.
   * 
   * @param query
   *          a query.
   * @return {@code true} if this category contains <tt>query</tt>,
   *         {@code false} otherwise.
   */
  public boolean contains(AdHocQuery query) {
    return f_queries.contains(query);
  }

  /**
   * Sets the set of queries in this category.
   * 
   * @param queries
   *          a set of queries.
   * @return {@code true} if the set of queries was changed, {@code false}
   *         otherwise.
   */
  public boolean setQueries(final Set<AdHocQuery> queries) {
    if (queries == null || f_queries.equals(queries)) {
      return false;
    }
    f_queries.clear();
    f_queries.addAll(queries);
    return true;
  }

  /**
   * Adds a query to this category if it is a query managed by the same query
   * manager as this category.
   * 
   * @param query
   *          a query.
   * @return {@code true} if the set of queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addQuery(final AdHocQuery query) {
    if (query == null || !f_manager.contains(query)) {
      return false;
    }
    return f_queries.add(query);
  }

  /**
   * Adds the queries to this category if, for each query, it is a query managed
   * by the same query manager as this category.
   * 
   * @param queries
   *          a collection of queries.
   * @return {@code true} if the set of queries was changed, {@code false}
   *         otherwise.
   */
  public boolean addQueries(final Collection<AdHocQuery> queries) {
    boolean val = false;
    for (AdHocQuery q : queries) {
      val |= addQuery(q);
    }
    return val;
  }

  /**
   * Removes a query from this category if it is present.
   * 
   * @param query
   *          a query.
   * @return {@code true} if the set of queries was changed, {@code false}
   *         otherwise.
   */
  public boolean removeQuery(final AdHocQuery query) {
    return f_queries.remove(query);
  }

  /**
   * Removes all queries form this category.
   */
  public void clearQueries() {
    f_queries.clear();
  }

  /**
   * Gets a copy of the set of queries in this category.
   * 
   * @return the set of queries in this category.
   */
  public Set<AdHocQuery> getQueries() {
    return new HashSet<AdHocQuery>(f_queries);
  }

  /**
   * Returns a list of the queries in this category sorted by their description.
   * 
   * @return a list of the queries in this category sorted by their description.
   * 
   * @see AdHocObjectDescriptionComparator
   */
  public List<AdHocQuery> getQueryList() {
    final ArrayList<AdHocQuery> result = new ArrayList<AdHocQuery>(f_queries);
    Collections.sort(result, AdHocObjectDescriptionComparator.getInstance());
    return result;
  }

  /**
   * Returns a list of the queries that should be displayed in the query menu
   * for this category sorted by their description.
   * 
   * @return a list of the queries that should be displayed in the query menu
   *         for this category sorted by their description.
   * 
   * @see #showInQueryMenu()
   */
  public List<AdHocQuery> getVisibleQueryList() {
    final List<AdHocQuery> result = getQueryList();
    for (final Iterator<AdHocQuery> i = result.iterator(); i.hasNext();) {
      final AdHocQuery query = i.next();
      if (!query.showInQueryMenu()) {
        i.remove();
      }
    }
    return result;
  }
}
