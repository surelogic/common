package com.surelogic.common.update;

import java.io.*;
import java.net.*;
import java.util.Properties;

public class FeatureVersions extends Properties implements IFeatureVersionMap {
  private static final long serialVersionUID = 1L;
  
  private static String LATEST = "http://www.surelogic.com/latest.properties";
  
  private FeatureVersions(String location) throws IOException {
    URL site      = new URL(location);
    Reader reader = new InputStreamReader(site.openStream());
    load(reader);
  }
  
  public static IFeatureVersionMap getLatestVersions() throws IOException {
    return new FeatureVersions(LATEST);
  }

  public String get(String id) {
    return (String) get((Object) id);
  }
  
  public static void main(String[] args) {
    try {
      IFeatureVersionMap versions = new FeatureVersions("file:///work/workspace/common/latest.properties");
      System.out.println("common = "+versions.get("common"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
