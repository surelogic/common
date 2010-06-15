package com.surelogic.common.license;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Encapsulates a license net check and the signed hex string that demonstrates
 * that the license net check was created by SureLogic.
 */
public final class SignedSLLicenseNetCheck {

	private final SLLicenseNetCheck f_licenseNetCheck;

	public SLLicenseNetCheck getLicenseNetCheck() {
		return f_licenseNetCheck;
	}

	private final String f_signedHexString;

	public String getSignedHexString() {
		return f_signedHexString;
	}

	private SignedSLLicenseNetCheck(final SLLicenseNetCheck licenseNetCheck,
			final String signedHexString) {
		f_licenseNetCheck = licenseNetCheck;
		f_signedHexString = signedHexString;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.getClass().toString());
		b.append(" : (signed hex string) ");
		b.append(getSignedHexString());
		b.append('\n');
		b.append(getLicenseNetCheck().toString());
		return b.toString();
	}

	public static SignedSLLicenseNetCheck getInstance(String signedHexString) {
		return getInstance(signedHexString, SLUtility.getPublicKey());
	}

	public static SignedSLLicenseNetCheck getInstance(String signedHexString,
			PublicKey key) {
		if (signedHexString == null)
			throw new IllegalArgumentException(I18N.err(44, "signedHexString"));
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		SLLicenseNetCheck nc = SLLicensePersistence.toLicenseNetCheck(
				signedHexString, key);
		return new SignedSLLicenseNetCheck(nc, signedHexString);
	}

	public static SignedSLLicenseNetCheck getInstance(
			SLLicenseNetCheck licenseNetCheck, PrivateKey key) {
		if (licenseNetCheck == null)
			throw new IllegalArgumentException(I18N.err(44, "licenseNetCheck"));
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		final String signedHexString = SLLicensePersistence.toSignedHexString(
				licenseNetCheck, key, false);
		return new SignedSLLicenseNetCheck(licenseNetCheck, signedHexString);
	}
}
