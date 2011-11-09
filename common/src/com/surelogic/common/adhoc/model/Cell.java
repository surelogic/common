package com.surelogic.common.adhoc.model;

import com.surelogic.common.CommonImages;

/**
 * Represents a cell in an {@link AdornedTreeTableModel}.
 */
public class Cell {

	/**
	 * Constructs a cell.
	 * 
	 * @param text
	 *            the text to be displayed in this cell. If this parameter is
	 *            {@code null} then the empty string is used.
	 * @param imageSymbolicName
	 *            a symbolic name from {@link CommonImages} or {@code null} if
	 *            no image should be displayed for this cell.
	 */
	Cell(String text, String imageSymbolicName) {
		if (text == null)
			f_text = "";
		else
			f_text = text;
		f_imageSymbolicName = imageSymbolicName;
	}

	/**
	 * The text to be displayed in this cell.
	 */
	private final String f_text;

	/**
	 * Gets the text to be displayed in this cell.
	 * 
	 * @return the non-null text to be displayed in this cell.
	 */
	public String getText() {
		return f_text;
	}

	/**
	 * A symbolic name from {@link CommonImages} or {@code null} if no image
	 * should be displayed for this cell.
	 */
	private final String f_imageSymbolicName;

	/**
	 * Gets the symbolic name from {@link CommonImages} or {@code null} if no
	 * image should be displayed for this cell.
	 * 
	 * @return a symbolic name from {@link CommonImages} or {@code null} if no
	 *         image should be displayed for this cell.
	 */
	public String getImageSymbolicName() {
		return f_imageSymbolicName;
	}
}
