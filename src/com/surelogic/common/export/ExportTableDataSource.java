package com.surelogic.common.export;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The abstract base class for defining an table data source for export to a
 * file. Subclasses override all the template methods in this class to become a
 * usable table data source.
 * <p>
 * Classes that implement export formats will typically use this class as shown
 * below.
 * 
 * <pre>
 * ExportTableDataSource source = ...;
 * for (String[] row : source) {
 * 	// do something with the row
 * }
 * </pre>
 */
public abstract class ExportTableDataSource implements Iterable<String[]> {

	/**
	 * Invoked first to allow this data source to configure itself and determine
	 * the number and names of its columns.
	 * 
	 * @return An array of column names. The size of this array determines the
	 *         number of columns being exported.
	 */
	protected abstract String[] init();

	/**
	 * Returns <tt>true</tt> if the table has more rows. In other words,
	 * returns <tt>true</tt> if {@link #nextRow()} would return an element
	 * rather than throwing an exception.
	 * <p>
	 * {@link #init()} will always be called before this method.
	 * 
	 * @return <tt>true</tt> if the table has more rows, <tt>false</tt>
	 *         otherwise.
	 */
	protected abstract boolean hasNextRow();

	/**
	 * Returns the next row in the table.
	 * <p>
	 * {@link #init()} will always be called before this method.
	 * 
	 * @return the next element in the iteration.
	 * @throws NoSuchElementException
	 *             if the table has no more elements. Consistent with the
	 *             semantics described for {@link #hasNextRow()}.
	 */
	protected abstract String[] nextRow() throws NoSuchElementException;

	/**
	 * Invoked last to allow this data source to close any used resources. It
	 * should be allowed that {@link #init()} can be called again. In other
	 * words, this table data source should be able to be iterated through
	 * several times&mdash;not just once.
	 */
	protected abstract void destroy();

	/**
	 * Support for the <tt>foreach</tt> statement.
	 */
	public final Iterator<String[]> iterator() {
		return new Iterator<String[]>() {

			private boolean f_initialized = false;

			public boolean hasNext() {
				if (f_initialized) {
					boolean result = hasNextRow();
					if (!result) {
						destroy();
					}
					return result;
				} else {
					return true;
				}
			}

			public String[] next() {
				if (f_initialized) {
					return next();
				} else {
					f_initialized = true;
					return init();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
