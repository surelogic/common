package com.surelogic.common.adhoc.model;

/**
 * Represents a leaf cell in the tree portion of an
 * {@link AdornedTreeTableModel}.
 * <p>
 * Every leaf cell maps directly to a row in the table representation of an
 * {@link AdornedTreeTableModel}. The index of this row can be obtained via the
 * {@link #getRowIndex()} method.
 */
public final class LeafTreeCell extends TreeCell {

  LeafTreeCell(String text, Long longValue, boolean blankText, String imageSymbolicName, int rowIndex) {
    super(text, longValue, blankText, imageSymbolicName);
    f_rowIndex = rowIndex;
  }

  LeafTreeCell(Cell cell, int rowIndex) {
    this(cell.getText(), cell.getLongValueThatStartsTextOrNull(), cell.getBlankText(), cell.getImageSymbolicName(), rowIndex);
  }

  final int f_rowIndex;

  /**
   * Gets the index of the row in the table representation of an
   * {@link AdornedTreeTableModel} that this leaf cell maps to.
   * 
   * @return the index of the row in the table representation of an
   *         {@link AdornedTreeTableModel} that this leaf cell maps to.
   */
  public int getRowIndex() {
    return f_rowIndex;
  }
}
