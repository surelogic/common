package com.surelogic.common.license;

import java.util.Date;
import java.util.UUID;

import com.surelogic.common.i18n.I18N;

/**
 * Represents a net check for a particular license. The clients pair these
 * instances with {@link SLLicense} instances if the license required a net
 * check to be installed. The digitally signed data that is used to construct
 * instances is constructed by the www.surelogic.com server as an affirmative
 * answer to installing or renewing a particular license.
 */
public final class SLLicenseNetCheck {

	/**
	 * The identify of the license license this net check refers to. May not be
	 * <tt>null</tt>.
	 */
	private final UUID f_uuid;

	/**
	 * Gets the {@link UUID} that identifies the license this net check refers
	 * to.
	 * 
	 * @return the non-<tt>null</tt> {@link UUID} the license this net check
	 *         refers to.
	 */
	public UUID getUuid() {
		return f_uuid;
	}

	/**
	 * The license expiration date or renewal deadline.
	 */
	private final Date f_date;

	/**
	 * Gets the license expiration date or renewal deadline.
	 * 
	 * @return the non-<tt>null</tt> license expiration date or renewal
	 *         deadline.
	 */
	public Date getDate() {
		return f_date;
	}

	/**
	 * Constructs a new SureLogic license.
	 * 
	 * @param uuid
	 *            a non-<tt>null</tt> {@link UUID} for the license this net
	 *            check refers to.
	 * @param date
	 *            a non-<tt>null</tt> license expiration date or renewal
	 *            deadline.
	 */
	public SLLicenseNetCheck(final UUID uuid, final Date date) {
		if (uuid == null)
			throw new IllegalArgumentException(I18N.err(44, "uuid"));
		f_uuid = uuid;
		if (date == null)
			throw new IllegalArgumentException(I18N.err(44, "date"));
		f_date = date;
	}

	@Override
	public String toString() {
		return SLLicensePersistence.toString(this);
	}
}
