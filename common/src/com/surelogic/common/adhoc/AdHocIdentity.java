package com.surelogic.common.adhoc;

import com.surelogic.NonNull;

public interface AdHocIdentity {

  @NonNull
  String getId();

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
