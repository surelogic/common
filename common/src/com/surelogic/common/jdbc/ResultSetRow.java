package com.surelogic.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ResultSetRow implements Row {

	private final ResultSet set;
	private int idx;

	ResultSetRow(ResultSet set) {
		this.set = set;
		clear();
	}

	@Override
  public Date nextDate() {
		try {
			return set.getTimestamp(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public int nextInt() {
		try {
			return set.getInt(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public long nextLong() {
		try {
			return set.getLong(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public String nextString() {
		try {
			return set.getString(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public Integer nullableInt() {
		try {
			return JDBCUtils.getNullableInteger(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public Long nullableLong() {
		try {
			return JDBCUtils.getNullableLong(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	void clear() {
		idx = 1;
	}

	@Override
  public boolean nextBoolean() {
		try {
			return JDBCUtils.getBoolean(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public Boolean nullableBoolean() {
		try {
			return JDBCUtils.getNullableBoolean(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	@Override
  public Timestamp nextTimestamp() {
		try {
			return set.getTimestamp(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}
}
