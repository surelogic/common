package com.surelogic.common.ui;

import java.util.ArrayList;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.surelogic.NonNull;
import com.surelogic.Utility;

@Utility
public final class TreeUtility {

  private TreeUtility() {
    // no instances
  }

  /**
   * Gets all the tree items in a tree. This is done recursively beginning at
   * the root item of the tree.
   * 
   * @param tree
   *          an SWT Tree widget.
   * @return the collection of items in the passed tree.
   */
  @NonNull
  public static ArrayList<TreeItem> getTreeItemsDeep(final Tree tree) {
    final ArrayList<TreeItem> result = new ArrayList<TreeItem>();
    for (TreeItem ti : tree.getItems()) {
      getItems_Helper(ti, result);
    }
    return result;
  }

  /**
   * Expands all elements in an SWT Tree widget.
   * 
   * @param tree
   *          an SWT Tree widget.
   */
  public static void expandTreeDeep(final Tree tree) {
    setTreeExpanded_Internal(tree, getTreeItemsDeep(tree), true);
  }

  /**
   * Collapses all elements in an SWT Tree widget.
   * 
   * @param tree
   *          an SWT Tree widget.
   */
  public static void collapseTreeDeep(final Tree tree) {
    setTreeExpanded_Internal(tree, getTreeItemsDeep(tree), false);
  }

  /**
   * Packs the tree columns to the correct width so that the data will fit when
   * the tree is expanded.
   * <p>
   * The current expansion state of the tree is saved. The tree is fully
   * expanded. The columns are then sized to the fully-expanded data. Then the
   * table is fully collapsed. And the original expansion state is restored.
   * 
   * @param tree
   *          an SWT Tree widget.
   */
  public static void packColumnsForExpansion(final Tree tree) {
    final ArrayList<TreeItem> items = getTreeItemsDeep(tree);
    final ArrayList<TreeItem> expandedItems = getExpansionState_Internal(tree, items);
    setTreeExpanded_Internal(tree, items, true); // expandAll
    packColumns(tree);
    setTreeExpanded_Internal(tree, items, false); // collapseAll
    setExpansionState_Internal(tree, expandedItems);
  }

  /**
   * Pack the columns of the passed SWT Tree to the ideal width.
   * <p>
   * Note that this is a simple pack of the current expansion state. Consider
   * using {@link #packColumnsForExpansion(Tree)} if you want the column widths
   * to be okay as the tree is expanded.
   * 
   * @param tree
   *          an SWT Tree widget.
   */
  public static void packColumns(final Tree tree) {
    boolean first = true;
    for (final TreeColumn col : tree.getColumns()) {
      col.pack();
      if (first) {
        first = false;
        if (SystemUtils.IS_OS_LINUX) {
          col.setWidth(col.getWidth() + 30);
        }
      }
    }
  }

  /*
   * These internal methods avoid having to make a new array of items if the
   * call makes multiple uses of the items without mutation.
   */

  private static void setTreeExpanded_Internal(final Tree tree, final ArrayList<TreeItem> items, boolean setExpanded) {
    for (final TreeItem ti : items) {
      ti.setExpanded(setExpanded);
    }
  }

  private static ArrayList<TreeItem> getExpansionState_Internal(final Tree tree, final ArrayList<TreeItem> items) {
    final ArrayList<TreeItem> result = new ArrayList<TreeItem>();
    for (final TreeItem ti : items) {
      System.out.println(ti.getText());
      if (ti.getExpanded()) {
        result.add(ti);
        System.out.println("(expanded) " + ti.getText());
      }
    }
    System.out.println(result);
    return result;
  }

  private static void setExpansionState_Internal(final Tree tree, final ArrayList<TreeItem> expandedItems) {
    for (final TreeItem ti : expandedItems) {
      ti.setExpanded(true);
    }
  }

  private static void getItems_Helper(final TreeItem item, final ArrayList<TreeItem> mutableItems) {
    mutableItems.add(item);
    for (TreeItem ti : item.getItems()) {
      getItems_Helper(ti, mutableItems);
    }
  }
}
