package com.surelogic.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class ResultSetResult implements Result {

	private final ResultSet set;
	private final ResultSetRow row;

	ResultSetResult(final ResultSet set) {
		this.set = set;
		this.row = new ResultSetRow(set);
	}

	@Override
  public Iterator<Row> iterator() {
		if (set == null) {
			throw new IllegalArgumentException(
					"Cannot iterate over a null result set.  Please make sure that your query is a SELECT statement.");
		}
		return new Iterator<Row>() {

			private boolean hasNexted = false;
			private boolean hasNext = true;

			@Override
      public boolean hasNext() {
				if (!hasNexted) {
					hasNexted = true;
					try {
						hasNext = set.next();
					} catch (SQLException e) {
						throw new ResultSetException(e);
					}
				}
				return hasNext;
			}

			@Override
      public Row next() {
				if (!hasNexted) {
					try {
						if (!set.next()) {
							throw new NoSuchElementException();
						}
					} catch (SQLException e) {
						throw new ResultSetException(e);
					}
				} else if (!hasNext) {
					throw new NoSuchElementException();
				}
				hasNexted = false;
				row.clear();
				return row;
			}

			@Override
      public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

	void close() throws SQLException {
		if (set != null) {
			set.close();
		}
	}
}
