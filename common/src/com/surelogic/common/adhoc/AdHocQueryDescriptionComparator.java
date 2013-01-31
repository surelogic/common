package com.surelogic.common.adhoc;

import java.util.Comparator;

import com.surelogic.common.i18n.I18N;

/**
 * A comparator that orders {@link AdHocQuery} objects by their description.
 * This comparator is case insensitive.
 */
public class AdHocQueryDescriptionComparator implements Comparator<AdHocQuery> {

	private static final AdHocQueryDescriptionComparator INSTANCE = new AdHocQueryDescriptionComparator();

	public static final AdHocQueryDescriptionComparator getInstance() {
		return INSTANCE;
	}

	private AdHocQueryDescriptionComparator() {
		// singleton
	}

	@Override
  public int compare(AdHocQuery o1, AdHocQuery o2) {
		if (o1 == null)
			throw new IllegalArgumentException(I18N.err(44, "o1"));
		if (o2 == null)
			throw new IllegalArgumentException(I18N.err(44, "o2"));
		return String.CASE_INSENSITIVE_ORDER.compare(o1.getDescription(), o2
				.getDescription());
	}
}
