package com.surelogic.common;

/**
 * Allows clients to discover if the user wants experimental functionality. By
 * defining the property <code>SureLogicX</code> experimental functionality is
 * enabled. For example,
 * 
 * <pre>
 * -DSureLogicX=on
 * -DrunJSureInMemory=true
 * </pre>
 * 
 * on the Java command line or as a VM argument within Eclipse.
 */
public final class XUtil {
  /**
   * When running regression tests
   */
  public static final String testingProp = "dc.testing";
  public static final String ignoreProposalsProp = "jsure.test.ignore.proposals";

  /**
   * To load all classes from known jars
   */
  public static final String LOAD_ALL_LIBS = "SureLogic.loadAllLibs";

  private static final String f_updateScript = System.getProperty("SureLogicUpdateScript");
  static {
    if (f_updateScript != null) {
      System.setProperty(testingProp, "true");
    }
  }

  /**
   * When running regression tests
   */
  public static final boolean testing = System.getProperty(testingProp, "false").equals("true");
  public static final boolean testingWorkspace = System.getProperty("jsure.test.workspace", "false").equals("true");
  public static final boolean ignoreProposals = System.getProperty(ignoreProposalsProp, "false").equals("true");

  /**
   * To load all classes from known jars
   */
  public static final boolean loadAllLibs = System.getProperty(LOAD_ALL_LIBS, "false").equals("true");

  /**
   * For doing remote debugging on the JSure JVM
   */
  public static final boolean debug = System.getProperty("SureLogicDebug", "false").equals("true");

  /**
   * For running JSure in memory&mdash;developers use this to debug in Eclipse.
   */
  public static final boolean runJSureInMemory = System.getProperty("runJSureInMemory", "false").equals("true")
      || System.getProperty("SureLogicProfile", "false").equals("true");

  /**
   * Indicates if experimental functionality is enabled.
   * 
   * @return <code>true</code> if experimental functionality is enabled,
   *         <code>false</code> otherwise.
   */
  public static final boolean useExperimental = System.getProperty("SureLogicX") != null;

  private static final String f_runTest = System.getProperty("SureLogicTest");

  /**
   * @return The name of the test to be run, e.g. UAM
   */
  public static String runTest() {
    return f_runTest;
  }

  private static final String f_recordScript = computeRecordScriptValue();

  private static String computeRecordScriptValue() {
    final String temp = System.getProperty("SureLogicRecordScript");
    if (f_updateScript != null) {
      if (f_updateScript.endsWith(".zip")) {
        final String prefix = FileUtility.getPrefix(f_updateScript);
        if (temp != null) {
          System.out.println("Overriding SureLogicRecordScript: using " + prefix + ", instead of " + temp);
        }
        return prefix;
      } else {
        throw new IllegalStateException("Not ending with .zip: " + f_updateScript);
      }
    }
    return temp;
  }

  /**
   * The name of the scripted testcase (zipped) to be updated
   */
  public static String updateScript() {
    return f_updateScript;
  }

  /**
   * The main project [/ script dir]
   * 
   * @return The (relative) directory to which the script (and other resources)
   *         should be written
   */
  public static String recordScript() {
    return f_recordScript;
  }
}
