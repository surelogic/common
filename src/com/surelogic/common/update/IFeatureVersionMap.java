package com.surelogic.common.update;

/**
 * Provides access to released versions of SureLogic software products.
 */
public interface IFeatureVersionMap {
	/**
	 * Gets the version number for the newest release of a product from
	 * SureLogic.
	 * 
	 * @param productName
	 *            the product identifier.
	 * @return the version number of the newest release from SureLogic, or
	 *         <tt>null</tt> if the product identifier is unknown.
	 */
	String get(String productName);

	/**
	 * Checks if an upgrade is available for a given version of a product.
	 * 
	 * @param productName
	 *            the product identifier.
	 * @param currentVersion
	 *            the installed version number of the product.
	 * @return <tt>true</tt> if an upgrade is available for this product,
	 *         <tt>false</tt> otherwise.
	 */
	boolean upgradeAvailable(String productName, String currentVersion);
}
