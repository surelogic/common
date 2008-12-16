package com.surelogic.common.serviceability;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.UUID;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

public final class ServiceUtility {
	private final static String f_serviceLocation = I18N
			.msg("common.serviceability.supportrequest.url");

	/**
	 * Composes a tip in a format to send to SureLogic.
	 * 
	 * @param tool
	 *            the SureLogic tool that this tip is about.
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
	public static String composeATip(final String tool, final String email,
			final String name, final String tip,
			final boolean includeVersionInfo, final String ideVersion,
			final boolean includeUsage) {
		final StringBuilder b = new StringBuilder();
		final String lf = System.getProperty("line.separator");

		b.append("   Date: ");
		b.append(SLUtility.toStringHMS(new Date()));
		b.append(lf);
		b.append("Subject: ");
		if (!"".equals(tool)) {
			b.append(tool);
			b.append(" ");
		}
		b.append("Tip for Improvement");
		b.append(lf);
		b.append("     To: SureLogic, Inc.");
		b.append(lf);
		b.append("   From: ");
		if ("".equals(name)) {
			b.append("(anonymous)");
		} else {
			b.append(name);
		}
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
	 * @param tool
	 *            the SureLogic tool that this problem report is about.
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
	public static String composeAProblemReport(final String tool,
			final String email, final String name, final String report,
			final boolean includeVersionInfo, final String ideVersion,
			final boolean includeUsage, final File ideLogFile,
			final String ideLogFileI18nKey) {
		final StringBuilder b = new StringBuilder();
		final String lf = System.getProperty("line.separator");

		b.append("   Date: ");
		b.append(SLUtility.toStringHMS(new Date()));
		b.append(lf);
		b.append("Subject: ");
		if (!"".equals(tool)) {
			b.append(tool);
			b.append(" ");
		}
		b.append("Problem Report");
		b.append(lf);
		b.append("     To: SureLogic, Inc.");
		b.append(lf);
		b.append("   From: ");
		if ("".equals(name)) {
			b.append("(anonymous)");
		} else {
			b.append(name);
		}
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

	/**
	 * Composes a notice that a license was installed or uninstalled in a format
	 * to send to SureLogic.
	 * 
	 * @param install
	 *            {@code true} if a license was installed, {@code false} if the
	 *            license was uninstalled.
	 * @param tool
	 *            the tool or product that the license is for.
	 * @param issuedTo
	 *            the name of the person or company to whom the license was
	 *            issued.
	 * @param licenseId
	 *            the license identifier.
	 * @return a notice that a license was installed or uninstalled in a format
	 *         to send to SureLogic.
	 */
	public static String composeAInstallationNotice(final boolean install,
			final String tool, final String issuedTo, final UUID licenseId,
			final Date expiration) {
		final StringBuilder b = new StringBuilder();
		final String lf = System.getProperty("line.separator");

		b.append("   Date: ");
		b.append(SLUtility.toStringHMS(new Date()));
		b.append(lf);
		b.append("Subject: License ");
		if (install) {
			b.append("Installation");
		} else {
			b.append("Removal");
		}
		b.append(lf);

		b.append("   Tool: ");
		b.append(tool);
		b.append(lf);

		b.append(" Holder: ");
		b.append(issuedTo);
		b.append(lf);

		b.append("     Id: ");
		b.append(licenseId);
		b.append(lf);

		b.append("Expires: ");
		b.append(SLUtility.toStringDay(expiration));
		b.append(lf);
		return b.toString();
	}

	/**
	 * Constructs a job that sends a message over the Internet to SureLogic.
	 * 
	 * @param msg
	 *            the message for SureLogic.
	 */
	public static SLJob sendToSureLogic(final String msg) {
		return new SLJob() {

			public String getName() {
				return "Sending a servicability message to SureLogic";
			}

			public SLStatus run(final SLProgressMonitor monitor) {
				monitor.begin();
				try {
					// Prepare the URL connection
					final URL url = new URL(f_serviceLocation);
					final URLConnection conn = url.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setUseCaches(false);

					final OutputStream os = null;
					OutputStreamWriter wr = null;
					try {
						// Send the request
						wr = new OutputStreamWriter(conn.getOutputStream());
						wr.write(msg);
						wr.flush();

						// Check the response
						final InputStream is = conn.getInputStream();
						is.close();
					} finally {
						if (wr != null) {
							wr.close();
						} else if (os != null) {
							os.close();
						}
					}
				} catch (final Exception e) {
					return SLStatus.createErrorStatus(I18N.err(154,
							f_serviceLocation), e);
				}

				monitor.done();
				return SLStatus.OK_STATUS;
			}
		};
	}

	private ServiceUtility() {
		// no instances
	}
}
