package com.surelogic.common.serviceability;

import java.io.File;

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
}
