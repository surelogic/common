package com.surelogic.common.serviceability;

import java.io.File;

public final class SierraScanFailureMessage extends MessageWithLog {

	@Override
	public String getMessageTypeString() {
		return "Sierra Scan Failure Report";
	}

	@Override
	public String propPfx() {
		return "common.send.scanFailureReport.wizard.";
	}

	public SierraScanFailureMessage(File scanLog) {
		super(scanLog);
	}
}
