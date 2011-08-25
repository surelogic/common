package com.surelogic.common.tool;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.surelogic.common.logging.SLLogger;

/**
 * Properties shared among the various SureLogic tools
 * 
 * @author edwin
 */
public class ToolProperties extends Properties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String[] noStrings = new String[0];
	
	public static final String PROPS_FILE = "surelogic-tools.properties";
	
	public static final String EXCLUDE_PATH = "scan.exclude.source.folder";
	public static final String EXCLUDED_PKGS = "scan.exclude.package";

	public static ToolProperties readFromProject(File projectDir) {
        final File properties = new File(projectDir, PROPS_FILE);
        return read(properties);
	}
	
	public static ToolProperties read(File properties) {
        if (properties.exists() && properties.isFile()) {
            final ToolProperties props = new ToolProperties();
            // props.put(PROJECT_KEY, p);
            try {
                InputStream is = new FileInputStream(properties);
                props.load(is);
                is.close();
            } catch (IOException e) {
                String msg = "Problem while loading "+PROPS_FILE + ": "+ e.getMessage();
                // reportProblem(msg, null);
                SLLogger.getLogger().log(Level.SEVERE, msg, e);
            } finally {
                // Nothing to do
            }
            return props;
        }
        return null;
	}
	
	private String[] getListProperty(String key) {
		final String l = getProperty(key);
		if (l == null) {
			return noStrings;
		}
		return l.split("[ ,]*");
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
			final String pattern2 = pattern.replaceAll("\\.", "\\.")
					.replaceAll("\\*", ".*");
			excludePatterns[i] = Pattern.compile(pattern2);
			i++;
		}
		return excludePatterns;
	}
}
