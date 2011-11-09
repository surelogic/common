package com.surelogic.common.serviceability;

import java.io.File;

public final class JSureScanFailureMessage extends MessageWithLog {

	@Override
	public String getMessageTypeString() {
		return "JSure Scan Failure Report";
	}

	@Override
	public String propPfx() {
		return "common.send.scanFailureReport.wizard.";
	}

	public JSureScanFailureMessage(File scanLog) {
		super(scanLog);
	}
}
