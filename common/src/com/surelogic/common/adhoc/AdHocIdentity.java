package com.surelogic.common.adhoc;

import java.util.Comparator;

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

  /*
   * The comparators below use the identifier as last resort for sorting and to
   * make sure output to a file is done in the same order each time.
   */

  public static final Comparator<AdHocIdentity> BY_DESCRIPTION = new Comparator<AdHocIdentity>() {
    @Override
    public int compare(AdHocIdentity o1, AdHocIdentity o2) {
      if (o1 == null && o2 == null)
        return 0;
      else if (o1 == null && o2 != null)
        return -1;
      else if (o1 != null && o2 == null)
        return 1;
      else {
        int result = String.CASE_INSENSITIVE_ORDER.compare(o1.getDescription(), o2.getDescription());
        if (result == 0) {
          // use id as last resort
          return o1.getId().compareTo(o2.getId());
        } else
          return result;
      }
    }
  };

  public static final Comparator<AdHocIdentity> BY_HINT_DESCRIPTION = new Comparator<AdHocIdentity>() {
    @Override
    public int compare(AdHocIdentity o1, AdHocIdentity o2) {
      if (o1 == null && o2 == null)
        return 0;
      else if (o1 == null && o2 != null)
        return -1;
      else if (o1 != null && o2 == null)
        return 1;
      else if (o1.getSortHint() > o2.getSortHint())
        return -1;
      else if (o1.getSortHint() < o2.getSortHint())
        return 1;
      else
        return BY_DESCRIPTION.compare(o1, o2);
    }
  };
}
