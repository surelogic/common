package com.surelogic.common;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import com.surelogic.NonNull;
import com.surelogic.common.logging.SLLogger;

public class CommonJVMPrefs {
  /**
   * Resource path in common for common properties
   */
  public static final String PATH = "/lib/scan_vm.properties";

  /**
   * Constant for looking up in resulting properties of {@link #getJvmPrefs()}.
   */
  public static final String VMARGS = "vmargs";

  /**
   * Gets properties defined in <tt>/lib/scan_vm.properties</tt> in the common
   * project.
   * 
   * @return a set of possibly empty properties
   */
  @NonNull
  public static Properties getJvmPrefs() {
    URL url = Thread.currentThread().getContextClassLoader().getResource(PATH);
    if (url == null) {
      url = CommonJVMPrefs.class.getResource(PATH);
    }
    final Properties result = new Properties();
    try {
      result.load(url.openStream());
    } catch (NullPointerException e) {
      SLLogger.getLogger().log(Level.WARNING, "NullPointerException occurred when loading " + PATH, e);
    } catch (IOException e) {
      SLLogger.getLogger().log(Level.WARNING, "IOException occurred when " + PATH, e);
    }
    return result;
  }
}
