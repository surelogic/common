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

      String[] tmp = new String[] { "0.7", "1.0.0", "1.0.0.1", "2.0", "2.0_2008" };
      String last  = null; 
      for(String v : tmp) {
        if (last != null) {
          System.out.println(last+" <= "+v+" : "+(last.compareTo(v) <= 0));
        }
        last = v;
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
