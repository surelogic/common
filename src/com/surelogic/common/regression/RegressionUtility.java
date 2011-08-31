package com.surelogic.common.regression;

import java.io.*;
import java.util.*;

import com.surelogic.common.FileUtility;

public class RegressionUtility {
	public static final String JSURE_SNAPSHOT_SUFFIX = ".sea.xml";
	public static final String JSURE_SNAPSHOT_DIFF_SUFFIX = ".sea.diffs.xml";
	
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
		return (file != null) ? file.getAbsolutePath() : projectPath
				+ File.separator + defaultName;
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
