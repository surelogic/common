package com.surelogic.common.license;

/**
 * Allows observation of license check failures.
 */
public interface ILicenseObserver {

	/**
	 * Indicates that a check was requested for the passed license subject, but
	 * no such license is installed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 */
	void notifyNoLicenseFor(String subject);

}
