package com.surelogic.common.adhoc;

import java.util.Comparator;

/**
 * A comparator that orders {@link AdHocQueryResult} objects by the time they
 * were run on the database.
 */
public final class AdHocQueryResultTimeComparator implements
		Comparator<AdHocQueryResult> {

	private static final AdHocQueryResultTimeComparator INSTANCE = new AdHocQueryResultTimeComparator();

	public static final AdHocQueryResultTimeComparator getInstance() {
		return INSTANCE;
	}

	private AdHocQueryResultTimeComparator() {
		// singleton
	}

	@Override
  public int compare(AdHocQueryResult o1, AdHocQueryResult o2) {
		return o1.getTimeQueryWasRun().compareTo(o2.getTimeQueryWasRun());
	}
}
