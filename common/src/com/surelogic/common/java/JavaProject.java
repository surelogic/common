package com.surelogic.common.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.surelogic.common.jobs.*;

public class JavaProject implements ISLJavaProject {
	private final JavaProjectSet<? extends JavaProject> parent;
	protected final Config config;
	protected final String name;	
	private final Map<File,File> mappedJars = new HashMap<File, File>();
	boolean active = true;
	final boolean containsJavaLangObject;
	
	protected JavaProject(JavaProjectSet<? extends JavaProject> p, Config cfg, String name, SLProgressMonitor monitor) {
		parent = p;
		config = cfg;
		this.name = name;
		containsJavaLangObject = cfg == null ? false : cfg.containsJavaLangObject();
	}
	
	public JavaProjectSet<? extends JavaProject> getParent() {
		return parent;
	}

	public final String getName() {
		return name;
	}
	
	public final Config getConfig() {
		return config;
	}
	
	public void deactivate() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean containsJavaLangObject() {
		return containsJavaLangObject;
	}
	
	public boolean shouldExistAsIProject() {
		return !name.startsWith(Config.JRE_NAME);
	}
	
	public boolean isAsBinary() {
		return !config.getBoolOption(Config.AS_SOURCE);
	}
	
	public void mapJar(File path, File origPath) {
		//System.out.println("Mapping "+path+" to "+orig);
		mappedJars.put(path, origPath);
	}
	
	public void collectMappedJars(Map<File, File> collected) {
		collected.putAll(mappedJars);
		/*
		for(Map.Entry<File, File> e : mappedJars.entrySet()) {
			Object replaced = collected.put(e.getKey(), e.getValue());
			if (replaced != null) {
				System.out.println("Replaced mapping for "+e.getKey());
			}
		}
		*/
	}
	
	public void addPackage(String pkg, Config.Type t) {
		// Nothing to do?
	}

	public void clear() {
		mappedJars.clear();
		config.clear();
	}	
}
