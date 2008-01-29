package com.surelogic.common.i18n;

import java.util.ResourceBundle;

/**
 * Manages a resource bundle of strings based upon the locale for SureLogic
 * applications. This is a J2SE approach so it is independent of any container
 * or IDE (e.g., Eclipse or NetBeans).
 * <p>
 * The default bundle is read from the <tt>SureLogic.properties</tt> file
 * found in this package.
 * 
 * @author Tim Halloran
 */
public class I18N {

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle(I18N.class.getPackage().getName() + ".SureLogic");

	private static final String ERROR_FORMAT = "(SureLogic #%d) %s";

	/**
	 * Gets the string defined for the given key from the i18n resource bundle.
	 * For example, <tt>I18N.msg("ad-hoc-query.cheer")</tt> would result in
	 * the string <tt>"Queries are great!"</tt> if the definition
	 * 
	 * <pre>
	 * ad-hoc-query.cheer=Queries are great!
	 * </pre>
	 * 
	 * is contained in the SureLogic properties file.
	 * <p>
	 * If the given key is not defined in the SureLogic properties file an
	 * exception is thrown.
	 * 
	 * @param key
	 *            the key for the desired message.
	 * @return the message for the given key.
	 */
	public static final String msg(final String key) {
		final String result = BUNDLE.getString(key);
		return result;
	}

	/**
	 * Gets and formats the string defined for the given key from the i18n
	 * resource bundle. Calling this method is equivalent to calling
	 * 
	 * <pre>
	 * String.format(I18N.msg(key), args).
	 * </pre>
	 * 
	 * For example, <tt>I18N.msg("hello.world", "Tim")</tt> would result in
	 * the string <tt>"Hi Tim!" if the definition
	 * 
	 * <pre>
	 * hello.world=Hi %s!
	 * </pre>
	 * 
	 * is contained in the SureLogic properties file.
	 * 
	 * @param key
	 *            the key for the desired message.
	 * @param args
	 *            the variable arguments to format the resulting message with.
	 * @return the formatted message for the given key.
	 * @see String#format(String, Object...)
	 */
	public static final String msg(final String key, Object... args) {
		return String.format(I18N.msg(key), args);
	}

	/**
	 * Gets the string defined for the given error number from the i18 resource
	 * bundle. The key for the error message in the SureLogic properties file is
	 * <tt>error.</tt><i>nnnnn</i>. For example, <tt>I18N.err(23)</tt>
	 * would result in the string <tt>"(SureLogic #23) A singular problem."</tt>
	 * if the definition
	 * 
	 * <pre>
	 * error.00023=A singular problem.
	 * </pre>
	 * 
	 * is contained in the SureLogic properties file. If the key is not defined
	 * in the SureLogic properties file an exception is thrown.
	 * 
	 * @param number
	 *            the error message number.
	 * @return the error message for the given number.
	 */
	public static final String err(final int number) {
		final String key = String.format("error.%05d", number);
		final String result = BUNDLE.getString(key);
		return String.format(ERROR_FORMAT, number, result);
	}

	/**
	 * Gets and formats the string defined for the given error number from the
	 * i18 resource bundle. Calling this method is equivalent to calling
	 * 
	 * <pre>
	 * String.format(I18N.err(number), args).
	 * </pre>
	 * 
	 * The key for the error message in the SureLogic properties file is
	 * <tt>error.</tt><i>nnnnn</i>. For example,
	 * <tt>I18N.err(24, "bad")</tt> would result in the string
	 * <tt>"(SureLogic #24) A bad problem."</tt> if the definition
	 * 
	 * <pre>
	 * error.00024=A %s problem.
	 * </pre>
	 * 
	 * is contained in the SureLogic properties file. If the key is not defined
	 * in the SureLogic properties file an exception is thrown.
	 * 
	 * @param number
	 *            the error message number.
	 * @param args
	 *            the variable arguments to format the resulting error message
	 *            with.
	 * @return the formatted error message for the given number.
	 * @see String#format(String, Object...)
	 */
	public static final String err(final int number, Object... args) {
		return String.format(I18N.err(number), args);
	}

	private I18N() {
		// no instances
	}
}
