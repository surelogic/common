package com.surelogic.common.adhoc.model;

/**
 * This class contains text to display in the table portion of a tree-table for
 * rows where the tree portion is not fully expanded. These summaries are
 * attached to {@link NonLeafTreeCell} instances during construction of a
 * {@link AdornedTreeTableModel}.
 * <p>
 * The column index must refer to a column in the table portion of the
 * tree-table.
 */
public final class NonLeafColumnSummaryCell extends AbstractCell {

  public NonLeafColumnSummaryCell(String text, boolean blankText, int columnIndex) {
    super(text, blankText);
    f_columnIndex = columnIndex;
  }

  /**
   * The index of a column in the table portion of the tree-table.
   */
  private final int f_columnIndex;

  /**
   * Gets the column index where this column summary should be displayed. This
   * index is in the table portion of the tree-table.
   * 
   * @return a column index in the table portion of the tree-table.
   */
  public int getColumnIndex() {
    return f_columnIndex;
  }
}
