package com.surelogic.common.regression;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.surelogic.common.FileUtility;

public class RegressionUtility {
	public static final String JSURE_LOG_SUFFIX = ".log.xml";
	public static final String JSURE_SNAPSHOT_SUFFIX = ".sea.xml";
	public static final String JSURE_SNAPSHOT_DIFF_SUFFIX = ".sea.diffs.xml";
	public static final String JSURE_SNAPSHOT_DIR_SUFFIX = ".sea";

	public static final String ORACLE = "oracle";
	public static final String ORACLE_JAVAC = "oracleJavac";
	public static final String ORACLE_SNAPSHOT = "snapshotOracle";
	public static final String ORACLE_SCAN_DIR = "oracleDir";
	
	public static class Filter implements FilenameFilter {
		private final String prefix, suffix;
		
		Filter(String pre, String post) {
			prefix = pre;
			suffix = post;
		}
		
		public boolean accept(File dir, String name) {
			return name.startsWith(prefix) && name.endsWith(suffix);
		}
		
		public String getDefault() {
			return prefix + suffix;
		}
	}
	
	public static final Filter logOracleFilter = new Filter(ORACLE, JSURE_LOG_SUFFIX);
	private static final Filter xmlOracleFilter = new Filter(ORACLE, JSURE_SNAPSHOT_SUFFIX) {
		@Override
		public boolean accept(File dir, String name) {
			return !name.startsWith(ORACLE_JAVAC) && super.accept(dir, name);
		}
	};
	private static final Filter javacOracleFilter = new Filter(ORACLE_JAVAC, JSURE_SNAPSHOT_SUFFIX);
	public static final Filter snapshotOracleFilter = new Filter(ORACLE_SNAPSHOT, JSURE_SNAPSHOT_SUFFIX);
	public static final Filter oracleScanDirFilter = new Filter(ORACLE_SCAN_DIR, JSURE_SNAPSHOT_DIR_SUFFIX);
	private static final Filter[] oracleFilters = {
		oracleScanDirFilter, snapshotOracleFilter, javacOracleFilter, xmlOracleFilter
	};
	
	public static File findOracle(String projectPath) {
		String xmlOracle = null;
		File xmlLocation = null;
		for(Filter f : oracleFilters) {
			String tempOracle = RegressionUtility.getOracleName(projectPath, f);
			File tempLocation = new File(tempOracle);
			System.out.println("Looking for " + tempOracle);

			final boolean noOracleYet = xmlLocation == null || !xmlLocation.exists();
			boolean replace;
			if (noOracleYet) {
				replace = true;
			} else {
				System.out.println("Checking for newer oracle");
				replace = tempLocation.exists() && RegressionUtility.isNewer(tempOracle, xmlOracle);
			}
			if (replace) {
				xmlOracle = tempOracle;
				xmlLocation = tempLocation;
			}
			System.out.println("Using " + xmlOracle);
		}
		assert (xmlLocation.exists());
		return xmlLocation;
	}
	
	public static String getOracleName(String projectPath, Filter filter) {
		File path = new File(projectPath);
		File[] files = path.listFiles(filter);
		if (files == null) {
			return projectPath; // No oracle to look at
		}
		File file = null;
		for (File zip : files) {
			if (file == null) {
				file = zip;
			} else if (zip.getName().length() > file.getName().length()) {
				// Intended for comparing 3.2.4 to 070221
				file = zip;
			} else if (zip.getName().length() == file.getName().length()
					&& zip.getName().compareTo(file.getName()) > 0) {
				// Intended for comparing 070107 to 070221
				file = zip;
			}
		}
		return (file != null) ? file.getAbsolutePath() : 
			projectPath + File.separator + filter.getDefault();
	}
	
	public static boolean isNewer(String oracle1, String oracle2) {
		String date1 = getDate(oracle1);
		String date2 = getDate(oracle2);
		boolean rv = date1.compareTo(date2) > 0;
		// if (XUtil.testing) {
		System.out.println(date1 + " ?= " + date2 + " => " + (rv ? "first" : "second"));
		// }
		return rv;
	}

	private static String getDate(String oracle) {
		// Start with last segment
		for (int i = oracle.lastIndexOf(File.separatorChar) + 1; i < oracle.length(); i++) {
			if (Character.isDigit(oracle.charAt(i))) {
				return oracle.substring(i);
			}
		}
		return oracle;
	}
	
	public static String computeOracleName() {
	    Date date = new Date();
	    DateFormat format = new SimpleDateFormat("yyyyMMdd");
	    //oracleName = "oracle"+format.format(date)+SeaSnapshot.SUFFIX;
	    return ORACLE_SNAPSHOT + format.format(date) + JSURE_SNAPSHOT_SUFFIX;	    
	    //return ORACLE_JAVAC + format.format(date) + JSURE_SNAPSHOT_SUFFIX;	
	}
	
	public static Set<String> readLinesAsSet(File lines) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(lines));
		final Set<String> cus   = new HashSet<String>();
		String line;
		while ((line = br.readLine()) != null) {
			cus.add(FileUtility.normalizePath(line.trim()));
		}
		return cus;
	}
}
