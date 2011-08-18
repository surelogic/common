package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

public final class JSureScanFailureMessage extends Message {

	@Override
	public String getMessageTypeString() {
		return "JSure Scan Failure Report";
	}

	@Override
	public String propPfx() {
		return "common.send.scanFailureReport.wizard.";
	}

	public JSureScanFailureMessage(File scanLog) {
		if (scanLog == null)
			throw new IllegalStateException(I18N.err(44, "scanLog"));
		f_scanLog = scanLog;
	}

	private final File f_scanLog;

	public File getScanLog() {
		return f_scanLog;
	}

	@Override
	protected void generateMessageHelper(StringBuilder b) {
		super.generateMessageHelper(b);

		final String lf = SLUtility.PLATFORM_LINE_SEPARATOR;
		b.append(lf).append(lf);
		b.append(ServiceabilityConstants.TITLE_PREFIX);
		b.append(" ");
		b.append(f_scanLog.getAbsolutePath());
		b.append(" ---");
		b.append(lf).append(lf);
		b.append(FileUtility.getFileContentsAsString(f_scanLog));
	}
}
