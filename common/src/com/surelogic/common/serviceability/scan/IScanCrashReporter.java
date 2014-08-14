package com.surelogic.common.serviceability.scan;

import java.io.File;

import com.surelogic.ThreadSafe;
import com.surelogic.common.jobs.SLStatus;

/**
 * Interface for external scan crash reporters.
 * <p>
 * This abstraction is necessary because the Eclipse UI, which is used by the
 * Eclipse JSure and Sierra clients to report scan failures, is not available to
 * the code that runs the external scans.
 */
@ThreadSafe
public interface IScanCrashReporter {

	/**
	 * Reports a analysis scan crash. Can be invoked from any thread context.
	 * 
	 * @param status
	 *            the status built up about the scan filter. Cannot be
	 *            {@code null}.
	 * @param scanLog
	 *            the log file for the scan within the scan directory. Cannot be
	 *            {@code null}, but reporters should check that the file exists.
	 */
	void reportScanCrash(SLStatus status, File scanLog);

	void reportScanCancellation(String msg);
}
