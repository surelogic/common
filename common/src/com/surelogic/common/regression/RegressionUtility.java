package com.surelogic.common.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;

public class RegressionUtility {
  public static final String JSURE_LOG_SUFFIX = ".log.xml";
  public static final String JSURE_SNAPSHOT_SUFFIX = ".sea.xml";
  public static final String JSURE_SNAPSHOT_DIFF_SUFFIX = ".sea.diffs.xml";

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
  public static final Filter oracleScanDirFilter = new Filter(ORACLE_SCAN_DIR, "") {
    public boolean accept(File dir, String name) {
      // Also accept a directory starting with the project name
      if (name.startsWith(dir.getName() + ' ')) {
        return true;
      }
      return super.accept(dir, name);
    }
  };

  private static final Filter[] oracleFilters = { oracleScanDirFilter, snapshotOracleFilter, javacOracleFilter, xmlOracleFilter };

  public static File findOracle(final File project) {
    File xmlOracle = null;
    for (Filter f : oracleFilters) {
      File tempOracle = RegressionUtility.getOracleName(project, f);
      System.out.println("Looking for " + tempOracle);

      final boolean noOracleYet = xmlOracle == null || !xmlOracle.exists();
      boolean replace;
      if (noOracleYet) {
        replace = true;
      } else {
        System.out.println("Checking for newer oracle");
        replace = tempOracle.exists() && RegressionUtility.isNewer(project, tempOracle, xmlOracle);
      }
      if (replace) {
        xmlOracle = tempOracle;
      }
      System.out.println("Using " + xmlOracle);
    }
    assert (xmlOracle.exists());
    return xmlOracle;
  }

  public static File getOracleName(File path, Filter filter) {
    File[] files = path.listFiles(filter);
    if (files == null) {
      return path; // No oracle to look at
    }
    File file = null;
    for (File zip : files) {
      if (file == null || isNewer(path, zip, file)) {
        file = zip;
      }
      /*
       * } else if (zip.getName().length() > file.getName().length()) { //
       * Intended for comparing 3.2.4 to 070221 file = zip; } else if
       * (zip.getName().length() == file.getName().length() &&
       * zip.getName().compareTo(file.getName()) > 0) { // Intended for
       * comparing 070107 to 070221 file = zip; }
       */
    }
    return (file != null) ? file : new File(path, filter.getDefault());
  }

  public static boolean isNewer(File project, File oracle1, File oracle2) {
    Date date1 = getDate(project, oracle1);
    Date date2 = getDate(project, oracle2);
    if (date1 == null) {
      return false;
    }
    if (date2 == null) {
      return true;
    }
    boolean rv = date1.compareTo(date2) > 0;
    // if (XUtil.testing) {
    System.out.println(date1 + " ?= " + date2 + " => " + (rv ? "first" : "second"));
    // }
    return rv;
  }

  private static Date getDate(File project, File oracleFile) {
    final String oracle = oracleFile.getName();
    if (oracle.startsWith(ORACLE_SCAN_DIR) || oracle.startsWith(project.getName())) {
      return extractDateFromName(oracle);
    }
    // Start with last segment

    for (int i = oracle.lastIndexOf(File.separatorChar) + 1; i < oracle.length(); i++) {
      if (Character.isDigit(oracle.charAt(i))) {
        // return oracle.substring(i);
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
          return format.parse(oracle.substring(i));
        } catch (ParseException e) {
          System.out.println("Couldn't parse as date: " + oracle);
          return null;
        }
      }
    }
    return null;
  }

  public static String computeOracleName(Date scanDate) {
    return computeScanName(ORACLE_SCAN_DIR, scanDate);
    // DateFormat format = new SimpleDateFormat("yyyyMMdd");
    // return ORACLE_SCAN_DIR + format.format(date) + JSURE_SNAPSHOT_DIR_SUFFIX;
  }

  public static String computeScanName(String label, Date scanDate) {
    final String time = SLUtility.toStringHMS(scanDate);
    final String name = label + ' ' + time.replace(':', '-');
    return name;
  }

  public static Set<String> readLinesAsSet(File lines) throws IOException {
    final Set<String> cus = new HashSet<String>();
    final BufferedReader br = new BufferedReader(new FileReader(lines));
    try {
      String line;
      while ((line = br.readLine()) != null) {
        cus.add(FileUtility.normalizePath(line.trim()));
      }
    } finally {
      br.close();
    }
    return cus;
  }

  public static Date extractDateFromName(String dirName) {
    if (dirName == null)
      return null;

    // There should be at least 3 segments: label date time
    final String[] name = dirName.split(" ");
    final int num = name.length;
    if (num < 3)
      return null;
    try {
      // try to parse the date and time (the last two segments)
      final String time = name[num - 2] + ' ' + name[num - 1].replace('-', ':');
      return SLUtility.fromStringHMS(time);
    } catch (Exception e) {
      return null;
    }
  }
}
