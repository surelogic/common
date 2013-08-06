package com.surelogic.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.SortedProperties;
import com.surelogic.common.logging.SLLogger;

public class PerformanceProperties {
	private static final long UNINIT = -1;
	
	private final String propPrefix;
	private final String label;
	private final File scanDir;
	private final String propsFileName;
	private final Properties props = new SortedProperties();
	private long time = UNINIT;
	private long start = UNINIT;
	
	public PerformanceProperties(String prefix, String l, File dir, String name) {
		propPrefix = prefix;
		label = l;
		scanDir = dir;
		propsFileName = name;
	}

	public final void setIntProperty(String key, int i) {	
		setProperty(key, Integer.toString(i));
	}
	
	public final void setLongProperty(String key, long i) {	
		setProperty(key, Long.toString(i));
	}
	
	public final void setProperty(String key, String value) {	
		if (key == null || value == null) {
			throw new IllegalArgumentException("Null key or value");
		}
		props.setProperty(propPrefix+key, value);
	}
	
	public final long startTiming() {
		if (start != UNINIT) {
			throw new IllegalStateException("Already started timer");
		}
		return start = time = System.currentTimeMillis();
	}
	
	/**
	 * Store the time difference under the given key, and keep the timer going
	 */
	public final long markTimeFor(final String key) {		
		if (time == UNINIT) {
			throw new IllegalStateException("Haven't started timer"); 
		}
		final long now = System.currentTimeMillis();
		final long diff = now - time;
		setLongProperty(key, diff);
		time = now;
		return diff;
	}
	
	public final long stopTiming(final String key) {
		if (start == UNINIT) {
			throw new IllegalStateException("Haven't started timer"); 
		}
		final long now = System.currentTimeMillis();
		final long diff = now - start;
		setLongProperty(key, diff);
		start = time = UNINIT;
		return diff;
	}

	public final void store() {
		File target = new File(scanDir, propsFileName);
		try {
			props.store(new FileWriter(target), 
					    "Performance data for "+label);
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.WARNING, "Unable to save performance data for "+scanDir.getName(), e);
		}
	}

	public final void print(PrintStream out) {
		try {
			props.store(out, "Performance data for "+label);
		} catch (IOException e) {
			// Ignore
		}
	}
}
