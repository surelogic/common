package com.surelogic.common.java;

import java.io.*;
import java.util.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.surelogic.Nullable;
import com.surelogic.common.Pair;
import com.surelogic.common.logging.SLLogger;

public class JavaClassPath<PS extends JavaProjectSet<?>> implements IJavacClassParser {
  private final Multimap<ISLJavaProject, Config> initialized = ArrayListMultimap.create();

  // Key: project, qualified name
  // TODO not thread-safe?
  private final Map<Pair<String, String>, IJavaFile> classToFile = new HashMap<>();
  private final Set<String> warningsForPackages = new HashSet<>();
  
  private final Map<String, IJavaFile> globalClassToFile = new HashMap<>();
  private final Set<String> potentiallyDuplicatedClassesAcrossProjects = new HashSet<>();
  
  protected final PS projects;
  /**
   * Only use binary; ignore sources
   */
  private final boolean useBinaries;

  public JavaClassPath(PS set, boolean useBin) throws IOException {
    projects = set;
    useBinaries = useBin;

    for (ISLJavaProject jp : set) {
      jp.getConfig().init(jp, this);
    }
  }

  /**
   * @return true if already initialized, false if not (and set to be true)
   */
  public final boolean ensureInitialized(ISLJavaProject jp, Config config) {
    if (initialized.containsEntry(jp, config)) {
      return true;
    }
    initialized.put(jp, config);
    return false;
  }

  /*
   * Checking for a duplicate effectively simulates searching the classpath
   */
  public final void map(final String destProj, final IJavaFile file) {
    if (useBinaries && file.isSource()) {
      // System.out.println("Ignoring "+destProj+" :
      // "+file.getQualifiedName());
      return; // ignore
    }
    final Pair<String, String> key = Pair.getInstance(destProj, file.getQualifiedName());
    if (!classToFile.containsKey(key)) {
      /*
       * if (!key.first().startsWith(Config.JRE_NAME) && file.getFile() != null)
       * { System.out.println("Mapping "+key.second()+" to " +file.getFile()); }
       */
      /*
       * System.out.println("Mapping "+destProj+" : " +file.getQualifiedName());
       * if ("com.surelogic.common.ref.Decl".equals(file.getQualifiedName())) {
       * System.out.println(); }
       */
      classToFile.put(key, file);
    } else {
      final IJavaFile old = classToFile.get(key);
      if (!old.equals(file) && canGenerateWarning(file)) {    	
        SLLogger.getLogger().info(file+" overrides "+old+" for type '"+file.getQualifiedName()+"' in "+destProj);
      }
    }
    // TODO use packages instead to reduce table size?
    final IJavaFile old = globalClassToFile.put(file.getQualifiedName(), file);
    if (old != null && !old.equals(file)) {
    	potentiallyDuplicatedClassesAcrossProjects.add(file.getQualifiedName());
    }
  }
  
  protected final boolean generateMD5Hash(String qname) {
	  return potentiallyDuplicatedClassesAcrossProjects.contains(qname);
  }

  /**
   * Marks true if not already generated
   */
  private boolean canGenerateWarning(IJavaFile file) {
	String key = file.toString();
	int lastSlash = key.lastIndexOf(File.separatorChar);
	if (lastSlash > 0) {
		key = key.substring(0, lastSlash);
	}
	// Returning true if added
	return warningsForPackages.add(key);
  }
  
  protected final boolean isMapped(String destProj, String qname) {
    /*
     * if ("com.surelogic.common.ref.Decl".equals(qname)) { System.out.println(
     * "Checking if mapped for "+destProj+": "+qname); }
     */
    final Pair<String, String> key = Pair.getInstance(destProj, qname);
    return classToFile.containsKey(key);
  }

  public final IJavaFile getMapping(String destProj, String qname) {
    final Pair<String, String> key = Pair.getInstance(destProj, qname);
    return getMapping(key);
  }

  public final IJavaFile getMapping(Pair<String, String> key) {
    return classToFile.get(key);
  }

  public final Collection<Pair<String, String>> getMapKeys() {
    return classToFile.keySet();
  }

  public File getRunDir() {
    return projects.getRunDir();
  }

  public interface Processor {
    /**
     * @return The qualified names of classes that it depends on
     */
    Iterable<String> process(IJavaFile file) throws IOException;
  }

  // Note that this may not be particularly efficient if parallelized
  public void process(final Processor p, final String project, final Iterable<String> classes) throws IOException {
    final Multimap<String, IJavaFile> byProject = ArrayListMultimap.create();
    for (final String qname : classes) {
      final Pair<String, String> key = new Pair<>(project, qname);
      if (!markAsLoaded(key)) {
        final IJavaFile file = classToFile.get(key);
        byProject.put(file.getProject(), file);
      }
    }
    process(p, byProject);
  }

  private void process(final Processor p, final Multimap<String, IJavaFile> byProject) throws IOException {
    for (final Map.Entry<String, Collection<IJavaFile>> e : byProject.asMap().entrySet()) {
      @Nullable
      final Collection<IJavaFile> values = e.getValue();
      if (values != null)
        for (final IJavaFile file : e.getValue()) {
          process(p, e.getKey(), p.process(file));
        }
    }
  }

  public void process(Processor p, List<IJavaFile> sources) throws IOException {
    final Multimap<String, IJavaFile> byProject = ArrayListMultimap.create();
    for (final IJavaFile file : sources) {
      final Pair<String, String> key = new Pair<>(file.getProject(), file.getQualifiedName());
      if (!markAsLoaded(key)) {
        byProject.put(file.getProject(), file);
      }
    }
    process(p, byProject);
  }

  /**
   * Marks the key as being seen
   * 
   * @return true if previously marked
   */
  private boolean markAsLoaded(final Pair<String, String> key) {
    boolean previouslySeen = !loaded.add(key);
    if (previouslySeen) {
      return true;
    }
    // Check if it's from the JRE
    //
    // TODO should I change this to return IJavaFile?
    final IJavaFile file = classToFile.get(key);
    if (file == null) {
      return true; // ignore
    }
    if (file.getProject().equals(key.first())) {
      // No need to continue checking
      return false;
    }
    if (file.getProject().startsWith(Config.JRE_NAME)) {
      return !loaded.add(new Pair<>(file.getProject(), file.getQualifiedName()));
    }
    return false;
  }

  private final Set<Pair<String, String>> loaded = new HashSet<>();
}
