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
	public static final boolean testing = System.getProperty("dc.testing","false").equals("true");
	public static final boolean testingWorkspace = System.getProperty("jsure.test.workspace","false").equals("true");
	
	private static final boolean f_useDeveloperMode = 
		System.getProperty("SureLogicDev") != null;

	/**
	 * Indicates if developer functionality is enabled.
	 * 
	 * @return <code>true</code> if developer functionality is enabled,
	 *         <code>false</code> otherwise.
	 */
	public static boolean useDeveloperMode() {
		return f_useDeveloperMode;
	}

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
	
	private static final String f_runTest = System.getProperty("SureLogicTest");
	
	/**	 
	 * @return The name of the test to be run, e.g. UAM
	 */
	public static String runTest() {
		return f_runTest;
	}
	
	private static final String f_recordScript = System.getProperty("SureLogicRecordScript");

	/**	 
	 * @return The directory to which the script (and other resources) should be written
	 */
	public static String recordScript() {
		return f_recordScript;
	}
}
