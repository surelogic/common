package com.surelogic.common.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.surelogic.common.i18n.I18N;

public final class ResultSetUtility {
	public static final class Result {
		public final String[] columnLabels;
		public final String[][] rows;
		public final int rowsSkipped;
		
		Result(int numSkipped, String[] labels, String[][] rows) {
			rowsSkipped = numSkipped;
			columnLabels = labels;
			this.rows = rows;
		}
	}
	
	public static Result getResult(final ResultSet rs, final int maxRows) throws SQLException {
		String[] labels = getColumnLabels(rs);
		return getRows(rs, maxRows, labels);
	}
	
	/**
	 * Obtains the column labels from a result set.
	 * <p>
	 * The result set is not closed by this method. Closing the result set is
	 * the responsibility of the caller.
	 * 
	 * @param rs
	 *            a result set.
	 * @return the, possibly empty, column labels.
	 * @throws SQLException
	 *             if something fails while working with the result set.
	 * @throws IllegalArgumentException
	 *             if the result set is null.
	 */
	private static String[] getColumnLabels(final ResultSet rs)
			throws SQLException {
		if (rs == null)
			throw new IllegalArgumentException(I18N.err(44, "rs"));

		final ResultSetMetaData meta = rs.getMetaData();
		final int columnCount = meta.getColumnCount();
		final String[] columnLabels = new String[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			columnLabels[i - 1] = meta.getColumnLabel(i);
		}
		return columnLabels;
	}

	/**
	 * Obtains rows, up to a maximum, from a result set. Some of the references
	 * in the resulting array will be {@link null} if SQL <tt>NULL</tt> was what
	 * was in the database.
	 * <p>
	 * The result set is not closed by this method. Closing the result set is
	 * the responsibility of the caller.
	 * 
	 * @param rs
	 *            a result set.
	 * @param maxRows
	 *            the maximum number of rows to return. A value of 0 indicates
	 *            no maximum.
	 * @return the rows from the result set. Some elements may be {@code null}
	 *         if SQL <tt>NULL</tt> was what was in the database.
	 * @throws SQLException
	 *             if something fails while working with the result set.
	 * @throws IllegalArgumentException
	 *             if the result set is null.
	 */
	public static Result getRows(final ResultSet rs, final int maxRows, final String[] labels)
			throws SQLException {
		if (rs == null)
			throw new IllegalArgumentException(I18N.err(44, "rs"));

		final boolean rowLimit = maxRows > 0;

		final ResultSetMetaData meta = rs.getMetaData();		
		final int columnCount = meta.getColumnCount();
		ArrayList<String[]> rowList = new ArrayList<String[]>();		
		boolean limited = false;
		int numSkipped = 0;
		while (rs.next()) {
			if (!limited) {
				final String[] rowContent = new String[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					rowContent[i - 1] = rs.getString(i);
				}
				rowList.add(rowContent);
				
				if (rowLimit)
					if (rowList.size() >= maxRows) {
						limited = true;						
					}
			} else {
				numSkipped++;
			}
		}
		return new Result(numSkipped, labels, rowList.toArray(new String[rowList.size()][]));
	}

	private ResultSetUtility() {
		// no instances
	}
}
