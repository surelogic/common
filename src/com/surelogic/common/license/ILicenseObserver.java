package com.surelogic.common.license;

import java.util.Date;

/**
 * Allows observation of license check failures.
 */
public interface ILicenseObserver {

	/**
	 * Indicates that a license check for a product failed.
	 * 
	 * @param productName
	 *            the name of a SureLogic product.
	 * @see SLLicenseUtility#validate(SLLicenseProduct)
	 */
	void notifyNoLicenseFor(String productName);

	/**
	 * Indicates that a license that was checked expires within a week.
	 * 
	 * @param productName
	 *            the name of a SureLogic product.
	 * @param expiration
	 *            the non-null expiration date.
	 * @see SLLicenseUtility#validate(SLLicenseProduct)
	 */
	void notifyExpiration(String productName, Date expiration);
}
