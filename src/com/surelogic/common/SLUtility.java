package com.surelogic.common;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
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
	public static final String UTF8 = "UTF8";

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
		final StringBuilder code = new StringBuilder(I18N.msg("common.os.1"));
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
	 *            the array of bytes.
	 * @return a Java string that can be cut and pasted into Java code.
	 */
	public static String toByteArrayJavaConstant(byte[] bytes) {
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
	 *            a byte.
	 * @return a hex string two characters long.
	 */
	public static String toHexString(byte b) {
		final String hs = "0123456789abcdef";
		final StringBuilder hex = new StringBuilder();
		hex.append(hs.charAt((b & 0xF0) >> 4)).append(hs.charAt((b & 0x0F)));
		return hex.toString();
	}

	/**
	 * Returns a hex string corresponding to the passed byte array.
	 * 
	 * @param ba
	 *            the byte array.
	 * @return a hex string <tt>ba.length * 2</tt> characters long.
	 */
	public static String toHexString(byte[] ba) {
		final StringBuilder hex = new StringBuilder();
		for (byte b : ba) {
			hex.append(toHexString(b));
		}
		return hex.toString();
	}

	/**
	 * Parses the passed hex string into a corresponding byte array. Strings
	 * must look like <tt>01FFAB</tt> and <tt>s.length()</tt> must be even.
	 * 
	 * @param s
	 *            the hex string to pares.
	 * @return a byte array of length <tt>s.length() / 2</tt>.
	 * @throws IllegalArgumentException
	 *             if <tt>s</tt> is <tt>null</tt> or <tt>s.length()</tt> is not
	 *             even.
	 * @throws NumberFormatException
	 *             if the string cannot be parsed.
	 */
	public static byte[] parseHexString(String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));
		if (s.length() % 2 != 0)
			throw new IllegalArgumentException(
					"Hex string must contain an even number of characters");
		byte[] result = new byte[s.length() / 2];

		for (int i = 0; i < result.length; i++) {
			final int sIndex = i * 2;
			result[i] = (byte) Integer.parseInt(
					s.substring(sIndex, sIndex + 2), 16);
		}
		return result;
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

	private static final void appendHexLiteral(final StringBuilder sb,
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
	private static void toBytes(long l, byte[] bytes, int off) {
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
	 *             if something goes wrong creating the key.
	 */
	public static PublicKey getPublicKey() {
		final byte[] slPublicKeyBytes = new byte[] { (byte) 0x30, (byte) 0x82,
				(byte) 0x01, (byte) 0x22, (byte) 0x30, (byte) 0x0D,
				(byte) 0x06, (byte) 0x09, (byte) 0x2A, (byte) 0x86,
				(byte) 0x48, (byte) 0x86, (byte) 0xF7, (byte) 0x0D,
				(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05,
				(byte) 0x00, (byte) 0x03, (byte) 0x82, (byte) 0x01,
				(byte) 0x0F, (byte) 0x00, (byte) 0x30, (byte) 0x82,
				(byte) 0x01, (byte) 0x0A, (byte) 0x02, (byte) 0x82,
				(byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0xEA,
				(byte) 0xE5, (byte) 0x3C, (byte) 0xD2, (byte) 0xDD,
				(byte) 0x24, (byte) 0x75, (byte) 0x96, (byte) 0x84,
				(byte) 0x7E, (byte) 0xDD, (byte) 0x39, (byte) 0x15,
				(byte) 0x3B, (byte) 0x8D, (byte) 0x0F, (byte) 0xB4,
				(byte) 0xFA, (byte) 0xA4, (byte) 0xA2, (byte) 0x91,
				(byte) 0x77, (byte) 0xF6, (byte) 0xA5, (byte) 0xD1,
				(byte) 0x58, (byte) 0xB3, (byte) 0x1C, (byte) 0x7B,
				(byte) 0xD4, (byte) 0xA5, (byte) 0x54, (byte) 0x46,
				(byte) 0x61, (byte) 0x1C, (byte) 0x62, (byte) 0x6C,
				(byte) 0x07, (byte) 0xE3, (byte) 0x8D, (byte) 0xBE,
				(byte) 0xAC, (byte) 0xB5, (byte) 0xD6, (byte) 0x5E,
				(byte) 0xA7, (byte) 0xAD, (byte) 0x9A, (byte) 0x05,
				(byte) 0x57, (byte) 0xB9, (byte) 0x2F, (byte) 0x9E,
				(byte) 0xCB, (byte) 0xBB, (byte) 0xC4, (byte) 0xE0,
				(byte) 0xCC, (byte) 0x41, (byte) 0x68, (byte) 0x73,
				(byte) 0x6B, (byte) 0x52, (byte) 0x4D, (byte) 0x05,
				(byte) 0x0B, (byte) 0x68, (byte) 0x1E, (byte) 0xDE,
				(byte) 0xC1, (byte) 0x77, (byte) 0xB5, (byte) 0xF4,
				(byte) 0x28, (byte) 0x2B, (byte) 0x1B, (byte) 0x53,
				(byte) 0xCE, (byte) 0x44, (byte) 0x3B, (byte) 0x72,
				(byte) 0x19, (byte) 0x19, (byte) 0x2B, (byte) 0x6C,
				(byte) 0x34, (byte) 0x4F, (byte) 0x34, (byte) 0xF8,
				(byte) 0x2A, (byte) 0x7E, (byte) 0xF5, (byte) 0x2C,
				(byte) 0xB5, (byte) 0x28, (byte) 0xA0, (byte) 0xBA,
				(byte) 0x2A, (byte) 0xB7, (byte) 0x71, (byte) 0xE3,
				(byte) 0x70, (byte) 0x7A, (byte) 0x93, (byte) 0x0A,
				(byte) 0x21, (byte) 0xDB, (byte) 0xA3, (byte) 0x16,
				(byte) 0x6F, (byte) 0x81, (byte) 0xC0, (byte) 0xD3,
				(byte) 0x18, (byte) 0xC1, (byte) 0xEE, (byte) 0x34,
				(byte) 0x73, (byte) 0x28, (byte) 0x12, (byte) 0xA6,
				(byte) 0x67, (byte) 0xFC, (byte) 0x4C, (byte) 0xBD,
				(byte) 0x90, (byte) 0x0D, (byte) 0xC6, (byte) 0xB0,
				(byte) 0xA5, (byte) 0x65, (byte) 0x26, (byte) 0xC9,
				(byte) 0x04, (byte) 0xD1, (byte) 0xD1, (byte) 0x3D,
				(byte) 0xA7, (byte) 0x88, (byte) 0xC5, (byte) 0x86,
				(byte) 0x89, (byte) 0xF0, (byte) 0x07, (byte) 0xDC,
				(byte) 0x40, (byte) 0x64, (byte) 0x99, (byte) 0x01,
				(byte) 0xF4, (byte) 0x40, (byte) 0xF1, (byte) 0x23,
				(byte) 0xE4, (byte) 0x76, (byte) 0xC7, (byte) 0x76,
				(byte) 0xF5, (byte) 0x70, (byte) 0x95, (byte) 0xC1,
				(byte) 0x9E, (byte) 0xD1, (byte) 0x40, (byte) 0xB2,
				(byte) 0x9C, (byte) 0xD2, (byte) 0x77, (byte) 0x8E,
				(byte) 0x75, (byte) 0x73, (byte) 0xEC, (byte) 0x46,
				(byte) 0x6B, (byte) 0xCF, (byte) 0xB9, (byte) 0x5A,
				(byte) 0x2F, (byte) 0xB8, (byte) 0xEA, (byte) 0x89,
				(byte) 0x8A, (byte) 0xE4, (byte) 0x85, (byte) 0x76,
				(byte) 0xFA, (byte) 0x5A, (byte) 0xBA, (byte) 0x73,
				(byte) 0xFF, (byte) 0x97, (byte) 0xD5, (byte) 0x37,
				(byte) 0xE4, (byte) 0x38, (byte) 0x18, (byte) 0x20,
				(byte) 0xF5, (byte) 0xB3, (byte) 0xEB, (byte) 0x95,
				(byte) 0x45, (byte) 0xE4, (byte) 0x54, (byte) 0x7F,
				(byte) 0x7C, (byte) 0xA3, (byte) 0x80, (byte) 0xBE,
				(byte) 0x21, (byte) 0xFB, (byte) 0x70, (byte) 0x2A,
				(byte) 0x46, (byte) 0xDC, (byte) 0x35, (byte) 0x3B,
				(byte) 0xC8, (byte) 0x1F, (byte) 0x05, (byte) 0xF1,
				(byte) 0x7B, (byte) 0xEA, (byte) 0xB6, (byte) 0xAC,
				(byte) 0xF9, (byte) 0x31, (byte) 0x28, (byte) 0xD3,
				(byte) 0xE5, (byte) 0xC5, (byte) 0xA2, (byte) 0x92,
				(byte) 0x1F, (byte) 0xB7, (byte) 0x81, (byte) 0x1F,
				(byte) 0x4A, (byte) 0x26, (byte) 0x9B, (byte) 0x52,
				(byte) 0x80, (byte) 0x9E, (byte) 0x9A, (byte) 0xF0,
				(byte) 0x3F, (byte) 0xB7, (byte) 0x7F, (byte) 0x4E,
				(byte) 0xEA, (byte) 0x13, (byte) 0xCC, (byte) 0xC6,
				(byte) 0x10, (byte) 0xA2, (byte) 0x81, (byte) 0x02,
				(byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01 };
		final PublicKey result;
		try {
			// create public key
			final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					slPublicKeyBytes);
			final KeyFactory kf = KeyFactory.getInstance("RSA");
			result = kf.generatePublic(publicKeySpec);
		} catch (Exception e) {
			/* Lots could go wrong, anything at all would indicate a bug */
			throw new IllegalStateException(I18N.err(178), e);
		}
		return result;
	}

	/**
	 * Generates and returns a signature for the passed data using the passed
	 * RSA private key.
	 * 
	 * @param data
	 *            the data to generate a signature for.
	 * @param key
	 *            the RSA private key to use.
	 * @return the signature.
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 * @throws IllegalStateException
	 *             if something goes wrong during the signature generation.
	 */
	public static byte[] getSignature(byte[] data, PrivateKey key) {
		if (data == null)
			throw new IllegalArgumentException(I18N.err(44, "data"));
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));

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
	 *            the data the signature is about.
	 * @param signature
	 *            the signature.
	 * @param key
	 *            the RSA public key to use.
	 * @return {@code true} if the signature is valid, {@code false} otherwise.
	 */
	public static boolean checkSignature(byte[] data, byte[] signature,
			PublicKey key) {
		if (data == null)
			throw new IllegalArgumentException(I18N.err(44, "data"));
		if (signature == null)
			throw new IllegalArgumentException(I18N.err(44, "signature"));

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
	 *            the data the signature is about.
	 * @param signature
	 *            the signature.
	 * @return {@code true} if the signature is valid, {@code false} otherwise.
	 * @see #getPublicKey()
	 */
	public static boolean checkSignature(byte[] data, byte[] signature) {
		return checkSignature(data, signature, getPublicKey());
	}

	/**
	 * Line wraps the passed string at the specified width by inserting
	 * newlines. The last line may not be the specified length.
	 * 
	 * @param b
	 *            the string to wrap.
	 * @param linewidth
	 *            the desired line width.
	 */
	public static void wrap(StringBuilder b, int linewidth) {
		int workIndex = linewidth;
		while (b.length() > workIndex) {
			b.insert(workIndex, '\n');
			workIndex = workIndex + linewidth + 1;
		}
	}

	/**
	 * Line wraps the passed string at the specified width by inserting
	 * newlines. The last line may not be the specified length.
	 * 
	 * @param s
	 *            the string to wrap.
	 * @param linewidth
	 *            the desired line width.
	 * @return the wrapped string.
	 */
	public static String wrap(String s, int linewidth) {
		StringBuilder b = new StringBuilder(s);
		wrap(b, linewidth);
		return b.toString();
	}

	private SLUtility() {
		// no instances
	}
}
