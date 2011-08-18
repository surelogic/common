package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;

public final class ProblemReportMessage extends MessageWithLog {

	@Override
	public String getMessageTypeString() {
		return "Problem Report";
	}

	@Override
	public String propPfx() {
		return "common.send.problemReport.wizard.";
	}

	public ProblemReportMessage(File eclipseLogFile) {
		super(eclipseLogFile);
	}

	@Override
	protected void generateMessageHelper(StringBuilder b) {
		super.generateMessageHelper(b);

		if (getSendLog()) {
			final String lf = SLUtility.PLATFORM_LINE_SEPARATOR;
			b.append(lf).append(lf);
			b.append(ServiceabilityConstants.ECLIPSE_LOG_TITLE);
			b.append(lf).append(lf);
			b.append(FileUtility.getFileContentsAsString(getLogFile()));
		}
	}
}
