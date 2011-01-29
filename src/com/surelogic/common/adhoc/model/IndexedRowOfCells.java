package com.surelogic.common.adhoc.model;

import java.util.LinkedList;

import com.surelogic.common.i18n.I18N;

/**
 * This class represents a row of cells that is indexed with an integer. It is
 * is a helper class for building {@link AdornedTreeTableModel} and is not
 * intended for other purposes.
 */
class IndexedRowOfCells {

	/**
	 * Constructs a linked list of indexed rows of cells. The index is the array
	 * index from the passed rows.
	 * 
	 * @param rows
	 *            a list of rows of cells.
	 * @return a linked list of rows of cells indexed by the passed array
	 *         indices.
	 */
	static LinkedList<IndexedRowOfCells> toList(Cell[][] rows) {
		final LinkedList<IndexedRowOfCells> result = new LinkedList<IndexedRowOfCells>();
		for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
			final Cell[] row = rows[rowIndex];
			result.add(new IndexedRowOfCells(rowIndex, row));
		}
		return result;
	}

	private final int f_rowIndex;

	/**
	 * Gets the index for this row of cells.
	 * 
	 * @return the index for this row of cells.
	 */
	int getRowIndex() {
		return f_rowIndex;
	}

	private final Cell[] f_row;

	/**
	 * Gets the contents of this row.
	 * 
	 * @return the contents of this row.
	 */
	Cell[] getRow() {
		return f_row;
	}

	private IndexedRowOfCells(final int rowIndex, final Cell[] row) {
		f_rowIndex = rowIndex;
		if (row == null)
			throw new IllegalArgumentException(I18N.err(44, "row"));
		f_row = row;
	}
}
