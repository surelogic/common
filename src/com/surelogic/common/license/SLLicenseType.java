package com.surelogic.common.license;

/**
 * The types of licenses supported by SureLogic.
 */
public enum SLLicenseType {

	/**
	 * This type of license stops use of licensed tools on an expiration date.
	 * To continue use of the tools after the expiration date a new license must
	 * be installed.
	 */
	USE,

	/**
	 * This type of license stops use of new tool releases after a given
	 * expiration date. Versions of the tools released before the expiration
	 * date remain usable. To use versions of the tools released after the
	 * expiration date a new license must be installed.
	 */
	SUPPORT,

	/**
	 * This type of license doesn't expire unless it is blacklisted. However, it
	 * must be renewed by a net check periodically (recorded in the expiration
	 * date used by the other two types of licenses but referred to as the
	 * renewal deadline). If a perpetual license is not renewed prior to its
	 * renewal deadline (i.e., its expiration date) then this type of license
	 * stops use of new tool releases (similar to an expired support license)
	 * until a renewal is successfully performed.
	 */
	PERPETUAL;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name().toLowerCase());
		b.replace(0, 1, b.substring(0, 1).toUpperCase());
		return b.toString();
	}

	/**
	 * Returns the license type from a string.
	 * 
	 * @param value
	 *            a case insensitive string.
	 * @return the license type, or {@code null} if the passed value is not
	 *         recognized.
	 */
	public static SLLicenseType fromString(String value) {
		try {
			return valueOf(value.toUpperCase());
		} catch (Exception noValue) {
			// ignore, return null below
		}
		return null;
	}
}
