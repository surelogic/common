package com.surelogic.common.license;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

/**
 * A utility to help manage licenses to use SureLogic tools.
 */
public final class SLLicenseUtility {

	private static final Set<ILicenseObserver> f_observers = new CopyOnWriteArraySet<ILicenseObserver>();

	/**
	 * Adds a license check observer.
	 * 
	 * @param observer
	 *            a license check observer.
	 */
	public static void addObserver(final ILicenseObserver observer) {
		if (observer != null) {
			f_observers.add(observer);
		}
	}

	/**
	 * Removes a license check observer.
	 * 
	 * @param observer
	 *            a license check observer.
	 */
	public static void removeObserver(final ILicenseObserver observer) {
		f_observers.remove(observer);
	}

	/**
	 * Checks if a license that allows use of the passed product is installed.
	 * <p>
	 * If an appropriate license is not installed then all registered
	 * {@link ILicenseObserver} instances are notified that the license check
	 * failed.
	 * <p>
	 * If the license is close to its expiration then all registered
	 * {@link ILicenseObserver} instances are notified.
	 * 
	 * @param product
	 *            the non-<tt>null</tt> product.
	 * @return {@code true} if a license exists that allows use of
	 *         <tt>product</tt>, {@code false} otherwise.
	 */
	public static boolean validate(final SLLicenseProduct product) {
		if (product == null) {
			throw new IllegalArgumentException(I18N.err(44, "product"));
		}
		return true;
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
	 * 		SLLicenseProduct.FLASHLIGHT, monitor);
	 * if (failed != null)
	 * 	return failed;
	 * </pre>
	 * 
	 * If the check fails then the {@link SLProgressMonitor#done()} method is
	 * called on <tt>monitor</tt>.
	 * 
	 * @param product
	 *            the non-<tt>null</tt> product.
	 * @param monitor
	 *            a progress monitor.
	 * @return {@code null} if the license check was successful and use of the
	 *         product is licensed, an error status otherwise.
	 */
	public static SLStatus validateSLJob(final SLLicenseProduct product,
			final SLProgressMonitor monitor) {
		if (!validate(product)) {
			final int code = 143;
			final String msg = I18N.err(code, product.toString());
			monitor.done();
			return SLStatus.createErrorStatus(code, msg);
		} else {
			return null;
		}
	}

	/**
	 * Takes the passed encoded string, assumes that it is an encoded signed
	 * license, and installs it.
	 * 
	 * @param value
	 *            the string to decode into a license.
	 * @throws Exception
	 *             should anything go wrong.
	 */
	public static void tryToInstallLicense(final String value) throws Exception {
		final List<PossiblyActivatedSLLicense> licenses = SLLicensePersistence
				.readPossiblyActivatedLicensesFromString(value);
		if (licenses.isEmpty()) {
			throw new Exception(I18N.err(201));
		}
		/*
		 * Iterate through the licenses that we are trying to install ensure
		 * that we are not installing this license past the install/activation
		 * deadline.
		 */
		final Date now = new Date();
		for (PossiblyActivatedSLLicense iLicense : licenses) {
			SLLicense license = iLicense.getSignedSLLicense().getLicense();
			/*
			 * The installation deadline only applies to non-perpetual licenses.
			 */
			if (license.getType() != SLLicenseType.PERPETUAL) {
				final Date deadline = license.getInstallBeforeDate();
				final boolean pastDeadline = now.after(deadline);
				if (pastDeadline) {
					throw new Exception(I18N.err(202, license.getType()
							.toString(), license.getProduct().toString(),
							SLUtility.toStringHumanDay(deadline)));
				}
			}
		}
		SLLicenseManager.getInstance().install(licenses);
	}

	/**
	 * Contacts the SureLogic server and tries to activate/renew a set of
	 * licenses. The server responds with a set of license net checks that then
	 * need to be installed locally.
	 * 
	 * @param licenses
	 *            the licenses to activate.
	 * @throws Exception
	 *             should anything go wrong.
	 */
	public static void tryToActivateRenewLicenses(
			final List<PossiblyActivatedSLLicense> licenses) throws Exception {
		if (licenses.isEmpty()) {
			return;
		}
		/*
		 * Check that either (1) each license is not activated or (2) that it is
		 * a perpetual license.
		 */
		for (PossiblyActivatedSLLicense iLicense : licenses) {
			SLLicense license = iLicense.getSignedSLLicense().getLicense();
			if (license.getType() != SLLicenseType.PERPETUAL) {
				if (iLicense.isActivated()) {
					throw new Exception(I18N.err(203, license.getType()
							.toString(), license.getProduct().toString(),
							SLUtility.toStringHumanDay(iLicense
									.getSignedSLLicenseNetCheck()
									.getLicenseNetCheck().getDate())));
				}
			}
		}

		/*
		 * Build the message to send to www.surelogic.com.
		 */
		final String l = SLLicensePersistence.toSignedHexString(licenses, true);
		final Map<String, String> param = new HashMap<String, String>();
		param.put(I18N.msg("common.serviceability.licenserequest.req"), I18N
				.msg("common.serviceability.licenserequest.req.actrew"));
		param.put(I18N.msg("common.serviceability.licenserequest.license"), l);
		final URL url = new URL(I18N
				.msg("common.serviceability.licenserequest.url"));
		final String response = SLUtility.sendPostToUrl(url, param);

		final List<SignedSLLicenseNetCheck> licenseNetChecks = SLLicensePersistence
				.readLicenseNetChecksFromString(response);
		SLLicenseManager.getInstance().activateOrRenew(licenseNetChecks);
	}

	/**
	 * Uninstalls a set of license from the client and notifies the SureLogic
	 * server that they have been uninstalled.
	 * 
	 * @param licenses
	 *            the licenses to uninstall.
	 * @throws Exception
	 *             should anything go wrong.
	 */
	public static void tryToUninstallLicenses(
			final List<PossiblyActivatedSLLicense> licenses) throws Exception {
		if (licenses.isEmpty()) {
			return;
		}

		/*
		 * Local removal
		 */
		SLLicenseManager.getInstance().remove(licenses);
		/*
		 * For each license removed that performs net checks we should notify
		 * the server that the license has been removed.
		 * 
		 * This is a best effort attempt so we should not get bother the user if
		 * for any reason the notification fails.
		 */
		List<PossiblyActivatedSLLicense> notifyList = new ArrayList<PossiblyActivatedSLLicense>();
		for (PossiblyActivatedSLLicense license : licenses) {
			if (license.getSignedSLLicense().getLicense().performNetCheck()) {
				notifyList.add(license);
			}
		}
		if (!notifyList.isEmpty()) {
			/*
			 * Perform notification message to the server.
			 */
			// TODO
		}
	}

	private SLLicenseUtility() {
		// no instances
	}
}
