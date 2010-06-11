package com.surelogic.common.license;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import com.surelogic.NotThreadSafe;
import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * A utility that handles encoding and decoding to and from strings and files of
 * {@link SLLicense} objects.
 * <p>
 * The (unencoded) string representation of a license instance, referenced by
 * <tt>license</tt>, is as follows:
 * <ul>
 * <li>The string constant {@link #UUID_LABEL} followed by
 * <tt>license.getUuid().toString()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #HOLDER_LABEL} followed by
 * <tt>license.getHolder()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #PRODUCT_LABEL} followed by
 * <tt>license.getProduct()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #DATE_LABEL} followed by
 * <tt>license.getDate()</tt> converted to a string using
 * {@link SLLicenseUtility#getThreadSafeDateFormat()}.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #TYPE_LABEL} followed by
 * <tt>license.getType().toString()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #MAXACTIVE_LABEL} followed by
 * <tt>Integer.toString(license.getMaxActive())</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #PERFORMNETCHECK_LABEL} followed by
 * <tt>Boolean.toString(license.performNetCheck())</tt>.
 * <li>The character {@link #SEP}.
 * </ul>
 * An example of this format is:
 * 
 * <pre>
 * id=58eab27c-e416-4b76-9225-49783707056f
 * holder=Tim
 * product=Flashlight
 * date=2010-06-11
 * type=P
 * maxActive=2
 * performNetCheck=true
 * </pre>
 * 
 * The encoded digitally signed license format is a stream of continuous
 * characters as follows:
 * <ul>
 * <li>The string constant {@link #BEGIN}</li>
 * <li>A hex string representation of the digital signature bytes</li>
 * <li>The string constant {@link #MIDDLE}</li>
 * <li>A hex string representation of the license data bytes (produced from
 * <tt>toString(license).getBytes()</tt>)</li>
 * <li>The string constant {@link #END}</li>
 * </ul>
 * The stream is wrapped at {@link #LINEWRAP} characters. An example of this
 * format is:
 * 
 * <pre>
 * [sl-#v1.0#(7dac0784358bd7605c1780ab11ef3b903e144cf298c66e3dd6898d35a
 * 05b116084b5882ccb4bc532e63ab0952b5696c894b267b98b0e72a239eda9698d4be
 * 9c62bd8966e57ec912f5efd2eda3c420961908bcc27546abfc81984b31d7a22edd4f
 * 501c17c35485cec2ffa1358bda629a5120695dee30d27d7a546363a3caf3808368f3
 * 48e4a7e1edb3293f65561f8a631b2e7cd2e6c8425f31b1a69d9e3b84384a9e58454d
 * 961d3429c422459189c078eed1a2791292838b3635ad678430affa959b18219861d8
 * 7602a73dc2fb047f905a09c8726cdb14bf9966192988c63cc114e7bc7699fe6b0d63
 * 5c3c913bf3c3696cd25e7cddd41fd273cf3f66f1a41d9ad8fbO69643d36336235366
 * 538612d623636622d346133382d613539662d3437333164343864323065330a686f6
 * c6465723d54696d0a70726f647563743d466c6173686c696768740a646174653d323
 * 031302d30362d31310a747970653d500a6d61784163746976653d320a706572666f7
 * 26d4e6574436865636b3d747275650a7e1da)]
 * </pre>
 * 
 * @see SLLicense
 * @see SLLicenseUtility
 */
@NotThreadSafe
public final class SLLicensePersistence {

	private static final String DATE_LABEL = "date=";
	private static final String HOLDER_LABEL = "holder=";
	private static final String MAXACTIVE_LABEL = "maxActive=";
	private static final String PERFORMNETCHECK_LABEL = "performNetCheck=";
	private static final String PRODUCT_LABEL = "product=";
	private static final String TYPE_LABEL = "type=";
	private static final String UUID_LABEL = "id=";
	private static final String BEGIN = "[sl-#v1.0#(7da";
	private static final String MIDDLE = "O";
	private static final String END = "7e1da)]";
	private static final char SEP = '\n';
	private static final int LINEWRAP = 68;

	/**
	 * Converts a license to a string representation. The
	 * {@link #toLicense(String)} method reverses this conversion.
	 * 
	 * @param license
	 *            the license
	 * @return the string representation of the license.
	 */
	public static String toString(final SLLicense license) {
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));
		final StringBuilder b = new StringBuilder();
		SimpleDateFormat sdf = SLLicenseUtility.getThreadSafeDateFormat();
		b.append(UUID_LABEL).append(license.getUuid().toString()).append(SEP);
		b.append(HOLDER_LABEL).append(license.getHolder()).append(SEP);
		b.append(PRODUCT_LABEL).append(license.getProduct()).append(SEP);
		b.append(DATE_LABEL).append(sdf.format(license.getDate())).append(SEP);
		b.append(TYPE_LABEL).append(license.getType().toString()).append(SEP);
		b.append(MAXACTIVE_LABEL).append(
				Integer.toString(license.getMaxActive())).append(SEP);
		b.append(PERFORMNETCHECK_LABEL).append(
				Boolean.toString(license.performNetCheck())).append(SEP);
		return b.toString();
	}

	/**
	 * Converts a string representation to a license. The string representation
	 * should have been created by a call to {@link #toLicense(String)}. If the
	 * string is not well-formed a {@code null} is returned and a message about
	 * what is wrong with the string is logged.
	 * 
	 * @param value
	 *            the string representation of the license.
	 * @return the license, or {@code null} if the string is not well-formed.
	 */
	public static SLLicense toLicense(final String value) {
		/*
		 * Parse the the {@link UUID} that identifies this license.
		 */
		final String uuidS = getValue(value, UUID_LABEL);
		if (uuidS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, UUID_LABEL, value));
			return null;
		}
		final UUID uuid;
		try {
			uuid = UUID.fromString(uuidS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(188, uuidS, value), e);
			return null;
		}

		/*
		 * Parse the the name of the license holder.
		 */
		final String holder = getValue(value, HOLDER_LABEL);
		if (holder == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, HOLDER_LABEL, value));
			return null;
		}

		/*
		 * Parse name of the product being licensed.
		 */
		final String product = getValue(value, PRODUCT_LABEL);
		if (product == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, PRODUCT_LABEL, value));
			return null;
		}

		/*
		 * Parse license expiration date or renewal deadline.
		 */
		final String dateS = getValue(value, DATE_LABEL);
		if (dateS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, DATE_LABEL, value));
			return null;
		}
		final Date date;
		try {
			SimpleDateFormat sdf = SLLicenseUtility.getThreadSafeDateFormat();
			date = sdf.parse(dateS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(189, dateS, value), e);
			return null;
		}

		/*
		 * Parse the type of the license.
		 */
		final String typeS = getValue(value, TYPE_LABEL);
		if (typeS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, TYPE_LABEL, value));
			return null;
		}
		final SLLicenseType type = SLLicenseType.fromString(typeS);
		if (type == null) {
			SLLogger.getLogger()
					.log(Level.WARNING, I18N.err(190, typeS, value));
			return null;
		}

		/*
		 * Parse the number of active installations allowed before an attempted
		 * installation fails. This value must be greater than zero.
		 */
		final String maxActiveS = getValue(value, MAXACTIVE_LABEL);
		if (maxActiveS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, MAXACTIVE_LABEL, value));
			return null;
		}
		final int maxActive;
		try {
			maxActive = Integer.parseInt(maxActiveS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(191, maxActiveS, value), e);
			return null;
		}

		/*
		 * Parse if a net check is required for installation of this license.
		 */
		final String performNetCheckS = getValue(value, PERFORMNETCHECK_LABEL);
		if (performNetCheckS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, PERFORMNETCHECK_LABEL, value));
			return null;
		}
		/*
		 * Here we enable net checks if the string is not strictly, ignoring
		 * case, "false". The call Boolean.getBoolean does the opposite, which
		 * would err toward a false value, disabling net checks, and not a true
		 * one.
		 */
		final boolean performNetCheck = !("false"
				.equalsIgnoreCase(performNetCheckS));

		/*
		 * Try to construct the license object.
		 */
		final SLLicense result;
		try {
			result = new SLLicense(uuid, holder, product, date, type,
					maxActive, performNetCheck);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING, I18N.err(193, value), e);
			return null;
		}
		return result;
	}

	/**
	 * A helper method to extract a particular value for a particular key from a
	 * property-file like string containing key-value pairs.
	 * <p>
	 * For example, if <tt>s</tt> is:
	 * 
	 * <pre>
	 * holder=ABC Company, Inc.
	 * date=2010-02-02
	 * </pre>
	 * 
	 * and <tt>key</tt> is <tt>"holder="</tt> the returned value is
	 * <tt>"ABC Company, Inc."</tt>. if <tt>s</tt> remains the same and
	 * <tt>key</tt> is <tt>"foo="</tt> the returned value will be <tt>null</tt>.
	 * <p>
	 * The line break must be (or start with) the character {@link #SEP}. If
	 * {@link #SEP} is not found after the passed key then the remainder of the
	 * string is returned as the value.
	 * 
	 * @param s
	 *            a string containing key-value pairs.
	 * @param key
	 *            a key (including the <tt>=</tt> character).
	 * @return the value, or {@code null} if the key is not found in <tt>s</tt>.
	 */
	private static String getValue(final String s, final String key) {
		final int keyIndex = s.indexOf(key);
		if (keyIndex == -1)
			return null;
		final int valueIndex = keyIndex + key.length();
		final int eolIndex = s.indexOf(SEP, valueIndex);
		if (eolIndex == -1)
			return s.substring(valueIndex);
		else
			return s.substring(valueIndex, eolIndex);
	}

	/**
	 * Converts a license to a digitally signed encoded string.
	 * 
	 * @param license
	 *            the license.
	 * @param key
	 *            a RSA private key used to digitally sign the data.
	 * @return the digitally signed encoded string representation of
	 *         <tt>license</tt>.
	 */
	public static String toSignedString(final SLLicense license,
			final PrivateKey key) {
		StringBuilder b = new StringBuilder();

		final byte[] data = toString(license).getBytes();
		final byte[] signature = SLUtility.getSignature(data, key);

		b.append(BEGIN);
		b.append(SLUtility.toHexString(signature));
		b.append(MIDDLE);
		b.append(SLUtility.toHexString(data));
		b.append(END);

		SLUtility.wrap(b, LINEWRAP);

		return b.toString();
	}

	/**
	 * Writes out a file that contains a license as a digitally signed encoded
	 * string.
	 * 
	 * @param license
	 *            the license.
	 * @param out
	 *            the file to create or overwrite.
	 * @param key
	 *            a RSA private key used to digitally sign the data.
	 */
	public static void outputToSignedFile(final SLLicense license, File out,
			PrivateKey key) {
		FileUtility.putStringIntoAFile(out, toSignedString(license, key));
	}

	/**
	 * Converts the encoded digitally signed license string to a usable license
	 * object, or {@code null} if the string is not well-formed, improperly
	 * signed, or missing required license data.
	 * <p>
	 * If the conversion fails (and {@code null} is returned) then diagnostic
	 * information is written to the log about what went wrong.
	 * 
	 * @param s
	 *            the encoded digitally signed license string.
	 * @param key
	 *            a RSA public key used to check the digital signature.
	 * @return a license object or {@code null} if <tt>s</tt> is not
	 *         well-formed, improperly signed, or missing required license data.
	 */
	public static SLLicense toLicense(final String s, final PublicKey key) {
		/*
		 * Trim off any extra spaces or tabs and use a mutable string.
		 */
		StringBuffer b = new StringBuffer(s.trim());

		/*
		 * Remove any newlines from the string (it was line-wrapped).
		 */
		while (true) {
			int newlineIndex = b.indexOf("\n");
			if (newlineIndex == -1)
				break;
			b.delete(newlineIndex, newlineIndex + 1);
		}

		/*
		 * Trim off any extra characters before or after the signed license
		 * data.
		 */
		final int startIndex = b.indexOf(BEGIN);
		if (startIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(181, s));
			return null;
		}
		if (b.length() <= startIndex + BEGIN.length()) {
			SLLogger.getLogger().warning(I18N.err(182, s));
			return null;
		}
		b.delete(0, startIndex + BEGIN.length());
		final int endIndex = b.indexOf(END);
		if (endIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(183, s));
			return null;
		}
		b.delete(endIndex, b.length());

		/*
		 * Separate the data from the digital signature.
		 */
		final int sigEndIndex = b.indexOf(MIDDLE);
		final int dataStartIndex = sigEndIndex + MIDDLE.length();
		if (sigEndIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(184, s));
			return null;
		}
		if (b.length() <= dataStartIndex) {
			SLLogger.getLogger().warning(I18N.err(185, s));
			return null;
		}
		String signatureS = b.substring(0, sigEndIndex);
		String dataS = b.substring(dataStartIndex);
		final byte[] signature;
		byte[] data;
		try {
			signature = SLUtility.parseHexString(signatureS);
			data = SLUtility.parseHexString(dataS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING, I18N.err(186, s), e);
			return null;
		}

		/*
		 * Check that the data is properly signed. This checks, with some
		 * assurance that SureLogic produced the data.
		 */
		final boolean valid = SLUtility.checkSignature(data, signature, key);

		if (valid) {
			String licenseInfo = new String(data);
			return toLicense(licenseInfo);
		} else {
			SLLogger.getLogger().warning(I18N.err(195, s));
			return null;
		}
	}

	/**
	 * Reads a file that contains a license as a digitally signed encoded string
	 * and converts the string to a license.
	 * 
	 * @param in
	 *            the file to read.
	 * @param key
	 *            a RSA public key used to check the digital signature.
	 * @return a license object or {@code null} if the contents of the file are
	 *         not well-formed, are improperly signed, or are missing required
	 *         license data.
	 */
	public static SLLicense inputFromSignedFile(File in, PublicKey key) {
		final String fileContents = FileUtility.getFileContentsAsString(in);
		SLLicense result = toLicense(fileContents, key);
		return result;
	}

	private SLLicensePersistence() {
		// no instances
	}
}
