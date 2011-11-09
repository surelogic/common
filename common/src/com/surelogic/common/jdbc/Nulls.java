package com.surelogic.common.jdbc;

import java.io.File;
import java.util.Date;

/**
 * Represents {@code null} values for common JDBC types
 * 
 * @author nathan
 * 
 */
public enum Nulls {
	INT, LONG, STRING, DATE, BOOLEAN, FILE;

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final Integer i) {
		return i == null ? INT : i;
	}

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final Long i) {
		return i == null ? LONG : i;
	}

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final String i) {
		return i == null ? STRING : i;
	}

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final Date i) {
		return i == null ? DATE : i;
	}

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final Boolean i) {
		return i == null ? BOOLEAN : i;
	}

	/**
	 * Return a Nulls value of the appropriate type if null.
	 * 
	 * @param i
	 * @return
	 */
	public static Object coerce(final File f) {
		return f == null ? FILE : f;
	}
}
