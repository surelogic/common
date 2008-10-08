package com.surelogic.common.license;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import com.surelogic.common.i18n.I18N;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;

public final class SLLicenseUtility {

	/*
	 * Keep the SUBJECTS array complete and in alphabetical order when new
	 * license subjects are added below.
	 */
	public static final String ALL_TOOL_SUBJECT = "All SureLogic Tools";
	public static final String FLASHLIGHT_SUBJECT = "SureLogic Flashlight";
	public static final String JSURE_SUBJECT = "SureLogic JSure";
	public static final String SIERRA_SUBJECT = "SureLogic Sierra";

	public static final String[] SUBJECTS = { ALL_TOOL_SUBJECT,
			FLASHLIGHT_SUBJECT, JSURE_SUBJECT, SIERRA_SUBJECT };

	/**
	 * Gets a possibly empty list of the installed licenses.
	 * 
	 * @return a possibly empty list of the installed licenses.
	 */
	public static List<LicenseContent> getInstalledLicenses() {
		final List<LicenseContent> result = new ArrayList<LicenseContent>();
		/*
		 * Collect up the possibly installed licenses.
		 */
		LicenseContent lc;
		for (String subject : SUBJECTS) {
			lc = tryToGetInstalledLicense(subject);
			if (lc != null)
				result.add(lc);
		}
		return result;
	}

	/**
	 * Gets the content of an installed license with the passed subject, or
	 * {@code null} if no such license is installed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @return the content of an installed license with the passed subject, or
	 *         {@code null} if no such license is installed.
	 */
	public static LicenseContent tryToGetInstalledLicense(final String subject) {
		if (subject == null)
			throw new IllegalArgumentException(I18N.err(44, "subject"));
		final LicenseManager lm = new LicenseManager(
				new SLLicenseParam(subject));
		LicenseContent lc = null;
		try {
			lc = lm.verify();
		} catch (Exception e) {
			// ignore, the license is not installed, return null.
		}
		return lc;
	}

	/**
	 * Tries to install a license for the passed subject from the passed file.
	 * This method will throw an exception if anything goes wrong during the
	 * installation.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @param keyFile
	 *            the non-null file file to load the license key from. This file
	 *            must exist and be able to be read from.
	 * @throws Exception
	 *             if the installation of the license fails for various reasons.
	 *             Note that you should always use
	 *             {@link Throwable#getLocalizedMessage()} to get a (possibly
	 *             localized) meaningful message.
	 */
	public static void tryToInstallLicense(final String subject,
			final File keyFile) throws Exception {
		if (subject == null)
			throw new IllegalArgumentException(I18N.err(44, "subject"));
		if (keyFile == null)
			throw new IllegalArgumentException(I18N.err(44, "keyFile"));
		if (!keyFile.canRead())
			throw new IllegalStateException(I18N.err(140, keyFile
					.getAbsolutePath()));

		final LicenseManager lm = new LicenseManager(
				new SLLicenseParam(subject));
		lm.install(keyFile);
	}

	/**
	 * Tries to uninstall a license for the passed subject. This method will
	 * throw an exception if anything goes wrong during the removal of the
	 * license.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @throws Exception
	 *             if the removal of the license fails for various reasons. Note
	 *             that you should always use
	 *             {@link Throwable#getLocalizedMessage()} to get a (possibly
	 *             localized) meaningful message.
	 */
	public static void tryToUninstallLicense(final String subject)
			throws Exception {
		if (subject == null)
			throw new IllegalArgumentException(I18N.err(44, "subject"));
		final LicenseManager lm = new LicenseManager(
				new SLLicenseParam(subject));
		lm.uninstall();
	}

	/**
	 * Creates an X500 principal for SureLogic.
	 * 
	 * @return an X500 principal for SureLogic.
	 */
	public static X500Principal getSureLogicX500Principal() {
		final X500Principal result = new X500Principal(
				"CN=SureLogic\\, Inc., OU=http://www.surelogic.com, L=Pittsburgh, ST=Pennsylvania, C=US");
		return result;
	}

	/**
	 * Creates an X500 principal for the passed <tt>CN</tt> name. The name is
	 * escaped to try to avoid parsing errors when the {@link X500Principal}
	 * instance is constructed.
	 * 
	 * @param cn
	 *            the <tt>CN</tt> value for the X500 principal. Should be
	 *            something like a name or a company name.
	 * @return an 500 principal.
	 * @throws IllegalArgumentException
	 *             if <tt>CN</tt> is null or the empty string or if after
	 *             escaping <tt>CN</tt> is not able to be parsed by the
	 *             constructor {@link X500Principal#X500Principal(String)}.
	 */
	public static X500Principal getX500PrincipalFor(final String cn) {
		if (cn == null)
			throw new IllegalArgumentException(I18N.err(44, "cn"));
		if ("".equals(cn))
			throw new IllegalArgumentException(I18N.err(139, "cn"));
		final StringBuilder b = new StringBuilder(cn);
		remove("\\", b);
		escape(",", b);
		escape(";", b);
		b.insert(0, "CN=");
		return new X500Principal(b.toString());
	}

	private static void escape(final String value, final StringBuilder b) {
		int index = 0;
		while (true) {
			index = b.indexOf(value, index);
			if (index == -1)
				break;
			b.insert(index, "\\");
			index = index + 1 + value.length();
		}
	}

	private static void remove(final String value, final StringBuilder b) {
		int index = 0;
		while (true) {
			index = b.indexOf(value, index);
			if (index == -1)
				break;
			b.replace(index, index + 1, "");
		}
	}

	private SLLicenseUtility() {
		// no instances
	}
}
