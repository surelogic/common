package com.surelogic.common.adhoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;

/**
 * Aggregates a reference to a query and its priority to be the default
 * sub-query.
 */
public final class AdHocSubQuery {

  public static final int DEFAULT_PRIORITY = 0;

  /**
   * Creates a sub-query aggregate with priority {@link #DEFAULT_PRIORITY}.
   * 
   * @param subQuery
   *          a sub-query.
   */
  AdHocSubQuery(@NonNull AdHocQuery subQuery) {
    this(subQuery, DEFAULT_PRIORITY);
  }

  /**
   * Creates a sub-query aggregate.
   * 
   * @param query
   *          a sub-query.
   * @param priorityAsDefault
   *          a priority for this query to be the default sub-query. The highest
   *          priority query able to be run gets chosen as the default.
   */
  AdHocSubQuery(@NonNull AdHocQuery query, int priorityAsDefault) {
    if (query == null)
      throw new IllegalArgumentException(I18N.err(44, "subQuery"));
    f_query = query;
    f_priorityAsDefault = priorityAsDefault;
  }

  private final AdHocQuery f_query;

  /**
   * Gets the query portion of this aggregate.
   * 
   * @return a query.
   */
  public AdHocQuery getQuery() {
    return f_query;
  }

  private int f_priorityAsDefault;

  /**
   * Sets the the priority for this query to be the default sub-query. The
   * highest priority query able to be run gets chosen as the default.
   * <p>
   * The default priority is {@link #DEFAULT_PRIORITY}.
   * 
   * @param value
   *          a new priority
   * @return {@code true} if the priority changed, {@code false} otherwise.
   */
  public boolean setPriorityAsDefault(int value) {
    if (f_priorityAsDefault != value) {
      f_priorityAsDefault = value;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets the priority for this query to be the default sub-query. The highest
   * priority query able to be run gets chosen as the default.
   * <p>
   * The default priority is {@link #DEFAULT_PRIORITY}.
   * 
   * @return a priority.
   */
  public int getPriorityAsDefault() {
    return f_priorityAsDefault;
  }

  /**
   * Adds sub-query aggregate to the passed collection if necessary. If a
   * sub-query aggregate is already found in the collection with the same query
   * basis then that aggregate is left in the collection and its priority is
   * changed, if necessary.
   * 
   * @param mutable
   *          a mutable collection of sub-query aggregates.
   * @param subQuery
   *          the query to add to the collection.
   * @return {@code true} if the passed collection was changed, {@code false}
   *         otherwise.
   */
  public static boolean addHelper(final Collection<AdHocSubQuery> mutable, final AdHocSubQuery subQuery) {
    for (final AdHocSubQuery item : mutable) {
      if (item.getQuery().equals(subQuery.getQuery())) {
        if (item.getPriorityAsDefault() != subQuery.getPriorityAsDefault()) {
          item.setPriorityAsDefault(subQuery.getPriorityAsDefault());
          return true;
        } else {
          return false;
        }
      }
    }
    mutable.add(subQuery);
    return true;
  }

  /**
   * Removes any sub-query aggregate in the passed collection with the same
   * query basis as the passed query. This action is used to remove the query as
   * a sub-query, for example, should a query be deleted.
   * 
   * @param mutable
   *          a mutable collection of sub-query aggregates.
   * @param query
   *          a query.
   * @return {@code true} if the passed collection was changed, {@code false}
   *         otherwise.
   */
  public static boolean removeHelper(final Collection<AdHocSubQuery> mutable, final AdHocQuery query) {
    boolean result = false;
    for (Iterator<AdHocSubQuery> iterator = mutable.iterator(); iterator.hasNext();) {
      final AdHocSubQuery item = iterator.next();
      if (item.getQuery().equals(query)) {
        iterator.remove();
        result = true;
      }
    }
    return result;
  }

  /**
   * Creates a set of the queries from the passed collection of sub-query
   * aggregates.
   * 
   * @param mutable
   *          a mutable collection of sub-query aggregates.
   * @return a set of queries.
   */
  @NonNull
  public static HashSet<AdHocQuery> querySetHelper(final Collection<AdHocSubQuery> mutable) {
    final HashSet<AdHocQuery> result = new HashSet<>();
    for (final AdHocSubQuery item : mutable) {
      result.add(item.getQuery());
    }
    return result;
  }

  /**
   * Sorts the passed list of sub-query aggregates using
   * {@link AdHocIdentity#BY_DESCRIPTION}.
   * 
   * @param mutable
   *          a mutable list of sub-query aggregates.
   */
  public static void sortByDescriptionHelper(final List<AdHocSubQuery> mutable) {
    final ArrayList<AdHocSubQuery> copy = new ArrayList<>(mutable);
    final ArrayList<AdHocQuery> queries = queryListSortedByDescriptionHelper(mutable);
    mutable.clear();
    for (final AdHocQuery query : queries) {
      final AdHocSubQuery sub = find(query, copy);
      if (sub == null)
        throw new IllegalStateException(I18N.err(306, query.getDescription()));
      mutable.add(sub);
    }
  }

  /**
   * Sorts the passed list of sub-query aggregates using
   * {@link AdHocIdentity#BY_HINT_DESCRIPTION}.
   * 
   * @param mutable
   *          a mutable list of sub-query aggregates.
   */
  public static void sortByHintDescriptionHelper(final List<AdHocSubQuery> mutable) {
    final ArrayList<AdHocSubQuery> copy = new ArrayList<>(mutable);
    final ArrayList<AdHocQuery> queries = queryListSortedByHintDescriptionHelper(mutable);
    mutable.clear();
    for (final AdHocQuery query : queries) {
      final AdHocSubQuery sub = find(query, copy);
      if (sub == null)
        throw new IllegalStateException(I18N.err(306, query.getDescription()));
      mutable.add(sub);
    }
  }

  /**
   * Creates a list of the query from the passed collection of sub-query
   * aggregates sorted using {@link AdHocIdentity#BY_DESCRIPTION}.
   * 
   * @param mutable
   *          a mutable collection of sub-query aggregates.
   * @return the sorted list.
   */
  @NonNull
  public static ArrayList<AdHocQuery> queryListSortedByDescriptionHelper(final Collection<AdHocSubQuery> mutable) {
    final ArrayList<AdHocQuery> queries = new ArrayList<>();
    for (final AdHocSubQuery item : mutable) {
      queries.add(item.getQuery());
    }
    Collections.sort(queries, AdHocIdentity.BY_DESCRIPTION);
    return queries;
  }

  /**
   * Creates a list of the query from the passed collection of sub-query
   * aggregates sorted using {@link AdHocIdentity#BY_HINT_DESCRIPTION}.
   * 
   * @param mutable
   *          a mutable collection of sub-query aggregates.
   * @return the sorted list.
   */
  @NonNull
  public static ArrayList<AdHocQuery> queryListSortedByHintDescriptionHelper(final Collection<AdHocSubQuery> mutable) {
    final ArrayList<AdHocQuery> queries = new ArrayList<>();
    for (final AdHocSubQuery item : mutable) {
      queries.add(item.getQuery());
    }
    Collections.sort(queries, AdHocIdentity.BY_HINT_DESCRIPTION);
    return queries;
  }

  /**
   * Sorts the passed set of sub-queries for the user interface. This method
   * considers if the sub-query can be run in the passed context (variable
   * values and parent query) as well as its priority.
   * 
   * @param mutable
   *          a mutable list of sub-query aggregates.
   * @param variableValues
   *          the context variable values or {@code null} if none.
   * @param parentQuery
   *          the context query or {@code null} if none.
   * @return the default sub-query or {@code null} if the passed list is empty
   *         or the parent query specifies that no default sub-query is allowed
   *         (via {@link AdHocQuery#noDefaultSubQuery()}).
   */
  @Nullable
  public static AdHocSubQuery sortHelper(final List<AdHocSubQuery> mutable, @Nullable Map<String, String> variableValues,
      @Nullable AdHocQuery parentQuery) {
    if (variableValues == null)
      variableValues = Collections.emptyMap();
    sortByHintDescriptionHelper(mutable);
    AdHocSubQuery defaultQueryResult = null;
    int defaultQueryPriority = Integer.MIN_VALUE;
    for (final AdHocSubQuery sub : mutable) {
      final AdHocQuery query = sub.getQuery();
      boolean considerForDefault = query.isCompletelySubstitutedBy(variableValues);
      if (considerForDefault) {
        if (sub.getPriorityAsDefault() > defaultQueryPriority) {
          // new default
          defaultQueryResult = sub;
          defaultQueryPriority = sub.getPriorityAsDefault();
        }
      }
    }
    if (parentQuery != null && parentQuery.noDefaultSubQuery()) {
      defaultQueryResult = null;
    }
    return defaultQueryResult;
  }

  /**
   * Extracts the sub-query aggregate from the passed collection that references
   * the specified query.
   * 
   * @param query
   *          a query.
   * @param within
   *          a collection of of sub-query aggregates.
   * @return a sub-query aggregate or {@code null} if none is found.
   */
  @Nullable
  public static AdHocSubQuery find(final AdHocQuery query, final Collection<AdHocSubQuery> within) {
    for (final AdHocSubQuery item : within) {
      if (item.getQuery().equals(query))
        return item;
    }
    return null;
  }
}
