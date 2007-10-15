package com.surelogic.common;

/**
 * Allows clients to discover if the user wants experimental functionality. By
 * defining the property <code>SureLogicX</code> experimental functionality is
 * enabled. For example,
 * 
 * <pre>
 * -DSureLogicX=on
 * </pre>
 * 
 * on the Java command line or as a VM argument within Eclipse.
 */
public final class XUtil {

	private static final boolean f_useExperimental = System
			.getProperty("SureLogicX") != null;

	/**
	 * Indicates if experimental functionality is enabled.
	 * 
	 * @return <code>true</code> if experimental functionality is enabled,
	 *         <code>false</code> otherwise.
	 */
	public static boolean useExperimental() {
		return f_useExperimental;
	}
}
