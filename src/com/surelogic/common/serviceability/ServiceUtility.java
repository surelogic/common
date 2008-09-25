package com.surelogic.common.serviceability;

import java.io.File;
import java.util.Date;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

public final class ServiceUtility {

	/**
	 * Composes a tip in a format to send to SureLogic.
	 * 
	 * @param email
	 *            email address of the tip author, may be {@code null}.
	 * @param name
	 *            name of the tip author, may be {@code null}.
	 * @param tip
	 *            the tip text.
	 * @param includeVersionInfo
	 *            {@code true} if OS, Java, and IDE version information should
	 *            be included in the generated tip, {@code false} if not.
	 * @param ideVersion
	 *            a string representing the version of the IDE that created this
	 *            tip. The OS and Java versions are looked up by this method.
	 * @param includeUsage
	 *            {@code true} if data from the SureLogic feature usage log
	 *            should be included in the generated tip, {@code false} if not.
	 * @return a tip in a format to send to SureLogic.
	 */
	public static String composeATip(String email, String name, String tip,
			boolean includeVersionInfo, String ideVersion, boolean includeUsage) {
		final StringBuilder b = new StringBuilder();
		final String lf = System.getProperty("line.separator");

		b.append("   Date: ");
		b.append(SLUtility.toStringMS(new Date()));
		b.append(lf);
		b.append("Subject: Tip for Improvement");
		b.append(lf);
		b.append("     To: SureLogic, Inc.");
		b.append(lf);
		b.append("   From: ");
		if ("".equals(name))
			b.append("(anonymous)");
		else
			b.append(name);
		b.append(" ");
		if (!"".equals(email)) {
			b.append("<");
			b.append(email);
			b.append(">");
		}

		b.append(lf).append(lf);

		if (includeVersionInfo) {
			b.append("     OS: ");
			b.append(System.getProperty("os.name"));
			b.append(" ");
			b.append(System.getProperty("os.version"));
			b.append(lf);
			b.append("   Java: ");
			b.append(System.getProperty("java.vendor"));
			b.append(" ");
			b.append(System.getProperty("java.version"));
			b.append(lf);
			b.append("    IDE: ");
			b.append(ideVersion);

			b.append(lf).append(lf);
		}

		b.append(tip);

		if (includeUsage) {
			b.append(lf).append(lf);
			b.append(I18N.msg("common.serviceability.usageLog.title"));
			b.append(lf).append(lf);
			b.append(UsageMeter.getInstance().getFileContents());
		}

		return b.toString();
	}

	/**
	 * Composes a problem report in a format to send to SureLogic.
	 * 
	 * @param email
	 *            email address of the tip author, may be {@code null}.
	 * @param name
	 *            name of the tip author, may be {@code null}.
	 * @param report
	 *            the problem report text.
	 * @param includeVersionInfo
	 *            {@code true} if OS, Java, and IDE version information should
	 *            be included in the generated problem report, {@code false} if
	 *            not.
	 * @param ideVersion
	 *            a string representing the version of the IDE that created this
	 *            problem report. The OS and Java versions are looked up by this
	 *            method.
	 * @param includeUsage
	 *            {@code true} if data from the SureLogic feature usage log
	 *            should be included in the generated problem report, {@code
	 *            false} if not.
	 * @param ideLogFile
	 *            a reference to the IDE log file, or {@code null} if no IDE log
	 *            file should be included in this problem report.
	 * @param ideLogFileI18nKey
	 *            a key to lookup the title for the IDE log file. See
	 *            {@link I18N#msg(String)}.
	 * @return a problem report in a format to send to SureLogic.
	 */
	public static String composeAProblemReport(String email, String name,
			String report, boolean includeVersionInfo, String ideVersion,
			boolean includeUsage, File ideLogFile, String ideLogFileI18nKey) {
		final StringBuilder b = new StringBuilder();
		final String lf = System.getProperty("line.separator");

		b.append("   Date: ");
		b.append(SLUtility.toStringMS(new Date()));
		b.append(lf);
		b.append("Subject: Problem Report");
		b.append(lf);
		b.append("     To: SureLogic, Inc.");
		b.append(lf);
		b.append("   From: ");
		if ("".equals(name))
			b.append("(anonymous)");
		else
			b.append(name);
		b.append(" ");
		if (!"".equals(email)) {
			b.append("<");
			b.append(email);
			b.append(">");
		}

		b.append(lf).append(lf);

		if (includeVersionInfo) {
			b.append("     OS: ");
			b.append(System.getProperty("os.name"));
			b.append(" ");
			b.append(System.getProperty("os.version"));
			b.append(lf);
			b.append("   Java: ");
			b.append(System.getProperty("java.vendor"));
			b.append(" ");
			b.append(System.getProperty("java.version"));
			b.append(lf);
			b.append("    IDE: ");
			b.append(ideVersion);

			b.append(lf).append(lf);
		}

		b.append(report);

		if (includeUsage) {
			b.append(lf).append(lf);
			b.append(I18N.msg("common.serviceability.usageLog.title"));
			b.append(lf).append(lf);
			b.append(UsageMeter.getInstance().getFileContents());
		}

		if (ideLogFile != null) {
			b.append(lf).append(lf);
			b.append(I18N.msg(ideLogFileI18nKey));
			b.append(lf).append(lf);
			b.append(FileUtility.getFileContents(ideLogFile));
		}

		return b.toString();
	}

	private ServiceUtility() {
		// no instances
	}
}
