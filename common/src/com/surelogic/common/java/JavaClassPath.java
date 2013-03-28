package com.surelogic.common.java;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.*;

import com.surelogic.common.Pair;

public class JavaClassPath<PS extends JavaProjectSet<?>> implements IJavacClassParser {
	private final MultiMap<ISLJavaProject,Config> initialized = new MultiHashMap<ISLJavaProject, Config>();
	
	// Key: project
	// Key: qualified name
	// Pair1 = new project
	// Map to String if a jar
	// Map to File   if source
	private final Map<Pair<String,String>,Pair<String,Object>> classToFile = 
		new HashMap<Pair<String,String>, Pair<String,Object>>();
	
	protected final PS projects;
	private final boolean useBinaries;
	
	public JavaClassPath(PS set, boolean useBin) throws IOException {
		projects = set;
		useBinaries = useBin;

		for(ISLJavaProject jp : set) {
        	jp.getConfig().init(jp, this);
		}
	}
	
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
	
	public void mapBinary(String destProj, String qname, String srcProj, String jarName) {
      if (useBinaries) {
    	  map(destProj, qname, srcProj, jarName);
      }
	}
	
	public void mapFile(String destProj, String qname, String srcProj, JavaSourceFile file) {
	  if (useBinaries) {
		  return;
	  }
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
	
	public Pair<String,Object> getMapping(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return getMapping(key);
	}
	
	public Pair<String,Object> getMapping(Pair<String,String> key) {
		return classToFile.get(key);
	}
	
	public Collection<Pair<String,String>> getMapKeys() {
		return classToFile.keySet();
	}
}
