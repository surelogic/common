package com.surelogic.common;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public class CommonJVMPrefs {
	public static final String PATH = "/lib/scan_vm.properties";
	public static final String VMARGS = "vmargs";
		
	public static Properties getJvmPrefs() {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource(PATH);
	    Properties prefs = new Properties();
	    try {
			prefs.load(url.openStream());
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.WARNING, "Problem loading "+PATH, e);
		}
		System.out.println("Loading JVM prefs from "+PATH);
	    return prefs;
	}
}
