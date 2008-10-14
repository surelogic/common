package com.surelogic.common.license;

import java.util.Date;

/**
 * Allows observation of license check failures.
 */
public interface ILicenseObserver {

	/**
	 * Indicates that a license check for a license subject failed. In addition
	 * to an exact subject match a check to see if an <i>All Tools</i> license
	 * was tried and failed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @see SLLicenseUtility#validate(String)
	 */
	void notifyNoLicenseFor(String subject);

	/**
	 * Indicates that a license that was checked expires within a week.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @param expiration
	 *            the non-null expiration date.
	 * @see SLLicenseUtility#validate(String)
	 */
	void notifyExpiration(String subject, Date expiration);
}
