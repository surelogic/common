package com.surelogic.common.i18n;

import java.util.ResourceBundle;

/**
 * Manages a resource bundle of strings based upon the locale for SureLogic
 * applications. This is a J2SE approach so it is independent of any container
 * or IDE (e.g., Eclipse or NetBeans).
 * <p>
 * The default bundle is read from the <tt>SureLogic.properties</tt> file found
 * in this package.
 * 
 * @author Tim Halloran
 */
public final class I18N {
  private static final ResourceBundle BUNDLE = ResourceBundle
      .getBundle(I18N.class.getPackage().getName() + ".SureLogic");

  private static final ResourceBundle ERRORS = ResourceBundle
      .getBundle(I18N.class.getPackage().getName() + ".SureLogicErrors");

  private static final String ERROR_FORMAT = "(SureLogic #%d) %s";

  private static final ResourceBundle RESULTS = ResourceBundle
      .getBundle(I18N.class.getPackage().getName() + ".SureLogicResults");
  
  
  
  private I18N() {
    // no instances
  }

  
  
  private static String getString(final ResourceBundle bundle,
      final String keyTemplate, final Object... args) {
    return bundle.getString(String.format(keyTemplate, args));
  }

  /**
   * Gets the string defined for the given key from the i18n resource bundle.
   * For example, <tt>I18N.msg("ad-hoc-query.cheer")</tt> would result in the
   * string <tt>"Queries are great!"</tt> if the definition
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
   *          the key for the desired message.
   * @return the message for the given key.
   */
  public static String msg(final String key) {
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
   * For example, <tt>I18N.msg("hello.world", "Tim")</tt> would result in the
   * string <tt>"Hi Tim!" if the definition
   * 
   * <pre>
   * hello.world=Hi %s!
   * </pre>
	 * 
	 * is contained in the SureLogic properties file.
   * 
   * @param key
   *          the key for the desired message.
   * @param args
   *          the variable arguments to format the resulting message with.
   * @return the formatted message for the given key.
   * @see String#format(String, Object...)
   */
  public static String msg(final String key, Object... args) {
    return String.format(I18N.msg(key), args);
  }

  /**
   * Gets the string defined for the given error number from the i18 resource
   * bundle. The key for the error message in the SureLogic properties file is
   * <tt>error.</tt><i>nnnnn</i>. For example, <tt>I18N.err(23)</tt> would
   * result in the string <tt>"(SureLogic #23) A singular problem."</tt> if the
   * definition
   * 
   * <pre>
   * error.00023=A singular problem.
   * </pre>
   * 
   * is contained in the SureLogicErrors properties file. If the key is not
   * defined in the SureLogicErrors properties file an exception is thrown.
   * 
   * @param number
   *          the error message number.
   * @return the error message for the given number.
   */
  public static String err(final int number) {
    final String result = getString(ERRORS, "error.%05d", number);
    return String.format(ERROR_FORMAT, number, result);
  }

  /**
   * Gets and formats the string defined for the given error number from the i18
   * resource bundle. Calling this method is equivalent to calling
   * 
   * <pre>
   * String.format(I18N.err(number), args).
   * </pre>
   * 
   * The key for the error message in the SureLogic properties file is
   * <tt>error.</tt><i>nnnnn</i>. For example, <tt>I18N.err(24, "bad")</tt>
   * would result in the string <tt>"(SureLogic #24) A bad problem."</tt> if the
   * definition
   * 
   * <pre>
   * error.00024=A %s problem.
   * </pre>
   * 
   * is contained in the SureLogicErrors properties file. If the key is not
   * defined in the SureLogicErrors properties file an exception is thrown.
   * 
   * @param number
   *          the error message number.
   * @param args
   *          the variable arguments to format the resulting error message with.
   * @return the formatted error message for the given number.
   * @see String#format(String, Object...)
   */
  public static String err(final int number, Object... args) {
    return String.format(I18N.err(number), args);
  }

  /**
   * Gets the string defined for the given result number from the i18 resource
   * bundle. The key for the result message in the SureLogic properties file is
   * <i>result.nnnnn</i>. For example, <tt>I18N.res(2001)</tt> would result in the
   * string <tt>"A singular problem."</tt> if the definition
   * 
   * <pre>
   * result.02001=A singular problem.
   * </pre>
   * 
   * is contained in the SureLogicResults properties file. If the key is not
   * defined in the SureLogicResults properties file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @return the result message for the given number.
   * 
   * @see #resc(int)
   */
  public static String res(final int number) {
    return getString(RESULTS, "result.%05d", number);
  }

  /**
   * Gets the string defined for the given category number from the i18 resource
   * bundle. The key for the result message in the SureLogic properties file is
   * <i>category.nnnnn</i>. For example, <tt>I18N.category(2001)</tt> would result in the
   * string <tt>"non-trivial effects"</tt> if the definition
   * 
   * <pre>
   * category.02001=non-trivial effects
   * </pre>
   * 
   * is contained in the SureLogicResults properties file. If the key is not
   * defined in the SureLogicResults properties file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @return the result message for the given number.
   * 
   * @see #resc(int)
   */
  public static String category(final int number) {
    return getString(RESULTS, "category.%05d", number);
  }

  /**
   * Gets the string defined for the given category number with the given
   * formatting type from the i18 resource
   * bundle. The key for the result message in the SureLogic properties file is
   * <i>category.formater.nnnnn</i>. For example, <tt>I18N.category(2001, "prefix")</tt> would result in the
   * string <tt>"non-trivial effects"</tt> if the definition
   * 
   * <pre>
   * category.prefix.02001=non-trivial effects
   * </pre>
   * 
   * is contained in the SureLogicResults properties file. If the key is not
   * defined in the SureLogicResults properties file an exception is thrown.
   * @param number
   *          the result message number.
   * 
   * @return the result message for the given number.
   */
  public static String category(final String formatter, final int number) {
    return getString(RESULTS, "category.%s.%05d", formatter, number);
  }
  
  /**
   * Gets and formats the string defined for the given result number from the
   * i18 resource bundle. Calling this method is equivalent to calling
   * 
   * <pre>
   * String.format(I18N.err(number), args).
   * </pre>
   * 
   * The key for the result message in the SureLogic properties file is
   * <i>nnnnn</i>. For example, <tt>I18N.res(456, "inconsistent")</tt> would
   * result in the string <tt>"A inconsistent result."</tt> if the definition
   * 
   * <pre>
   * 00456=A %s result.
   * </pre>
   * 
   * is contained in the SureLogicResults properties file. If the key is not
   * defined in the SureLogic SureLogicResults file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @param args
   *          the variable arguments to format the resulting result message
   *          with.
   * @return the formatted result message for the given number.
   * @see String#format(String, Object...)
   * @see #resc(int, Object...)
   */
  public static String res(final int number, Object... args) {
    return String.format(I18N.res(number), args);
  }

  /**
   * Returns a canonical version of the analysis result. For example, {@code
   * I18N.resc(2001)} will return <tt>"(2001)"</tt>.
   * <p>
   * Throws an exception if a call to {@code I18N.res} is not legal for the
   * passed result message number.
   * 
   * @param number
   *          the result message number.
   * @return a canonical version of the analysis result.
   * @see #res(int)
   */
  public static String resc(final int number) {
    res(number); // toss result, but ensure the call works
    return "(" + number + ")";
  }

  /**
   * Returns a canonical version of the analysis result. For example, {@code
   * I18N.resc(2001, "foo", 5)} will return <tt>"(2001,foo,5)"</tt>. *
   * <p>
   * Throws an exception if a call to {@code I18N.res} is not legal for the
   * passed result message number.
   * 
   * @param number
   *          the result message number.
   * @param args
   *          the variable arguments to format the resulting result message
   *          with.
   * @return a canonical version of the analysis result.
   * @see #res(int, Object...)
   */
  public static String resc(final int number, Object... args) {
    res(number, args); // toss result, but ensure the call works
    final StringBuilder b = new StringBuilder();
    b.append('(').append(number);
    for (Object o : args) {
      b.append(',').append(o);
    }
    b.append(')');
    return b.toString();
  }

  /**
   * Gets a string defined for the given category number from the i18 resource
   * bundle; the string is used for for miscellaneous internal tagging, such as for
   * {@link edu.cmu.cs.fluid.sea.proxy.ResultDropBuilder#addTrustedPromise_or} labels.
   * The key for the result message in the SureLogic properties file is
   * <i>misc.nnnnn</i>. For example, <tt>I18N.misc(2001)</tt> would result in the
   * string <tt>"by effects"</tt> if the definition
   * 
   * <pre>
   * category.02001=by effects
   * </pre>
   * 
   * is contained in the SureLogicResults properties file. If the key is not
   * defined in the SureLogicResults properties file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @return the result message for the given number.
   */
  public static String misc(final int number) {
    return getString(RESULTS, "misc.%05d", number);
  }
}
