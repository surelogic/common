package com.surelogic.common.license;

import java.io.File;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

public final class SLLicensePersistence {

	private static final String DATE_LABEL = "date=";
	private static final String HOLDER_LABEL = "holder=";
	private static final String MAXACTIVE_LABEL = "maxActive=";
	private static final String PERFORMNETCHECK_LABEL = "performNetCheck=";
	private static final String PRODUCT_LABEL = "product=";
	private static final String TYPE_LABEL = "type=";
	private static final String UUID_LABEL = "id=";

	public static String toString(final SLLicense license) {
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));
		final StringBuilder b = new StringBuilder();
		final String sep = "\n";
		SimpleDateFormat sdf = SLLicenseUtility.getThreadSafeDateFormat();
		b.append(UUID_LABEL).append(license.getUuid().toString()).append(sep);
		b.append(HOLDER_LABEL).append(license.getHolder()).append(sep);
		b.append(PRODUCT_LABEL).append(license.getProduct()).append(sep);
		b.append(DATE_LABEL).append(sdf.format(license.getDate())).append(sep);
		b.append(TYPE_LABEL).append(license.getType().toString()).append(sep);
		b.append(MAXACTIVE_LABEL).append(
				Integer.toString(license.getMaxActive())).append(sep);
		b.append(PERFORMNETCHECK_LABEL).append(
				Boolean.toString(license.performNetCheck())).append(sep);
		return b.toString();
	}

	public static String toSignedString(final SLLicense license,
			final PrivateKey key) {
		StringBuilder b = new StringBuilder();

		final byte[] data = toString(license).getBytes();
		final byte[] signature = SLUtility.getSignature(data, key);
		

		b.append(SLUtility.toHexString(signature));
		b.append(':');
		b.append(SLUtility.toHexString(data));

		SLUtility.wrap(b, 60);

		return b.toString();
	}

	public static void outputToSignedFile(final SLLicense license, File out,
			PrivateKey key) {
		FileUtility.putStringIntoAFile(out, toSignedString(license, key));
	}

	private SLLicensePersistence() {
		// no instances
	}
}
