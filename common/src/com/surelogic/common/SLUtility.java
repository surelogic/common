package com.surelogic.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.SystemUtils;

import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;

/**
 * A utility with SureLogic common code.
 */
public final class SLUtility {
  public static final boolean is64bit = SystemUtils.OS_ARCH.indexOf("64") >= 0;
  /**
   * This is a very JDT friendly constant&mdash;many Eclipse methods recognize
   * this particular name.
   */
  public static final String JAVA_DEFAULT_PACKAGE = "(default package)";
  public static final String UNKNOWN_PROJECT = "(unknown project)";
  public static final String LIBRARY_PROJECT = "(standard library)";
  public static final String PACKAGE_INFO = "package-info";
  public static final String UTF8 = "UTF8";
  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final String ECLIPSE_MARKER_TYPE_NAME = "com.surelogic.marker";
  public static final String PLATFORM_LINE_SEPARATOR = String.format("%n");
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final String JAVADOC_ANNOTATE_TAG = "annotate";

  public static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
  public static final String ANDROID_NATURE = "com.android.ide.eclipse.adt.AndroidNature";

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
   * <tt>java.util</tt>, <tt>Map.Entry</tt>, <tt>java.util.concurrent.locks</tt>, <tt>ClassInDefaultPkg</tt>, <tt>edu.afit.smallworld</tt>
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
      List<String> result = new ArrayList<String>();
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
  public static @NonNull
  String nullToEmpty(String value) {
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
   *         {@link Integer#MAX_VALUE} if the long is too bit to fit into an
   *         {@code int}.
   */
  public static int safeLongToInt(final long value) {
    final long result = Math.min(value, Integer.MAX_VALUE);
    return (int) result;
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

  private final static ThreadLocal<SimpleDateFormat> tl_hms_format = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
  };

  public static String toStringHMS(final Date date) {
    return tl_hms_format.get().format(date);
  }

  public static Date fromStringHMS(final String dateStr) throws ParseException {
    return tl_hms_format.get().parse(dateStr);
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

  /**
   * Returns a hex string corresponding to the passed byte.
   * 
   * @param b
   *          a byte.
   * @return a hex string two characters long.
   */
  public static String toHexString(final byte b) {
    final String hs = "0123456789abcdef";
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
  public static String[] separateLines(final String s) {
    if (s == null) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    List<String> result = new ArrayList<String>();

    final BufferedReader r = new BufferedReader(new StringReader(s));
    while (true) {
      try {
        final String line = r.readLine();
        if (line == null) {
          break;
        }
        result.add(line);
      } catch (IOException ignore) {
        break;
      }

    }
    return result.toArray(new String[result.size()]);
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
    final List<String> result = new ArrayList<String>();
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

  private SLUtility() {
    // no instances
  }
}
