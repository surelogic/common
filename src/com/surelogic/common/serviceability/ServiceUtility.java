package com.surelogic.common.serviceability;

import java.util.Date;

import com.surelogic.common.SLUtility;

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
	 * @param IdeVersion
	 *            a string representing the version of the IDE that created this
	 *            tip. The OS and Java versions are looked up by this method.
	 * @return a tip in a format to send to SureLogic.
	 */
	public static String composeATip(String email, String name, String tip,
			boolean includeVersionInfo, String IdeVersion) {
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
			b.append(IdeVersion);

			b.append(lf).append(lf);
		}

		b.append(tip);

		return b.toString();
	}

	private ServiceUtility() {
		// no instances
	}
}
