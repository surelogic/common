package com.surelogic.common;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Random;

import org.apache.commons.lang.SystemUtils;

import com.surelogic.common.i18n.I18N;

/**
 * A utility with SureLogic common code.
 */
public final class SLUtility {
	public static final boolean is64bit = (SystemUtils.OS_ARCH.indexOf("64") >= 0);
	public static final String JAVA_DEFAULT_PACKAGE = "(default package)";
	private static final String UTF8 = new String(new char[] { '\u0055',
			'\u0054', '\u0046', '\u0038' }); // => "UTF8"

	/**
	 * Returns an {@code int} value of the passed {@code long} value or
	 * {@link Integer#MAX_VALUE} if the long is too bit to fit into an {@code
	 * int}.
	 * 
	 * @param value
	 *            the long value.
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

	public static Date fromStringDay(final String dateStr)
			throws ParseException {
		return tl_day_format.get().parse(dateStr);
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

	public static Date fromStringHMS(final String dateStr)
			throws ParseException {
		return tl_hms_format.get().parse(dateStr);
	}

	public static Timestamp getWall(final Timestamp start, final long startNS,
			final long timeNS) {
		long tMS = start.getTime();
		final long deltaNS = timeNS - startNS;
		if (deltaNS < 0) {
			throw new IllegalStateException("timeNS=" + timeNS
					+ " cannot be less than startedNS=" + startNS);
		}
		final long deltaMS = deltaNS / 1000000;
		tMS = tMS + deltaMS;
		long tDecNS = (tMS % 1000) * 1000000;
		tDecNS = tDecNS + (deltaNS % 1000000);
		final Timestamp result = new Timestamp(tMS);
		result.setNanos((int) tDecNS);
		return result;
	}

	public static String toCommaSepString(final int i) {
		return toCommaSepString((long) i);
	}

	public static String toCommaSepString(final long i) {
		final Formatter f = new Formatter();
		f.format("%,d", i);
		return f.toString();
	}

	public static long byteToMByte(final long value) {
		return value / 1024L / 1024L;
	}

	/**
	 * Gets the current maximum heap memory size in megabytes of this process.
	 * It uses the result provided by {@link Runtime#maxMemory()} to compute its
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
	 * For example, The string <code>This "doggie" \ "kitty" is nice</code>
	 * would generate the string <code>This \"doggie\" \\ \"kitty\" is nice</code>.
	 * 
	 * @param s
	 *            the string to escape.
	 * @return the resulting string literal (not surrounded by double
	 *         quotations).
	 */
	public static String escapeJavaStringForQuoting(String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));
		final StringBuilder b = new StringBuilder(s);

		int index = 0;
		while (true) {
			final int length = b.length();
			if (index >= length)
				break;
			char c = b.charAt(index);
			if (c == '\\' || c == '\"') {
				b.insert(index, '\\');
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
	 * which is embedded in some Java code which would produce the original
	 * string again. The sequence of pseudo random numbers is seeded with a 48
	 * bit random number in order to provide a non-deterministic result for the
	 * generated code. Hence, two subsequent calls with the same string will
	 * produce equal results by a chance of 1/(2<sup>48</sup>-1) (0 isn't used
	 * as a seed) only!
	 * <p>
	 * As an example, calling this method with <code>"Hello world!"</code> as
	 * its parameter may produce the result <code>"new OString(new long[] {
	 *     0x3676CB307FBD35FEL, 0xECFB991E2033C169L, 0xD8C3D3E365645589L
	 * }).toString()"</code>. If this code is compiled and executed later, it will produce the
	 * string <code>"Hello world!"</code> again.
	 * 
	 * @param s
	 *            The string to obfuscate. This may not contain null characters.
	 * 
	 * @return Some obfuscated Java code to produce the given string again.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>s</code> contains a null character.
	 */
	public static String obfuscate(final String s) {
		// Check that the string is free of null characters.
		if (s.indexOf(0) != -1)
			throw new IllegalArgumentException(I18N.err(175));

		// Obtain the string as a sequence of UTF-8 encoded bytes.
		final byte[] encoded;
		try {
			encoded = s.getBytes(UTF8);
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex); // UTF8 is always supported
		}

		/*
		 * Create and seed a Pseudo Random Number Generator (PRNG) with a random
		 * long number generated by another PRNG. Note that using a PRNG to
		 * generate a seed for itself wouldn't make it behave deterministically
		 * because each subsequent call to setSeed() SUPPLEMENTS, rather than
		 * replaces, the existing seed.
		 */
		long seed;
		Random prng = new Random(); // randomly seeded
		do {
			seed = prng.nextLong(); // seed strength is effectively 48 bits
		} while (seed == 0); // setSeed(0) could cause issues
		prng = new Random(seed);

		/*
		 * Construct a StringBuffer to hold the generated code and append the
		 * seed as the first element of the encoded array of longs. The value is
		 * represented in hexadecimal in order to keep the string representation
		 * as short as possible.
		 */
		final StringBuffer code = new StringBuffer(I18N.msg("common.os.1"));
		appendHexLiteral(code, seed);

		final int length = encoded.length;
		for (int i = 0; i < length; i += 8) {
			final long key = prng.nextLong();
			/*
			 * Compute the value of the next array element as an obfuscated
			 * version of the next eight bytes of the UTF8 encoded string.
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
	 * Decodes an obfuscated string from its representation as an array of
	 * longs.
	 * 
	 * @param obfuscated
	 *            The obfuscated representation of the string.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>obfuscated</code> is <code>null</code>.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If the provided array does not contain at least one element.
	 */
	public static String toString(final long[] obfuscated) {
		if (obfuscated == null)
			throw new IllegalArgumentException(I18N.err(44, "obfuscated"));

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

	private static final void appendHexLiteral(final StringBuffer sb,
			final long l) {
		sb.append("0x");
		sb.append(Long.toHexString(l).toUpperCase());
		sb.append('L');
	}

	/**
	 * Decodes a long value from eight bytes in little endian order, beginning
	 * at index <code>off</code>. This is the inverse of
	 * {@link #toBytes(long, byte[], int)}. If less than eight bytes are
	 * remaining in the array, only these low order bytes are processed and the
	 * complementary high order bytes of the returned value are set to zero.
	 * 
	 * @param bytes
	 *            The array containing the bytes to decode in little endian
	 *            order.
	 * @param off
	 *            The offset of the bytes in the array.
	 * 
	 * @return The decoded long value.
	 */
	private static final long toLong(final byte[] bytes, int off) {
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
	 * index <code>off</code>. This is the inverse of
	 * {@link #toLong(byte[], int)}. If less than eight bytes are remaining in
	 * the array, only these low order bytes of the long value are processed and
	 * the complementary high order bytes are ignored.
	 * 
	 * @param l
	 *            The long value to encode.
	 * @param bytes
	 *            The array which holds the encoded bytes upon return.
	 * @param off
	 *            The offset of the bytes in the array.
	 */
	private static final void toBytes(long l, byte[] bytes, int off) {
		final int end = Math.min(bytes.length, off + 8);
		for (int i = off; i < end; i++) {
			bytes[i] = (byte) l;
			l >>= 8;
		}
	}

	private SLUtility() {
		// no instances
	}
}
