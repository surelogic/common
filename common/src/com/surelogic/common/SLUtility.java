package com.surelogic.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * A utility with SureLogic common code.
 */
public final class SLUtility {
  public static final boolean is64bit = SystemUtils.OS_ARCH.indexOf("64") >= 0;

  public static final String COMMON_PLUGIN_ID = "com.surelogic.common";

  /**
   * This defines the server to get to the serviceability servlets on the
   * SureLogic website. Normally the default of <tt>http://surelogic.com</tt> is
   * used , but for testing a system property can be set
   * <code>-Dserviceability.url=http://test.com:8080</code>
   */
  public static final String SERVICEABILITY_SERVER = System.getProperty("serviceability.url", "http://surelogic.com");

  /**
   * This defines the SureLogic email address used for the serviceability
   * servlets on the SureLogic website. Normally the default of
   * <tt>support@surelogic.com</tt> is used , but for testing a system property
   * can be set <code>-Dserviceability.email=tim@myserver.com</code>
   */
  public static final String SERVICEABILITY_EMAIL = System.getProperty("serviceability.email", "support@surelogic.com");

  /**
   * This is a very JDT friendly constant&mdash;many Eclipse methods recognize
   * this particular name.
   */
  public static final String JAVA_DEFAULT_PACKAGE = "(default package)";
  public static final String UNKNOWN_PROJECT = "(unknown project)";
  public static final String LIBRARY_PROJECT = "(standard library)";
  public static final String PACKAGE_INFO = "package-info";
  public static final String PACKAGE_INFO_JAVA = SLUtility.PACKAGE_INFO + ".java";
  public static final String UTF8 = "UTF8";
  public static final String ENCODING = "UTF-8";
  public static final String AMPERSAND = "&amp;";
  public static final String APOSTROPHE = "&apos;";
  public static final String DOUBLE_QUOTE = "&quot;";
  public static final String GREATER_THAN = "&gt;";
  public static final String LESS_THAN = "&lt;";
  public static final String INDENT = "  ";
  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final String ECLIPSE_MARKER_TYPE_NAME = "com.surelogic.marker";
  public static final String PLATFORM_LINE_SEPARATOR = String.format("%n");
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final String JAVADOC_ANNOTATE_TAG = "annotate";

  public static final String SURELOGIC_ANNOTATION_PACKAGE = "com.surelogic";

  public static final String OBJECT = "Object";
  public static final String JAVA_LANG_OBJECT = "java.lang.Object";
  public static final String JAVA_LANG = "java.lang";

  public static final String SLASH_STAR_COMMENT_START = "/*";
  public static final String SLASH_STAR_COMMENT_END = "*/";

  public static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
  public static final String ANDROID_NATURE = "com.android.ide.eclipse.adt.AndroidNature";

  public static final String VIEW_PERSISTENCE_PREFIX = "view-state_";
  public static final String DOT_XML = ".xml";

  public static final String SL_TOOLS_PROPS_FILE = "surelogic-tools.properties";

  public static final String LOG_NAME = "log.txt";

  public static final String DERBY_LOG_NAME = ".surelogic-derby-log.txt";

  /*
   * Constants for special ad hoc query meta variables
   */
  public static final String ADHOC_META_ROW = "(meta-row)";
  public static final String ADHOC_META_PARTIAL_ROW = "(meta-partial-row)";

  public static final String ADHOC_META_VALUE = "defined";

  /**
   * The first install of a perpetual license is shorter than all other renewals
   * so that we get quick feedback and an indication if the license was
   * abandoned (no renewal).
   */
  public static int DURATION_IN_DAYS_OF_PERPETUAL_LICENSE_FIRST_INSTALL = 30; // days

  /**
   * This method returns a list of the string version of all non-loopback,
   * non-system hardware addresses on the computer. There might be more than on,
   * for example, if the computer has both wired and wireless network
   * capability.
   * <p>
   * For example, my desktop returns
   * <tt>[60-a4-4c-61-20-40, 08-00-27-00-68-fb]</tt> if <tt>toString()</tt> is
   * invoked on the result of this method.
   * 
   * @return a set of the mac addresses used by the computer the method is
   *         invoked on. The list may be empty.
   */
  @NonNull
  public static ImmutableSet<String> getMacAddressesOfThisMachine() {
    final HashSet<String> result = new HashSet<>();
    try {
      for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
        final NetworkInterface ni = e.nextElement();
        if (ni.isLoopback())
          continue;

        @Nullable
        final byte[] mac = ni.getHardwareAddress();
        if (mac == null)
          continue;
        if (mac.length == 8) {
          // check for odd Microsoft-used mac addresses (zeros ended by 0xe0)
          boolean zeroEzero = mac[0] == 0 && mac[1] == 0 && mac[2] == 0 && mac[03] == 0 && mac[4] == 0 && mac[5] == 0 && mac[6] == 0
              && mac[7] == (byte) 0xe0;
          if (zeroEzero)
            continue;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
          sb.append(String.format("%02x%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        result.add(sb.toString());
      }
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.WARNING, "Failure obtaining MAC addresses of this machine", e);
    }
    return ImmutableSet.copyOf(result);
  }

  /**
   * This is a hack to remove the package from type names in the
   * <tt>java.lang</tt> package, but not other packages (including nested ones).
   * 
   * @param typeName
   *          a fully-qualified type name.
   * @return the passed type name or the simple type if the passed type name is
   *         in the package <tt>java.lang</tt>
   */
  public static String unqualifyTypeNameInJavaLang(final String typeName) {
    if (typeName == null)
      throw new IllegalArgumentException(I18N.err(44, "typeName"));
    if (typeName.startsWith(JAVA_LANG)) {
      if (typeName.equals(JAVA_LANG))
        return typeName;
      /*
       * Check if this is a type in a nested package, e.g.,
       * java.lang.annotation.SuppressWarning
       */
      final String proposedToReturn = typeName.substring(JAVA_LANG.length() + 1);
      if (proposedToReturn.indexOf('.') == -1)
        return proposedToReturn;
      else
        return typeName;

    } else
      return typeName;
  }

  /**
   * Removes defined ad hoc query meta variables, if any, from the passed map.
   * 
   * @param mutableMap
   *          a mutable map of ad hoc variables.
   */
  public static <T> void removeAdHocQueryMetaVariablesFrom(final Map<String, T> mutableMap) {
    if (mutableMap != null) {
      mutableMap.remove(ADHOC_META_ROW);
      mutableMap.remove(ADHOC_META_PARTIAL_ROW);
    }
  }

  /**
   * Removes any entry from the passed map that has an empty string or
   * {@code null} as its value. Optionally, {@link String#trim()} can be invoked
   * on the value before it is checked if it is equal to the empty string.
   * 
   * @param mutableMap
   *          a map
   * @param trimValue
   *          {@code true} if {@link String#trim()} should be invoked on the map
   *          entry's value prior to being checked if it is equal to the empty
   *          string.
   */
  public static <T> void removeEmptyStringValuesFromMap(final Map<T, String> mutableMap, final boolean trimValue) {
    for (Iterator<Map.Entry<T, String>> iterator = mutableMap.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry<T, String> entry = iterator.next();
      String value = entry.getValue();
      if (value == null) {
        iterator.remove();
      } else {
        if (trimValue)
          value = value.trim();
        if ("".equals(value))
          iterator.remove();
      }
    }
  }

  /**
   * Extracts text from a comment block that starts with a whole-line comment
   * delimiter, such a <tt>--</tt> or </tt>//</tt>. This doesn't handle
   * parenthetical comments and lines in the passed text that do not contain the
   * delimiter are not processed in the output block.
   * <p>
   * Note the examples below use <tt>--</tt> as the whole-line comment
   * delimiter.
   * <p>
   * Only the first delimiter per line is removed and all text prior to it is
   * ignored. Thus, " <tt>fff -- my--dog</tt> " would result in "
   * <tt> my--dog</tt>".
   * <p>
   * Lines in the passed text that do not contain the delimiter are not
   * processed in the output block. Thus
   * 
   * <pre>
   * -- My work
   * select from dual
   * -- is done
   * </pre>
   * 
   * would result in "<tt> My work\n is done</tt>".
   * 
   * @param text
   *          the comment block.
   * @param commentDelimiter
   *          a whole-line comment delimiter.
   * @return the block with the whole-line comment delimiters removed.
   */
  public static String extractTextFromWholeLineCommentBlock(final String text, final String commentDelimiter) {
    if (text == null)
      throw new IllegalArgumentException(I18N.err(44, "text"));
    if (commentDelimiter == null)
      throw new IllegalArgumentException(I18N.err(44, "commentDelimiter"));

    final StringBuilder b = new StringBuilder();
    final BufferedReader r = new BufferedReader(new StringReader(text));

    String line;
    try {
      while ((line = r.readLine()) != null) {
        int comment = line.indexOf(commentDelimiter);
        if (comment != -1) {
          b.append(line.substring(comment + commentDelimiter.length()));
          b.append('\n');
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return b.toString();
  }

  /**
   * Checks if a string is both non-null and not the empty string.
   * 
   * @param value
   *          a string.
   * @return {@code true} if the passed string is both non-null and not the
   *         empty string.
   */
  public static boolean isNotEmptyOrNull(String value) {
    if (value == null)
      return false;
    if ("".equals(value))
      return false;
    return true;
  }

  /**
   * Checks if the passed string is a valid Java identifier. Handles UNICODE
   * names.
   * 
   * @param value
   *          an identifier.
   * @return {@code true} if the if the passed string is valid, {@code false}
   *         otherwise.
   */
  public static boolean isValidJavaIdentifier(final String value) {
    if (value == null)
      return false;
    if (value.length() == 0)
      return false;
    if (!Character.isJavaIdentifierStart(value.charAt(0)))
      return false;
    for (int i = 1; i < value.length(); i++) {
      if (!Character.isJavaIdentifierPart(value.charAt(i)))
        return false;
    }
    return true;
  }

  /**
   * Checks if the passed string is series of <tt>"."</tt> separated valid Java
   * identifiers.
   * <p>
   * Examples that would return {@code true}: <tt>java.lang</tt>,
   * <tt>java.util</tt>, <tt>Map.Entry</tt>, <tt>java.util.concurrent.locks</tt>
   * , <tt>ClassInDefaultPkg</tt>, <tt>edu.afit.smallworld</tt>
   * 
   * @param value
   *          a series of dot-separated Java identifiers.
   * @return {@code true} if the if the passed string is valid, {@code false}
   *         otherwise.
   */
  public static boolean isValidDotSeparatedJavaIdentifier(final String value) {
    int lastDotIndex = 0;
    while (true) {
      int dotIndex = value.indexOf('.', lastDotIndex);
      if (dotIndex == -1) {
        final String id = value.substring(lastDotIndex);
        if (!isValidJavaIdentifier(id))
          return false;
        break;
      } else {
        String id = value.substring(lastDotIndex, dotIndex);
        if (!isValidJavaIdentifier(id))
          return false;
      }
      lastDotIndex = dotIndex + 1;
    }
    return true;
  }

  /**
   * Checks if the passed string is a valid fully-qualified Java type name in a
   * particular SureLogic format. Nested package names are separated by
   * <tt>"."</tt>, the package name is separated from the type name by a "/",
   * and nested type names are separated by <tt>"."</tt>. The "/" must always
   * appear&mdash;even if the type is in the default package or just a package
   * name is being returned.
   * <p>
   * Examples that would return {@code true}: <tt>java.lang/Object</tt>,
   * <tt>java.util/Map.Entry</tt>,
   * <tt>java.util.concurrent.locks/ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>/ClassInDefaultPkg</tt>, <tt>org.apache/</tt>, <tt>/</tt>
   * 
   * @param value
   *          a fully-qualified Java type name in the SureLogic format.
   * @return {@code true} if the if the passed string is valid, {@code false}
   *         otherwise.
   */
  public static boolean isValidTypeNameFullyQualifiedSureLogic(final String value) {
    if (value == null)
      return false;
    if (value.length() == 0)
      return false;
    if ("/".equals(value))
      return true;
    final int slashIndex = value.indexOf('/');
    if (slashIndex == -1)
      return false;

    if (slashIndex > 0) {
      /*
       * Package part
       */
      final String pkgPart = value.substring(0, slashIndex);
      if (!isValidDotSeparatedJavaIdentifier(pkgPart))
        return false;
    }

    /*
     * Type name
     */
    final String typePart = value.substring(slashIndex + 1);
    if ("".equals(typePart))
      return true;
    if (!isValidDotSeparatedJavaIdentifier(typePart))
      return false;

    return true;
  }

  /**
   * Gets the package name from the passed string if it is a valid
   * fully-qualified Java type name in a particular SureLogic format.
   * 
   * @param value
   *          a valid fully-qualified Java type name in a particular SureLogic
   *          format.
   * @return the package name or the empty string.
   * 
   * @throws IllegalArgumentException
   *           if {@link #isValidTypeNameFullyQualifiedSureLogic(String)} fails
   *           for <tt>value</tt>.
   */
  public static String getPackageNameOrEmptyFromTypeNameFullyQualifiedSureLogic(final String value) {
    if (!isValidTypeNameFullyQualifiedSureLogic(value))
      throw new IllegalArgumentException(I18N.err(287, value));
    if ("/".equals(value))
      return "";
    final int slashIndex = value.indexOf('/');
    if (slashIndex > 0) {
      /*
       * Package part
       */
      final String pkgPart = value.substring(0, slashIndex);
      return pkgPart;
    } else
      return "";
  }

  /**
   * Gets an array containing types names, in order from outer to inner, from
   * the passed string if it is a valid fully-qualified Java type name in a
   * particular SureLogic format.
   * 
   * @param value
   *          a valid fully-qualified Java type name in a particular SureLogic
   *          format.
   * @return an array containing types names or an empty array.
   * @throws IllegalArgumentException
   *           if {@link #isValidTypeNameFullyQualifiedSureLogic(String)} fails
   *           for <tt>value</tt>.
   */
  public static String[] getTypeNamesOrEmptyFromTypeNameFullyQualifiedSureLogic(final String value) {
    if (!isValidTypeNameFullyQualifiedSureLogic(value))
      throw new IllegalArgumentException(I18N.err(287, value));
    if ("/".equals(value))
      return EMPTY_STRING_ARRAY;
    final int slashIndex = value.indexOf('/');
    if (slashIndex > 0) {
      /*
       * Type name
       */
      final String typePart = value.substring(slashIndex + 1);
      if ("".equals(typePart))
        return EMPTY_STRING_ARRAY;
      final List<String> result = new ArrayList<>();
      int lastDotIndex = 0;
      while (true) {
        int dotIndex = typePart.indexOf('.', lastDotIndex);
        if (dotIndex == -1) {
          final String id = typePart.substring(lastDotIndex);
          result.add(id);
          break;
        } else {
          String id = typePart.substring(lastDotIndex, dotIndex);
          result.add(id);
        }
        lastDotIndex = dotIndex + 1;
      }
      return result.toArray(new String[result.size()]);
    } else
      return EMPTY_STRING_ARRAY;
  }

  /**
   * Checks if two objects are equal guarding that {@code null} checks are done
   * properly.
   * 
   * @param o1
   *          an object.
   * @param o2
   *          an object.
   * @return {@code true} if the two objects are both {@code null} or
   *         <tt>o1.equals(o2)</tt>, {@code false} otherwise.
   */
  public static boolean nullSafeEquals(Object o1, Object o2) {
    if (o1 == o2)
      return true;
    if (o1 == null)
      return false;
    if (o2 == null)
      return false;
    return o1.equals(o2);
  }

  /**
   * The string identifier of the Flashlight client plug-in.
   */
  public static final String FLASHLIGHT_ID = "com.surelogic.flashlight.client.eclipse";

  /**
   * Filters a string returning the passed string if it is non-{@code null} and
   * an empty string if it is {@code null}.
   * 
   * @param value
   *          a string.
   * @return <tt>value</tt> if <tt>(value != null)</tt>, <tt>""</tt> otherwise.
   */
  public static @NonNull String nullToEmpty(String value) {
    if (value == null)
      return "";
    else
      return value;
  }

  /**
   * Returns an {@code int} value of the passed {@code long} value or
   * {@link Integer#MAX_VALUE} if the long is too bit to fit into an {@code int}
   * .
   * 
   * @param value
   *          the long value.
   * @return an {@code int} value of the {@code long} value or
   *         {@link Integer#MAX_VALUE} if the long is too big to fit into an
   *         {@code int}.
   */
  public static int safeLongToInt(final long value) {
    final long result = Math.min(value, Integer.MAX_VALUE);
    return (int) result;
  }

  /**
   * This method converts a string to a long but it ignores non-numeric
   * suffices. For example, invoking {@code safeParseLong("40 ns")} would result
   * in 40 (i.e., not an error). Also commas are skipped so
   * {@code safeParseLong("47,340 ns")} would result in 47340 (i.e., not an
   * error).
   * 
   * @param value
   *          the string to convert.
   * @return the resulting long value. If the value is entirely non-numeric the
   *         result will be 0.
   */
  public static long safeParseLong(String value) {
    value = value.trim();
    long result = 0;
    if (value != null) {
      for (int i = 0; i < value.length(); i++) {
        final char ch = value.charAt(i);
        if (ch != ',') { // skip commas
          final long digit = ch - '0';
          final boolean isNumeric = 0 <= digit && digit <= 9;
          if (isNumeric) {
            result = (result * 10) + digit;
          } else {
            return result;
          }
        }
      }
    }
    return result;
  }

  /**
   * Rounds the passed double with {@link Math#round(double)} then invokes
   * {@link #safeLongToInt(long)}.
   * 
   * @param value
   *          the double value.
   * @return an {@code int} value of the {@code long} value or
   *         {@link Integer#MAX_VALUE} if the long is too big to fit into an
   *         {@code int}.
   */
  public static int safeDoubleToInt(final double value) {
    return safeLongToInt(Math.round(value));
  }

  private final static ThreadLocal<SimpleDateFormat> tl_dir_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("-yyyy.MM.dd-'at'-HH.mm.ss.SSS");
    }
  };

  public static String toStringForDir(final Date date) {
    return tl_dir_format.get().format(date);
  }

  public static Date fromStringForDir(final String dateStr) throws ParseException {
    return tl_dir_format.get().parse(dateStr);
  }

  private final static ThreadLocal<SimpleDateFormat> tl_day_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd");
    }
  };

  public static String toStringDay(final Date date) {
    return tl_day_format.get().format(date);
  }

  public static Date fromStringDay(final String dateStr) throws ParseException {
    return tl_day_format.get().parse(dateStr);
  }

  private final static ThreadLocal<SimpleDateFormat> tl_human_day_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("dd-MMM-yyyy");
    }
  };

  public static String toStringHumanDay(final Date date) {
    return tl_human_day_format.get().format(date);
  }

  public static Date fromStringHumanDay(final String dateStr) throws ParseException {
    return tl_human_day_format.get().parse(dateStr);
  }

  private final static ThreadLocal<SimpleDateFormat> tl_day_hms_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
  };

  public static String toStringDayHMS(final Date date) {
    return tl_day_hms_format.get().format(date);
  }

  public static Date fromStringDayHMS(final String dateStr) throws ParseException {
    return tl_day_hms_format.get().parse(dateStr);
  }

  private final static ThreadLocal<SimpleDateFormat> tl_no_day_hms_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("HH:mm:ss");
    }
  };

  public static String toStringNoDayHMS(final Date date) {
    return tl_no_day_hms_format.get().format(date);
  }

  public static Date fromStringNoDayHMS(final String dateStr) throws ParseException {
    return tl_no_day_hms_format.get().parse(dateStr);
  }

  public static Timestamp getWall(final Timestamp start, final long startNS, final long timeNS) {
    long tMS = start.getTime();
    final long deltaNS = timeNS - startNS;
    if (deltaNS < 0) {
      throw new IllegalStateException("timeNS=" + timeNS + " cannot be less than startedNS=" + startNS);
    }
    final long deltaMS = deltaNS / 1000000;
    tMS = tMS + deltaMS;
    long tDecNS = tMS % 1000 * 1000000;
    tDecNS = tDecNS + deltaNS % 1000000;
    final Timestamp result = new Timestamp(tMS);
    result.setNanos((int) tDecNS);
    return result;
  }

  public static String toStringHumanWithCommas(final int i) {
    return String.format("%,d", i);
  }

  public static String toStringHumanWithCommas(final long l) {
    return String.format("%,d", l);
  }

  /**
   * This converts a time into a human readable string in days, hours, minutes
   * and seconds. It is useful for user interface display or logging of
   * durations.
   * 
   * @param duration
   *          a length of time.
   * @param unit
   *          the time unit for the passed duration.
   * @return a human readable string in days, hours, minutes and seconds.
   */
  public static String toStringDurationS(long duration, TimeUnit unit) {
    return toStringDuration(duration, unit, true, false);
  }

  /**
   * This converts a time into a human readable string in days, hours, minutes,
   * seconds, and milliseconds. It is useful for user interface display or
   * logging of durations.
   * 
   * @param duration
   *          a length of time.
   * @param unit
   *          the time unit for the passed duration.
   * @return a human readable string in days, hours, minutes seconds, and
   *         milliseconds.
   */
  public static String toStringDurationMS(long duration, TimeUnit unit) {
    return toStringDuration(duration, unit, false, true);
  }

  /**
   * This converts a time into a human readable string in days, hours, minutes,
   * seconds, milliseconds, and nanoseconds. It is useful for user interface
   * display or logging of precise durations.
   * 
   * @param duration
   *          a length of time.
   * @param unit
   *          the time unit for the passed duration.
   * @return a human readable string in days, hours, minutes seconds,
   *         milliseconds, and nanoseconds.
   */
  public static String toStringDurationNS(long duration, TimeUnit unit) {
    return toStringDuration(duration, unit, false, false);
  }

  private static String toStringDuration(long duration, final TimeUnit unit, final boolean stopAtSeconds, final boolean stopAtMS) {
    final StringBuilder b = new StringBuilder();
    final long days = unit.toDays(duration);
    if (days > 0) {
      b.append(days).append(" day");
      if (days > 1)
        b.append("s");
      duration -= unit.convert(days, TimeUnit.DAYS);
    }
    if (duration > 0) {
      final long hours = unit.toHours(duration);
      if (hours > 0) {
        b.append(" ").append(hours).append(" hour");
        if (hours > 1)
          b.append("s");
        duration -= unit.convert(hours, TimeUnit.HOURS);
      }
    }
    if (duration > 0) {
      final long minutes = unit.toMinutes(duration);
      if (minutes > 0) {
        b.append(" ").append(minutes).append(" minute");
        if (minutes > 1)
          b.append("s");
        duration -= unit.convert(minutes, TimeUnit.MINUTES);
      }
    }
    if (duration > 0) {
      final long seconds = unit.toSeconds(duration);
      if (seconds > 0) {
        b.append(" ").append(seconds).append(" second");
        if (seconds > 1)
          b.append("s");
        duration -= unit.convert(seconds, TimeUnit.SECONDS);
      }
    }
    if (stopAtSeconds) {
      if (b.length() == 0) {
        if (duration > 0)
          b.append("under one second");
        else
          b.append("0 seconds");
      }
      return b.toString().trim();
    }
    if (duration > 0) {
      final long millis = unit.toMillis(duration);
      if (millis > 0) {
        b.append(" ").append(millis).append(" ms");
        duration -= unit.convert(millis, TimeUnit.MILLISECONDS);
      }
    }
    if (stopAtMS) {
      if (b.length() == 0) {
        if (duration > 0)
          b.append("under one millisecond");
        else
          b.append("0 ms");
      }
      return b.toString().trim();
    }
    if (duration > 0) {
      final long nanos = unit.toNanos(duration);
      if (nanos > 0) {
        b.append(" ").append(toStringHumanWithCommas(duration)).append(" ns");
      }
    }
    if (b.length() == 0) {
      b.append("0 ns");
    }
    return b.toString().trim();
  }

  public static long byteToMByte(final long value) {
    return value / 1024L / 1024L;
  }

  /**
   * Gets the current maximum heap memory size in megabytes of this process. It
   * uses the result provided by {@link Runtime#maxMemory()} to compute its
   * result.
   * 
   * @return The maximum heap memory size in megabytes.
   */
  public static int getCurrentMaxMemorySizeInMb() {
    final Runtime rt = Runtime.getRuntime();
    return SLUtility.safeLongToInt(SLUtility.byteToMByte(rt.maxMemory()));
  }

  /**
   * Escapes a Java string so that it can be output as a quoted string literal
   * and be legal Java code.
   * <p>
   * For example, The string <code>This "doggie" \ "kitty" is nice</code> would
   * generate the string <code>This \"doggie\" \\ \"kitty\" is nice</code>.
   * 
   * @param s
   *          the string to escape.
   * @return the resulting string literal (not surrounded by double quotations).
   */
  public static String escapeJavaStringForQuoting(final String s) {
    if (s == null) {
      throw new IllegalArgumentException(I18N.err(44, "s"));
    }
    final StringBuilder b = new StringBuilder(s);

    int index = 0;
    while (true) {
      final int length = b.length();
      if (index >= length) {
        break;
      }
      char c = b.charAt(index);
      if (c == '\\' || c == '\"') {
        b.insert(index, '\\');
        index++;
      } else if (c == '\t') {
        b.replace(index, index + 1, "\\t");
        index++;
      } else if (c == '\b') {
        b.replace(index, index + 1, "\\b");
        index++;
      } else if (c == '\n') {
        b.replace(index, index + 1, "\\n");
        index++;
      } else if (c == '\r') {
        b.replace(index, index + 1, "\\r");
        index++;
      } else if (c == '\f') {
        b.replace(index, index + 1, "\\f");
        index++;
      }
      index++;
    }
    return b.toString();
  }

  /**
   * Returns a string containing obfuscated Java code which you can copy-paste
   * into your source code in order to represent the given string. Obfuscation
   * is performed by encoding the given string into UTF8 and then XOR-ing a
   * sequence of pseudo random numbers to it in order to prevent attacks based
   * on character probability. The result is encoded into an array of longs
   * which is embedded in some Java code which would produce the original string
   * again. The sequence of pseudo random numbers is seeded with a 48 bit random
   * number in order to provide a non-deterministic result for the generated
   * code. Hence, two subsequent calls with the same string will produce equal
   * results by a chance of 1/(2<sup>48</sup>-1) (0 isn't used as a seed) only!
   * <p>
   * As an example, calling this method with <code>"Hello world!"</code> as its
   * parameter may produce the result <code>"new OString(new long[] {
   *     0x3676CB307FBD35FEL, 0xECFB991E2033C169L, 0xD8C3D3E365645589L
   * }).toString()"</code>. If this code is compiled and executed later, it will
   * produce the string <code>"Hello world!"</code> again.
   * 
   * @param s
   *          The string to obfuscate. This may not contain null characters.
   * 
   * @return Some obfuscated Java code to produce the given string again.
   * 
   * @throws IllegalArgumentException
   *           If <code>s</code> contains a null character.
   */
  public static String obfuscate(final String s) {
    // Check that the string is free of null characters.
    if (s.indexOf(0) != -1) {
      throw new IllegalArgumentException(I18N.err(175));
    }

    // Obtain the string as a sequence of UTF-8 encoded bytes.
    final byte[] encoded;
    try {
      encoded = s.getBytes(UTF8);
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex); // UTF8 is always supported
    }

    /*
     * Create and seed a Pseudo Random Number Generator (PRNG) with a random
     * long number generated by another PRNG. Note that using a PRNG to generate
     * a seed for itself wouldn't make it behave deterministically because each
     * subsequent call to setSeed() SUPPLEMENTS, rather than replaces, the
     * existing seed.
     */
    long seed;
    Random prng = new Random(); // randomly seeded
    do {
      seed = prng.nextLong(); // seed strength is effectively 48 bits
    } while (seed == 0); // setSeed(0) could cause issues
    prng = new Random(seed);

    /*
     * Construct a StringBuffer to hold the generated code and append the seed
     * as the first element of the encoded array of longs. The value is
     * represented in hexadecimal in order to keep the string representation as
     * short as possible.
     */
    final StringBuilder code = new StringBuilder(I18N.msg("common.os.1"));
    appendHexLiteral(code, seed);

    final int length = encoded.length;
    for (int i = 0; i < length; i += 8) {
      final long key = prng.nextLong();
      /*
       * Compute the value of the next array element as an obfuscated version of
       * the next eight bytes of the UTF8 encoded string.
       */
      final long obfuscated = toLong(encoded, i) ^ key;

      code.append(", ");
      appendHexLiteral(code, obfuscated);
    }

    code.append(I18N.msg("common.os.2"));
    code.append(SLUtility.escapeJavaStringForQuoting(s));
    code.append(I18N.msg("common.os.3"));

    return code.toString();
  }

  /**
   * Converts a byte array into a Java declaration that can be cut and pasted
   * into code as a constant.
   * <p>
   * For example the code
   * 
   * <pre>
   * byte[] b = new byte[] { (byte) 0xFF, (byte) 0x01, (byte) 0xAB };
   * String s = toByteArrayJavaConstant(b);
   * </pre>
   * 
   * would result in <tt>s</tt> referencing the string
   * <tt>"new byte[] {(byte) 0xFF, (byte) 0x01, (byte) 0xAB}"</tt>
   * 
   * @param bytes
   *          the array of bytes.
   * @return a Java string that can be cut and pasted into Java code.
   */
  public static String toByteArrayJavaConstant(final byte[] bytes) {
    final StringBuilder code = new StringBuilder(I18N.msg("common.ob.1"));

    boolean first = true;
    for (byte b : bytes) {
      if (first) {
        first = false;
      } else {
        code.append(", ");
      }
      code.append("(byte) 0x").append(toHexString(b));
    }
    code.append("}");

    return code.toString();
  }

  private static final String hs = "0123456789abcdef";

  /**
   * Returns a hex string corresponding to the passed byte.
   * 
   * @param b
   *          a byte.
   * @return a hex string two characters long.
   */
  public static String toHexString(final byte b) {
    final StringBuilder hex = new StringBuilder();
    hex.append(hs.charAt((b & 0xF0) >> 4)).append(hs.charAt((b & 0x0F)));
    return hex.toString();
  }

  /**
   * Returns a hex string corresponding to the passed byte array.
   * 
   * @param ba
   *          the byte array.
   * @return a hex string <tt>ba.length * 2</tt> characters long.
   */
  public static String toHexString(final byte[] ba) {
    final StringBuilder hex = new StringBuilder();
    for (byte b : ba) {
      hex.append(toHexString(b));
    }
    return hex.toString();
  }

  /**
   * Parses the passed hex string into a corresponding byte array. Strings must
   * look like <tt>01FFAB</tt> and <tt>s.length()</tt> must be even.
   * 
   * @param s
   *          the hex string to pares.
   * @return a byte array of length <tt>s.length() / 2</tt>.
   * @throws IllegalArgumentException
   *           if <tt>s</tt> is <tt>null</tt> or <tt>s.length()</tt> is not
   *           even.
   * @throws NumberFormatException
   *           if the string cannot be parsed.
   */
  public static byte[] parseHexString(final String s) {
    if (s == null) {
      throw new IllegalArgumentException(I18N.err(44, "s"));
    }
    if (s.length() % 2 != 0) {
      throw new IllegalArgumentException("Hex string must contain an even number of characters");
    }
    byte[] result = new byte[s.length() / 2];

    for (int i = 0; i < result.length; i++) {
      final int sIndex = i * 2;
      result[i] = (byte) Integer.parseInt(s.substring(sIndex, sIndex + 2), 16);
    }
    return result;
  }

  /**
   * Decodes an obfuscated string from its representation as an array of longs.
   * 
   * @param obfuscated
   *          The obfuscated representation of the string.
   * 
   * @throws IllegalArgumentException
   *           If <code>obfuscated</code> is <code>null</code>.
   * @throws ArrayIndexOutOfBoundsException
   *           If the provided array does not contain at least one element.
   */
  public static String toString(final long[] obfuscated) {
    if (obfuscated == null) {
      throw new IllegalArgumentException(I18N.err(44, "obfuscated"));
    }

    final int length = obfuscated.length;

    // The original UTF8 encoded string was probably not a multiple
    // of eight bytes long and is thus actually shorter than this array.
    final byte[] encoded = new byte[8 * (length - 1)];

    // Obtain the seed and initialize a new PRNG with it.
    final long seed = obfuscated[0];
    final Random prng = new Random(seed);

    // De-obfuscate.
    for (int i = 1; i < length; i++) {
      final long key = prng.nextLong();
      toBytes(obfuscated[i] ^ key, encoded, 8 * (i - 1));
    }

    // Decode the UTF-8 encoded byte array into a string.
    // This will create null characters at the end of the decoded string
    // in case the original UTF8 encoded string was not a multiple of
    // eight bytes long.
    final String decoded;
    try {
      decoded = new String(encoded, UTF8);
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex); // UTF-8 is always supported
    }

    // Cut off trailing null characters in case the original UTF8 encoded
    // string was not a multiple of eight bytes long.
    final int i = decoded.indexOf(0);
    final String result = i != -1 ? decoded.substring(0, i) : decoded;
    return result;
  }

  private static final void appendHexLiteral(final StringBuilder sb, final long l) {
    sb.append("0x");
    sb.append(Long.toHexString(l).toUpperCase());
    sb.append('L');
  }

  /**
   * Decodes a long value from eight bytes in little endian order, beginning at
   * index <code>off</code>. This is the inverse of
   * {@link #toBytes(long, byte[], int)}. If less than eight bytes are remaining
   * in the array, only these low order bytes are processed and the
   * complementary high order bytes of the returned value are set to zero.
   * 
   * @param bytes
   *          The array containing the bytes to decode in little endian order.
   * @param off
   *          The offset of the bytes in the array.
   * 
   * @return The decoded long value.
   */
  private static final long toLong(final byte[] bytes, final int off) {
    long l = 0;

    final int end = Math.min(bytes.length, off + 8);
    for (int i = end; --i >= off;) {
      l <<= 8;
      l |= bytes[i] & 0xFF;
    }

    return l;
  }

  /**
   * Encodes a long value to eight bytes in little endian order, beginning at
   * index <code>off</code>. This is the inverse of {@link #toLong(byte[], int)}
   * . If less than eight bytes are remaining in the array, only these low order
   * bytes of the long value are processed and the complementary high order
   * bytes are ignored.
   * 
   * @param l
   *          The long value to encode.
   * @param bytes
   *          The array which holds the encoded bytes upon return.
   * @param off
   *          The offset of the bytes in the array.
   */
  private static void toBytes(long l, final byte[] bytes, final int off) {
    final int end = Math.min(bytes.length, off + 8);
    for (int i = off; i < end; i++) {
      bytes[i] = (byte) l;
      l >>= 8;
    }
  }

  /**
   * This method returns the SureLogic RSA public key.
   * 
   * @return SureLogic RSA public key.
   * @throws IllegalStateException
   *           if something goes wrong creating the key.
   */
  public static PublicKey getPublicKey() {
    final byte[] slPublicKeyBytes = new byte[] { (byte) 0x30, (byte) 0x82, (byte) 0x01, (byte) 0x22, (byte) 0x30, (byte) 0x0D,
        (byte) 0x06, (byte) 0x09, (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7, (byte) 0x0D, (byte) 0x01,
        (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x03, (byte) 0x82, (byte) 0x01, (byte) 0x0F, (byte) 0x00,
        (byte) 0x30, (byte) 0x82, (byte) 0x01, (byte) 0x0A, (byte) 0x02, (byte) 0x82, (byte) 0x01, (byte) 0x01, (byte) 0x00,
        (byte) 0xEA, (byte) 0xE5, (byte) 0x3C, (byte) 0xD2, (byte) 0xDD, (byte) 0x24, (byte) 0x75, (byte) 0x96, (byte) 0x84,
        (byte) 0x7E, (byte) 0xDD, (byte) 0x39, (byte) 0x15, (byte) 0x3B, (byte) 0x8D, (byte) 0x0F, (byte) 0xB4, (byte) 0xFA,
        (byte) 0xA4, (byte) 0xA2, (byte) 0x91, (byte) 0x77, (byte) 0xF6, (byte) 0xA5, (byte) 0xD1, (byte) 0x58, (byte) 0xB3,
        (byte) 0x1C, (byte) 0x7B, (byte) 0xD4, (byte) 0xA5, (byte) 0x54, (byte) 0x46, (byte) 0x61, (byte) 0x1C, (byte) 0x62,
        (byte) 0x6C, (byte) 0x07, (byte) 0xE3, (byte) 0x8D, (byte) 0xBE, (byte) 0xAC, (byte) 0xB5, (byte) 0xD6, (byte) 0x5E,
        (byte) 0xA7, (byte) 0xAD, (byte) 0x9A, (byte) 0x05, (byte) 0x57, (byte) 0xB9, (byte) 0x2F, (byte) 0x9E, (byte) 0xCB,
        (byte) 0xBB, (byte) 0xC4, (byte) 0xE0, (byte) 0xCC, (byte) 0x41, (byte) 0x68, (byte) 0x73, (byte) 0x6B, (byte) 0x52,
        (byte) 0x4D, (byte) 0x05, (byte) 0x0B, (byte) 0x68, (byte) 0x1E, (byte) 0xDE, (byte) 0xC1, (byte) 0x77, (byte) 0xB5,
        (byte) 0xF4, (byte) 0x28, (byte) 0x2B, (byte) 0x1B, (byte) 0x53, (byte) 0xCE, (byte) 0x44, (byte) 0x3B, (byte) 0x72,
        (byte) 0x19, (byte) 0x19, (byte) 0x2B, (byte) 0x6C, (byte) 0x34, (byte) 0x4F, (byte) 0x34, (byte) 0xF8, (byte) 0x2A,
        (byte) 0x7E, (byte) 0xF5, (byte) 0x2C, (byte) 0xB5, (byte) 0x28, (byte) 0xA0, (byte) 0xBA, (byte) 0x2A, (byte) 0xB7,
        (byte) 0x71, (byte) 0xE3, (byte) 0x70, (byte) 0x7A, (byte) 0x93, (byte) 0x0A, (byte) 0x21, (byte) 0xDB, (byte) 0xA3,
        (byte) 0x16, (byte) 0x6F, (byte) 0x81, (byte) 0xC0, (byte) 0xD3, (byte) 0x18, (byte) 0xC1, (byte) 0xEE, (byte) 0x34,
        (byte) 0x73, (byte) 0x28, (byte) 0x12, (byte) 0xA6, (byte) 0x67, (byte) 0xFC, (byte) 0x4C, (byte) 0xBD, (byte) 0x90,
        (byte) 0x0D, (byte) 0xC6, (byte) 0xB0, (byte) 0xA5, (byte) 0x65, (byte) 0x26, (byte) 0xC9, (byte) 0x04, (byte) 0xD1,
        (byte) 0xD1, (byte) 0x3D, (byte) 0xA7, (byte) 0x88, (byte) 0xC5, (byte) 0x86, (byte) 0x89, (byte) 0xF0, (byte) 0x07,
        (byte) 0xDC, (byte) 0x40, (byte) 0x64, (byte) 0x99, (byte) 0x01, (byte) 0xF4, (byte) 0x40, (byte) 0xF1, (byte) 0x23,
        (byte) 0xE4, (byte) 0x76, (byte) 0xC7, (byte) 0x76, (byte) 0xF5, (byte) 0x70, (byte) 0x95, (byte) 0xC1, (byte) 0x9E,
        (byte) 0xD1, (byte) 0x40, (byte) 0xB2, (byte) 0x9C, (byte) 0xD2, (byte) 0x77, (byte) 0x8E, (byte) 0x75, (byte) 0x73,
        (byte) 0xEC, (byte) 0x46, (byte) 0x6B, (byte) 0xCF, (byte) 0xB9, (byte) 0x5A, (byte) 0x2F, (byte) 0xB8, (byte) 0xEA,
        (byte) 0x89, (byte) 0x8A, (byte) 0xE4, (byte) 0x85, (byte) 0x76, (byte) 0xFA, (byte) 0x5A, (byte) 0xBA, (byte) 0x73,
        (byte) 0xFF, (byte) 0x97, (byte) 0xD5, (byte) 0x37, (byte) 0xE4, (byte) 0x38, (byte) 0x18, (byte) 0x20, (byte) 0xF5,
        (byte) 0xB3, (byte) 0xEB, (byte) 0x95, (byte) 0x45, (byte) 0xE4, (byte) 0x54, (byte) 0x7F, (byte) 0x7C, (byte) 0xA3,
        (byte) 0x80, (byte) 0xBE, (byte) 0x21, (byte) 0xFB, (byte) 0x70, (byte) 0x2A, (byte) 0x46, (byte) 0xDC, (byte) 0x35,
        (byte) 0x3B, (byte) 0xC8, (byte) 0x1F, (byte) 0x05, (byte) 0xF1, (byte) 0x7B, (byte) 0xEA, (byte) 0xB6, (byte) 0xAC,
        (byte) 0xF9, (byte) 0x31, (byte) 0x28, (byte) 0xD3, (byte) 0xE5, (byte) 0xC5, (byte) 0xA2, (byte) 0x92, (byte) 0x1F,
        (byte) 0xB7, (byte) 0x81, (byte) 0x1F, (byte) 0x4A, (byte) 0x26, (byte) 0x9B, (byte) 0x52, (byte) 0x80, (byte) 0x9E,
        (byte) 0x9A, (byte) 0xF0, (byte) 0x3F, (byte) 0xB7, (byte) 0x7F, (byte) 0x4E, (byte) 0xEA, (byte) 0x13, (byte) 0xCC,
        (byte) 0xC6, (byte) 0x10, (byte) 0xA2, (byte) 0x81, (byte) 0x02, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01 };
    final PublicKey result;
    try {
      // create public key
      final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(slPublicKeyBytes);
      final KeyFactory kf = KeyFactory.getInstance("RSA");
      result = kf.generatePublic(publicKeySpec);
    } catch (Exception e) {
      /* Lots could go wrong, anything at all would indicate a bug */
      throw new IllegalStateException(I18N.err(178), e);
    }
    return result;
  }

  /**
   * Generates and returns a signature for the passed data using the passed RSA
   * private key.
   * 
   * @param data
   *          the data to generate a signature for.
   * @param key
   *          the RSA private key to use.
   * @return the signature.
   * @throws IllegalArgumentException
   *           if one of the parameters is <tt>null</tt>.
   * @throws IllegalStateException
   *           if something goes wrong during the signature generation.
   */
  public static byte[] getSignature(final byte[] data, final PrivateKey key) {
    if (data == null) {
      throw new IllegalArgumentException(I18N.err(44, "data"));
    }
    if (key == null) {
      throw new IllegalArgumentException(I18N.err(44, "key"));
    }

    final byte[] signature;
    try {
      final Signature rsaSig = Signature.getInstance("SHA1withRSA");
      rsaSig.initSign(key);
      rsaSig.update(data);
      signature = rsaSig.sign();
    } catch (Exception e) {
      /* Lots could go wrong, anything at all would indicate a bug */
      throw new IllegalStateException(I18N.err(180), e);
    }
    return signature;
  }

  /**
   * Checks a signature using the passed public key.
   * 
   * @param data
   *          the data the signature is about.
   * @param signature
   *          the signature.
   * @param key
   *          the RSA public key to use.
   * @return {@code true} if the signature is valid, {@code false} otherwise.
   */
  public static boolean checkSignature(final byte[] data, final byte[] signature, final PublicKey key) {
    if (data == null) {
      throw new IllegalArgumentException(I18N.err(44, "data"));
    }
    if (signature == null) {
      throw new IllegalArgumentException(I18N.err(44, "signature"));
    }

    try {
      final Signature rsaSig = Signature.getInstance("SHA1withRSA");
      rsaSig.initVerify(key);
      rsaSig.update(data);
      final boolean verifies = rsaSig.verify(signature);
      return verifies;
    } catch (Exception e) {
      // fall through and return false
    }
    /* if we get here the signature is bad or broken */
    return false;
  }

  /**
   * Checks a signature using the SureLogic RSA public key.
   * 
   * @param data
   *          the data the signature is about.
   * @param signature
   *          the signature.
   * @return {@code true} if the signature is valid, {@code false} otherwise.
   * @see #getPublicKey()
   */
  public static boolean checkSignature(final byte[] data, final byte[] signature) {
    return checkSignature(data, signature, getPublicKey());
  }

  /**
   * Line wraps the passed string at the specified width by inserting newlines.
   * The last line may not be the specified length.
   * 
   * @param b
   *          the string to wrap.
   * @param linewidth
   *          the desired line width.
   */
  public static void wrap(final StringBuilder b, final int linewidth) {
    int workIndex = linewidth;
    while (b.length() > workIndex) {
      b.insert(workIndex, '\n');
      workIndex = workIndex + linewidth + 1;
    }
  }

  /**
   * Line wraps the passed string at the specified width by inserting newlines.
   * The last line may not be the specified length.
   * 
   * @param s
   *          the string to wrap.
   * @param linewidth
   *          the desired line width.
   * @return the wrapped string.
   */
  public static String wrap(final String s, final int linewidth) {
    StringBuilder b = new StringBuilder(s);
    wrap(b, linewidth);
    return b.toString();
  }

  /**
   * Separates a string containing multiple lines into an array containing each
   * line.
   * <p>
   * If the string passed is {@code null} then an empty array is returned.
   * <p>
   * <i>Implementation Note:</i> The implementation uses a {@link StringReader}
   * wrapped by a {@link BufferedReader} to break apart the string.
   * 
   * @param s
   *          the string to separate lines from.
   * @return a possibly empty array containing the lines within <tt>s</tt>.
   */
  public static ArrayList<String> separateLines(final String s) {
    final ArrayList<String> result = new ArrayList<>();
    if (s == null)
      return result;

    final BufferedReader r = new BufferedReader(new StringReader(s));
    while (true) {
      try {
        final String line = r.readLine();
        if (line == null) {
          break;
        }
        result.add(line);
      } catch (IOException ioe) {
        ioe.printStackTrace(System.err);
      }
    }
    return result;
  }

  /**
   * Trims out newlines, spaces, tabs, formfeeds, and backspaces from the entire
   * passed string.
   * 
   * @param s
   *          the string to trim the above out of.
   * @return the string with the above removed.
   */
  public static StringBuilder trimInternal(final String s) {
    /*
     * Trim off any extra spaces or tabs and use a mutable string.
     */
    StringBuilder b = new StringBuilder(s.trim());

    String[] stripout = new String[] { "\n", "\r", "\f", " ", "\t", "\b" };

    for (String c : stripout) {
      while (true) {
        int newlineIndex = b.indexOf(c);
        if (newlineIndex == -1) {
          break;
        }
        b.delete(newlineIndex, newlineIndex + 1);
      }
    }
    return b;
  }

  /**
   * Sends a string to a URL and returns the response. If anything goes wrong an
   * exception is thrown.
   * 
   * @param url
   *          a URL to communicate with.
   * @param param
   *          a map of parameters to send.
   * @return the result.
   * @throws IOException
   *           if anything goes wrong.
   * @throws IllegalArgumentException
   *           if either of the parameters is {@code null}.
   */
  public static String sendPostToUrl(final URL url, final Map<String, String> param) throws IOException {
    if (url == null) {
      throw new IllegalArgumentException(I18N.err(44, "url"));
    }
    if (param == null) {
      throw new IllegalArgumentException(I18N.err(44, "param"));
    }

    /*
     * Prepare the connection.
     */
    final URLConnection conn = url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    PrintWriter wr = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
    try {
      /*
       * Send the request.
       */
      if (!param.isEmpty()) {
        Iterator<Map.Entry<String, String>> iter = param.entrySet().iterator();
        do {
          Map.Entry<String, String> entry = iter.next();
          wr.print(URLEncoder.encode(entry.getKey(), UTF8));
          wr.print('=');
          wr.print(URLEncoder.encode(entry.getValue(), UTF8));
          if (iter.hasNext()) {
            wr.print('&');
          }
        } while (iter.hasNext());
      }
      wr.flush();
      /*
       * Get the response.
       */
      final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      try {
        StringBuilder response = new StringBuilder();
        char[] buf = new char[512];
        for (int len = reader.read(buf); len != -1; len = reader.read(buf)) {
          response.append(buf, 0, len);
        }
        return response.toString();
      } finally {
        reader.close();
      }
    } finally {
      wr.close();
    }
  }

  private static final int ENCODED_STRING_LENGTH_LENGTH = 8;
  private static final String ENCODED_FORMAT = "%0" + ENCODED_STRING_LENGTH_LENGTH + "d%s";

  public static List<String> decodeStringList(String value) {
    final List<String> result = new ArrayList<>();
    int at = 0;
    while (true) {
      if (value.length() < at + ENCODED_STRING_LENGTH_LENGTH)
        break;
      final String encodedLength = value.substring(at, at + ENCODED_STRING_LENGTH_LENGTH);
      final int length = Integer.parseInt(encodedLength);
      at += ENCODED_STRING_LENGTH_LENGTH;

      if (value.length() < at + length)
        break;
      final String string = value.substring(at, at + length);
      result.add(string);
      at += length;
    }
    return result;
  }

  public static String encodeStringList(List<String> list) {
    final StringBuilder b = new StringBuilder();
    for (final String s : list) {
      b.append(String.format(ENCODED_FORMAT, s.length(), s));
    }
    return b.toString();
  }

  /**
   * Creates a directory name for a scan using the project names and a
   * timestamp.
   * <p>
   * For example <tt>encodeScanDirectoryName("core project", true, now)</tt>
   * would produce <tt>core_project-etc-2012.11.12-at-18.19.38.815</tt> if
   * <i>now</i> is <i>2012.11.12 18:19:38 815ms</i>.
   * 
   * @param firstProjectNameOrNull
   *          the first alphabetical project name involved. If {@code null} then
   *          <tt>"unknown_project"</tt> is used. Any spaces in the passed name
   *          are converted to <tt>_</tt> characters.
   * @param moreProjects
   *          {@code true} if more than one project was involved, {@code false}
   *          if only one project or unknown.
   * @param timestamp
   *          when the scan or run occurred.
   * @return a directory name for a scan or run.
   * @throws IllegalArgumentException
   *           if <tt>timestamp</tt> is null.
   */
  @NonNull
  public static String getScanDirectoryName(@Nullable String firstProjectNameOrNull, boolean moreProjects,
      @NonNull Date timestamp) {
    if (timestamp == null)
      throw new IllegalArgumentException(I18N.err(44, "timestamp"));

    final StringBuilder b = new StringBuilder();
    b.append(getTruncatedProjectName(firstProjectNameOrNull));
    if (moreProjects)
      b.append("-etc");
    String timeString = toStringForDir(timestamp);
    b.append(timeString);
    return b.toString();
  }

  @NonNull
  public static String getTruncatedProjectName(@Nullable String firstProjectNameOrNull) {
    if (firstProjectNameOrNull == null) {
      firstProjectNameOrNull = "unknown_project";
    }
    firstProjectNameOrNull = firstProjectNameOrNull.replace(' ', '_');
    if (firstProjectNameOrNull.length() > 20) {
      firstProjectNameOrNull = firstProjectNameOrNull.substring(0, 20);
    }
    return firstProjectNameOrNull;
  }

  /**
   * Reads a date from a scan directory name created, and in the format
   * described, by {@link #getScanDirectoryName(String, boolean, Date)}.
   * 
   * @param scanDirectoryName
   *          a directory name, such as
   *          <tt>core_project-etc-2012.11.12-at-18.19.38.815</tt>.
   * @return a date, or {@code null} if none can be read.
   */
  @Nullable
  public static Date getDateFromScanDirectoryNameOrNull(@Nullable String scanDirectoryName) {
    if (scanDirectoryName == null)
      return null;
    // right string 27
    final int length = scanDirectoryName.length();
    if (length < 28)
      return null;
    String timePart = scanDirectoryName.substring(length - 27);
    try {
      final Date result = fromStringForDir(timePart);
      return result;
    } catch (ParseException ignore) {
      // ignore
    }
    return null;
  }

  /**
   * Creates a list of the passed elements.
   * 
   * @param elements
   *          elements for the list.
   * @return a new list
   */
  public static <T> ArrayList<T> list(@SuppressWarnings("unchecked") T... elements) {
    final ArrayList<T> l = new ArrayList<>();
    for (T e : elements) {
      l.add(e);
    }
    return l;
  }

  /**
   * Passed subsequent lines of a Java compilation unit this method can help
   * track if, at the end of the passed line, the program text is within a Java
   * slash-star comment block or not.
   * <p>
   * Note that this method does not consider slash-slash to the end-of-line
   * comments.
   * 
   * @param inASlashStarComment
   *          {@code true} if the previous line ended with the program text
   *          still in a slash-star comment block.
   * @param line
   *          the Java line of code.
   * @return {@code true} if the passed line ended with the program text still
   *         in slash-star comment block, {@code false} if not in a slash-star
   *         comment block.
   */
  public static boolean getSlashStarCommentState(boolean inASlashStarComment, String line) {
    StringBuilder b = new StringBuilder(line);
    return updateSlashStarCommentStateHelper(inASlashStarComment, b);
  }

  private static boolean updateSlashStarCommentStateHelper(boolean inASlashStarComment, final StringBuilder b) {
    if (inASlashStarComment) {
      final int endIndex = b.indexOf(SLASH_STAR_COMMENT_END);
      if (endIndex != -1) {
        b.delete(0, endIndex + SLASH_STAR_COMMENT_END.length());
        return updateSlashStarCommentStateHelper(!inASlashStarComment, b);
      }
    } else {
      final int startIndex = b.indexOf(SLASH_STAR_COMMENT_START);
      if (startIndex != -1) {
        b.delete(0, startIndex + SLASH_STAR_COMMENT_START.length());
        return updateSlashStarCommentStateHelper(!inASlashStarComment, b);
      }
    }
    return inASlashStarComment;
  }

  /**
   * Gets the stack trace from an exception and returns it as a string.
   * 
   * @param t
   *          the exception.
   * @return the stack trace.
   * @throws IllegalArgumentException
   *           if the passed exception is {code null}.
   */
  public static String getStackTrace(Throwable t) {
    if (t == null)
      throw new IllegalArgumentException(I18N.err(44, "t"));
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    t.printStackTrace(pw);
    pw.flush();
    sw.flush();
    return sw.toString();
  }

  /**
   * Decodes a Base64 encoded string to a normal string.
   * 
   * @param s
   *          the encoded string.
   * @return the decoded string.
   */
  public static String decodeBase64(String s) {
    return new String(BaseEncoding.base64().decode(s), Charsets.UTF_8);
  }

  /**
   * Encodes a normal string to a Base64 encoded string.
   * 
   * @param s
   *          the normal string.
   * @return the encoded string.
   */
  public static String encodeBase64(String s) {
    return BaseEncoding.base64().encode(s.getBytes(Charsets.UTF_8));
  }

  /**
   * Obtains the output of <tt>t.printStackTrace()</tt> as a string. Useful for
   * logging and other purposes.
   * 
   * @param t
   *          an exception.
   * @return the output of <tt>t.printStackTrace()</tt>.
   */
  public static String toString(Throwable t) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  /**
   * Returns a string that is clipped to a maximum length. All characters above
   * the maximum length are removed.
   * 
   * @param value
   *          the string to clip
   * @param length
   *          the maximum number of characters in the returned string.
   * @return the clipped string. If it is less than the maximum length than the
   *         string reference passed is returned. If null is passed, null is
   *         always returned.
   */
  @Nullable
  public static String clipString(@Nullable final String value, final int length) {
    if (value == null || value.length() <= length) {
      return value;
    }
    return value.substring(0, length);
  }

  private SLUtility() {
    // no instances
  }
}
