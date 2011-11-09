package com.surelogic.common.license;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import com.surelogic.NotThreadSafe;
import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * A utility that handles encoding and decoding to and from strings and files of
 * {@link SLLicense} objects and {@link SLLicenseNetCheck} objects.
 * <p>
 * <b>SLLicense persistence formats</b>
 * <p>
 * The hex encoded string representation of a license instance, referenced by
 * <tt>license</tt>, is as follows:
 * <ul>
 * <li>The string constant {@link #UUID_LABEL} followed by
 * <tt>license.getUuid().toString()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #HOLDER_LABEL} followed by
 * <tt>license.getHolder()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #PRODUCT_LABEL} followed by
 * <tt>license.getProduct().toString()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #DURATION_LABEL} followed by
 * <tt>Integer.toString(license.getDurationInDays())</tt>.
 * <li>The character {@link #SEP}.
 * <li>(Optional) The string constant {@link #INSTALLATION_DEADLINE_LABEL}
 * followed by <tt>SLUtility.toStringDay(license.getInstallBeforeDate())</tt>.
 * <li>(Optional) The character {@link #SEP}.
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
 * An example of this format that specifies a duration is:
 * 
 * <pre>
 * id=58eab27c-e416-4b76-9225-49783707056f
 * holder=Tim
 * product=Flashlight
 * durationInDays=365
 * type=Perpetual
 * maxActive=2
 * performNetCheck=true
 * </pre>
 * 
 * An example of this format that specifies an installation deadline is:
 * 
 * <pre>
 * id=58eab27c-e416-4b76-9225-497837070544
 * holder=Tim
 * product=JSure
 * durationInDays=60
 * installationDeadline=2012-06-11
 * type=Support
 * maxActive=2
 * performNetCheck=true
 * </pre>
 * 
 * <p>
 * The encoded digitally signed license format is a stream of continuous
 * characters as follows:
 * <ul>
 * <li>The string constant {@link #BEGIN_LICENSE}</li>
 * <li>A hex string representation of the digital signature bytes</li>
 * <li>The string constant {@link #MIDDLE}</li>
 * <li>A hex string representation of the license data bytes (produced from
 * <tt>toString(license).getBytes()</tt>)</li>
 * <li>The string constant {@link #END_LICENSE}</li>
 * </ul>
 * The stream is wrapped at {@link #LINEWRAP} characters. An example of this
 * format is:
 * 
 * <pre>
 * [sl-#v1.0#(7da088c4b6c9c88097221636d32f5a3ea892b69a757bb4d358fdcd8b4
 * a0190d65b9b4926409affae8c3fb3b5ddc748fbc2e4dd93b7c7c68fb1f1600b30cf0
 * 290d852d696b0aaf082ec78b54ebc1441863ca5b0f0916054b6b22ed6d9675db5490
 * 77e91e35dd869c531031b24b4190d472700cea7629debc788624be16efade18a84da
 * 53cac8ba322bd8666d88569bece949ee4e75ca8afe95fe5ac79b4f325b01b13482e1
 * fd9d01451bf3017d8c5acc9c011ad0b962fa8ce52e922ea04c7bfb825350ffa20f74
 * cc5d12b2cc119754f4c0e34a207d5d80e5b0e22373ea94658dfa75c47cd918c2b035
 * 9c1bac04012bc962eeadf133201b5acaded33692f1e78ef66cO69643d63333836636
 * 236612d393931652d346132622d623133612d3732306636623533383163360a686f6
 * c6465723d54696d0a70726f647563743d466c6173686c696768740a6475726174696
 * f6e496e446179733d3336350a747970653d500a6d61784163746976653d330a70657
 * 2666f726d4e6574436865636b3d747275650a7e1da)]
 * </pre>
 * 
 * <b>SLLicenseNetCheck persistence formats</b>
 * <p>
 * The format of license net check instances is similar to the format used by
 * license instances.
 * <p>
 * The hex encoded string representation of a license net check instance,
 * referenced by <tt>nc</tt>, is as follows:
 * <ul>
 * <li>The string constant {@link #UUID_LABEL} followed by
 * <tt>nc.getUuid().toString()</tt>.
 * <li>The character {@link #SEP}.
 * <li>The string constant {@link #DATE_LABEL} followed by
 * <tt>SLUtility.toStringDay(licenseNetCheck.getDate())</tt>.
 * <li>The character {@link #SEP}.
 * </ul>
 * An example of this format is:
 * 
 * <pre>
 * id=58eab27c-e416-4b76-9225-49783707056f
 * date=2010-06-11
 * </pre>
 * 
 * The encoded digitally signed license net check format is a stream of
 * continuous characters as follows:
 * <ul>
 * <li>The string constant {@link #BEGIN_NET_CHECK}</li>
 * <li>A hex string representation of the digital signature bytes</li>
 * <li>The string constant {@link #MIDDLE}</li>
 * <li>A hex string representation of the license data bytes (produced from
 * <tt>toString(nc).getBytes()</tt>)</li>
 * <li>The string constant {@link #END_NET_CHECK}</li>
 * </ul>
 * The stream is wrapped at {@link #LINEWRAP} characters. An example of this
 * format is:
 * 
 * <pre>
 * [sl-nc-#v1.0#(5f0940e201bce17b785ea73588755f55794dadac635ef6e0735bbd
 * aa1edf323b22db461a32a296cac956951d12929cad837af43745c3ffe8e3bb260e5c
 * f7e1bf3e500b0fd6b5019433b6d9c0b2cef6001a81ca1f3a535c22f09055c305a957
 * d08d5454fcbfc62859c6d4ccbbddd96b001bffc0388860f514a9e224dd0819190065
 * 11e81b1d8ec98e83f44c2c903e56f70d80e61ac0e89420e3c45606f86ad856cb62bd
 * e90696e71c05a8d4a25dae9f918cd235b4dea05806b55b627a067b6a9a80ece6e758
 * 51ba7e29b4326900d835000db3d983b5faf6c1b1bd37963f24d0574696b9a5afcaf5
 * 84d759341b91659e81c63aa670aa14b14efde3e445c3891f5f6f7O69643d31323534
 * 323266612d656163332d343330612d396637382d3238656339393332383839390a64
 * 6174653d323031302d30362d31310a6ae)]
 * </pre>
 * 
 * @see SLLicense
 * @see SLLicenseNetCheck
 * @see SLLicenseUtility
 */
@NotThreadSafe
public final class SLLicensePersistence {

	private static final String DATE_LABEL = "date=";
	private static final String DURATION_LABEL = "durationInDays=";
	private static final String HOLDER_LABEL = "holder=";
	private static final String INSTALLATION_DEADLINE_LABEL = "installationDeadline=";
	private static final String MAXACTIVE_LABEL = "maxActive=";
	private static final String PERFORMNETCHECK_LABEL = "performNetCheck=";
	private static final String PRODUCT_LABEL = "product=";
	private static final String TYPE_LABEL = "type=";
	private static final String UUID_LABEL = "id=";

	private static final String BEGIN_LICENSE = "[sl-#v1.0#(7da";
	private static final String END_LICENSE = "7e1da)]";

	private static final String BEGIN_NET_CHECK = "[sl-nc-#v1.0#(5f0";
	private static final String END_NET_CHECK = "6ae)]";

	private static final String MIDDLE = "O";
	private static final char SEP = '\n';

	private static final int LINEWRAP = 68;

	/**
	 * Converts a license to a string representation. The
	 * {@link #toLicense(String)} method reverses this conversion.
	 * 
	 * @param license
	 *            a license
	 * @return the string representation of the license.
	 */
	public static String toString(final SLLicense license) {
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));
		final StringBuilder b = new StringBuilder();
		b.append(UUID_LABEL).append(license.getUuid().toString()).append(SEP);
		b.append(HOLDER_LABEL).append(license.getHolder()).append(SEP);
		b.append(PRODUCT_LABEL).append(license.getProduct().toString()).append(
				SEP);
		b.append(DURATION_LABEL).append(
				Integer.toString(license.getDurationInDays())).append(SEP);
		b.append(INSTALLATION_DEADLINE_LABEL).append(
				SLUtility.toStringDay(license.getInstallBeforeDate())).append(
				SEP);
		b.append(TYPE_LABEL).append(license.getType().toString()).append(SEP);
		b.append(MAXACTIVE_LABEL).append(
				Integer.toString(license.getMaxActive())).append(SEP);
		b.append(PERFORMNETCHECK_LABEL).append(
				Boolean.toString(license.performNetCheck())).append(SEP);
		return b.toString();
	}

	/**
	 * Converts a license net check to a string representation.
	 * 
	 * @param licenseNetCheck
	 *            a license net check.
	 * @return the string representation of the license net check.
	 */
	public static String toString(final SLLicenseNetCheck licenseNetCheck) {
		if (licenseNetCheck == null)
			throw new IllegalArgumentException(I18N.err(44, "licenseNetCheck"));
		final StringBuilder b = new StringBuilder();
		b.append(UUID_LABEL).append(licenseNetCheck.getUuid().toString())
				.append(SEP);
		b.append(DATE_LABEL).append(
				SLUtility.toStringDay(licenseNetCheck.getDate())).append(SEP);
		return b.toString();
	}

	/**
	 * Converts a string representation to a license. The string representation
	 * should have been created by a call to {@link #toString(SLLicense)}. If
	 * the string is not well-formed a {@code null} is returned and a message
	 * about what is wrong with the string is logged.
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
		final String productS = getValue(value, PRODUCT_LABEL);
		if (productS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, PRODUCT_LABEL, value));
			return null;
		}
		final SLLicenseProduct product = SLLicenseProduct.fromString(productS);
		if (product == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(199, productS, value));
			return null;
		}

		/*
		 * Parse license license duration in days from installation until
		 * expiration or renewal.
		 */
		final String durationInDaysS = getValue(value, DURATION_LABEL);
		if (durationInDaysS == null) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(187, DURATION_LABEL, value));
			return null;
		}
		final int durationInDays;
		try {
			durationInDays = Integer.parseInt(durationInDaysS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(191, durationInDaysS, value), e);
			return null;
		}

		/*
		 * Parse license installation deadline. This value is optional so it may
		 * not exist.
		 */
		final String dateS = getValue(value, INSTALLATION_DEADLINE_LABEL);
		final Date installBeforeDate;
		if (dateS == null) {
			installBeforeDate = null;
		} else {
			try {
				installBeforeDate = SLUtility.fromStringDay(dateS);
			} catch (Exception e) {
				SLLogger.getLogger().log(Level.WARNING,
						I18N.err(189, dateS, value), e);
				return null;
			}
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
			result = new SLLicense(uuid, holder, product, durationInDays,
					installBeforeDate, type, maxActive, performNetCheck);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING, I18N.err(193, value), e);
			return null;
		}
		return result;
	}

	/**
	 * Converts a string representation to a license net check. The string
	 * representation should have been created by a call to
	 * {@link #toString(SLLicenseNetCheck)}. If the string is not well-formed a
	 * {@code null} is returned and a message about what is wrong with the
	 * string is logged.
	 * 
	 * @param value
	 *            the string representation of the license net check.
	 * @return the license net check, or {@code null} if the string is not
	 *         well-formed.
	 */
	public static SLLicenseNetCheck toLicenseNetCheck(final String value) {
		/*
		 * Parse the the {@link UUID} that identifies the license this net check
		 * refers to.
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
			date = SLUtility.fromStringDay(dateS);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(189, dateS, value), e);
			return null;
		}

		/*
		 * Try to construct the license net check object.
		 */
		final SLLicenseNetCheck result;
		try {
			result = new SLLicenseNetCheck(uuid, date);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.WARNING, I18N.err(197, value), e);
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
	 * Converts an array of data to a digitally signed encoded hex string.
	 * 
	 * @param data
	 *            the data
	 * @param key
	 *            a RSA private key used to digitally sign the data.
	 * @param beginMark
	 *            a known string to mark the beginning of the encoded digital
	 *            signature.
	 * @param middleMark
	 *            a known string to separate the digital signature from the
	 *            data.
	 * @param endMark
	 *            a known string to mark the end of the data.
	 * @param linewrap
	 *            a width (in characters) to line wrap the returned string, a
	 *            value less than 1 indicates no wrapping is to be performed.
	 * @return a digitally signed encoded hex string representation (perhaps
	 *         line wrapped) of <tt>data</tt>
	 */
	public static String toSignedHexString(final byte[] data,
			final PrivateKey key, final String beginMark,
			final String middleMark, final String endMark, final int linewrap) {
		StringBuilder b = new StringBuilder();

		final byte[] signature = SLUtility.getSignature(data, key);

		b.append(beginMark);
		b.append(SLUtility.toHexString(signature));
		b.append(middleMark);
		b.append(SLUtility.toHexString(data));
		b.append(endMark);

		if (linewrap > 0)
			SLUtility.wrap(b, linewrap);

		return b.toString();
	}

	/**
	 * Converts a license to a digitally signed encoded hex string.
	 * 
	 * @param license
	 *            a license.
	 * @param key
	 *            a RSA private key used to digitally sign the data.
	 * @param linewrap
	 *            {@code true} if the returned string should be line wrapped,
	 *            {@code false} for no line wrapping.
	 * @return the digitally signed encoded hex string representation of
	 *         <tt>license</tt>.
	 */
	public static String toSignedHexString(final SLLicense license,
			final PrivateKey key, final boolean linewrap) {
		final byte[] data = toString(license).getBytes();
		return toSignedHexString(data, key, BEGIN_LICENSE, MIDDLE, END_LICENSE,
				linewrap ? LINEWRAP : 0);
	}

	/**
	 * Converts a license net check to a digitally signed encoded hex string.
	 * 
	 * @param licenseNetCheck
	 *            a license net check.
	 * @param key
	 *            a RSA private key used to digitally sign the data.
	 * @param linewrap
	 *            {@code true} if the returned string should be line wrapped,
	 *            {@code false} for no line wrapping.
	 * @return the digitally signed encoded hex string representation of
	 *         <tt>licenseNetCheck</tt>.
	 */
	public static String toSignedHexString(
			final SLLicenseNetCheck licenseNetCheck, final PrivateKey key,
			final boolean linewrap) {
		final byte[] data = toString(licenseNetCheck).getBytes();
		return toSignedHexString(data, key, BEGIN_NET_CHECK, MIDDLE,
				END_NET_CHECK, linewrap ? LINEWRAP : 0);
	}

	/**
	 * Converts a list of {@link PossiblyActivatedSLLicense} objects to a string
	 * that contains a set of licenses and license net checks as digitally
	 * signed encoded hex strings and outputs the string to a file.
	 * 
	 * @param newFile
	 *            a text file. If this file exists its contents will be
	 *            replaced, if not it will be created.
	 * @param licenses
	 *            the list of {@link PossiblyActivatedSLLicense} objects to
	 *            convert.
	 * 
	 * @see SLLicensePersistence#toSignedHexString(List)
	 */
	public static void writeLicensesToFile(File newFile,
			final List<PossiblyActivatedSLLicense> licenses) {
		if (newFile == null)
			throw new IllegalArgumentException(I18N.err(44, "newFile"));
		if (licenses == null)
			throw new IllegalArgumentException(I18N.err(44, "licenses"));

		final String output = toSignedHexString(licenses, true);
		FileUtility.putStringIntoAFile(newFile, output);
	}

	/**
	 * Converts a single {@link PossiblyActivatedSLLicense} object to a string
	 * that contains a licenses and, optionally, a license net check as
	 * digitally signed encoded hex strings and outputs the string to a file.
	 * 
	 * @param newFile
	 *            a text file. If this file exists its contents will be
	 *            replaced, if not it will be created.
	 * @param license
	 *            AN {@link PossiblyActivatedSLLicense} object to convert.
	 * 
	 * @see #writeLicensesToFile(File, List)
	 */
	public static void writeLicenseToFile(File newFile,
			PossiblyActivatedSLLicense license) {
		if (newFile == null)
			throw new IllegalArgumentException(I18N.err(44, "newFile"));
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));

		final List<PossiblyActivatedSLLicense> licenses = new ArrayList<PossiblyActivatedSLLicense>();
		licenses.add(license);
		writeLicensesToFile(newFile, licenses);
	}

	/**
	 * Converts a list of {@link PossiblyActivatedSLLicense} objects to a string
	 * that contains a set of licenses and license net checks as digitally
	 * signed encoded hex strings.
	 * 
	 * @param licenses
	 *            the list of {@link PossiblyActivatedSLLicense} objects to
	 *            convert.
	 * @param linewrap
	 *            {@code true} if the returned string should be line wrapped,
	 *            {@code false} for no line wrapping.
	 * @return a string that contains a set of licenses and license net checks
	 *         as digitally signed encoded hex strings.
	 */
	public static String toSignedHexString(
			final List<PossiblyActivatedSLLicense> licenses,
			final boolean linewrap) {
		if (licenses == null)
			throw new IllegalArgumentException(I18N.err(44, "licenses"));

		final StringBuilder b = new StringBuilder();
		for (PossiblyActivatedSLLicense license : licenses) {
			b.append(license.getSignedSLLicense().getSignedHexString());
			SignedSLLicenseNetCheck nc = license.getSignedSLLicenseNetCheck();
			if (nc != null) {
				b.append(nc.getSignedHexString());
			}
		}

		if (linewrap) {
			SLUtility.wrap(b, LINEWRAP);
		}
		return b.toString();
	}

	/**
	 * Extract the data from an encoded digitally signed hex string. A {@code
	 * null} results if the string is not well-formed or its is improperly
	 * signed.
	 * <p>
	 * If the conversion fails (and {@code null} is returned) then diagnostic
	 * information is written to the log about what went wrong.
	 * 
	 * @param s
	 *            an encoded digitally signed hex string.
	 * @param key
	 *            a RSA public key used to check the digital signature.
	 * @param beginMark
	 *            a known string to mark the beginning of the encoded digital
	 *            signature.
	 * @param middleMark
	 *            a known string to separate the digital signature from the
	 *            data.
	 * @param endMark
	 *            a known string to mark the end of the data.
	 * @return the data, or {@code null} if <tt>s</tt> is not well-formed or it
	 *         is improperly signed.
	 */
	public static byte[] getData(final String s, final PublicKey key,
			final String beginMark, final String middleMark,
			final String endMark) {
		/*
		 * Trim off any extra spaces or tabs and use a mutable string.
		 */
		StringBuilder b = new StringBuilder(s.trim());

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
		final int startIndex = b.indexOf(beginMark);
		if (startIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(181, beginMark, s));
			return null;
		}
		if (b.length() <= startIndex + beginMark.length()) {
			SLLogger.getLogger().warning(I18N.err(182, beginMark, s));
			return null;
		}
		b.delete(0, startIndex + beginMark.length());
		final int endIndex = b.indexOf(endMark);
		if (endIndex == -1) {
			SLLogger.getLogger().warning(I18N.err(183, endMark, s));
			return null;
		}
		b.delete(endIndex, b.length());

		/*
		 * Separate the data from the digital signature.
		 */
		final int sigEndIndex = b.indexOf(middleMark);
		final int dataStartIndex = sigEndIndex + middleMark.length();
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
			return data;
		} else {
			SLLogger.getLogger().warning(I18N.err(195, s));
			return null;
		}
	}

	/**
	 * Converts the encoded digitally signed license hex string to a usable
	 * license object, or {@code null} if the string is not well-formed,
	 * improperly signed, or missing required license data.
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
		final byte[] data = getData(s, key, BEGIN_LICENSE, MIDDLE, END_LICENSE);
		if (data == null) {
			return null;
		} else {
			final String licenseInfo = new String(data);
			return toLicense(licenseInfo);
		}
	}

	/**
	 * Converts the encoded digitally signed license net check hex string to a
	 * usable license net check object, or {@code null} if the string is not
	 * well-formed, improperly signed, or missing required license net check
	 * data.
	 * <p>
	 * If the conversion fails (and {@code null} is returned) then diagnostic
	 * information is written to the log about what went wrong.
	 * 
	 * @param s
	 *            the encoded digitally signed license net check string.
	 * @param key
	 *            a RSA public key used to check the digital signature.
	 * @return a license object or {@code null} if <tt>s</tt> is not
	 *         well-formed, improperly signed, or missing required license net
	 *         check data.
	 */
	public static SLLicenseNetCheck toLicenseNetCheck(final String s,
			final PublicKey key) {
		final byte[] data = getData(s, key, BEGIN_NET_CHECK, MIDDLE,
				END_NET_CHECK);
		if (data == null) {
			return null;
		} else {
			final String licenseNetCheckInfo = new String(data);
			return toLicenseNetCheck(licenseNetCheckInfo);
		}
	}

	/**
	 * Reads a file that contains a set licenses and license net checks as
	 * digitally signed encoded strings and returns a list of
	 * {@link PossiblyActivatedSLLicense} objects.
	 * <p>
	 * This is a robust call, in that it will report problems but not throw an
	 * exception unless the passed file is {@code null}.
	 * 
	 * @param textFile
	 *            the file to read.
	 * @return the (possibly empty) list of {@link PossiblyActivatedSLLicense}
	 *         objects.
	 * @throws IllegalArgumentException
	 *             if <tt>textFile</tt> is {@code null}.
	 */
	public static List<PossiblyActivatedSLLicense> readLicensesFromFile(
			final File textFile) {
		if (textFile == null)
			throw new IllegalArgumentException(I18N.err(44, "textFile"));
		try {
			final String fileContents = FileUtility
					.getFileContentsAsString(textFile);
			return readPossiblyActivatedLicensesFromString(fileContents);
		} catch (Exception trouble) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(206, textFile.getAbsolutePath()), trouble);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads a string that contains a set of licenses and license net checks as
	 * digitally signed encoded hex strings and returns a list of
	 * {@link PossiblyActivatedSLLicense} objects.
	 * 
	 * @param s
	 *            the string.
	 * @return the (possibly empty) list of {@link PossiblyActivatedSLLicense}
	 *         objects.
	 */
	public static List<PossiblyActivatedSLLicense> readPossiblyActivatedLicensesFromString(
			final String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));

		/*
		 * Extract the set of signed license from the file contents.
		 */
		final List<SignedSLLicense> licenses = readLicensesFromString(s);

		/*
		 * Extract the set of net checks from the file contents..
		 */
		final List<SignedSLLicenseNetCheck> licenseNetChecks = readLicenseNetChecksFromString(s);

		final List<PossiblyActivatedSLLicense> result = new ArrayList<PossiblyActivatedSLLicense>();

		for (SignedSLLicenseNetCheck licenseNetCheck : licenseNetChecks) {
			final SignedSLLicense associatedLicense = lookupLicense(licenses,
					licenseNetCheck.getLicenseNetCheck().getUuid());
			if (associatedLicense == null) {
				SLLogger.getLogger().log(
						Level.WARNING,
						I18N.err(200, licenseNetCheck.getLicenseNetCheck()
								.getUuid(), s));
			} else {
				/*
				 * Create the paired license and license net check and add it to
				 * our result.
				 */
				result.add(new PossiblyActivatedSLLicense(associatedLicense,
						licenseNetCheck));
				/*
				 * Ensure that we remove the associated license from the
				 * remaining unmatched licenses.
				 */
				licenses.remove(associatedLicense);
			}
		}

		/*
		 * Add the license with no license net check to the result.
		 */
		for (SignedSLLicense license : licenses) {
			result.add(new PossiblyActivatedSLLicense(license, null));
		}

		return result;
	}

	/**
	 * Reads a string that contains a set of licenses as digitally signed
	 * encoded hex strings and returns a list of {@link SignedSLLicense}
	 * objects.
	 * 
	 * @param s
	 *            the string.
	 * @return the (possibly empty) list of {@link SignedSLLicense} objects.
	 */
	public static List<SignedSLLicense> readLicensesFromString(final String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));

		final StringBuilder b = SLUtility.trimInternal(s);

		/*
		 * Extract the set of signed license from the file contents.
		 */
		final List<SignedSLLicense> result = new ArrayList<SignedSLLicense>();
		int fromIndex = 0;
		while (true) {
			final int index = b.indexOf(BEGIN_LICENSE, fromIndex);
			if (index == -1)
				break;
			int endIndex = b.indexOf(END_LICENSE, index);
			if (endIndex == -1) {
				break;
			}
			endIndex += END_LICENSE.length();
			final SignedSLLicense license = SignedSLLicense.getInstance(b
					.substring(index, endIndex));
			result.add(license);
			fromIndex = endIndex;
		}
		return result;
	}

	/**
	 * Reads a string that contains a set of license net checks as digitally
	 * signed encoded hex strings and returns a list of
	 * {@link SignedSLLicenseNetCheck} objects.
	 * 
	 * @param s
	 *            the string.
	 * @return the (possibly empty) list of {@link SignedSLLicenseNetCheck}
	 *         objects.
	 */
	public static List<SignedSLLicenseNetCheck> readLicenseNetChecksFromString(
			final String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));

		final StringBuilder b = SLUtility.trimInternal(s);

		/*
		 * Extract the set of signed license from the file contents.
		 */
		final List<SignedSLLicenseNetCheck> result = new ArrayList<SignedSLLicenseNetCheck>();
		int fromIndex = 0;
		while (true) {
			final int index = b.indexOf(BEGIN_NET_CHECK, fromIndex);
			if (index == -1)
				break;
			int endIndex = b.indexOf(END_NET_CHECK, index);
			if (endIndex == -1) {
				break;
			}
			endIndex += END_NET_CHECK.length();
			final SignedSLLicenseNetCheck licenseNetCheck = SignedSLLicenseNetCheck
					.getInstance(b.substring(index, endIndex));

			result.add(licenseNetCheck);
			fromIndex = endIndex;
		}
		return result;
	}

	/**
	 * Helper method to find a {@link SignedSLLicense} in a list with a
	 * particular {@link UUID}.
	 * 
	 * @param licenses
	 *            the list of {@link SignedSLLicense} objects.
	 * @param id
	 *            the {@link UUID} to search for.
	 * @return the {@link SignedSLLicense} object with <tt>id</tt> for its
	 *         {@link UUID}.
	 */
	private static SignedSLLicense lookupLicense(
			List<SignedSLLicense> licenses, UUID id) {
		for (SignedSLLicense license : licenses) {
			if (license.getLicense().getUuid().equals(id))
				return license;
		}
		return null;
	}

	/**
	 * Reads a file that contains a license net check as a digitally signed
	 * encoded string and converts the string to a license net check.
	 * 
	 * @param in
	 *            the file to read.
	 * @param key
	 *            a RSA public key used to check the digital signature.
	 * @return a license net check object or {@code null} if the contents of the
	 *         file are not well-formed, are improperly signed, or are missing
	 *         required license net check data.
	 */
	public static SLLicenseNetCheck inputLicenseNetCheckFromSignedFile(File in,
			PublicKey key) {
		final String fileContents = FileUtility.getFileContentsAsString(in);
		SLLicenseNetCheck result = toLicenseNetCheck(fileContents, key);
		return result;
	}

	private SLLicensePersistence() {
		// no instances
	}
}
