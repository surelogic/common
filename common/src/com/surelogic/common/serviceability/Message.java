package com.surelogic.common.serviceability;

import java.util.Date;

import com.surelogic.common.SLUtility;

public abstract class Message {

	private String f_product;

	public final String getProduct() {
		return f_product;
	}

	public final void setProduct(String product) {
		setDirty(f_product, product);
		f_product = product;
	}

	private String f_email;

	public final String getEmail() {
		return f_email;
	}

	public final void setEmail(String email) {
		setDirty(f_email, email);
		f_email = email;
	}

	private String f_name;

	public final String getName() {
		return f_name;
	}

	public final void setName(String name) {
		setDirty(f_name, name);
		f_name = name;
	}

	private String f_summary;

	public final String getSummary() {
		return f_summary;
	}

	public final void setSummary(String summary) {
		setDirty(f_summary, summary);
		f_summary = summary;
	}

	private String f_description;

	public final String getDescription() {
		return f_description;
	}

	public final void setDescription(String description) {
		setDirty(f_description, description);
		f_description = description;
	}

	private boolean f_sendVersionInfo;

	public final boolean isSendVersionInfo() {
		return f_sendVersionInfo;
	}

	public final void setSendVersionInfo(boolean sendVersionInfo) {
		setDirty(f_sendVersionInfo, sendVersionInfo);
		f_sendVersionInfo = sendVersionInfo;
	}

	private String f_ideVersion;

	public final String getIdeVersion() {
		return f_ideVersion;
	}

	public final void setIdeVersion(String ideVersion) {
		setDirty(f_ideVersion, ideVersion);
		f_ideVersion = ideVersion;
	}

	public boolean minimumDataEntered() {
		if (f_description == null || f_summary == null)
			return false;
		final boolean tipTyped = f_description.length() != 0;
		final boolean summaryTyped = f_summary.length() != 0;
		return tipTyped && summaryTyped;
	}

	/**
	 * This bit is updated in the background job that loads in logs. See the
	 * implementation of {@link MessageWithLog}.
	 */
	private volatile boolean f_dirty = false;

	public final void setDirty() {
		f_dirty = true;
	}

	protected <T> void setDirty(T currentValue, T newValue) {
		if ((currentValue == null && newValue != null)
				|| (currentValue != null && !currentValue.equals(newValue)))
			setDirty();
	}

	private String f_message;

	public final String getMessage() {
		return f_message;
	}

	public final void setMessage(String message) {
		f_message = message;
	}

	public final void generateMessage(boolean force) {
		if (force || f_dirty) {
			f_dirty = false;
			final StringBuilder b = new StringBuilder();
			generateMessageHelper(b);
			f_message = b.toString();
		}
	}

	public abstract String getMessageTypeString();

	public abstract String propPfx();

	protected void generateMessageHelper(StringBuilder b) {
		final String lf = SLUtility.PLATFORM_LINE_SEPARATOR;

		b.append("   Date: ");
		b.append(SLUtility.toStringHMS(new Date()));
		b.append(lf);
		b.append("Subject: ");
		b.append(f_product);
		b.append(" ");
		b.append(getMessageTypeString());
		b.append(lf);
		b.append("     To: SureLogic, Inc.");
		b.append(lf);
		b.append("   From: ");
		if (f_name == null || "".equals(f_name)) {
			b.append("(anonymous)");
		} else {
			b.append(f_name);
		}
		b.append(" ");
		if (!(f_email == null || "".equals(f_email))) {
			b.append("<");
			b.append(f_email);
			b.append(">");
		}

		b.append(lf).append(lf);

		if (f_sendVersionInfo) {
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
			b.append(f_ideVersion);

			b.append(lf).append(lf);
		}

		if (!(f_summary == null || "".equals(f_summary))) {
			b.append("Summary: ").append(f_summary).append(lf).append(lf);
		}

		if (!(f_description == null || "".equals(f_summary))) {
			b.append(f_description);
		}
	}
}
