package com.surelogic.common.java;

import java.io.File;
import java.util.*;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.*;

import com.surelogic.common.Pair;

public class JavaClassPath implements IJavacClassParser {
	private final MultiMap<ISLJavaProject,Config> initialized = new MultiHashMap<ISLJavaProject, Config>();
	
	// Key: project
	// Key: qualified name
	// Pair1 = new project
	// Map to String if a jar
	// Map to File   if source
	private final Map<Pair<String,String>,Pair<String,Object>> classToFile = 
		new HashMap<Pair<String,String>, Pair<String,Object>>();
	
	/**
	 * @return true if already initialized, false if not (and set to be true)
	 */
	public final boolean ensureInitialized(ISLJavaProject jp, Config config) {
		if (initialized.containsValue(jp, config)) {
			return true;
		}
		initialized.put(jp, config);
		return false;
	}
	
	/*
	 * Checking for a duplicate effectively simulates 
	 * searching the classpath
	 */
	public void map(String destProj, String qname, String srcProj, String jarName) {
	  final Pair<String,String> key = Pair.getInstance(destProj, qname);
		if (!classToFile.containsKey(key)) {
/*
			if (!jarName.contains("jdk")) {
				System.out.println("Mapping "+name+" to "+jarName);
			}
*/
			classToFile.put(key, new Pair<String,Object>(srcProj, jarName));		
		}
	}
	
	public void mapFile(String destProj, String qname, String srcProj, JavaSourceFile file) {
	  final Pair<String,String> key = Pair.getInstance(destProj, qname);
		if (!classToFile.containsKey(key)) {
/*
			if (!file.toString().contains("jdk")) {
				System.out.println("Mapping "+qname+" to "+file);
			}
*/
			classToFile.put(key, new Pair<String,Object>(srcProj, file));
		}
	}

	public void mapClass(String destProj, String qname, String srcProj, File f) {
	  final Pair<String,String> key = Pair.getInstance(destProj, qname);
		if (!classToFile.containsKey(key)) {
/*
			if (!f.toString().contains("jdk")) {
				System.out.println("Mapping "+qname+" to "+f);
			}
*/
			classToFile.put(key, new Pair<String,Object>(srcProj, f));
		}
	}
	
	protected boolean isMapped(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return classToFile.containsKey(key);
	}
	
	protected Pair<String,Object> getMapping(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return classToFile.get(key);
	}
	
	protected Collection<Pair<String,String>> getMapKeys() {
		return classToFile.keySet();
	}
}
