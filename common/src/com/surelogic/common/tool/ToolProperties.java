package com.surelogic.common.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
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

  /**
   * Reads in the passed file, it is suggested that the {@link #PROPS_FILE} file
   * name be used.
   * 
   * @param properties
   *          a <tt>surelogic-tools.properties</tt> file.
   * @return a properties object, or {@code null} if the file doesn't exist or
   *         can't be loaded.
   */
  @Nullable
  public static ToolProperties readFileOrNull(File properties) {
    if (properties.exists() && properties.isFile()) {
      System.out.println("FOUND " + properties.getAbsolutePath());
      final ToolProperties props = new ToolProperties();
      try {
        InputStream is = new FileInputStream(properties);
        props.load(is);
        is.close();
      } catch (IOException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Problem while loading " + PROPS_FILE + ": " + e.getMessage(), e);
      }
      return props;
    } else {
      System.out.println("NOT FOUND " + properties.getAbsolutePath());
      return null;
    }
  }

  private String[] getListProperty(String key) {
    final String l = getProperty(key);
    if (l == null) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    return l.split("[ ,]+");
  }

  @NonNull
  public String[] getExcludedSourcePaths() {
    return getListProperty(EXCLUDE_PATH);
  }

  @NonNull
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
}
