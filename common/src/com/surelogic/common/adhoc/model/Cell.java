package com.surelogic.common.adhoc.model;

import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;

/**
 * Represents a cell in an {@link AdornedTreeTableModel}. This implementation of
 * a cell adds a named image for the cell.
 */
public class Cell extends AbstractCell {

  /**
   * Constructs a cell.
   * 
   * @param text
   *          the text to be displayed in this cell. If this parameter is
   *          {@code null} then the empty string is used.
   * @param longValue
   *          the numeric value which started the text before any
   *          transformations. A value of null indicates the text did not start
   *          with a numeric value.
   * @param blankText
   *          {@code true} if the text should be blanked in the user interface,
   *          {@code false} if it should not.
   * @param imageSymbolicName
   *          a symbolic name from {@link CommonImages} or {@code null} if no
   *          image should be displayed for this cell.
   */
  Cell(String text, Long longValue, boolean blankText, String imageSymbolicName) {
    super(text, blankText);
    f_longValue = longValue;
    f_imageSymbolicName = imageSymbolicName;
  }

  /**
   * A symbolic name from {@link CommonImages} or {@code null} if no image
   * should be displayed for this cell.
   */
  @Nullable
  private final String f_imageSymbolicName;

  /**
   * Gets the symbolic name from {@link CommonImages} or {@code null} if no
   * image should be displayed for this cell.
   * 
   * @return a symbolic name from {@link CommonImages} or {@code null} if no
   *         image should be displayed for this cell.
   */
  @Nullable
  public String getImageSymbolicName() {
    return f_imageSymbolicName;
  }

  /**
   * A numeric value which started the text before any transformations. A value
   * of -1 indicates the text did not start with a numeric value.
   */
  private final Long f_longValue;

  /**
   * Gets the numeric value which started the text before any transformations. A
   * value of null indicates the text did not start with a numeric value.
   * 
   * @return the numeric value which started the text before any
   *         transformations. A value of null indicates the text did not start
   *         with a numeric value.
   */
  @Nullable
  public Long getLongValueThatStartsTextOrNull() {
    return f_longValue;
  }
}
