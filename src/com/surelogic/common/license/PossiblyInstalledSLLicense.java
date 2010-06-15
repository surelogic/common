package com.surelogic.common.license;

import java.util.UUID;

import com.surelogic.common.i18n.I18N;

/**
 * Represents a possibly installed license. A license is considered installed if
 * this class aggregates both of the following:
 * <ul>
 * <li>A signed license
 * <li>A signed license net check
 * </ul>
 * Note that being installed is a necessary but not sufficient condition for a
 * license to be considered valid.
 */
public final class PossiblyInstalledSLLicense {

	/**
	 * A license and the signed hex string that demonstrates that the license
	 * was created by SureLogic.
	 */
	private final SignedSLLicense f_license;

	/**
	 * Gets the signed license associated with this possibly installed license.
	 * 
	 * @return the non-<tt>null</tt> signed license.
	 */
	public SignedSLLicense getSignedSLLicense() {
		return f_license;
	}

	/**
	 * A license net check and the signed hex string that demonstrates that the
	 * license net check was created by SureLogic. May be {@code null} if the
	 * {@link #f_license} has not been net checked.
	 */
	private final SignedSLLicenseNetCheck f_licenseNetCheck;

	/**
	 * Gets the signed license net check associated with this possibly installed
	 * license.
	 * 
	 * @return the signed license net check, or {@code null} if there is no net
	 *         check.
	 */
	public SignedSLLicenseNetCheck getSignedSLLicenseNetCheck() {
		return f_licenseNetCheck;
	}

	/**
	 * Flags if this license is installed.
	 * 
	 * @return {@code true} if this license has been installed properly, {@code
	 *         false} otherwise.
	 */
	public boolean isInstalled() {
		return f_licenseNetCheck != null;
	}

	/**
	 * Constructs a new instance from the passed signed license and signed
	 * license net check.
	 * 
	 * @param license
	 *            a non-<tt>null</tt> signed license.
	 * @param licenseNetCheck
	 *            a signed license net check, or {@code null} if there is no net
	 *            check.
	 */
	public PossiblyInstalledSLLicense(final SignedSLLicense license,
			final SignedSLLicenseNetCheck licenseNetCheck) {
		if (license == null)
			throw new IllegalArgumentException(I18N.err(44, "license"));
		f_license = license;
		f_licenseNetCheck = licenseNetCheck;

		/*
		 * Validate the two objects: If the license net check is not null then
		 * ensure that the two identifiers are the same.
		 */
		if (f_licenseNetCheck != null) {
			final UUID liUuid = f_license.getLicense().getUuid();
			final UUID ncUuid = f_licenseNetCheck.getLicenseNetCheck()
					.getUuid();
			final boolean differentUuids = !liUuid.equals(ncUuid);
			if (differentUuids)
				throw new IllegalStateException(I18N.err(189,
						liUuid.toString(), ncUuid.toString()));
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.getClass().toString());
		if (isInstalled()) {
			b.append(" : installed\n");
			b.append(getSignedSLLicense().toString());
			b.append(getSignedSLLicenseNetCheck().toString());
		} else {
			b.append(" : not installed\n");
			b.append(getSignedSLLicense().toString());
		}
		return b.toString();
	}
}
