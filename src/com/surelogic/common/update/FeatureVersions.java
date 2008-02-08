package com.surelogic.common.update;

import java.io.*;
import java.net.*;
import java.util.Properties;

public final class FeatureVersions extends Properties implements
		IFeatureVersionMap {

	private static final long serialVersionUID = -771734749876004796L;

	private static final String LATEST = "http://www.surelogic.com/product-releases.properties";

	private FeatureVersions(String location) throws IOException {
		URL site = new URL(location);
		load(site.openStream());
	}

	public static IFeatureVersionMap getLatestVersions() throws IOException {
		return new FeatureVersions(LATEST);
	}

	public String get(String productName) {
		return (String) get((Object) productName);
	}

	public boolean upgradeAvailable(String productName, String currentVersion) {
		final String latestVersion = get(productName);
		if (latestVersion != null) {

		}
		return false;
	}

	public static void main(String[] args) {
		try {
			IFeatureVersionMap versions = new FeatureVersions(
					"file:///Users/Tim/Documents/Source/SierraEclipseClient/common/latest.properties");
			System.out.println("common = " + versions.get("common"));

			String[] tmp = new String[] { "0.9", "1.0.0", "1.0.0.1", "2.0",
					"2.0_2008" };
			String last = null;
			for (String v : tmp) {
				if (last != null) {
					System.out.println(last + " <= " + v + " : "
							+ (last.compareTo(v) <= 0));
				}
				last = v;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
