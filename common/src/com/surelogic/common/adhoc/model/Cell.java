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
   *          the text to be displayed in this cell. If this parameter is
   *          {@code null} then the empty string is used.
   * @param blankText
   *          {@code true} if the text should be blanked in the user interface,
   *          {@code false} if it should not.
   * @param imageSymbolicName
   *          a symbolic name from {@link CommonImages} or {@code null} if no
   *          image should be displayed for this cell.
   */
  Cell(String text, boolean blankText, String imageSymbolicName) {
    if (text == null)
      f_text = "";
    else
      f_text = text;
    f_blankText = blankText;
    f_imageSymbolicName = imageSymbolicName;
  }

  /**
   * The text to be displayed in this cell.
   */
  private String f_text;

  /**
   * Gets the text to be displayed in this cell.
   * 
   * @return the non-null text to be displayed in this cell.
   */
  public String getText() {
    return f_text;
  }

  /**
   * Should only be called from {@link AdornedTreeTableModel}.
   */
  void setText(String value) {
    f_text = value;
  }

  /**
   * {@code true} if the text should be blanked in the user interface,
   * {@code false} if it should not.
   */
  private final boolean f_blankText;

  /**
   * Gets if the text of this cell should be blanked out in the user interface.
   * 
   * @return {@code true} if the text should be blanked, {@code false}
   *         otherwise.
   */
  public boolean getBlankText() {
    return f_blankText;
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
