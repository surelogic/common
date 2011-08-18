package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.i18n.I18N;

/**
 * Marker type for implementations that send a log to SureLogic.
 */
public abstract class MessageWithLog extends Message {

	private final File f_logFile;

	private volatile boolean f_sendLog = true;

	protected MessageWithLog(File logFile) {
		if (logFile == null)
			throw new IllegalStateException(I18N.err(44, "logFile"));
		f_logFile = logFile;
	}

	public File getLogFile() {
		return f_logFile;
	}

	public boolean getSendLog() {
		return f_sendLog;
	}

	public void setSendLog(boolean value) {
		f_sendLog = value;
	}
}
