package com.surelogic.common.jdbc;

import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This utility implements a bank of queries that are localized to a particular
 * type of database and a qualifier.
 */
public final class QB {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(QB.class.getPackage().getName() + ".Query");

  /**
   * Must be non-null.
   */
  private static final AtomicReference<DatabaseType> f_databaseType = new AtomicReference<>(DatabaseType.derby);

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
   *          the new database type.
   */
  public static void setDatabaseType(final DatabaseType value) {
    if (value != null && getDatabaseType() != value) {
      f_databaseType.set(value);
    }
  }

  /**
   * May be null.
   */
  private static final AtomicReference<String> f_databaseQualifier = new AtomicReference<>();

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
   *          the new database qualifier, may be {@code null}.
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
   * <tt>(2)</tt> if the database type is {@link DatabaseType#oracle} and the
   * qualifier is not <tt>11</tt>. The call will result in <tt>(3)</tt> if the
   * database type is {@link DatabaseType#oracle} and the qualifier is
   * <tt>11</tt>. The call will result in <tt>(4)</tt> if the database type is
   * {@link DatabaseType#derby} for any qualifier. The call will throw an
   * exception if the database type is not {@link DatabaseType#oracle} or
   * {@link DatabaseType#derby}.
   * <p>
   * If the given key is not defined in the query bundle an exception is thrown.
   * 
   * @param key
   *          the key for the desired query.
   * @return the query.
   */
  public static String get(final String key) {
    final String result = getQueryString(key);
    final Logger log = SLLogger.getLogger();
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, I18N.msg("db.QB.get", key, result));
    }
    return result;
  }

  /**
   * This method basically implements {@link #get(String)}. It doesn't, however,
   * log its result.
   * 
   * @see #get(String)
   */
  private static String getQueryString(final String key) {
    if (key == null)
      throw new IllegalArgumentException(I18N.err(44, "key"));

    final String type = f_databaseType.get().toString();
    final String qualifier = f_databaseQualifier.get();

    final String keyDotType = key + "." + type;
    if (qualifier != null) {
      final String keyDotTypeDotQualifier = keyDotType + "." + qualifier;
      final String result = getStringOrNull(keyDotTypeDotQualifier);
      if (result != null)
        return result;
    }
    final String result = getStringOrNull(keyDotType);
    if (result != null)
      return result;
    else
      return BUNDLE.getString(key);
  }

  private static final AtomicBoolean f_firstGetStringOrNullCall = new AtomicBoolean(true);

  /**
   * A handle to the containsKey method in the {@link ResourceBundle} class.
   * This method was added in Java 6 and may or may not exist depending upon the
   * VM we are running on.
   */
  private static final AtomicReference<Method> f_containsKey = new AtomicReference<>(null);

  /**
   * Looks up a key in the bundle, but returns {@code null} rather than throwing
   * an exception if the key doesn't exist.
   * <p>
   * <i>Implementation Note:</i> This method tries, via reflection, to use the
   * Java 6 containsKey method on the {@link ResourceBundle} class, if it can't
   * find it it defaults to a try-catch block strategy.
   * 
   * @param key
   *          the key.
   * @return the string for the given key.
   */
  private static String getStringOrNull(final String key) {
    if (f_firstGetStringOrNullCall.getAndSet(false)) {
      try {
        final Method containsKey = BUNDLE.getClass().getMethod("containsKey", String.class);
        f_containsKey.set(containsKey);
      } catch (Exception e) {
        // no luck, have to use the exception approach
      }
    }
    final Method containsKey = f_containsKey.get();
    if (containsKey != null) {
      /*
       * We can use the containsKey method added in Java 6.
       */
      try {
        final Object result = containsKey.invoke(BUNDLE, key);
        if (result instanceof Boolean) {
          final Boolean booleanResult = (Boolean) result;
          if (booleanResult.booleanValue()) {
            return BUNDLE.getString(key);
          } else {
            return null;
          }
        }
      } catch (Exception e) {
        // Should not happen
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(90));
      }
    }
    /*
     * Fall back on using a try-catch block.
     */
    try {
      return BUNDLE.getString(key);
    } catch (MissingResourceException e) {
    }
    return null;
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
   * string <tt>select phone from folks where name='Tim'</tt> if the definition
   * 
   * <pre>
   * phone.query=select phone from folks where name='%s'
   * </pre>
   * 
   * is contained in the query bank file.
   * <p>
   * If the given key is not defined in the query bundle an exception is thrown.
   * 
   * @param key
   *          the key for the desired query.
   * @param args
   *          the variable arguments to format the resulting query string with.
   * @return the formatted string for the query.
   * @see String#format(String, Object...)
   */
  public static String get(final String key, Object... args) {
    final String result = String.format(getQueryString(key), args);
    final Logger log = SLLogger.getLogger();
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, I18N.msg("db.QB.get", key, result));
    }
    return result;
  }

  /**
   * Gets the query defined for the given query number from query bank file.
   * This method turns the number into a string of the form <tt>query.</tt>
   * <i>nnnnn</i> and passes this generated key to {@link #get(String)}.
   * <p>
   * For example, consider the query bank file containing:
   * 
   * <pre>
   * query.00023=(1)
   * query.00023.oracle=(2)
   * </pre>
   * 
   * The call {@code QB.get(23)} will result in <tt>(2)</tt> if the current
   * database type is {@link DatabaseType#oracle} and in <tt>(1)</tt> otherwise.
   * <p>
   * If the generated key is not defined in the query bundle an exception is
   * thrown.
   * 
   * @param number
   *          the query number.
   * @return the query.
   */
  public static String get(final int number) {
    final String key = numberToKey(number);
    final String result = getQueryString(key);
    final Logger log = SLLogger.getLogger();
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, I18N.msg("db.QB.getNumber", number, result));
    }
    return result;
  }

  private static String numberToKey(final int number) {
    return String.format("query.%05d", number);
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
   *          the query number.
   * @param args
   *          the variable arguments to format the resulting query string with.
   * @return the formatted string for the query.
   * @see String#format(String, Object...)
   */
  public static String get(final int number, Object... args) {
    final String key = numberToKey(number);
    final String result = String.format(getQueryString(key), args);
    final Logger log = SLLogger.getLogger();
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, I18N.msg("db.QB.getNumber", number, result));
    }
    return result;
  }

  private QB() {
    // no instances
  }
}
