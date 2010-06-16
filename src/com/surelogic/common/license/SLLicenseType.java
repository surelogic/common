package com.surelogic.common.license;

import java.util.HashMap;
import java.util.Map;

/**
 * The types of licenses supported by SureLogic.
 */
public enum SLLicenseType {

	/**
	 * This type of license stops use of licensed tools on an expiration date.
	 * To continue use of the tools after the expiration date a new license must
	 * be installed.
	 */
	USE("U"),

	/**
	 * This type of license stops use of new tool releases after a given
	 * expiration date. Versions of the tools released before the expiration
	 * date remain usable. To use versions of the tools released after the
	 * expiration date a new license must be installed.
	 */
	SUPPORT("S"),

	/**
	 * This type of license doesn't expire unless it is blacklisted. However, it
	 * must be renewed by a net check periodically (recorded in the expiration
	 * date used by the other two types of licenses but referred to as the
	 * renewal deadline). If a perpetual license is not renewed prior to its
	 * renewal deadline (i.e., its expiration date) then this type of license
	 * stops use of new tool releases (similar to an expired support license)
	 * until a renewal is successfully performed.
	 */
	PERPETUAL("P");

	private final String f_symbol;

	SLLicenseType(String symbol) {
		f_symbol = symbol;
	}

	public String toSymbol() {
		return f_symbol;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public String toHumanString() {
		StringBuilder b = new StringBuilder(name().toLowerCase());
		b.replace(0, 1, b.substring(0, 1).toUpperCase());
		return b.toString();
	}

	/*
	 * See page 154 of Bloch's <i>Effective Java</i> (second edition) for a
	 * further description of supporting a fromString operation.
	 */

	private static final Map<String, SLLicenseType> stringToEnum = new HashMap<String, SLLicenseType>();
	static {
		for (SLLicenseType type : values()) {
			stringToEnum.put(type.toSymbol(), type);
		}
	}

	/**
	 * Returns the license type from a one letter code.
	 * 
	 * @param value
	 *            one of <tt>"U"</tt>, <tt>"S"</tt>, or <tt>"P"</tt>.
	 * @return the license type, or {@code null} if the passed value is not
	 *         recognized.
	 */
	public static SLLicenseType fromSymbol(String value) {
		return stringToEnum.get(value);
	}
}
