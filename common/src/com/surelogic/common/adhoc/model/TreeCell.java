package com.surelogic.common.adhoc.model;

/**
 * Represents a cell in the tree portion of an {@link AdornedTreeTableModel}.
 */
public abstract class TreeCell extends Cell {

  TreeCell(String text, Long longValue, boolean blankText, String imageSymbolicName) {
    super(text, longValue, blankText, imageSymbolicName);
  }

  TreeCell(Cell cell) {
    this(cell.getText(), cell.getLongValueThatStartsTextOrNull(), cell.getBlankText(), cell.getImageSymbolicName());
  }

  /**
   * We mutate the parent reference only during the construction of an
   * {@link AdornedTreeTableModel}.
   */
  private NonLeafTreeCell f_parent;

  /**
   * Should only be called from {@link NonLeafTreeCell#addChild(TreeCell)}.
   */
  protected void setParent(NonLeafTreeCell parent) {
    f_parent = parent;
  }

  /**
   * Gets the parent tree cell of this cell.
   * 
   * @return the parent tree cell of this cell or {@code null} if the cell is at
   *         the root of the tree.
   */
  public final NonLeafTreeCell getParent() {
    return f_parent;
  }

  /**
   * Gets the number of columns filled in this partial or whole row. Cells at
   * the root of the tree return 1 and so on.
   * <p>
   * This value is calculated by walking up the parent pointers until they
   * become {@link null}.
   * 
   * @return the number of columns filled in this partial or whole row.
   */
  public int filledColumnCount() {
    int result = 1;
    NonLeafTreeCell parent = getParent();
    while (parent != null) {
      parent = parent.getParent();
      result++;
    }
    return result;
  }
}
