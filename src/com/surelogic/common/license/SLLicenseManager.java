package com.surelogic.common.license;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.surelogic.common.i18n.I18N;

/**
 * Manages the contents of the <tt>~/.surelogic-licenses</tt> file. This class
 * manages the persistence of a list of {@link PossiblyActivatedSLLicense}
 * objects. This is focused around persistence and does not include
 * functionality such as performing net checks.
 */
public final class SLLicenseManager {

	private static SLLicenseManager INSTANCE = null;

	public static SLLicenseManager getInstance() {
		synchronized (SLLicenseManager.class) {
			if (INSTANCE == null) {
				INSTANCE = new SLLicenseManager();
				INSTANCE.loadHelper();
			}
			return INSTANCE;
		}
	}

	private final File f_licenseFile = new File(System.getProperty("user.home")
			+ File.separator + ".surelogic-licenses");

	private final List<PossiblyActivatedSLLicense> f_licenses = new ArrayList<PossiblyActivatedSLLicense>();

	/**
	 * Gets a copy of the list of possibly installed licenses.
	 * 
	 * @return a copy of the list of possibly installed licenses.
	 */
	public List<PossiblyActivatedSLLicense> getLicenses() {
		synchronized (SLLicenseManager.class) {
			return new ArrayList<PossiblyActivatedSLLicense>(f_licenses);
		}
	}

	/**
	 * Adds a list of licenses to the set managed by this class. If a license
	 * with the same license id exists it is removed before a new license is
	 * added. The result is immediately persisted to the
	 * <tt>~/.surelogic-licenses</tt> file.
	 * 
	 * @param licenses
	 *            a list of licenses to add to the set managed by this class.
	 */
	public void install(List<PossiblyActivatedSLLicense> licenses) {
		if (licenses == null)
			throw new IllegalArgumentException(I18N.err(44, "licenses"));
		synchronized (SLLicenseManager.class) {
			for (PossiblyActivatedSLLicense license : licenses) {
				/*
				 * Remove any existing licenses with the same id.
				 */
				removeHelper(license.getSignedSLLicense().getLicense()
						.getUuid());
				/*
				 * Add this license into the list managed by this class.
				 */
				f_licenses.add(license);
			}
			/*
			 * Persist the result
			 */
			SLLicensePersistence.writeLicensesToFile(f_licenseFile, f_licenses);
		}
	}

	/**
	 * Adds a license to the set managed by this class. If a license with the
	 * same license id exists it is removed before a new license is added. The
	 * result is immediately persisted to the <tt>~/.surelogic-licenses</tt>
	 * file.
	 * 
	 * @param license
	 *            a license to add to the set managed by this class.
	 */
	public void install(PossiblyActivatedSLLicense license) {
		List<PossiblyActivatedSLLicense> licenses = new ArrayList<PossiblyActivatedSLLicense>();
		licenses.add(license);
		install(licenses);
	}

	/**
	 * Updates the license net checks for a set of managed licenses. This
	 * activates or renews the licenses. It must be the case that for each
	 * license net check passed to this method that a corresponding license is
	 * already managed by this class.
	 * <p>
	 * The result is immediately persisted to the <tt>~/.surelogic-licenses</tt>
	 * file.
	 * 
	 * @param licenseNetChecks
	 *            the set of license net checks to install.
	 * @throws IllegalStateException
	 *             if a net check in <tt>licenseNetChecks</tt> does not activate
	 *             or renew a managed license.
	 */
	public void activateOrRenew(List<SignedSLLicenseNetCheck> licenseNetChecks) {
		final List<PossiblyActivatedSLLicense> licensesToAddOrUpdate = new ArrayList<PossiblyActivatedSLLicense>();
		for (SignedSLLicenseNetCheck nc : licenseNetChecks) {
			final PossiblyActivatedSLLicense license = getLicense(nc
					.getLicenseNetCheck().getUuid());
			if (license == null) {
				throw new IllegalStateException(I18N.err(207, nc.toString()));
			}
			licensesToAddOrUpdate.add(new PossiblyActivatedSLLicense(license
					.getSignedSLLicense(), nc));
		}
		/*
		 * The install method does the right thing in this case it overwrites,
		 * so we can call it to do the rest of the work.
		 */
		install(licensesToAddOrUpdate);
	}

	/**
	 * Removes a list of licenses from the set managed by this class. Removal is
	 * performed by matching license identifiers (not by reference equality).
	 * The result is immediately persisted to the <tt>~/.surelogic-licenses</tt>
	 * file.
	 * 
	 * @param licenses
	 *            a list of licenses to remove from the set managed by this
	 *            class.
	 */
	public void remove(List<PossiblyActivatedSLLicense> licenses) {
		if (licenses == null)
			throw new IllegalArgumentException(I18N.err(44, "licenses"));
		synchronized (SLLicenseManager.class) {
			for (PossiblyActivatedSLLicense license : licenses) {
				/*
				 * Remove any existing licenses with the same id.
				 */
				removeHelper(license.getSignedSLLicense().getLicense()
						.getUuid());
			}
			/*
			 * Persist the result
			 */
			SLLicensePersistence.writeLicensesToFile(f_licenseFile, f_licenses);
		}
	}

	/**
	 * Removes a license from the set managed by this class. Removal is
	 * performed by matching license identifiers (not by reference equality).
	 * The result is immediately persisted to the <tt>~/.surelogic-licenses</tt>
	 * file.
	 * 
	 * @param license
	 *            a license to remove from the set managed by this class.
	 */
	public void remove(PossiblyActivatedSLLicense license) {
		List<PossiblyActivatedSLLicense> licenses = new ArrayList<PossiblyActivatedSLLicense>();
		licenses.add(license);
		install(licenses);
	}

	/**
	 * Gets a {@link PossiblyActivatedSLLicense} object with a particular
	 * {@link UUID} if it is managed by this class. If no such license exists
	 * {@code null} is returned.
	 * 
	 * @param id
	 *            the {@link UUID} to search for.
	 * @return the {@link PossiblyActivatedSLLicense} object with <tt>id</tt>
	 *         for its {@link UUID}, or {@code null} if no such license exists.
	 */
	public PossiblyActivatedSLLicense getLicense(UUID id) {
		for (PossiblyActivatedSLLicense license : f_licenses) {
			if (license.getSignedSLLicense().getLicense().getUuid().equals(id))
				return license;
		}
		return null;
	}

	private SLLicenseManager() {
		// singleton
	}

	/**
	 * Iterate through a copy of the list of licenses and remove any license
	 * with the passed id.
	 * <p>
	 * Invoke while synchronized on {@code SLLicenseManager.class}.
	 * 
	 * @param id
	 *            a license id.
	 */
	private void removeHelper(UUID id) {
		for (PossiblyActivatedSLLicense license : getLicenses()) {
			if (license.getSignedSLLicense().getLicense().getUuid().equals(id)) {
				f_licenses.remove(license);
			}
		}
	}

	/**
	 * Loads the persisted license data from the <tt>~/.surelogic-licenses</tt>
	 * file.
	 * <p>
	 * Invoke while synchronized on {@code SLLicenseManager.class}.
	 */
	private void loadHelper() {
		f_licenses.clear();
		if (f_licenseFile.exists()) {
			f_licenses.addAll(SLLicensePersistence
					.readLicensesFromFile(f_licenseFile));
		}
	}
}
