package com.surelogic.common.serviceability.scan;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import com.surelogic.Singleton;
import com.surelogic.ThreadSafe;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.logging.SLLogger;

/**
 * Manages how to report scan crashes. Typical use is
 * 
 * <pre>
 * JSureScanCrashReport.getInstance().getReporter()
 * 		.reportJSureScanCrash(status, logFile);
 * </pre>
 * 
 * this will always work because {@code null} is never returned from
 * {@link #getInstance()} or {@link #getReporter()}.
 * <p>
 * The reporter can be changed via {@link #setReporter(IScanCrashReporter)}
 * which is done by the JSure Eclipse plug-in to prompt the user to send a
 * report to SureLogic.
 * <p>
 * The default reporter just logs the crash
 * 
 * <pre>
 * public void reportJSureScanCrash(SLStatus status, File scanLog) {
 * 	status.logTo(SLLogger.getLogger());
 * }
 * </pre>
 * 
 * it doesn't do anything with the log file, but the Eclipse handler does send a
 * copy of the log file to SureLogic.
 */
@ThreadSafe
@Singleton
public final class JSureScanCrashReport {

	private static final JSureScanCrashReport INSTANCE = new JSureScanCrashReport();

	public static JSureScanCrashReport getInstance() {
		return INSTANCE;
	}

	private JSureScanCrashReport() {
		// singleton
	}

	static private final IScanCrashReporter f_defaultReporter = new IScanCrashReporter() {
		public void reportJSureScanCrash(SLStatus status, File scanLog) {
			status.logTo(SLLogger.getLogger());
		}
	};

	private final AtomicReference<IScanCrashReporter> f_reporter = new AtomicReference<IScanCrashReporter>(
			f_defaultReporter);

	/**
	 * Gets the reporter for scan crashes. Will never be {@code null}.
	 * 
	 * @return the non-{@code null} reporter for scan crashes.
	 */
	public IScanCrashReporter getReporter() {
		return f_reporter.get();
	}

	/**
	 * Sets the reporter for scan crashes. A value of {@code null} resets the
	 * reporter to the default.
	 * 
	 * @param reporter
	 *            a reporter or {@code null} to reset to the default.
	 * @return the old reporter.
	 */
	public IScanCrashReporter setReporter(IScanCrashReporter reporter) {
		if (reporter == null)
			reporter = f_defaultReporter;

		return f_reporter.getAndSet(reporter);
	}
}
