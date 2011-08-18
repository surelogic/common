package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;

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
			b.append(FileUtility.getFileContentsAsString(getLogFile()));
		}
	}
}
