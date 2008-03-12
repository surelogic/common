package com.surelogic.common.jdbc;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import com.surelogic.common.i18n.I18N;

/**
 * This utility implements a bank of queries that are localized to a particular
 * type of database and a qualifier.
 */
public final class QB {

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle(QB.class.getPackage().getName() + ".Query");

	/**
	 * Must be non-null.
	 */
	private static final AtomicReference<DatabaseType> f_databaseType = new AtomicReference<DatabaseType>(
			DatabaseType.derby);

	/**
	 * Gets the database type being used to return queries.
	 * 
	 * @return the database type.
	 */
	public static DatabaseType getDatabaseType() {
		return f_databaseType.get();
	}

	/**
	 * Changes the database type being used to return queries. This call has no
	 * effect if the new value is {@code null}.
	 * 
	 * @param value
	 *            the new database type.
	 */
	public static void setDatabaseType(final DatabaseType value) {
		if (value != null && getDatabaseType() != value) {
			f_databaseType.set(value);
		}
	}

	/**
	 * May be null.
	 */
	private static final AtomicReference<String> f_databaseQualifier = new AtomicReference<String>();

	/**
	 * Gets the database qualifier being used to return queries.
	 * 
	 * @return the database qualifier, may be {@code null}.
	 */
	public static String getDatabaseQualifier() {
		return f_databaseQualifier.get();
	}

	/**
	 * Changes the database qualifier being used to return queries.
	 * 
	 * @param value
	 *            the new database qualifier, may be {@code null}.
	 */
	public static void setDatabaseQualifier(final String value) {
		f_databaseQualifier.set(value);
	}

	/**
	 * Gets the query defined in the query bank for the given key.
	 * <p>
	 * For example, consider the query bank file containing:
	 * 
	 * <pre>
	 * portal.contributions.select=(1)
	 * portal.contributions.update.oracle=(2)
	 * portal.contributions.update.oracle.11=(3)
	 * portal.contributions.update.derby=(4)
	 * </pre>
	 * 
	 * The call {@code QB.get("portal.contributions.select")} will always result
	 * in <tt>(1)</tt> regardless of the current database type and qualifier.
	 * <p>
	 * The call {@code QB.get("portal.contributions.update")} will result in
	 * <tt>(2)</tt> if the database type is {@link DatabaseType#oracle} and
	 * the qualifier is not <tt>11</tt>. The call will result in <tt>(3)</tt>
	 * if the database type is {@link DatabaseType#oracle} and the qualifier is
	 * <tt>11</tt>. The call will result in <tt>(4)</tt> if the database
	 * type is {@link DatabaseType#derby} for any qualifier. The call will throw
	 * an exception if the database type is not {@link DatabaseType#oracle} or
	 * {@link DatabaseType#derby}.
	 * <p>
	 * If the given key is not defined in the query bundle an exception is
	 * thrown.
	 * 
	 * @param key
	 *            the key for the desired query.
	 * @return the query.
	 */
	public static final String get(final String key) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));

		final String type = f_databaseType.get().toString();
		final String qualifier = f_databaseQualifier.get();

		final String keyDotType = key + "." + type;
		if (qualifier != null) {
			final String keyDotTypeDotQualifier = keyDotType + "." + qualifier;
			if (BUNDLE.containsKey(keyDotTypeDotQualifier)) {
				return BUNDLE.getString(keyDotTypeDotQualifier);
			}
		}
		if (BUNDLE.containsKey(keyDotType)) {
			return BUNDLE.getString(keyDotType);
		}
		return BUNDLE.getString(key);
	}

	/**
	 * Gets and formats the query defined in the query bank for the given key.
	 * Calling this method is equivalent to calling
	 * 
	 * <pre>
	 * String.format(QB.get(key), args).
	 * </pre>
	 * 
	 * For example, <tt>QB.get("phone.query", "Tim")</tt> would result in the
	 * string <tt>select phone from folks where name='Tim'</tt> if the
	 * definition
	 * 
	 * <pre>
	 * phone.query=select phone from folks where name='%s'
	 * </pre>
	 * 
	 * is contained in the query bank file.
	 * <p>
	 * If the given key is not defined in the query bundle an exception is
	 * thrown.
	 * 
	 * @param key
	 *            the key for the desired query.
	 * @param args
	 *            the variable arguments to format the resulting query string
	 *            with.
	 * @return the formatted string for the query.
	 * @see String#format(String, Object...)
	 */
	public static final String get(final String key, Object... args) {
		return String.format(I18N.msg(key), args);
	}

	/**
	 * Gets the query defined for the given query number from query bank file.
	 * This method turns the number into a string of the form <tt>query.</tt><i>nnnnn</i>
	 * and passes this generated key to {@link #get(String)}.
	 * <p>
	 * For example, consider the query bank file containing:
	 * 
	 * <pre>
	 * query.00023=(1)
	 * query.00023.oracle=(2)
	 * </pre>
	 * 
	 * The call {@code QB.get(23)} will result in <tt>(2)</tt> if the current
	 * database type is {@link DatabaseType#oracle} and in <tt>(1)</tt>
	 * otherwise.
	 * <p>
	 * If the generated key is not defined in the query bundle an exception is
	 * thrown.
	 * 
	 * @param number
	 *            the query number.
	 * @return the query.
	 */
	public static final String get(final int number) {
		final String key = String.format("query.%05d", number);
		return get(key);
	}

	/**
	 * Gets and formats the query defined in the query bank for the given query
	 * number. Calling this method is equivalent to calling
	 * 
	 * <pre>
	 * String.format(QB.get(number), args).
	 * </pre>
	 * 
	 * For example, <tt>QB.get(61, "Tim")</tt> would result in the string
	 * <tt>select phone from folks where name='Tim'</tt> if the definition
	 * 
	 * <pre>
	 * query.00061=select phone from folks where name='%s'
	 * </pre>
	 * 
	 * is contained in the query bank file.
	 * <p>
	 * If the generated key is not defined in the query bundle an exception is
	 * thrown.
	 * 
	 * @param number
	 *            the query number.
	 * @param args
	 *            the variable arguments to format the resulting query string
	 *            with.
	 * @return the formatted string for the query.
	 * @see String#format(String, Object...)
	 */
	public static final String get(final int number, Object... args) {
		return String.format(I18N.err(number), args);
	}

	private QB() {
		// no instances
	}
}
