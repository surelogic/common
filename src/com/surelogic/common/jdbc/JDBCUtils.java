package com.surelogic.common.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * A collection of utility methods to help w/ using JDBC. (a subset copied from
 * Sierra)
 * 
 * @author nathan
 */
public class JDBCUtils {

	/**
	 * Set a parameter to the specified String, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param string
	 * @throws SQLException
	 */
	public static void setNullableString(final int idx, PreparedStatement st,
			final String string) throws SQLException {
		if (string == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, string);
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
	public static void setNullableLong(final int idx, PreparedStatement st,
			final Long longValue) throws SQLException {
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
	public static void setNullableInt(final int idx, PreparedStatement st,
			final Integer intValue) throws SQLException {
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
			PreparedStatement st, final Date dateValue) throws SQLException {
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
	public static Long getNullableLong(final int idx, ResultSet set)
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
	public static Integer getNullableInteger(final int idx, ResultSet set)
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
	 * @return the string with all ' changed into ''.
	 */
	public static String escapeString(final String string) {
		return string.replaceAll("'", "''");
	}
}
