package com.surelogic.common.core.jobs;

import java.io.File;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.jobs.remote.AbstractLocalConfig;

public class EclipseLocalConfig extends AbstractLocalConfig {
  public EclipseLocalConfig(int mem, File dir) {
    super(mem, dir);
  }

  public File getPluginDirectory(String pluginId) {
    return EclipseUtility.getInstallationDirectoryOf(pluginId);
  }
}
