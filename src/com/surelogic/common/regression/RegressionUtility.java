package com.surelogic.common.regression;

import java.io.*;

public class RegressionUtility {
	public static final String JSURE_SNAPSHOT_SUFFIX = ".sea.xml";
	
	public static FilenameFilter oracleFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.startsWith("oracle") && name.endsWith(".zip");
		}
	};

	public static FilenameFilter logOracleFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.startsWith("oracle") && name.endsWith(".log.xml");
		}
	};

	public static FilenameFilter xmlOracleFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return !name.startsWith("oracleJavac") && 
			name.startsWith("oracle") && name.endsWith(JSURE_SNAPSHOT_SUFFIX);
		}
	};

	public static FilenameFilter javacOracleFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.startsWith("oracleJavac") && name.endsWith(JSURE_SNAPSHOT_SUFFIX);
		}
	};

	public static String getOracleName(String projectPath, FilenameFilter filter,
			String defaultName) {
		File path = new File(projectPath);
		File[] files = path.listFiles(filter);
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
		return (file != null) ? file.getAbsolutePath() : projectPath
				+ File.separator + defaultName;
	}
}
