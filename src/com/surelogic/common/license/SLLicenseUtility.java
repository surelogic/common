package com.surelogic.common.license;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
		if (product == null)
			throw new IllegalArgumentException(I18N.err(44, "product"));
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
		} else
			return null;
	}

	private SLLicenseUtility() {
		// no instances
	}
}
