package com.surelogic.common.jobs.remote;

import java.io.File;

public interface ILocalConfig {
  /**
   * Gets the path to the passed plugin, may be a directory or a Jar.
   * 
   * @param pluginId
   *          the plugin identifier.
   * @return the path to the plugin directory or Jar for the passed identifier.
   *         The returned file or directory must exist.
   * 
   * @throws IllegalArgumentException
   *           if the passed plugin identifier does not have a directory or Jar.
   */
  File getPluginDirectory(String pluginId);

  int getMemorySize();

  String getTestCode();

  boolean isVerbose();

  /**
   * @return A path to where you want the console output to go
   */
  String getLogPath();

  /**
   * 
   * @return A path to a directory to store output in
   */
  String getRunDirectory();
}
