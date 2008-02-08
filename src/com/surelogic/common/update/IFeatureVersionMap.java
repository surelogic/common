package com.surelogic.common.update;

/**
 * Provides access to information about SureLogic software products.
 */
public interface IFeatureVersionMap {
	/**
	 * Looks up the value for the given key. Typically this gets the version
	 * number for the newest release of a product from SureLogic.
	 * 
	 * @param key
	 *            the identifier (typically a product identifier).
	 * @return the value (typically the version number of the newest release
	 *         from SureLogic), or <tt>null</tt> if no value is defined.
	 */
	String get(String key);

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
