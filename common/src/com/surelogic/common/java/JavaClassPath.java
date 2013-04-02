package com.surelogic.common.java;

import java.io.*;
import java.util.*;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.*;

import com.surelogic.common.Pair;

public class JavaClassPath<PS extends JavaProjectSet<?>> implements IJavacClassParser {
	private final MultiMap<ISLJavaProject,Config> initialized = new MultiHashMap<ISLJavaProject, Config>();
	
	// Key: project, qualified name
	private final Map<Pair<String,String>,IJavaFile> classToFile = 
		new HashMap<Pair<String,String>, IJavaFile>();
	
	protected final PS projects;
	private final boolean useBinaries; // TODO
	
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
	public void map(String destProj, IJavaFile file) {
	  final Pair<String,String> key = Pair.getInstance(destProj, file.getQualifiedName());
		if (!classToFile.containsKey(key)) {
/*
			if (!jarName.contains("jdk")) {
				System.out.println("Mapping "+name+" to "+jarName);
			}
*/
			classToFile.put(key, file);		
		}
	}
	
	protected boolean isMapped(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return classToFile.containsKey(key);
	}
	
	public IJavaFile getMapping(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return getMapping(key);
	}
	
	public IJavaFile getMapping(Pair<String,String> key) {
		return classToFile.get(key);
	}
	
	public Collection<Pair<String,String>> getMapKeys() {
		return classToFile.keySet();
	}
}
