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
    private SureLogicToolsPropertiesUtility() {
        // To prevent instantiation
    }

    public static final String PROPS_FILE = "surelogic-tools.properties";

    public static final String SCAN_EXCLUDE_SOURCE_FOLDER = "scan.exclude.source.folder";
    public static final String SCAN_EXCLUDE_SOURCE_PACKAGE = "scan.exclude.source.package";
    public static final String SCAN_EXCLUDE_SOURCE_CLASSES = "scan.exclude.source.classes";
    public static final String SCAN_SOURCE_FOLDER_AS_BYTECODE = "scan.source.folder.as.bytecode";
    public static final String SCAN_SOURCE_PACKAGE_AS_BYTECODE = "scan.source.package.as.bytecode";

    /**
     * Reads in the passed file, it is suggested that the {@link #PROPS_FILE}
     * file name be used.
     * 
     * @param properties
     *            a <tt>surelogic-tools.properties</tt> file.
     * @return a properties object, or {@code null} if the file doesn't exist or
     *         can't be loaded.
     */
    @Nullable
    public static Properties readFileOrNull(File properties) {
        if (properties.exists() && properties.isFile()) {
            final Properties props = new Properties();
            try {
                InputStream is = new FileInputStream(properties);
                props.load(is);
                is.close();
            } catch (IOException e) {
                SLLogger.getLogger().log(
                        Level.SEVERE,
                        "Problem while loading " + PROPS_FILE + ": "
                                + e.getMessage(), e);
            }
            return props;
        } else {
            return null;
        }
    }

    private static String[] getListProperty(@NonNull Properties props,
            String key) {
        if (props == null) {
            return SLUtility.EMPTY_STRING_ARRAY;
        }
        final String l = props.getProperty(key);
        if (l == null) {
            return SLUtility.EMPTY_STRING_ARRAY;
        }
        return l.split("[ ,]+");
    }

    public static String combineListProperties(String p, String p2) {
        if (p == null) {
            return p2;
        }
        if (p2 == null) {
            return p;
        }
        return p + ", " + p2;
    }

    public static String[] combineLists(String[] s1, String[] s2) {
        if (s1 == null || s1.length == 0) {
            return s2;
        }
        if (s2 == null || s2.length == 0) {
            return s1;
        }
        String[] rv = new String[s1.length + s2.length];
        System.arraycopy(s1, 0, rv, 0, s1.length);
        System.arraycopy(s2, 0, rv, s1.length, s2.length);
        return rv;
    }

    /**
     * Assuming the passed properties file is from a
     * <tt>surelogic-tools.properties</tt> file, this method returns a list of
     * the excluded source folders.
     * 
     * @param props
     *            properties from a <tt>surelogic-tools.properties</tt> file or
     *            {@code null}.
     * @return a possibly empty array listing exclusions.
     */
    @NonNull
    public static String[] getExcludedSourceFolders(@Nullable Properties props) {
        return getListProperty(props, SCAN_EXCLUDE_SOURCE_FOLDER);
    }

    /**
     * Assuming the passed properties file is from a
     * <tt>surelogic-tools.properties</tt> file, this method returns a list of
     * the excluded packages.
     * <p>
     * These could include wildcards such as <tt>*test*</tt>.
     * 
     * @param props
     *            properties from a <tt>surelogic-tools.properties</tt> file or
     *            {@code null}.
     * @return a possibly empty array listing exclusions.
     */
    @NonNull
    public static String[] getExcludedPackagePatterns(@Nullable Properties props) {
        return getListProperty(props, SCAN_EXCLUDE_SOURCE_PACKAGE);
    }

    /**
     * Assuming the passed properties file is from a
     * <tt>surelogic-tools.properties</tt> file, this method returns a list of
     * the source folders to be loaded as bytecode.
     * 
     * @param props
     *            properties from a <tt>surelogic-tools.properties</tt> file or
     *            {@code null}.
     * @return a possibly empty array listing exclusions.
     */
    @NonNull
    public static String[] getBytecodeSourceFolders(@Nullable Properties props) {
        return getListProperty(props, SCAN_SOURCE_FOLDER_AS_BYTECODE);
    }

    /**
     * Assuming the passed properties file is from a
     * <tt>surelogic-tools.properties</tt> file, this method returns a list of
     * the packages to be loaded as bytecode.
     * <p>
     * These could include wildcards such as <tt>*test*</tt>.
     * 
     * @param props
     *            properties from a <tt>surelogic-tools.properties</tt> file or
     *            {@code null}.
     * @return a possibly empty array listing exclusions.
     */
    @NonNull
    public static String[] getBytecodePackagePatterns(@Nullable Properties props) {
        return getListProperty(props, SCAN_SOURCE_PACKAGE_AS_BYTECODE);
    }

    private static final SureLogicToolsFilter f_null = new SureLogicToolsFilter() {
        @Override
        public boolean matches(@NonNull String absoluteOrRelativePath,
                @NonNull String packageName) {
            return false;
        }
    };

    /**
     * Creates a string that shows the excluded folders and packages in a manner
     * suitable for the user interface.
     * 
     * @param excludedSourceFolders
     *            the excluded source folders.
     * @param excludedPackagePatterns
     *            the excluded package patterns.
     * @return a string that shows the excluded folders and packages in a manner
     *         suitable for the user interface.
     */
    public static String toStringConciseExcludedFoldersAndPackages(
            String[] excludedSourceFolders, String[] excludedPackagePatterns) {
        final StringBuilder b = new StringBuilder();

        if (excludedSourceFolders != null && excludedSourceFolders.length > 0) {
            b.append("Folders: ");
            boolean first = true;
            for (String s : excludedSourceFolders) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                b.append(s);
            }
        }

        if (excludedPackagePatterns != null
                && excludedPackagePatterns.length > 0) {
            if (excludedSourceFolders.length > 0) {
                b.append("; ");
            }
            b.append("Packages: ");
            boolean first = true;
            for (String s : excludedPackagePatterns) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                b.append(s);
            }
        }
        return b.toString();
    }

    /**
     * Creates a tools filter using the passed exclusions.
     * 
     * @param excludedSourceFolders
     *            the excluded source folders.
     * @param excludedPackagePatterns
     *            the excluded package patterns.
     * @return a tools filter using the passed exclusions.
     */
    public static SureLogicToolsFilter getFilterFor(
            String[] excludedSourceFolders, String[] excludedPackagePatterns) {
        if (excludedSourceFolders == null) {
            excludedSourceFolders = SLUtility.EMPTY_STRING_ARRAY;
        }
        if (excludedPackagePatterns == null) {
            excludedPackagePatterns = SLUtility.EMPTY_STRING_ARRAY;
        }
        if (excludedSourceFolders.length == 0
                && excludedPackagePatterns.length == 0) {
            return f_null;
        }

        final String[] exSourceFolders = excludedSourceFolders;
        final Pattern[] packagePatterns = makePackageMatchers(excludedPackagePatterns);
        return new SureLogicToolsFilter() {

            @Override
            public boolean matches(@NonNull String absoluteOrRelativePath,
                    @NonNull String packageName) {
                if (absoluteOrRelativePath == null) {
                    absoluteOrRelativePath = "";
                }
                if (packageName == null) {
                    packageName = "";
                }

                /*
                 * Are they both null/blank? no match
                 */
                if ("".equals(absoluteOrRelativePath) && "".equals(packageName)) {
                    return false;
                }

                /*
                 * Filter by package name pattern
                 */
                for (Pattern p : packagePatterns) {
                    if (p.matcher(packageName).matches()) {
                        return true;
                    }
                }

                /*
                 * Filter by source folder.
                 * 
                 * We do this heuristically by generating a sub path that we
                 * match in the full path using the source folder and the
                 * package name.
                 * 
                 * We fix up the strings if they are using DOS/Windows
                 * pathnames.
                 */
                final String pathToCheck = absoluteOrRelativePath.replace('\\',
                        '/');
                final String pkgPath = packageName
                        .equals(SLUtility.JAVA_DEFAULT_PACKAGE) ? ""
                        : packageName.replace('.', '/');
                for (String srcFolderFrag : exSourceFolders) {
                    String frag = srcFolderFrag.replace('\\', '/');
                    frag = frag.endsWith("/") ? frag + pkgPath : frag + "/"
                            + pkgPath;
                    if (pathToCheck.contains(frag)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static SureLogicToolsFilter combine(final SureLogicToolsFilter f,
            final SureLogicToolsFilter f2) {
        if (f == null || f == f_null) {
            return f2;
        }
        if (f2 == null || f2 == f_null) {
            return f;
        }
        if (f == f2) {
            return f;
        }
        return new SureLogicToolsFilter() {
            @Override
            public boolean matches(@NonNull String absoluteOrRelativePath,
                    @NonNull String packageName) {
                return f.matches(absoluteOrRelativePath, packageName)
                        || f2.matches(absoluteOrRelativePath, packageName);
            }
        };
    }

    public static Pattern[] makePackageMatchers(String[] patterns) {
        final Pattern[] excludePatterns = new Pattern[patterns.length];
        int i = 0;
        for (String pattern : patterns) {
            final String pattern2 = pattern.replaceAll("\\.", "\\.")
                    .replaceAll("\\*", ".*");
            excludePatterns[i] = Pattern.compile(pattern2);
            i++;
        }
        return excludePatterns;
    }
}
