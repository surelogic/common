package com.surelogic.common.license;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.surelogic.PolicyLock;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

/**
 * A utility to help manage licenses to use SureLogic tools.
 */
@PolicyLock("Lock is class")
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
	 * If a license does not exist then all registered {@link ILicenseObserver}
	 * instances are notified that the license check failed.
	 * <p>
	 * If the license is close to its expiration then all registered
	 * {@link ILicenseObserver} instances are notified.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @return {@code true} if a license exists, {@code false} otherwise.
	 */
	public static boolean validate(final String subject) {
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
