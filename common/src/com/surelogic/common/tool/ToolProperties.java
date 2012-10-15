package com.surelogic.common.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * Properties shared among the various SureLogic tools.
 */
public class ToolProperties extends Properties {
  private static final long serialVersionUID = 1L;

  public static final String PROPS_FILE = "surelogic-tools.properties";

  public static final String EXCLUDE_PATH = "scan.exclude.source.folder";
  public static final String EXCLUDED_PKGS = "scan.exclude.source.package";

  public static ToolProperties readFromProject(File projectDir) {
    final File properties = new File(projectDir, PROPS_FILE);
    return read(properties);
  }

  public static ToolProperties read(File properties) {
    if (properties.exists() && properties.isFile()) {
      final ToolProperties props = new ToolProperties();
      try {
        InputStream is = new FileInputStream(properties);
        props.load(is);
        is.close();
      } catch (IOException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Problem while loading " + PROPS_FILE + ": " + e.getMessage(), e);
      }
      return props;
    }
    return null;
  }

  private String[] getListProperty(String key) {
    final String l = getProperty(key);
    if (l == null) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    return l.split("[ ,]+");
  }

  public String[] getExcludedSourcePaths() {
    return getListProperty(EXCLUDE_PATH);
  }

  /**
   * 
   * @return
   */
  public String[] getExcludedPackages() {
    return getListProperty(EXCLUDED_PKGS);
  }

  public static Pattern[] makePackageMatchers(String[] patterns) {
    final Pattern[] excludePatterns = new Pattern[patterns.length];
    int i = 0;
    for (String pattern : patterns) {
      final String pattern2 = pattern.replaceAll("\\.", "\\.").replaceAll("\\*", ".*");
      excludePatterns[i] = Pattern.compile(pattern2);
      i++;
    }
    return excludePatterns;
  }

  public static String[] convertPkgsToRelativePaths(String[] pkgs) {
    if (pkgs == null || pkgs.length == 0) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    final String[] paths = new String[pkgs.length];
    int i = 0;
    for (String p : pkgs) {
      paths[i] = p.replace('.', '/').replaceAll("\\*", "**"); // +"/*.java";
      i++;
    }
    return paths;
  }
}
