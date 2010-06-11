package com.surelogic.common.license;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

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

	public static SLLicense toLicense(final String value) {
		/*
		 * Parse the the {@link UUID} that identifies this license.
		 */
		final String uuidS = getValue(value, UUID_LABEL);
		if (uuidS == null)
			return null;
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

		/*
		 * Parse name of the product being licensed.
		 */
		final String product = getValue(value, PRODUCT_LABEL);

		/*
		 * Parse license expiration date or renewal deadline.
		 */
		final String dateS = getValue(value, DATE_LABEL);
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
		final boolean performNetCheck;
		try {
			performNetCheck = Boolean.parseBoolean(performNetCheckS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(192, performNetCheckS, value), e);
			return null;
		}

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

	private static String getValue(final String value, final String key) {
		final int keyIndex = value.indexOf(key);
		if (keyIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(187, key));
			return null;
		}
		final int valueIndex = keyIndex + key.length();
		final int eolIndex = value.indexOf(SEP, valueIndex);
		if (eolIndex == -1)
			return value.substring(valueIndex);
		else
			return value.substring(valueIndex, eolIndex);
	}

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

		SLUtility.wrap(b, 60);

		return b.toString();
	}

	public static void outputToSignedFile(final SLLicense license, File out,
			PrivateKey key) {
		FileUtility.putStringIntoAFile(out, toSignedString(license, key));
	}

	/**
	 * Converts the encoded signed license string to a usable license object, or
	 * {@code null} if the string is not well-formed, improperly signed, or
	 * missing required license data.
	 * 
	 * @param s
	 *            the encoded signed license string.
	 * @param key
	 *            the public key used to check the data is signed.
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

	public static SLLicense inputFromSignedFile(File in, PublicKey key) {
		final String fileContents = FileUtility.getFileContentsAsString(in);
		SLLicense result = toLicense(fileContents, key);
		return result;
	}

	private SLLicensePersistence() {
		// no instances
	}
}
