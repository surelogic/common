package com.surelogic.common.adhoc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.common.i18n.I18N;

/**
 * Represents a non-leaf cell in the tree portion of an
 * {@link AdornedTreeTableModel}.
 */
public final class NonLeafTreeCell extends TreeCell {

	NonLeafTreeCell(String text, String imageSymbolicName) {
		super(text, imageSymbolicName);
	}

	NonLeafTreeCell(Cell cell) {
		this(cell.getText(), cell.getImageSymbolicName());
	}

	/**
	 * Should only be called from {@link AdornedTreeTableModel}.
	 */
	void addChild(final TreeCell child) {
		if (child == null)
			throw new IllegalArgumentException(I18N.err(44, "child"));
		f_children.add(child);
		child.setParent(this);
	}

	/**
	 * We mutate the children only during the construction of an
	 * {@link AdornedTreeTableModel}.
	 */
	private final List<TreeCell> f_children = new ArrayList<TreeCell>();

	/**
	 * Gets the children of this tree cell or an empty list if the tree cell has
	 * no children.
	 * <p>
	 * The returned list should not be mutated.
	 * 
	 * @return the children of this tree cell or an empty list if the tree cell
	 *         has no children.
	 */
	public List<TreeCell> getChildren() {
		return f_children;
	}

	/**
	 * We mutate the column summaries only during the construction of an
	 * {@link AdornedTreeTableModel}.
	 */
	private List<NonLeafColumnSummary> f_columnSummaries = null;

	/**
	 * Should only be called from {@link AdornedTreeTableModel}.
	 */
	void addColumnSummary(NonLeafColumnSummary columnSummary) {
		assert columnSummary != null;
		if (f_columnSummaries == null) {
			f_columnSummaries = new ArrayList<NonLeafColumnSummary>();
		}
		f_columnSummaries.add(columnSummary);
	}

	/**
	 * Gets the column summaries or an empty list if the tree cell has no column
	 * summaries.
	 * <p>
	 * Each column summary contains the index of the column it should be
	 * displayed in and some text. The indices should refer to columns in the
	 * table portion of the tree-table.
	 * <p>
	 * The returned list should not be mutated.
	 * 
	 * @return the column summaries or an empty list if the tree cell has no
	 *         column summaries.
	 */
	public List<NonLeafColumnSummary> getColumnSummaries() {
		if (f_columnSummaries == null)
			return Collections.emptyList();
		else
			return f_columnSummaries;
	}

	/**
	 * Gets the set of leaf cells below this cell.
	 * 
	 * @return the set of leaf cells below this cell. This set might be empty
	 *         but it will not be {@code null}.
	 */
	public Set<LeafTreeCell> getLeaves() {
		final Set<LeafTreeCell> result = new HashSet<LeafTreeCell>();
		getLeavesHelper(getChildren(), result);
		return result;
	}

	/**
	 * A recursive helper method for {@link #getLeaves()}.
	 * 
	 * @param children
	 *            a set of cells to traverse downward.
	 * @param mutableResult
	 *            collects leaf cells discovered during child traversal.
	 */
	private void getLeavesHelper(List<TreeCell> children,
			Set<LeafTreeCell> mutableResult) {
		if (children == null)
			return;
		for (TreeCell cell : children) {
			if (cell instanceof LeafTreeCell) {
				final LeafTreeCell leaf = (LeafTreeCell) cell;
				mutableResult.add(leaf);
			} else if (cell instanceof NonLeafTreeCell) {
				final NonLeafTreeCell nonLeaf = (NonLeafTreeCell) cell;
				getLeavesHelper(nonLeaf.getChildren(), mutableResult);
			}
		}
	}
}
