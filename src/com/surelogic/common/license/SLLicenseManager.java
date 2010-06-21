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
