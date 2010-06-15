package com.surelogic.common.license;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Encapsulates a license and the signed hex string that demonstrates that the
 * license was created by SureLogic.
 */
public final class SignedSLLicense {

	private final SLLicense f_license;

	public SLLicense getLicense() {
		return f_license;
	}

	private final String f_signedHexString;

	public String getSignedHexString() {
		return f_signedHexString;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.getClass().toString());
		b.append(" : (signed hex string) ");
		b.append(getSignedHexString());
		b.append('\n');
		b.append(getLicense().toString());
		return b.toString();
	}

	private SignedSLLicense(final SLLicense license,
			final String signedHexString) {
		f_license = license;
		f_signedHexString = signedHexString;
	}

	public static SignedSLLicense getInstance(String signedHexString) {
		return getInstance(signedHexString, SLUtility.getPublicKey());
	}

	public static SignedSLLicense getInstance(String signedHexString,
			PublicKey key) {
		if (signedHexString == null)
			throw new IllegalArgumentException(I18N.err(44, "signedHexString"));
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		SLLicense license = SLLicensePersistence
				.toLicense(signedHexString, key);
		return new SignedSLLicense(license, signedHexString);
	}

	public static SignedSLLicense getInstance(SLLicense license, PrivateKey key) {
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		final String signedHexString = SLLicensePersistence.toSignedHexString(
				license, key, false);
		return new SignedSLLicense(license, signedHexString);
	}
}
