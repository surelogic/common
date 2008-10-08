package com.surelogic.common.license;

/**
 * Allows observation of license check failures.
 */
public interface ILicenseObserver {

	/**
	 * Indicates that a license check for a license subject failed. No no such
	 * license is installed, nor is there an all SureLogic tools license
	 * installed.
	 * 
	 * @param subject
	 *            the non-null license subject.
	 * @see SLLicenseUtility#validate(String)
	 */
	void notifyNoLicenseFor(String subject);
}
