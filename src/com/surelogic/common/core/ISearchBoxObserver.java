package com.surelogic.common.core;

public interface ISearchBoxObserver {

	/**
	 * Notifies this object that the search text has been changed. It is only
	 * called when the text appears to be stable, e.g., when no typing has
	 * occurred for 500ms.
	 * 
	 * @param text
	 *            the new search text.
	 */
	void searchTextChangedTo(String text);

	/**
	 * Invoked when the search text is cleared.
	 */
	void searchTextCleared();
}
