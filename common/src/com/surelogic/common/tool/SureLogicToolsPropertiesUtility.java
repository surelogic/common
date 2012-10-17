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
import com.surelogic.Utility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * Properties shared among the various SureLogic tools.
 */
@Utility
public class SureLogicToolsPropertiesUtility {
  private static final long serialVersionUID = 1L;

  public static final String PROPS_FILE = "surelogic-tools.properties";

  public static final String SCAN_EXCLUDE_SOURCE_FOLDER = "scan.exclude.source.folder";
  public static final String SCAN_EXCLUDE_SOURCE_PACKAGE = "scan.exclude.source.package";

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
  public static Properties readFileOrNull(File properties) {
    if (properties.exists() && properties.isFile()) {
      System.out.println("FOUND " + properties.getAbsolutePath());
      final Properties props = new Properties();
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

  private static String[] getListProperty(@NonNull Properties props, String key) {
    if (props == null)
      return SLUtility.EMPTY_STRING_ARRAY;
    final String l = props.getProperty(key);
    if (l == null) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    return l.split("[ ,]+");
  }

  /**
   * Assuming the passed properties file is from a
   * <tt>surelogic-tools.properties</tt> file, this method returns a list of the
   * excluded source folders.
   * 
   * @param props
   *          properties from a <tt>surelogic-tools.properties</tt> file or
   *          {@code null}.
   * @return a possibly empty array listing exclusions.
   */
  @NonNull
  public static String[] getExcludedSourceFolders(@Nullable Properties props) {
    return getListProperty(props, SCAN_EXCLUDE_SOURCE_FOLDER);
  }

  /**
   * Assuming the passed properties file is from a
   * <tt>surelogic-tools.properties</tt> file, this method returns a list of the
   * excluded packages.
   * <p>
   * These could include wildcards such as <tt>*test*</tt>.
   * 
   * @param props
   *          properties from a <tt>surelogic-tools.properties</tt> file or
   *          {@code null}.
   * @return a possibly empty array listing exclusions.
   */
  @NonNull
  public static String[] getExcludedPackagePatterns(@Nullable Properties props) {
    return getListProperty(props, SCAN_EXCLUDE_SOURCE_PACKAGE);
  }

  private static final SureLogicToolsFilter f_null = new SureLogicToolsFilter() {
    public boolean matches(@NonNull String absoluteOrRelativePath, @NonNull String packageName) {
      return false;
    }
  };

  public static SureLogicToolsFilter getFilterFor(String[] excludedSourceFolders, String[] excludedPackagePatterns) {
    if (excludedSourceFolders == null)
      excludedSourceFolders = SLUtility.EMPTY_STRING_ARRAY;
    if (excludedPackagePatterns == null)
      excludedPackagePatterns = SLUtility.EMPTY_STRING_ARRAY;
    if (excludedSourceFolders.length == 0 && excludedPackagePatterns.length == 0)
      return f_null;

    final String[] exSourceFolders = excludedSourceFolders;
    final Pattern[] packagePatterns = makePackageMatchers(excludedPackagePatterns);
    return new SureLogicToolsFilter() {

      @Override
      public boolean matches(@NonNull String absoluteOrRelativePath, @NonNull String packageName) {
        if (absoluteOrRelativePath == null)
          absoluteOrRelativePath = "";
        if (packageName == null)
          packageName = "";

        System.out.println("matches(" + absoluteOrRelativePath + " | " + packageName);
        return false;
      }
    };
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
