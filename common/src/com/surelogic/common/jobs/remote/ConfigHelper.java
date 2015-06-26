package com.surelogic.common.jobs.remote;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import com.surelogic.common.logging.SLLogger;

/**
 * Collects various convenience methods for dealing with plugins and their
 * resulting classpaths
 *
 * @author edwin
 */
public final class ConfigHelper {
  public final boolean debug;
  private final ILocalConfig config;
  private final Collection<File> path;

  public ConfigHelper(boolean debug, ILocalConfig config) {
    this(debug, config, new HashSet<File>());
  }

  public ConfigHelper(boolean debug, ILocalConfig config, Collection<File> path) {
    this.debug = debug;
    this.config = config;
    this.path = path;
  }

  protected String getPluginDir(final String pluginId) {
    return getPluginDir(pluginId, true);
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
   * Adds libraries required to use the given Eclipse plugin
   */
  public void addPluginToPath(final String pluginId) {
    addPluginToPath(pluginId, false);
  }

  public void addPluginToPath(final String pluginId, boolean unpacked) {
    final String pluginDir = getPluginDir(pluginId);
    if (unpacked) {
      File loc = new File(pluginDir);
      if (loc.isDirectory()) {
        boolean workspaceExists = addToPath(pluginDir + "/bin", false); // in
        // workspace
        if (!workspaceExists) {
          boolean jarExists = addToPath(pluginDir + "/" + loc.getName() + ".jar", false); // in
          // Ant
          if (!jarExists) {
            addToPath(pluginDir); // as plugin
          }
        }
      } else {
        addToPath(pluginDir); // as plugin
      }

    } else if (pluginDir.endsWith(".jar")) {
      addToPath(pluginDir); // as plugin
    } else {
      addToPath(pluginDir + "/bin"); // in workspace
    }
  }

  /**
   * @return true if found
   */
  public boolean addPluginJarsToPath(final String pluginId, String... jars) {
    return addPluginJarsToPath(pluginId, false, jars);
  }

  /**
   * @param exclusive
   *          If true, try each of the jars in sequence until one exists
   * @return true if found
   */
  public boolean addPluginJarsToPath(final String pluginId, boolean exclusive, String... jars) {
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
    final String pluginDir = getPluginDir(pluginId);
    findJars(pluginDir + '/' + libPath);
  }

  public boolean addToPath(String name) {
    return addToPath(new File(name), true);
  }

  protected boolean addToPath(String name, boolean required) {
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

  protected void findJars(String folder) {
    findJars(new File(folder));
  }

  protected void findJars(File folder) {
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
    // All unpacked
    addPluginToPath(pluginId, true);
    addAllPluginJarsToPath(pluginId, jarPath);
  }

  public Collection<File> getPath() {
    return path;
  }

  public void clear() {
    path.clear();
  }
}
