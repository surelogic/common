package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.logging.SLLogger;

/**
 * Marker type for implementations that send a log to SureLogic.
 * <p>
 * Loading the log text should occur in the background using the {@link SLJob}
 * obtained from
 */
public abstract class MessageWithLog extends Message {

	private final File f_logFile;

	private volatile String f_logContents = "";

	private volatile boolean f_sendLog = true;

	protected MessageWithLog(File logFile) {
		if (logFile == null)
			throw new IllegalStateException(I18N.err(44, "logFile"));
		f_logFile = logFile;
	}

	public File getLogFile() {
		return f_logFile;
	}

	public String getLogFileContents() {
		return f_logContents;
	}

	public SLJob getReadInLogContentsJob() {
		final SLJob job = new AbstractSLJob("Loading contents of "
				+ f_logFile.getAbsolutePath()) {
			@Override
			public SLStatus run(SLProgressMonitor monitor) {
				/*
				 * Sometimes the Eclipse log file doesn't exist because the user
				 * can delete it. Basically, bail if we can't read the file
				 * passed.
				 */
				if (f_logFile.canRead()) {
					try {
						/*
						 * Lots can go wrong.
						 */
						final String contents = FileUtility
								.getFileContentsAsString(f_logFile);
						if (contents != null) {
							f_logContents = contents;
							setDirty();
						}
					} catch (Exception e) {
						// Just log it because the file might not exist
						SLStatus.createErrorStatus(e).logTo(
								SLLogger.getLogger());
					}
				}
				return SLStatus.OK_STATUS;
			}
		};
		return job;
	}

	public boolean getSendLog() {
		return f_sendLog;
	}

	public void setSendLog(boolean value) {
		setDirty(f_sendLog, value);
		f_sendLog = value;
	}
	
	@Override
	protected void generateMessageHelper(StringBuilder b) {
		super.generateMessageHelper(b);

		if (getSendLog()) {
			final String lf = SLUtility.PLATFORM_LINE_SEPARATOR;
			b.append(lf).append(lf);
			b.append(ServiceabilityConstants.TITLE_PREFIX);
			b.append(" ");
			b.append(getLogFile().getAbsolutePath());
			b.append(" ---");
			b.append(lf).append(lf);
			b.append(getLogFileContents());
		}
	}
}
