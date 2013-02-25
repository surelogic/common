package com.surelogic.common.adhoc;

import com.surelogic.NonNull;

/**
 * This interface captures the common identifying information of ad hoc queries
 * and categories&mdash;called <i>ad hoc objects</i>&mdash; for the purpose of
 * sorting and displaying them.
 */
public interface AdHocIdentity {

  /**
   * Gets the identifier for this ad hoc object.
   * 
   * @return the identifier for this ad hoc object.
   */
  @NonNull
  String getId();

  /**
   * Gets the description of this ad hoc object.
   * 
   * @return the description of this ad hoc object.
   */
  @NonNull
  String getDescription();

  /**
   * Gets a number to help sort this ad hoc object with other ad hoc objects.
   * 
   * @return a value. May be negative, zero, or positive. The larger the more
   *         important. The default value is zero.
   */
  int getSortHint();
}
