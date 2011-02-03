package com.surelogic.common.core.logging;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import com.surelogic.common.SLUtility;
import com.surelogic.common.core.Activator;
import com.surelogic.common.core.preferences.PreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLSeverity;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.ILicenseObserver;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * A utility to help log messages within Eclipse by creating valid
 * {@link IStatus} objects. This code is very roughly based upon the code on
 * pages 122-123 of <i>Eclipse: Building Commercial-Quality Plug-Ins</i> (2nd
 * edition) by Eric Clayberg and Dan Rubel. The primary difference is that this
 * code provides better servicability support targeted to meet Ready for
 * Rational requirements.
 */
public final class SLEclipseStatusUtility {
	public static final int LOW_MEM_THRESHOLD = SLUtility.is64bit ? 768 : 512;
	public static final int LOW_PERMGEN_THRESHOLD = SLUtility.is64bit ? 256
			: 128;

	private static final String MAX_PERM_SIZE = "-XX:MaxPermSize=";

	private static final AtomicBoolean f_firstTouch = new AtomicBoolean(false);

	public interface TouchNotificationUI {
		/**
		 * Provides a user interface for license notifications. This method must
		 * not return null.
		 * 
		 * @return a non-null implementation.
		 */
		ILicenseObserver getLicenseObserver();

		/**
		 * Called if the memory appears to low to run the SureLogic tools.
		 * 
		 * @param maxMemoryMB
		 *            the current JVM maximum memory in megabytes.
		 * @param maxPermGenMB
		 *            the current JVM maximum PermGen memory in megabytes.
		 */
		void notifyLowMemory(long maxMemoryMB, long maxPermGenMB);
	}

	/**
	 * A console implementation to use as a default if {@code null} is passed to
	 * {@link SLEclipseStatusUtility#touch(TouchNotificationUI)}.
	 */
	public static class LogTouchNotificationUI implements TouchNotificationUI {

		@Override
		public ILicenseObserver getLicenseObserver() {
			return new LogOutputLicenseObserver();
		}

		@Override
		public void notifyLowMemory(long maxMemoryMB, long maxPermGenMB) {
			if (maxMemoryMB < SLEclipseStatusUtility.LOW_MEM_THRESHOLD) {
				SLLogger.getLogger().warning(
						I18N.msg("common.touch.lowMemory", maxMemoryMB,
								SLEclipseStatusUtility.LOW_MEM_THRESHOLD,
								SLEclipseStatusUtility.LOW_MEM_THRESHOLD));
			}
			if (maxPermGenMB < SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD) {
			}
			SLLogger.getLogger().warning(
					I18N.msg("common.touch.lowPermGen", maxPermGenMB,
							SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD,
							SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD));
		}
	}

	/**
	 * An implementation of {@link ILicenseObserver} that outputs errors and
	 * warnings to the log.
	 */
	public static class LogOutputLicenseObserver implements ILicenseObserver {

		@Override
		public void notifyNoLicenseFor(String productName) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.msg("common.touch.license.noLicense", productName));
		}

		@Override
		public void notifyExpiration(String productName, Date expiration) {
			SLLogger.getLogger().warning(
					I18N.msg("common.touch.license.expiration", productName,
							expiration));
		}
	}

	/**
	 * Used by other plug-ins to "touch" common-eclipse-core and ensure it
	 * loads.
	 * 
	 * @param ui
	 *            an {@link TouchNotificationUI} implementation or {@code null}
	 *            to use the default console notification scheme.
	 */
	public static void touch(TouchNotificationUI ui) {
		if (ui == null)
			ui = new LogTouchNotificationUI();
		/*
		 * Only warn when our first plug-in is loaded.
		 */
		if (!f_firstTouch.compareAndSet(false, true))
			return;
		warnAboutLowMaximumMemory(ui);
		SLLicenseUtility.addObserver(ui.getLicenseObserver());
	}

	/**
	 * This method logs the JVM memory configuration and warns the user if
	 * Eclipse seems to be using too little memory.
	 * <p>
	 * The user can opt-out of this warning.
	 */
	private static void warnAboutLowMaximumMemory(TouchNotificationUI ui) {
		if (!PreferencesUtility.warnAboutLowMaximumMemory())
			return;
		final Runtime rt = Runtime.getRuntime();
		final long maxMemoryMB = SLUtility.byteToMByte(rt.maxMemory());
		final long totalMemoryMB = SLUtility.byteToMByte(rt.totalMemory());
		final long freeMemoryMB = SLUtility.byteToMByte(rt.freeMemory());
		SLLogger.getLogger().fine(
				"SureLogic : Java runtime: maxMemory=" + maxMemoryMB
						+ " MB; totalMemory=" + totalMemoryMB
						+ " MB; freeMemory=" + freeMemoryMB
						+ " MB; availableProcessors="
						+ rt.availableProcessors());

		final PermGenArg permGenArg = getPermGenArg();
		if (permGenArg.arg != null) {
			SLLogger.getLogger().fine(
					"SureLogic : Java runtime: " + permGenArg.arg
							+ "; maxPermSize=" + permGenArg.sizeInMB + " MB");
		}

		if (maxMemoryMB < LOW_MEM_THRESHOLD
				|| permGenArg.sizeInMB < LOW_PERMGEN_THRESHOLD) {
			ui.notifyLowMemory(maxMemoryMB, permGenArg.sizeInMB);
		}
	}

	private static class PermGenArg {
		final String arg;
		final int sizeInMB;

		PermGenArg(String arg, int size) {
			this.arg = arg;
			sizeInMB = size;
		}
	}

	/**
	 * Attempts to get the PermGen setting. However, this only works on Sun JVMs
	 * so it is set to {@link Integer#MAX_VALUE} if we are not on a Sun JVM.
	 * <p>
	 * The implementation of this method is brittle, e.g., it assumes the
	 * default setting is 64m.
	 * 
	 * @return the PermGen setting.
	 */
	private static PermGenArg getPermGenArg() {
		String vendor = System.getProperty("java.vm.vendor");
		if (!vendor.startsWith("Sun")) {
			return new PermGenArg(null, Integer.MAX_VALUE);
		}
		for (String arg : ManagementFactory.getRuntimeMXBean()
				.getInputArguments()) {
			if (arg.startsWith(MAX_PERM_SIZE)) {
				final int start = MAX_PERM_SIZE.length();
				int end = start;
				while (end < arg.length() && Character.isDigit(arg.charAt(end))) {
					end++;
				}
				final String num = arg.substring(start, end);
				if (end >= arg.length()) {
					// Just digits
					return new PermGenArg(arg, Integer.valueOf(num)
							/ (1024 * 1024));
				}
				final char units = arg.charAt(end);
				switch (units) {
				case 'm':
				case 'M':
					return new PermGenArg(arg, Integer.valueOf(num));
				case 'g':
				case 'G':
					return new PermGenArg(arg, Integer.valueOf(num) * 1024);
				case 'k':
				case 'K':
					return new PermGenArg(arg, Integer.valueOf(num) / 1024);
				default:
					return new PermGenArg(arg, -1);
				}
			}
		}
		// defaults to PermGen size 64MB
		return new PermGenArg(null, 64);
	}

	/**
	 * Converts a {@link SLStatus} to an {@link IStatus} for use within Eclipse.
	 * 
	 * @param status
	 *            the non-null {@link SLStatus} object.
	 * @return an equivalent {@link IStatus} object.
	 * @throws IllegalArgumentException
	 *             if status is {@code null}.
	 */
	public static IStatus convert(final SLStatus status) {
		return convert(status, Activator.getDefault());
	}

	/**
	 * Converts a {@link SLStatus} to an {@link IStatus} for use within Eclipse.
	 * Creates a serviceable status if plugin is non-null
	 */
	public static IStatus convert(final SLStatus status, Plugin plugin) {
		if (status == null)
			throw new IllegalArgumentException(I18N.err(44, "status"));

		final SLSeverity from = status.getSeverity();
		final int to;
		switch (from) {
		case CANCEL:
			to = IStatus.CANCEL;
			break;
		case ERROR:
			to = IStatus.ERROR;
			break;
		case INFO:
			to = IStatus.INFO;
			break;
		case OK:
			to = IStatus.OK;
			break;
		case WARNING:
			to = IStatus.WARNING;
			break;
		default:
			throw new AssertionError(I18N.err(100, from.toString()));
		}
		MultiStatus multi;
		if (plugin == null) {
			if (status.getChildren().isEmpty()) {
				// No children
				return createStatus(to, status.getCode(), status.getMessage(),
						status.getException());
			} else {
				multi = createMultiStatus(status.getCode(),
						status.getMessage(), status.getException());
			}
		} else {
			multi = createServicableStatus(to, status.getCode(),
					status.getMessage(), status.getException(), plugin);
		}
		for (SLStatus child : status.getChildren()) {
			multi.add(convert(child, null));
		}
		return multi;
	}

	/**
	 * Creates a new <code>INFO</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 */
	public static IStatus createInfoStatus(final String message) {
		return createServicableStatus(IStatus.INFO, IStatus.OK, message, null);
	}

	/**
	 * Creates a new <code>INFO</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	public static IStatus createInfoStatus(final String message,
			final Throwable exception) {
		return createServicableStatus(IStatus.INFO, IStatus.OK, message,
				exception);
	}

	/**
	 * Creates a new <code>WARNING</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 */
	public static IStatus createWarningStatus(final int code,
			final String message) {
		return createServicableStatus(IStatus.WARNING, code, message, null);
	}

	/**
	 * Creates a new <code>WARNING</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	public static IStatus createWarningStatus(final int code,
			final String message, final Throwable exception) {
		return createServicableStatus(IStatus.WARNING, code, message, exception);
	}

	/**
	 * Creates a new <code>ERROR</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 */
	public static IStatus createErrorStatus(final int code, final String message) {
		return createServicableStatus(IStatus.ERROR, code, message, null);
	}

	/**
	 * Creates a new <code>ERROR</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	public static IStatus createErrorStatus(final int code,
			final Throwable exception) {
		return createServicableStatus(IStatus.ERROR, code, "unexpected error",
				exception);
	}

	/**
	 * Creates a new <code>ERROR</code> multi-status object with children
	 * representing servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	public static IStatus createErrorStatus(final int code,
			final String message, final Throwable exception) {
		return createServicableStatus(IStatus.ERROR, code, message, exception);
	}

	/**
	 * Creates a new multi-status object with children representing
	 * servicability information.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param severity
	 *            the severity; one of <code>OK</code>, <code>ERROR</code>,
	 *            <code>INFO</code>, <code>WARNING</code>, or
	 *            <code>CANCEL</code>.
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	public static MultiStatus createServicableStatus(final int severity,
			final int code, final String message, final Throwable exception) {
		final Activator activator = Activator.getDefault();
		return createServicableStatus(severity, code, message, exception,
				activator);
	}

	private static MultiStatus createServicableStatus(final int severity,
			final int code, final String message, final Throwable exception,
			Plugin plugin) {
		final MultiStatus result = createMultiStatus(code, message, exception);
		final Bundle bundle = plugin == null ? null : plugin.getBundle();
		if (bundle != null) {
			final String bundleName = bundle.getHeaders().get("Bundle-Name")
					.toString();
			final String bundleVendor = bundle.getHeaders()
					.get("Bundle-Vendor").toString();
			final String bundleVersion = bundle.getHeaders()
					.get("Bundle-Version").toString();
			result.add(createStatus(severity, code, "Plug-in Vendor: "
					+ bundleVendor, null));
			result.add(createStatus(severity, code, "Plug-in Name: "
					+ bundleName, null));
			result.add(createStatus(severity, code,
					"Version: " + bundleVersion, null));
			result.add(createStatus(severity, code,
					"Plug-in ID: " + bundle.getSymbolicName(), null));
		} else {
			result.add(createStatus(severity, code, "Plug-in ID: "
					+ Activator.getDefault().getPlugInId(), null));
		}
		if (code > 0) {
			result.add(createStatus(severity, code,
					"Plug-in Serviceability Code: (SureLogic #" + code + ")",
					null));
		}
		return result;
	}

	/**
	 * Creates a new multi-status object with no children.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	private static MultiStatus createMultiStatus(final int code,
			final String message, final Throwable exception) {
		return new MultiStatus(Activator.getDefault().getPlugInId(), code,
				message, exception);
	}

	/**
	 * Creates a new status object. The created status has no children.
	 * <p>
	 * The new object is associated with the SureLogic plug-in.
	 * 
	 * @param severity
	 *            the severity; one of <code>OK</code>, <code>ERROR</code>,
	 *            <code>INFO</code>, <code>WARNING</code>, or
	 *            <code>CANCEL</code>.
	 * @param code
	 *            the plug-in-specific status code, or <code>OK</code>.
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not applicable.
	 */
	private static IStatus createStatus(final int severity, final int code,
			final String message, final Throwable exception) {
		return new Status(severity, Activator.getDefault().getPlugInId(), code,
				message, exception);
	}

	private SLEclipseStatusUtility() {
		// no instances
	}
}
