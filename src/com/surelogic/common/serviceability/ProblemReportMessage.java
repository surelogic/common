package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;

public final class ProblemReportMessage extends Message {

	@Override
	public String getMessageTypeString() {
		return "Problem Report";
	}

	@Override
	public String propPfx() {
		return "common.send.problemReport.wizard.";
	}

	private File f_ideLogFile;

	public File getIdeLogFile() {
		return f_ideLogFile;
	}

	public void setIdeLogFile(File ideLogFile) {
		setDirty(f_ideLogFile, ideLogFile);
		f_ideLogFile = ideLogFile;
	}

	@Override
	protected void generateMessageHelper(StringBuilder b) {
		super.generateMessageHelper(b);

		if (f_ideLogFile != null) {
			final String lf = SLUtility.PLATFORM_LINE_SEPARATOR;
			b.append(lf).append(lf);
			b.append(ServiceabilityConstants.ECLIPSE_LOG_TITLE);
			b.append(lf).append(lf);
			b.append(FileUtility.getFileContentsAsString(f_ideLogFile));
		}
	}
}
