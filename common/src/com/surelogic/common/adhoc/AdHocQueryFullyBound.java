package com.surelogic.common.adhoc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;

/**
 * A query that has bound all its variables.
 */
public final class AdHocQueryFullyBound {

  public AdHocQueryFullyBound(@NonNull AdHocQuery query, @NonNull Map<String, String> variableValues,
      @Nullable Map<String, String> topVariableValues) {
    if (query == null)
      throw new IllegalArgumentException(I18N.err(44, "query"));
    f_query = query;
    if (variableValues == null)
      throw new IllegalArgumentException(I18N.err(44, "variableValues"));
    f_variableValues = variableValues;
    if (topVariableValues == null)
      f_topVariableValues = Collections.emptyMap();
    else
      f_topVariableValues = topVariableValues;

    /*
     * Check if all variables are resolved
     */
    if (!query.isCompletelySubstitutedBy(variableValues)) {
      throw new IllegalStateException(I18N.err(120, query, variableValues));
    }
  }

  private final AdHocQuery f_query;

  /**
   * Gets the query which is bound by this.
   * 
   * @return the query which is bound by this.
   */
  public AdHocQuery getQuery() {
    return f_query;
  }

  /**
   * Gets the SQL with all variable substitutions performed. This method is
   * equivalent to {@code o.getQuery().getSql(o.getVariableValues())} on an
   * object {@code o} of this type.
   * 
   * @return the SQL with all variable substitutions performed.
   */
  public String getSql() {
    return f_query.getSql(f_variableValues);
  }

  /**
   * Gets the query manager that owns this query.
   * 
   * @return the query manager that owns this query.
   */
  public AdHocManager getManager() {
    return f_query.getManager();
  }

  private final Map<String, String> f_variableValues;

  /**
   * Gets the set of variable values for this query. This set will fully
   * substitute the query but may contain values for other variables as well.
   * These variables should be passed along to sub-queries of this query.
   * 
   * @return the set of variable values for this query.
   */
  public Map<String, String> getVariableValues() {
    return new HashMap<String, String>(f_variableValues);
  }

  private final Map<String, String> f_topVariableValues;

  /**
   * Gets the set of top variable values for this query. This value is only
   * useful for heuristic linking into the code when trying to use the most
   * recent, or top, variables.
   * 
   * @return the set of top variable values for this query.
   */
  public Map<String, String> getTopVariableValues() {
    return new HashMap<String, String>(f_topVariableValues);
  }
}
