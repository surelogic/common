package com.surelogic.common.core;

import java.io.File;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.surelogic.common.SLUtility;
import com.surelogic.common.core.logging.EclipseHandler;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.feedback.Counts;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

  private static Activator plugin;

  public Activator() {
    if (plugin != null)
      throw new IllegalStateException(Activator.class.getName() + " instance already exits, it should be a singleton.");
    // change derby.log location to the workspace
    System.setProperty("derby.stream.error.file",
        new File(EclipseUtility.getWorkspacePath(), SLUtility.DERBY_LOG_NAME).getAbsolutePath());
    plugin = this;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    /*
     * Configure the SLLogger to write to the Eclipse log.
     * 
     * The EclipseHandler also adjusts the Level of logging based upon the debug
     * trace settings for this plug-in.
     */
    SLLogger.addHandler(new EclipseHandler());

    CommonCorePreferencesUtility.initializeDefaultScope();

    SLLicenseUtility.setToolReleaseDate(EclipseUtility.getReleaseDate());

    Counts.getInstance().load();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    try {
      EclipseUtility.persistPreferences();
      Counts.getInstance().persist();
    } finally {
      super.stop(context);
    }
    plugin = null;
  }

  /**
   * Returns the shared instance.
   * 
   * @return the shared instance.
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Gets the identifier for this plug in.
   * 
   * @return an identifier, such as <tt>com.surelogic.common</tt>. In rare
   *         cases, for example bad plug in XML, it may be {@code null}.
   * @see Bundle#getSymbolicName()
   */
  public String getPlugInId() {
    return plugin.getBundle().getSymbolicName();
  }
}
