package com.surelogic.common.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * A collection of utility methods specific to SIERRA to help w/ using JDBC.
 * 
 * @author nathan
 * 
 */
public class JDBCUtils {
	/**
	 * Fill the parameters of a {@link PreparedStatement} with the values in
	 * args. Supported types include {@link Integer}, {@link Long},
	 * {@link Boolean}, {@link String}, {@link Date}, and {@link File}.
	 * {@link File} objects are represented as a BLOB. Due to constraints in
	 * JDBC, null values for these types may not be represented as {@code null}.
	 * Instead, use one of the values of {@link Nulls}.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * // Fill a statement with an int, a long, and an empty date
	 * fill(statement, new Object[] { 3, 4L, Nulls.DATE });
	 * 
	 * </pre>
	 * 
	 * @param st
	 * @param args
	 *            the arguments given to the prepared statement
	 * @throws SQLException
	 */
	public static void fill(final PreparedStatement st, final Object[] args)
			throws SQLException {
		int idx = 1;
		for (final Object o : args) {
			if (o == null) {
				throw new IllegalArgumentException(
						"Parameter may not be null.  Use one of the com.surelogic.common.jdbc.Nulls enumerable objects instead.");
			}
			if (o instanceof Nulls) {
				switch ((Nulls) o) {
				case INT:
					setNullableInt(idx, st, null);
					break;
				case LONG:
					setNullableLong(idx, st, null);
					break;
				case STRING:
				case BOOLEAN:
					setNullableString(idx, st, null);
					break;
				case DATE:
					setNullableTimestamp(idx, st, null);
					break;
				case FILE:
					st.setClob(idx, (Clob) null);
					break;
				default:
					break;
				}
			} else if (o instanceof Integer) {
				st.setInt(idx, (Integer) o);
			} else if (o instanceof Long) {
				st.setLong(idx, (Long) o);
			} else if (o instanceof String) {
				st.setString(idx, (String) o);
			} else if (o instanceof Timestamp) {
				st.setTimestamp(idx, (Timestamp) o);
			} else if (o instanceof Date) {
				st.setTimestamp(idx, new Timestamp(((Date) o).getTime()));
			} else if (o instanceof Boolean) {
				st.setString(idx, (Boolean) o ? "Y" : "N");
			} else if (o instanceof File) {
				final File f = (File) o;
				FileInputStream fin;
				try {
					fin = new FileInputStream(f);
				} catch (final FileNotFoundException e) {
					throw new StatementException(e);
				}
				if (f.length() > Integer.MAX_VALUE) {
					throw new IllegalArgumentException(
							"File argument too big: " + f);
				}
				st.setBinaryStream(idx, fin, (int) f.length());
			} else {
				throw new IllegalArgumentException(o.getClass()
						+ " can not be interpreted as an SQL parameter.");
			}
			idx++;
		}
	}

	/**
	 * Set a paramter to the specified String, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param string
	 * @throws SQLException
	 */
	public static void setNullableString(final int idx,
			final PreparedStatement st, final String string)
			throws SQLException {
		if (string == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, string);
		}
	}

	/**
	 * Set a parameter to the String value "Y" or "N", or to null if none is
	 * supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param string
	 * @throws SQLException
	 */
	public static void setNullableBoolean(final int idx,
			final PreparedStatement st, final Boolean bool) throws SQLException {
		if (bool == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, bool ? "Y" : "N");
		}
	}

	/**
	 * Set a paramter to the specified Long, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param longValue
	 * @throws SQLException
	 */
	public static void setNullableLong(final int idx,
			final PreparedStatement st, final Long longValue)
			throws SQLException {
		if (longValue == null) {
			st.setNull(idx, Types.BIGINT);
		} else {
			st.setLong(idx, longValue);
		}
	}

	/**
	 * Set a paramter to the specified Integer, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param intValue
	 * @throws SQLException
	 */
	public static void setNullableInt(final int idx,
			final PreparedStatement st, final Integer intValue)
			throws SQLException {
		if (intValue == null) {
			st.setNull(idx, Types.INTEGER);
		} else {
			st.setLong(idx, intValue);
		}
	}

	/**
	 * Set a parameter to the specified Date, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param dateValue
	 * @throws SQLException
	 */
	public static void setNullableTimestamp(final int idx,
			final PreparedStatement st, final Date dateValue)
			throws SQLException {
		if (dateValue == null) {
			st.setNull(idx, Types.TIMESTAMP);
		} else {
			st.setTimestamp(idx, new Timestamp(dateValue.getTime()));
		}
	}

	/**
	 * Returns a Long, or null if the corresponding column was null in the
	 * result set.
	 * 
	 * @param idx
	 * @param set
	 * @return
	 * @throws SQLException
	 */
	public static Long getNullableLong(final int idx, final ResultSet set)
			throws SQLException {
		final long l = set.getLong(idx);
		if (set.wasNull()) {
			return null;
		} else {
			return l;
		}
	}

	/**
	 * Returns an Integer, or null if the corresponding column was null in the
	 * database.
	 * 
	 * @param idx
	 * @param set
	 * @return
	 * @throws SQLException
	 */
	public static Integer getNullableInteger(final int idx, final ResultSet set)
			throws SQLException {
		final int i = set.getInt(idx);
		if (set.wasNull()) {
			return null;
		} else {
			return i;
		}
	}

	/**
	 * The current system time as a timestamp.
	 * 
	 * @return
	 */
	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Escape a string to be used as input in a JDBC query.
	 * 
	 * @param string
	 * @return
	 */
	public static String escapeString(final String string) {
		return string.replaceAll("'", "''");
	}

	/**
	 * Return the database type, based on what the JDBC metadata reports.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static DBType getDb(final Connection conn) throws SQLException {
		return "Oracle".equals(conn.getMetaData().getDatabaseProductName()) ? DBType.ORACLE
				: DBType.DERBY;
	}

	/**
	 * Returns whether the current connection points to a Sierra server or
	 * client.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static boolean isServer(final Connection conn) throws SQLException {
		try {
			final Statement st = conn.createStatement();
			try {
				st.executeQuery("SELECT UUID FROM SERVER");
				return true;
			} finally {
				st.close();
			}
		} catch (final SQLException e) {
			return false;
		}
	}

	public static boolean getBoolean(final int i, final ResultSet set)
			throws SQLException {
		return set.getString(i).equals("Y");
	}

	public static Boolean getNullableBoolean(final int i, final ResultSet set)
			throws SQLException {
		final String c = set.getString(i);
		return c == null ? null : c.equals("Y");
	}
}
