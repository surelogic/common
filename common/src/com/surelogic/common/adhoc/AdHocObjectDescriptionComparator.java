package com.surelogic.common.adhoc;

import java.util.Comparator;

import com.surelogic.common.i18n.I18N;

/**
 * A comparator that orders {@link AdHocIdentity} objects by their description.
 * This comparator is case insensitive.
 */
public class AdHocObjectDescriptionComparator implements Comparator<AdHocIdentity> {

  private static final AdHocObjectDescriptionComparator INSTANCE = new AdHocObjectDescriptionComparator();

  public static final AdHocObjectDescriptionComparator getInstance() {
    return INSTANCE;
  }

  private AdHocObjectDescriptionComparator() {
    // singleton
  }

  @Override
  public int compare(AdHocIdentity o1, AdHocIdentity o2) {
    if (o1 == null)
      throw new IllegalArgumentException(I18N.err(44, "o1"));
    if (o2 == null)
      throw new IllegalArgumentException(I18N.err(44, "o2"));
    return String.CASE_INSENSITIVE_ORDER.compare(o1.getDescription(), o2.getDescription());
  }
}
