package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A queryable prepared statement. Arguments passed to call are interpreted as
 * parameters to the statement, and should be of the form specified by
 * {@link JDBCUtils#fill(PreparedStatement, Object[])}
 * 
 * @author nathan
 * 
 * @param <T>
 */
public class QueryablePreparedStatement<T> implements Queryable<T> {

	private final PreparedStatement st;
	private final ResultHandler<T> rh;

	public QueryablePreparedStatement(final PreparedStatement st,
			final ResultHandler<T> rh) {
		this.st = st;
		this.rh = rh;
	}

	public QueryablePreparedStatement(final Connection conn, final String key,
			final ResultHandler<T> rh) {
		try {
			st = conn.prepareStatement(QB.get(key));
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
		this.rh = rh;
	}

	@Override
	public T call(final Object... args) {
		try {
			JDBCUtils.fill(st, args);
			st.execute();
			final ResultSetResult rs = new ResultSetResult(st.getResultSet());
			try {
				return rh.handle(rs);
			} finally {
				rs.close();
			}
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
	}

	@Override
	public void finished() {
		try {
			st.close();
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
	}

}
