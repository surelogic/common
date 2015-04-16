package com.surelogic.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public class CommonJVMPrefs {
	public static final String PROP = "com.surelogic.common.jvm.prefs.url";
    public static final String PATH = "/lib/scan_vm.properties";
    public static final String VMARGS = "vmargs";

    public static Properties getJvmPrefs() {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(PATH);
        if (url == null) {
            url = CommonJVMPrefs.class.getResource(PATH);
        }
        if (url == null) {
        	String prop = System.getProperty(PROP);
        	if (prop != null) {
        		try {
					url = new URL(prop);
				} catch (MalformedURLException e) {
					SLLogger.getLogger().log(Level.WARNING, "Problem with jvm prefs: " + prop, e);
					url = null;
				}
        	}
        }
        Properties prefs = new Properties();
        try {
            prefs.load(url.openStream());
        } catch (NullPointerException e) {
            SLLogger.getLogger().log(Level.WARNING, "Problem loading " + PATH,
                    e);
        } catch (IOException e) {
            SLLogger.getLogger().log(Level.WARNING, "Problem loading " + PATH,
                    e);
        }
        return prefs;
    }
}
