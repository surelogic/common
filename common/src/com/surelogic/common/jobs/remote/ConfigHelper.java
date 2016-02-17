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
  /**
   * A set is used to avoid duplicate entries
   */
  private final Set<File> path = new HashSet<>();

  public ConfigHelper(boolean debug, ILocalConfig config) {
    this.debug = debug;
    this.config = config;
  }

  /**
   * Adds a plugin directory or (packed) Jar to the classpath. This needs to
   * handle meta-workspace, release, Ant, and Maven.
   * <p>
   * If it finds a <tt>bin</tt> directory it assumes it is in a meta-workspace.
   * In the packaged release the classpath starts at the root of the plugin. We
   * also look for a <tt>bin.jar</tt> file which may be used in Ant. Even if we
   * add the <tt>bin</tt> or <tt>bin.jar</tt> file to the classpath the plugin
   * directory is always added for resource loading outside the code hierarchy.
   * <p>
   * If the plugin is not located in a directory it is in a Jar (a packaged
   * Eclipse release). In this case just the Jar is added to the classpath.
   * 
   * @param pluginId
   *          the plugin id.
   */
  public void addPluginToPath(final String pluginId) {
    final File pluginDir = config.getPluginDirectory(pluginId);
    if (pluginDir.isDirectory()) {
      final File pluginDirBin = new File(pluginDir, "bin");
      boolean inMetaWorkspace = pluginDirBin.isDirectory();
      if (inMetaWorkspace)
        addToPath(pluginDirBin);
      final File pluginJar = new File(pluginDir, "bin.jar");
      boolean inAntOrSimilar = pluginJar.isFile();
      if (inAntOrSimilar) {
        addToPath(pluginJar);
      }
    }
    addToPath(pluginDir);
  }

  /**
   * Adds a series of required Jars to the classpath, the plugin must be
   * unpacked.
   * 
   * @param pluginId
   *          the plugin id.
   * @param jars
   *          a list of paths relative to the root directory of the plugin to
   *          add.
   * 
   * @return {@code true} if all passed Jars exist and are added, {@code false}
   *         otherwise.
   */
  public boolean addPluginJarsToPath(final String pluginId, String... jars) {
    final File pluginDir = config.getPluginDirectory(pluginId);
    boolean result = true;
    if (pluginDir.isDirectory()) {
      for (String jar : jars) {
        result = result && addToPath(new File(pluginDir, jar).getAbsolutePath(), true);
      }
      return result;
    } else {
      SLLogger.getLogger().warning("Attempted to add Jars to a plugin " + pluginDir.getAbsolutePath() + " that is not unpacked:");
      return false;
    }
  }

  public void addAllPluginJarsToPath(final String pluginId, String libPath, String... excludes) {
    findJarsAndAddToPathIn(new File(config.getPluginDirectory(pluginId), libPath), excludes);
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

  void findJarsAndAddToPathIn(File folder, String... excludes) {
    if (!folder.exists()) {
      SLLogger.getLogger().warning("Unable to find jars in non-existent folder: " + folder);
    }
    outer:
    for (File f : folder.listFiles()) {
      String name = f.getName();
      if (name.endsWith(".jar")) {
    	for(String exclude : excludes) {
    	  if (name.equals(exclude)) {
    		continue outer;
    	  }
    	}
        path.add(f);
      }
    }
  }

  /**
   * Add the plugin and all the jars on the associated path
   */
  public void addPluginAndJarsToPath(String pluginId, String jarPath, String... excludes) {
    addPluginToPath(pluginId);
    addAllPluginJarsToPath(pluginId, jarPath, excludes);
  }

  public Collection<File> getPath() {
    return path;
  }

  public void clear() {
    path.clear();
  }
}
