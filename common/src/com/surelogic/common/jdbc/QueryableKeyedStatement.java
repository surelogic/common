package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A queryable prepared statement that returns the keys generated when called.
 * Arguments passed to call are interpreted as parameters to the statement, and
 * should be of the form specified by
 * {@link JDBCUtils#fill(PreparedStatement, Object[])}
 * 
 * @author nathan
 * 
 * @param <T>
 */
public class QueryableKeyedStatement<T> implements Queryable<T> {

	private final PreparedStatement st;
	private final KeyHandler<T> kh;

	public QueryableKeyedStatement(PreparedStatement st, KeyHandler<T> kh) {
		this.st = st;
		this.kh = kh;
	}

	public QueryableKeyedStatement(Connection conn, String key, KeyHandler<T> kh) {
		try {
			st = conn.prepareStatement(QB.get(key));
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
		this.kh = kh;
	}

	@Override
  public T call(Object... args) {
		try {
			JDBCUtils.fill(st, args);
			st.execute();
			final ResultSet set = st.getGeneratedKeys();
			set.next();
			return kh.handle(new ResultSetRow(set));
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
