package com.surelogic.common.eclipse.logging;

import java.lang.management.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;

import com.surelogic.common.SLUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.dialogs.LowMaximumMemoryDialog;
import com.surelogic.common.eclipse.dialogs.NoLicenseDialog;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.preferences.PreferenceConstants;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLSeverity;
import com.surelogic.common.jobs.SLStatus;
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
	public static final int LOW_PERMGEN_THRESHOLD = SLUtility.is64bit ? 256 : 128;
	
	private static final String MAX_PERM_SIZE = "-XX:MaxPermSize=";
	
	private static final AtomicBoolean f_firstTouch = new AtomicBoolean(false);

	/**
	 * Used by other plug-ins to "touch" common-eclipse and ensure it loads.
	 */
	public static void touch() {
		/*
		 * Only warn when our first plug-in is loaded.
		 */
		if (!f_firstTouch.compareAndSet(false, true))
			return;
		warnAboutLowMaximumMemory();
		warnAboutIBMJavaVirtualMachine();
		warnAboutRunningJava6PluginOnJava5();
		SLLicenseUtility.addObserver(NoLicenseDialog.getInstance());
	}

	/**
	 * This method logs the JVM memory configuration and warns the user if
	 * Eclipse seems to be using too little memory.
	 * <p>
	 * The user can opt-out of this warning.
	 */
	private static void warnAboutLowMaximumMemory() {
		if (!PreferenceConstants.warnAboutLowMaximumMemory())
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
			SLLogger.getLogger().fine("SureLogic : Java runtime: "+permGenArg.arg+
					                  "; maxPermSize="+permGenArg.sizeInMB+" MB");
		}		
		
		if (maxMemoryMB < LOW_MEM_THRESHOLD || permGenArg.sizeInMB < LOW_PERMGEN_THRESHOLD) {
			if (!XUtil.testing) {
				final UIJob job = new SLUIJob() {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						final LowMaximumMemoryDialog dialog = new LowMaximumMemoryDialog(
								maxMemoryMB, permGenArg.sizeInMB);
						dialog.open();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			} else {
				SLLogger.getLogger().warning("Low max memory: "+maxMemoryMB+" MB, permGen: "+permGenArg.sizeInMB+" MB");
			}
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
	
	private static PermGenArg getPermGenArg() {
		String vendor = System.getProperty("java.vm.vendor");
		if (!vendor.startsWith("Sun")) {
			return new PermGenArg(null, Integer.MAX_VALUE);
		}
		for(String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (arg.startsWith(MAX_PERM_SIZE)) {
				final int start = MAX_PERM_SIZE.length();
				int end = start;
				while (end < arg.length() && Character.isDigit(arg.charAt(end))) {
					end++;
				}
				final String num = arg.substring(start, end);
				if (end >= arg.length()) {
					// Just digits
					return new PermGenArg(arg, Integer.valueOf(num) / (1024*1024)); 
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
	 * This method warns the user if Eclipse is running on an IBM JVM (Java 5)
	 * using the optimization setting '-Xsharedclasses:singleJVM;keep'. This
	 * setting doesn't work with our code (in particular JAXB). For more
	 * information on this problem see bug 1360.
	 */
	private static void warnAboutIBMJavaVirtualMachine() {
		/*
		 * A workaround to bug 1360 which only happens on Windows with the IBM
		 * Java Virtual Machine. We believe this is an IBM bug.
		 */
		if (!SystemUtils.IS_OS_WINDOWS)
			return;
		/*
		 * Edwin has tested the IBM JVM for Java 6 and it does not exhibit this
		 * problem only the Java 5 JVM.
		 */
		if (!SystemUtils.IS_JAVA_1_5)
			return;
		/*
		 * Only applies to IBM JVMs.
		 */
		if (!System.getProperty("java.vm.vendor").startsWith("IBM"))
			return;

		final String vmargs = System.getProperty("eclipse.vmargs");
		if (vmargs != null) {
			/*
			 * We found some vmargs so check if the broken IBM setting is within
			 * them.
			 */
			if (vmargs.indexOf("Xshareclasses") != -1) {
				final int errNo = 62;
				final String msg = I18N.err(errNo, System
						.getProperty("java.vm.vendor"), System
						.getProperty("java.vm.version"), System
						.getProperty("java.vm.name"));
				final IStatus reason = SLEclipseStatusUtility
						.createErrorStatus(errNo, msg);
				ErrorDialogUtility.open(null, "Unsupported JVM Optimization",
						reason);
			}
		}
	}

	/**
	 * This method warns the user if they are running on a Java 5 virtual
	 * machine without the Sierra Java 5 compatibility plug-in. This plug-in
	 * provides the JAXB libraries, which are standard on any Java 6 virtual
	 * machine.
	 */
	private static void warnAboutRunningJava6PluginOnJava5() {
		if (!SystemUtils.IS_JAVA_1_5)
			return;

		final String sierraPluginName = "com.surelogic.sierra.client.eclipse";
		if (Platform.getBundle(sierraPluginName) != null) {
			/*
			 * The Sierra plug-in is loaded so this check is of concern to us.
			 */

			final String pluginName = "com.surelogic.sierra.java5.compatibility";
			/*
			 * We are in Java 5 so we need the Java 5 compatibility plug-in to
			 * be loaded.
			 */
			if (Platform.getBundle(pluginName) == null) {
				final int errNo = 61;
				final String msg = I18N.err(errNo, System
						.getProperty("java.vm.version"), System
						.getProperty("java.vm.vendor"));
				final IStatus reason = SLEclipseStatusUtility
						.createErrorStatus(errNo, msg);
				ErrorDialogUtility
						.open(
								null,
								"Wrong Sierra Eclipse Client Version Installed",
								reason);
			}
		}
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
				return createStatus(to, status.getCode(),		
		   	           status.getMessage(), status.getException());
			} else {
				multi = createMultiStatus(status.getCode(),		
			   	           status.getMessage(), status.getException());
			}
		} else {
			multi = createServicableStatus(to, status.getCode(),		
		   	           status.getMessage(), status.getException(), plugin);
		}
		for(SLStatus child : status.getChildren()) {
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
		return createServicableStatus(severity, code, message, exception, activator);
	}
	
	private static MultiStatus createServicableStatus(final int severity,
			final int code, final String message, final Throwable exception, 
			Plugin plugin) {
		final MultiStatus result = createMultiStatus(code, message, exception);
		final Bundle bundle = plugin == null ? null : plugin.getBundle();
		if (bundle != null) {
			final String bundleName = bundle.getHeaders().get("Bundle-Name")
			.toString();
			final String bundleVendor = bundle.getHeaders().get("Bundle-Vendor")
			.toString();
			final String bundleVersion = bundle.getHeaders().get("Bundle-Version")
			.toString();
			result.add(createStatus(severity, code, "Plug-in Vendor: "
					+ bundleVendor, null));
			result.add(createStatus(severity, code, "Plug-in Name: " + bundleName,
					null));
			result.add(createStatus(severity, code, "Version: " + bundleVersion,
					null));
			result.add(createStatus(severity, code, "Plug-in ID: "
					+ bundle.getSymbolicName(), null));
		} else {
			result.add(createStatus(severity, code, "Plug-in ID: "
					+ Activator.PLUGIN_ID, null));
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
		return new MultiStatus(Activator.PLUGIN_ID, code, message, exception);
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
		return new Status(severity, Activator.PLUGIN_ID, code, message,
				exception);
	}

	private SLEclipseStatusUtility() {
		// no instances
	}
}
