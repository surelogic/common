package com.surelogic.common.java;

import java.io.*;
import java.util.*;

import org.apache.commons.collections15.*;
import org.apache.commons.collections15.multimap.*;

import com.surelogic.common.Pair;

public class JavaClassPath<PS extends JavaProjectSet<?>> implements IJavacClassParser {
	private final MultiMap<ISLJavaProject,Config> initialized = new MultiHashMap<ISLJavaProject, Config>();
	
	// Key: project, qualified name
	// TODO not thread-safe?
	private final Map<Pair<String,String>,IJavaFile> classToFile = 
		new HashMap<Pair<String,String>, IJavaFile>();
	
	protected final PS projects;
	/**
	 * Only use binary; ignore sources
	 */
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
	public final void map(String destProj, IJavaFile file) {
	  if (useBinaries && file.isSource()) {
		  return; // ignore
	  }
	  final Pair<String,String> key = Pair.getInstance(destProj, file.getQualifiedName());
		if (!classToFile.containsKey(key)) {
			if (!key.first().startsWith(Config.JRE_NAME) && file.getFile() != null) {
				System.out.println("Mapping "+key.second()+" to "+file.getFile());
			}
			classToFile.put(key, file);		
		}
	}
	
	protected final boolean isMapped(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return classToFile.containsKey(key);
	}
	
	public final IJavaFile getMapping(String destProj, String qname) {
		final Pair<String,String> key = Pair.getInstance(destProj, qname);
		return getMapping(key);
	}
	
	public final IJavaFile getMapping(Pair<String,String> key) {
		return classToFile.get(key);
	}
	
	public final Collection<Pair<String,String>> getMapKeys() {
		return classToFile.keySet();
	}

	public File getRunDir() {
		return projects.getRunDir();
	}
	
	public interface Processor {
		/**
		 * @return The qualified names of classes that it depends on
		 */
		Iterable<String> process(String referencingProject, IJavaFile file);
	}
	
	// Note that this may not be particularly efficient if parallelized
	public void process(final Processor p, final String project, final Iterable<String> classes) {
		final MultiMap<String, IJavaFile> byProject = new MultiHashMap<String, IJavaFile>();
		for(final String qname : classes) {
			final Pair<String,String> key = new Pair<String, String>(project, qname);
			if (!markAsLoaded(key)) {
				final IJavaFile file = classToFile.get(key);
				byProject.put(file.getProject(), file);
			}
		}
		for(final Map.Entry<String, Collection<IJavaFile>> e : byProject.entrySet()) {
			for(final IJavaFile file : e.getValue()) {
				process(p, e.getKey(), p.process(project, file));			
			}
		}
	}

	/**
	 * @return true if previously marked
	 */
	private boolean markAsLoaded(Pair<String, String> key) {
		// TODO what if it's part of the JRE?
		return false;
	}
	
	private final Set<Pair<String, String>> loaded = new HashSet<Pair<String,String>>();
}
