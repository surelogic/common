package com.surelogic.common.adhoc.model;

import com.surelogic.NonNull;

/**
 * Abstract class that describes of a cell in an {@link AdornedTreeTableModel}.
 */
public abstract class AbstractCell {

  /**
   * Constructs a cell with text and a flag to indicate if the text should be
   * blanked in the user interface.
   * 
   * @param text
   *          the text to be displayed in this cell. If this parameter is
   *          {@code null} then the empty string is used.
   * @param blankText
   *          {@code true} if the text should be blanked in the user interface,
   *          {@code false} if it should not.
   */
  AbstractCell(String text, boolean blankText) {
    if (text == null)
      f_text = "";
    else
      f_text = text;
    f_blankText = blankText;
  }

  /**
   * The text to be displayed in this cell.
   */
  @NonNull
  private String f_text;

  /**
   * Gets the text to be displayed in this cell.
   * 
   * @return the non-null text to be displayed in this cell.
   */
  @NonNull
  public final String getText() {
    return f_text;
  }

  /**
   * Should only be called from {@link AdornedTreeTableModel}.
   */
  final void setText(String value) {
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
  public final boolean getBlankText() {
    return f_blankText;
  }

}
