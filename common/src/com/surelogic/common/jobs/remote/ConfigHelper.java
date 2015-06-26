package com.surelogic.common.jobs.remote;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.surelogic.common.logging.SLLogger;

/**
 * Collects various convenience methods for dealing with a classpath for Eclipse
 * plugins and their associated resources.
 * <p>
 * This needs to handle meta-workspace, release, Ant, and Maven.
 */
public final class ConfigHelper {
  public final boolean debug;
  private final ILocalConfig config;
  // avoid duplicate entries
  private final Set<File> path = new HashSet<>();

  public ConfigHelper(boolean debug, ILocalConfig config) {
    this.debug = debug;
    this.config = config;
  }

  String getPluginDir(final String pluginId) {
    return getPluginDir(pluginId, true);
  }

  File getPluginDirAsFile(final String pluginId) {
    return new File(getPluginDir(pluginId));
  }

  public String getPluginDir(final String pluginId, boolean required) {
    final String pluginDir = config.getPluginDir(pluginId, required);
    if (debug) {
      System.out.println(pluginId + " = " + pluginDir);
    }
    // usedPlugins.add(pluginId);
    return pluginDir;
  }

  /**
   * Adds a plugin directory or (packed) Jar to the classpath. This needs to
   * handle meta-workspace, release, Ant, and Maven.
   * 
   * @param pluginId
   *          the plugin id.
   */
  public void addPluginToPath(final String pluginId) {
    final String pluginDirStr = getPluginDir(pluginId);
    final File pluginDir = new File(pluginDirStr);
    if (pluginDir.isDirectory()) {
      final File pluginDirBin = new File(pluginDir, "bin");
      boolean inMetaWorkspace = pluginDirBin.isDirectory();
      if (inMetaWorkspace)
        addToPath(pluginDirBin);
    }
    addToPath(pluginDir);
  }

  /**
   * @return true if found
   */
  public boolean addPluginJarsToPath(final String pluginId, String... jars) {
    return addPluginJarsToPath(pluginId, false, jars);
  }

  /**
   * Adds a series of Jars to the classpath, the plugin must be unpacked.
   * 
   * @param exclusive
   *          If true, try each of the jars in sequence until one exists
   * @return true if found
   */
  private boolean addPluginJarsToPath(final String pluginId, boolean exclusive, String... jars) {
    boolean rv = true;
    final String pluginDirStr = getPluginDir(pluginId);
    if (pluginDirStr != null) {
      File pluginDir = new File(pluginDirStr);
      if (pluginDir.exists() && !pluginDir.isDirectory() && pluginDirStr.endsWith(".jar")) {
        final File dirInAnt = pluginDir.getParentFile();
        // When we have a jar, we are in Ant, so go up one level
        pluginDir = dirInAnt;
      }
      for (String jar : jars) {
        boolean exists = addToPath(new File(pluginDir, jar).getAbsolutePath(), !exclusive);
        if (exclusive && exists) {
          return true;
        }
        rv = rv && exists;
      }
    }
    return rv;
  }

  public void addAllPluginJarsToPath(final String pluginId, String libPath) {
    findJarsAndAddToPathIn(new File(getPluginDirAsFile(pluginId), libPath));
  }

  public boolean addToPath(String name) {
    return addToPath(new File(name), true);
  }

  public boolean addToPath(File f) {
    return addToPath(f, true);
  }

  boolean addToPath(String name, boolean required) {
    return addToPath(new File(name), required);
  }

  public boolean addToPath(File f, boolean required) {
    final boolean exists = f.exists();
    if (!exists) {
      if (required) {
        // FIX
        throw new RuntimeException("Missing required library: " + f.getAbsolutePath());
        /*
         * RemoteSLJobConstants.ERROR_CODE_MISSING_FOR_JOB,
         * f.getAbsolutePath());
         */
      }
    } else {
      path.add(f);
    }
    return exists;
  }

  void findJarsAndAddToPathIn(File folder) {
    if (!folder.exists()) {
      SLLogger.getLogger().warning("Unable to find jars in non-existent folder: " + folder);
    }
    for (File f : folder.listFiles()) {
      String name = f.getName();
      if (name.endsWith(".jar")) {
        path.add(f);
      }
    }
  }

  /**
   * Add the plugin and all the jars on the associated path
   */
  public void addPluginAndJarsToPath(String pluginId, String jarPath) {
    addPluginToPath(pluginId);
    addAllPluginJarsToPath(pluginId, jarPath);
  }

  public Collection<File> getPath() {
    return path;
  }

  public void clear() {
    path.clear();
  }
}
