package com.surelogic.common.license;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.security.auth.x500.X500Principal;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;

/**
 * A utility to help manage licenses to use SureLogic tools.
 */
public final class SLLicenseUtility {

	/*
	 * Keep the SUBJECTS array complete and in alphabetical order when new
	 * license subjects are added below.
	 */
	public static final String ALL_TOOL_SUBJECT = "All Tools";
	public static final String FLASHLIGHT_SUBJECT = "Flashlight";
	public static final String JSURE_SUBJECT = "JSure";
	public static final String SIERRA_SUBJECT = "Sierra";

	public static final String[] SUBJECTS = { ALL_TOOL_SUBJECT,
			FLASHLIGHT_SUBJECT, JSURE_SUBJECT, SIERRA_SUBJECT };

	/**
	 * Gets the content of an installed license with the passed subject, or
	 * {@code null} if no such license is installed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @return the content of an installed license with the passed subject, or
	 *         {@code null} if no such license is installed.
	 */
	public static synchronized LicenseContent tryToGetInstalledLicense(
			final String subject) {
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
	public static synchronized void tryToInstallLicense(final String subject,
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
		final LicenseContent lc = lm.install(keyFile);
		try {
			if (getPerformNetCheckFrom(lc.getHolder())) {
				final UUID uuid = UUID.fromString(getUUIDFrom(lc.getHolder()));
				final LicenseBlacklist b = new LicenseBlacklist();
				b.updateFromNet();
				if (!b.fromNet()) {
					throw new IllegalStateException(I18N.err(144, I18N
							.msg("common.manage.licenses.blacklist.url")));
				}
				if (b.getList().contains(uuid)) {
					/*
					 * This license is blacklisted
					 */
					throw new IllegalArgumentException(I18N.err(145, uuid
							.toString(), getNameFrom(lc.getHolder())));
				}
			}

		} catch (Exception e) {
			lm.uninstall();
			throw new IllegalStateException("Network check failed", e);
		}
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
	public static synchronized void tryToUninstallLicense(final String subject)
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
	 * <p>
	 * The <tt>OU</tt> field is set to the string representation of a randomly
	 * generated UUID (generated with the {@link UUID#randomUUID()} method).
	 * <p>
	 * The <tt>O</tt> field contains {@code true} or {@code false} indicating if
	 * a network check should be performed on any license issued to this
	 * principal.
	 * 
	 * @param name
	 *            the <tt>CN</tt> value for the X500 principal. Should be
	 *            something like a name or a company name.
	 * @param performNetCheck
	 *            the <tt>O</tt> value for the X500 principal. Represents if a
	 *            network check should be done on the license.
	 * @return an 500 principal.
	 * @throws IllegalArgumentException
	 *             if <tt>CN</tt> is null or the empty string or if after
	 *             escaping <tt>CN</tt> is not able to be parsed by the
	 *             constructor {@link X500Principal#X500Principal(String)}.
	 */
	public static X500Principal getX500PrincipalFor(final String name,
			final boolean performNetCheck) {
		if (name == null)
			throw new IllegalArgumentException(I18N.err(44, "cn"));
		if ("".equals(name))
			throw new IllegalArgumentException(I18N.err(139, "cn"));
		final StringBuilder b = new StringBuilder(name);
		remove("\\", b);
		escape(",", b);
		escape(";", b);
		b.insert(0, "CN=");
		b.append(", OU=");
		UUID uuid = UUID.randomUUID();
		b.append(uuid.toString());
		b.append(", O=");
		b.append(performNetCheck);
		return new X500Principal(b.toString());
	}

	/**
	 * This method gets the <tt>CN</tt> portion of an {@link X500Principal}
	 * created via a call to {@link #getX500PrincipalFor(String)}. The
	 * <tt>CN</tt> field is used to hold the name (of a person or a company) who
	 * holds a license. A result of {@code null} indicates that the value could
	 * not be determined.
	 * 
	 * @param p
	 *            an {@link X500Principal}.
	 * @return the <tt>CN</tt> portion, or {@code null} if it could not be
	 *         extracted.
	 */
	public static String getNameFrom(X500Principal p) {
		if (p == null)
			throw new IllegalArgumentException(I18N.err(44, "p"));
		final String ou = ",OU=";
		final StringBuilder b = new StringBuilder(p.getName());
		final int oIndex = b.indexOf(ou);
		if (oIndex == -1)
			return null;
		b.delete(oIndex, b.length());
		if (b.length() < 3)
			return null;
		b.delete(0, 3);
		remove("\\", b);
		return b.toString();
	}

	/**
	 * This method gets the <tt>OU</tt> portion of an {@link X500Principal}
	 * created via a call to {@link #getX500PrincipalFor(String)}. The
	 * <tt>OU</tt> field is used to hold a {@link UUID} that provides an
	 * identity for an issued license. A result of {@code null} indicates that
	 * the value could not be determined.
	 * 
	 * @param p
	 *            an {@link X500Principal}.
	 * @return the <tt>OU</tt> portion, or {@code null} if it could not be
	 *         extracted.
	 */
	public static String getUUIDFrom(X500Principal p) {
		if (p == null)
			throw new IllegalArgumentException(I18N.err(44, "p"));
		final String o = ",O=";
		final String ou = ",OU=";
		final StringBuilder b = new StringBuilder(p.getName());
		final int oIndex = b.indexOf(o);
		if (oIndex != -1)
			b.delete(oIndex, b.length());
		final int ouIndex = b.indexOf(ou);
		if (ouIndex == -1)
			return null;
		b.delete(0, ouIndex + ou.length());
		return b.toString();
	}

	/**
	 * Gets if a network check should be performed on any license issued to this
	 * principal.
	 * 
	 * @param p
	 *            an {@link X500Principal}.
	 * @return {@code true} if a network check should be performed on any
	 *         license issued to this principal, {@code false} otherwise.
	 */
	public static boolean getPerformNetCheckFrom(X500Principal p) {
		if (p == null)
			throw new IllegalArgumentException(I18N.err(44, "p"));
		return p.getName().endsWith("O=true");
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
			b.delete(index, index + 1);
		}
	}

	private static final Set<ILicenseObserver> f_observers = new CopyOnWriteArraySet<ILicenseObserver>();

	/**
	 * Adds a license check observer.
	 * 
	 * @param observer
	 *            a license check observer.
	 */
	public static void addObserver(ILicenseObserver observer) {
		if (observer != null)
			f_observers.add(observer);
	}

	/**
	 * Removes a license check observer.
	 * 
	 * @param observer
	 *            a license check observer.
	 */
	public static void removeObserver(ILicenseObserver observer) {
		f_observers.remove(observer);
	}

	/**
	 * Checks if the passed license subject is installed or if an all SureLogic
	 * tools license is installed.
	 * <p>
	 * If a license does not exists then all registered {@link ILicenseObserver}
	 * instances are notified that the license check failed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @return {@code true} if a license exists, {@code false} otherwise.
	 */
	public static boolean validate(final String subject) {
		boolean licensed;
		synchronized (SLLicenseUtility.class) {
			licensed = tryToGetInstalledLicense(subject) != null;
			if (!licensed) {
				licensed = tryToGetInstalledLicense(ALL_TOOL_SUBJECT) != null;
			}
		}
		/*
		 * Don't call the observers holding a lock.
		 */
		if (!licensed)
			for (ILicenseObserver o : f_observers)
				o.notifyNoLicenseFor(subject);
		return licensed;
	}

	/**
	 * A helper routine to validate a license from within the
	 * {@link SLJob#run(SLProgressMonitor)} method. A result of {@code null}
	 * indicates the check succeeded, otherwise the resulting {@link SLStatus}
	 * object should be returned to cause the job to fail. A typical use would
	 * be as shown below.
	 * 
	 * <pre>
	 * final SLStatus failed = SLLicenseUtility.validateSLJob(
	 * 		SLLicenseUtility.FLASHLIGHT_SUBJECT, monitor);
	 * if (failed != null)
	 * 	return failed;
	 * </pre>
	 * 
	 * If the check fails then the {@link SLProgressMonitor#done()} method is
	 * called on <tt>monitor</tt>.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @param monitor
	 *            a progress monitor.
	 * @return {@code null} if the license check was successful, an error status
	 *         otherwise.
	 */
	public static SLStatus validateSLJob(final String subject,
			final SLProgressMonitor monitor) {
		if (!validate(subject)) {
			final int code = 143;
			final String msg = I18N.err(code, subject);
			monitor.done();
			return SLStatus.createErrorStatus(code, msg);
		} else
			return null;
	}

	private SLLicenseUtility() {
		// no instances
	}
}
