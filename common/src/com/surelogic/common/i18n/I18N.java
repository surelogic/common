package com.surelogic.common.i18n;

import java.util.MissingResourceException;
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
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18N.class.getPackage().getName() + ".SureLogic");

  private static final ResourceBundle ERRORS = ResourceBundle.getBundle(I18N.class.getPackage().getName() + ".SureLogicErrors");

  private static final String ERROR_FORMAT = "(SureLogic #%d) %s";

  private static final ResourceBundle RESULTS = ResourceBundle.getBundle(I18N.class.getPackage().getName() + ".SureLogicResults");

  private I18N() {
    // no instances
  }

  private static String getString(final ResourceBundle bundle, final String keyTemplate, final Object... args) {
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
   * is contained in the <tt>SureLogic.properties</tt> file.
   * <p>
   * If the given key is not defined in the <tt>SureLogic.properties</tt> file
   * an exception is thrown.
   * 
   * @param key
   *          the key for the desired message.
   * @return the message for the given key.
   * 
   * @throws MissingResourceException
   *           if the computed key is not found.
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
   * string <tt>"Hi Tim!"</tt> if the definition
   * 
   * <pre>
   * hello.world=Hi %s!
   * </pre>
   * 
   * is contained in the <tt>SureLogic.properties</tt> file.
   * 
   * @param key
   *          the key for the desired message.
   * @param args
   *          the variable arguments to format the resulting message with.
   * @return the formatted message for the given key.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see String#format(String, Object...)
   */
  public static String msg(final String key, Object... args) {
    return String.format(I18N.msg(key), args);
  }

  /**
   * Gets the string defined for the given error number from the i18 resource
   * bundle. The key for the error message in the
   * <tt>SureLogicErrors.properties</tt> file is <tt>error.</tt><i>nnnnn</i>.
   * For example, <tt>I18N.err(23)</tt> would result in the string
   * <tt>"(SureLogic #23) A singular problem."</tt> if the definition
   * 
   * <pre>
   * error.00023=A singular problem.
   * </pre>
   * 
   * is contained in the <tt>SureLogicErrors.properties</tt> file. If the key is
   * not defined in the file an exception is thrown.
   * 
   * @param number
   *          the error message number.
   * @return the error message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
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
   * The key for the error message in the <tt>SureLogicErrors.properties</tt>
   * file is <tt>error.</tt><i>nnnnn</i>. For example,
   * <tt>I18N.err(24, "bad")</tt> would result in the string
   * <tt>"(SureLogic #24) A bad problem."</tt> if the definition
   * 
   * <pre>
   * error.00024=A %s problem.
   * </pre>
   * 
   * is contained in the <tt>SureLogicErrors.properties</tt> file. If the key is
   * not defined in the file an exception is thrown.
   * 
   * @param number
   *          the error message number.
   * @param args
   *          the variable arguments to format the resulting error message with.
   * @return the formatted error message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see String#format(String, Object...)
   */
  public static String err(final int number, Object... args) {
    return String.format(I18N.err(number), args);
  }

  /**
   * Gets the string defined for the given result number from the i18 resource
   * bundle. The key for the result message in the
   * <tt>SureLogicResults.properties</tt> file is <i>result.nnnnn</i>. For
   * example, <tt>I18N.res(2001)</tt> would result in the string
   * <tt>"A singular problem."</tt> if the definition
   * 
   * <pre>
   * result.02001=A singular problem.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @return the result message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see #resc(int)
   */
  public static String res(final int number) {
    return getString(RESULTS, "result.%05d", number);
  }

  /**
   * Gets and formats the string defined for the given result number from the
   * i18 resource bundle. Calling this method is equivalent to calling
   * 
   * <pre>
   * String.format(I18N.res(number), args).
   * </pre>
   * 
   * The key for the result message in the <tt>SureLogicResults.properties</tt>
   * file is <i>result.nnnnn</i>. For example, <tt>I18N.res(456, "inconsistent")</tt>
   * would result in the string <tt>"A inconsistent result."</tt> if the
   * definition
   * 
   * <pre>
   * result.00456=A %s result.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the result message number.
   * @param args
   *          the variable arguments to format the resulting result message
   *          with.
   * @return the formatted result message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see String#format(String, Object...)
   * @see #resc(int, Object...)
   */
  public static String res(final int number, Object... args) {
    return String.format(I18N.res(number), args);
  }

  /**
   * Gets the string defined for the given modeling problem number from the i18 resource
   * bundle. The key for the result message in the
   * <tt>SureLogicResults.properties</tt> file is <i>problem.nnnnn</i>. For
   * example, <tt>I18N.res(2001)</tt> would result in the string
   * <tt>"A singular problem."</tt> if the definition
   * 
   * <pre>
   * problem.02001=A singular problem.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the modeling problem message number.
   * @return the modeling problem message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see #mpc(int)
   */
  public static String mp(final int number) {
    return getString(RESULTS, "problem.%05d", number);
  }

  /**
   * Gets and formats the string defined for the given modeling problem number from the
   * i18 resource bundle. Calling this method is equivalent to calling
   * 
   * <pre>
   * String.format(I18N.mp(number), args).
   * </pre>
   * 
   * The key for the modeling problem message in the <tt>SureLogicResults.properties</tt>
   * file is <i>problem.nnnnn</i>. For example, <tt>I18N.res(456, "inconsistent")</tt>
   * would result in the string <tt>"A inconsistent result."</tt> if the
   * definition
   * 
   * <pre>
   * problem.00456=A %s result.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the modeling problem message number.
   * @param args
   *          the variable arguments to format the resulting modeling problem message
   *          with.
   * @return the formatted modeling problem message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see String#format(String, Object...)
   * @see #mpc(int, Object...)
   */
  public static String mp(final int number, Object... args) {
    return String.format(I18N.mp(number), args);
  }

  /**
   * Returns a canonical version of the analysis result. For example,
   * {@code I18N.resc(2001)} will return <tt>"(2001)"</tt>.
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
   * Returns a canonical version of the analysis result. For example,
   * {@code I18N.resc(2001, "foo", 5)} will return <tt>"(2001,foo,5)"</tt>. 
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
   * Returns a canonical version of the analysis modeling problem. For example,
   * {@code I18N.mpc(2001)} will return <tt>"(2001)"</tt>.
   * <p>
   * Throws an exception if a call to {@code I18N.mp} is not legal for the
   * passed modeling problem message number.
   * 
   * @param number
   *          the modeling problem message number.
   * @return a canonical version of the analysis modeling problem.
   * @see #res(int)
   */
  public static String mpc(final int number) {
    mp(number); // toss result, but ensure the call works
    return "(" + number + ")";
  }

  /**
   * Returns a canonical version of the analysis modeling problem. For example,
   * {@code I18N.mpc(2001, "foo", 5)} will return <tt>"(2001,foo,5)"</tt>. 
   * <p>
   * Throws an exception if a call to {@code I18N.mp} is not legal for the
   * passed modeling problem message number.
   * 
   * @param number
   *          the modeling problem message number.
   * @param args
   *          the variable arguments to format the resulting modeling problem message
   *          with.
   * @return a canonical version of the analysis modeling problem.
   * @see #res(int, Object...)
   */
  public static String mpc(final int number, Object... args) {
    mp(number, args); // toss result, but ensure the call works
    final StringBuilder b = new StringBuilder();
    b.append('(').append(number);
    for (Object o : args) {
      b.append(',').append(o);
    }
    b.append(')');
    return b.toString();
  }

  /**
   * Gets the string defined for the given category number from the i18 resource
   * bundle. The key for the result message in the
   * <tt>SureLogicResults.properties</tt> file is <i>category.nnnnn</i>. For
   * example, <tt>I18N.res(2001)</tt> would result in the string
   * <tt>"A singular problem."</tt> if the definition
   * 
   * <pre>
   * category.02001=A singular problem.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the category message number.
   * @return the category message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see #resc(int)
   */
  public static String cat(final int number) {
    return getString(RESULTS, "category.%05d", number);
  }

  /**
   * Gets and formats the string defined for the given category number from the
   * i18 resource bundle. Calling this method is equivalent to calling
   * 
   * <pre>
   * String.format(I18N.cat(number), args).
   * </pre>
   * 
   * The key for the category message in the
   * <tt>SureLogicResults.properties</tt> file is <i>nnnnn</i>. For example,
   * <tt>I18N.cat(456, "inconsistent")</tt> would result in the string
   * <tt>"A inconsistent result."</tt> if the definition
   * 
   * <pre>
   * category.00456=A %s result.
   * </pre>
   * 
   * is contained in the <tt>SureLogicResults.properties</tt> file. If the key
   * is not defined in the file an exception is thrown.
   * 
   * @param number
   *          the category message number.
   * @param args
   *          the variable arguments to format the resulting category message
   *          with.
   * @return the formatted category message for the given number.
   * @throws MissingResourceException
   *           if the computed key is not found.
   * @see String#format(String, Object...)
   * @see #resc(int, Object...)
   */
  public static String cat(final int number, Object... args) {
    return String.format(I18N.res(number), args);
  }

  private static final String OPAR = "{{{";
  private static final String SEP = "|||";
  private static final String CPAR = "}}}";
  private static final String NUM = "###";

  /**
   * 
   * For categorizing messages and analysis results folders special processing
   * is done on the string just before it is displayed in the user interface. It
   * occurs after the normal String.format substitution of arguments into the
   * format string. Special processing is based upon the number of children,
   * <i>c</i>, of the folder or category.
   * <ol>
   * <li><tt>"{{{one|||many}}}"</tt> if <i>c</i> == 1 this results in
   * <tt>"one"</tt>, if <i>c</i> > 1 this results in <tt>"many"</tt>.
   * <li><tt>"###"</tt> is changed to <i>c</i>.
   * </ol>
   * 
   * <h3>Examples:</h3>
   * 
   * <tt>"### java.lang.Thread subtype instance {{{created|||creations}}}"</tt><br>
   * when <i>c</i> = 3 becomes
   * <tt>"3 java.lang.Thread subtype instance creations"</tt><br>
   * when <i>c</i> = 1 becomes
   * <tt>"1 java.lang.Thread subtype instance created"</tt>
   * <p>
   * <tt>"Concurrency ({{{one issue|||### issues}}})"</tt><br>
   * when <i>c</i> = 1 "Concurrency (one issue)"</tt><br>
   * when <i>c</i> = 50 "Concurrency (50 issues)"</tt>
   * <p>
   * <tt>"### java.lang.Runnable subtype instance {{{created|||creations}}} - not{{{ a|||}}} Thread{{{|||s}}}"</tt>
   * <br>
   * when <i>c</i> = 1
   * <tt>"1 java.lang.Runnable subtype instance created - not a Thread"</tt><br>
   * when <i>c</i> = 2
   * <tt>"2 java.lang.Runnable subtype instance creations - not Threads"</tt>
   * <br>
   * 
   * @param s
   *          the string to process.
   * @param count
   *          how many children are in the container.
   * @return the resulting string.
   */
  public static String toStringForUIFolderLabel(final String s, final int count) {
    if (s == null)
      return null;
    final boolean single = count < 2;
    final StringBuilder b = new StringBuilder(s);
    while (singlePluralHelper(single, b)) {
      // loop until it returns false
    }
    while (numberHelper(b, count)) {
      // loop until it returns false
    }
    return b.toString();
  }

  private static final boolean singlePluralHelper(final boolean single, final StringBuilder b) {
    final int so = b.indexOf(OPAR);
    int ss = b.indexOf(SEP);
    int sc = b.indexOf(CPAR);
    if (so == -1 || sc == -1 || ss == -1)
      return false;
    // make sure they are in order or we ignore
    final boolean inorder = so < ss && ss < sc;
    if (!inorder)
      return false;

    if (single) {
      b.delete(so, so + OPAR.length());
      // adjust index to delete
      final int deletedCharCount = OPAR.length();
      ss -= deletedCharCount;
      sc -= deletedCharCount;
      b.delete(ss, sc + CPAR.length());
    } else {
      b.delete(so, ss + SEP.length());
      // adjust index to delete
      final int deletedCharCount = ss + SEP.length() - so;
      sc -= deletedCharCount;
      b.delete(sc, sc + CPAR.length());
    }
    return true;
  }

  private static final boolean numberHelper(final StringBuilder b, int count) {
    final int start = b.indexOf(NUM);
    if (start == -1)
      return false;
    b.replace(start, start + NUM.length(), Integer.toString(count));
    return true;
  }
}
