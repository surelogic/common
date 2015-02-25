package com.surelogic.common.java;

import java.io.*;
import java.util.*;

import com.surelogic.common.*;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.*;
import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;
import com.surelogic.common.util.*;

/**
 * A set for projects specified for a run/scan
 * 
 * @author edwin
 */
public class JavaProjectSet<P extends ISLJavaProject> implements Iterable<P> {
  private final IJavaFactory<P> factory;
  protected SLProgressMonitor monitor;
  // private final Map<String,Object> options = new HashMap<String, Object>();
  protected final Map<String, P> projects = new HashMap<String, P>();
  // In dependency order
  private final List<P> ordering = new ArrayList<P>();
  // To project names
  private final Map<File, String> fileMap = new HashMap<File, String>();

  private final Date date;
  protected final File location;
  protected File f_scanDir;
  protected File f_resultsFile;

  protected static final String UNINIT = "<uninitialized>";
  protected String f_scanDirName;
  protected String f_previousPartialScan;
  protected boolean delta = false;
  protected final boolean isAuto;
  protected final Map<String, Object> args;

  public JavaProjectSet(IJavaFactory<P> f, File loc, boolean isAuto, Date d, Map<String, Object> args) {
    factory = f;
    location = loc;
    this.isAuto = isAuto;
    this.args = args;
    f_scanDirName = UNINIT;
    date = d;
  }

  /**
   * Only used by Util and jsure-ant
   */
  public JavaProjectSet(IJavaFactory<P> f, Config cfg, SLProgressMonitor monitor) {
    factory = f;
    f_scanDirName = UNINIT;
    this.monitor = monitor;
    add(cfg);
    location = cfg.getLocation();
    isAuto = false;
    args = new HashMap<String, Object>();
    date = new Date();
  }

  public void setScanDir(File scanDir) {
    if (f_scanDirName != UNINIT) {
      throw new IllegalStateException("Run already set: " + f_scanDirName);
    }
    if (scanDir == null) {
      throw new IllegalArgumentException("Null scanDir");
    }
    // TODO check if it's for the right thing?
    f_scanDir = scanDir;
    f_scanDirName = scanDir.getName();
  }

  public File getLocation() {
    return location;
  }

  public File getRunDir() {
    return f_scanDir;
  }

  public String getRun() {
    return f_scanDirName;
  }

  public Date getDate() {
    return date;
  }

  public int size() {
    return projects.size();
  }

  public int getNumSourceFiles() {
    int rv = 0;
    for (Config c : getConfigs()) {
      rv += c.getFiles().size();
    }
    return rv;
  }

  public boolean multiProject() {
    int count = 0;
    for (P p : projects.values()) {
      if (p.getConfig().getBoolOption(Config.AS_SOURCE)) {
        count++;
        if (count > 1) {
          return true;
        }
      }
    }
    return count > 1;
  }

  public ArrayList<String> getSourceProjectNamesAlpha() {
    final ArrayList<String> srcProjects = new ArrayList<String>();
    for (P p : projects.values()) {
      if (p.getConfig().getBoolOption(Config.AS_SOURCE)) {
        srcProjects.add(p.getName());
      }
    }
    Collections.sort(srcProjects);
    return srcProjects;
  }

  public String getFirstProjectNameAlphaOrNull() {
    final ArrayList<String> srcProjects = getSourceProjectNamesAlpha();
    if (srcProjects.isEmpty())
      return null;
    else
      return srcProjects.get(0);
  }

  public String getLabel() {
    final StringBuilder sb = new StringBuilder();
    for (String p : getSourceProjectNamesAlpha()) {
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(p);
    }
    return sb.toString();
  }

  public SLProgressMonitor getMonitor() {
    return monitor;
  }

  public P get(String name) {
    return projects.get(name);
  }

  public boolean contains(String name) {
    return get(name) != null;
  }

  public Iterable<Config> getConfigs() {
    return new FilterIterator<P, Config>(iterator()) {
      @Override
      protected Object select(P p) {
        return p.getConfig();
      }
    };
  }

  public Iterable<String> getProjectNames() {
    return new FilterIterator<P, String>(iterator()) {
      @Override
      protected Object select(P p) {
        return p.getName();
      }
    };
  }

  public Iterable<? extends P> getProjects() {
    populateOrdering();
    return ordering;
  }

  @Override
  public Iterator<P> iterator() {
    populateOrdering();
    return ordering.iterator();
  }

  public void resetOrdering() {
    ordering.clear();
  }

  private void populateOrdering() {
    if (ordering.isEmpty()) {
      // Populate the ordering
      for (P p : projects.values()) {
        populateOrdering(p);
      }
    }
  }

  private void populateOrdering(final P p) {
    if (p != null && !ordering.contains(p)) {
      for (Config c : p.getConfig().getDependencies()) {
        if (c != p.getConfig()) {
          P jp = get(c.getProject());
          populateOrdering(jp);
        }
      }
      ordering.add(p);
    }
  }

  public P getFirstProjectOrNull() {
    for (P p : projects.values()) {
      return p;
    }
    return null;
  }

  public void mapToProject(File f, String project) {
    fileMap.put(f, project);
  }

  /**
   * @return the project that the file is in
   */
  public String checkMapping(File f) {
    return fileMap.get(f);
  }

  public boolean isAutoBuild() {
    return isAuto;
  }

  public boolean isDelta() {
    return delta;
  }

  public String getPreviousPartialScan() {
    return f_previousPartialScan;
  }

  public void setPreviousPartialScan(String last) {
    if (last == null) {
      throw new IllegalArgumentException(I18N.err(44, "last"));
    }
    if (f_previousPartialScan != null) {
      throw new IllegalStateException(I18N.err(230, f_previousPartialScan));
    }
    f_previousPartialScan = last;
    delta = last != null;
  }

  public Object getArg(String key) {
    return args.get(key);
  }

  public void setArg(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Null key");
    }
    args.put(key, value);
  }

  public File getResultsFile() {
    return f_resultsFile;
  }

  /**
   * Gets the source folders that were excluded from analysis (relative to the
   * workspace).
   * 
   * @return the excluded source folders, or an empty list if none.
   */
  public List<String> getExcludedSourceFolders() {
    return getSourceFoldersFor(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_FOLDER);
  }

  /**
   * Gets the excluded source packages with wildcards.
   * 
   * @return the excluded source package spec with wildcards, or an empty list
   *         if none.
   */
  public List<String> getExcludedSourcePackageSpec() {
    return getSourcePackageSpecFor(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_PACKAGE);
  }

  /**
   * Gets the source folders that were examined as bytecode instead of source
   * (relative to the workspace).
   * 
   * @return the source folders examined as bytecode, or an empty list if none.
   */
  public List<String> getSourceFoldersLoadedAsBytecode() {
    return getSourceFoldersFor(SureLogicToolsPropertiesUtility.SCAN_SOURCE_FOLDER_AS_BYTECODE);
  }

  /**
   * Gets the source packages with wildcards that were examined as bytecode
   * instead of source.
   * 
   * @return the source package spec with wildcards examined as bytecode, or an
   *         empty list if none.
   */
  public List<String> getSourcePackageSpecLoadedAsBytecode() {
    return getSourcePackageSpecFor(SureLogicToolsPropertiesUtility.SCAN_SOURCE_PACKAGE_AS_BYTECODE);
  }

  /**
   * Gets the source folders for the given property (relative to the workspace).
   * 
   * @return the source folders, or an empty list if none.
   */
  private List<String> getSourceFoldersFor(String prop) {
    List<String> folders = new ArrayList<String>();
    for (Config c : getConfigs()) {
      String[] here = c.getListOption(prop);
      for (String p : here) {
        folders.add('/' + c.getProject() + '/' + p);
      }
    }
    if (folders.size() == 0) {
      return Collections.emptyList();
    }
    return folders;
  }

  private List<String> getSourcePackageSpecFor(String prop) {
    List<String> pkgs = new ArrayList<String>();
    for (Config c : getConfigs()) {
      String[] here = c.getListOption(prop);
      for (String p : here) {
        pkgs.add(p);
      }
    }
    if (pkgs.size() == 0) {
      return Collections.emptyList();
    }
    return pkgs;
  }

  String getConciseFoldersAndPackagesHelper(List<String> flds, List<String> pkgs) {
    final StringBuilder b = new StringBuilder();

    if (flds != null && !flds.isEmpty()) {
      b.append("Folders: ");
      boolean first = true;
      for (String s : flds) {
        if (first) {
          first = false;
        } else {
          b.append(", ");
        }
        b.append(s);
      }
    }

    if (pkgs != null && !pkgs.isEmpty()) {
      if (!flds.isEmpty()) {
        b.append("; ");
      }
      b.append("Packages: ");
      boolean first = true;
      for (String s : pkgs) {
        if (first) {
          first = false;
        } else {
          b.append(", ");
        }
        b.append(s);
      }
    }
    return b.toString();
  }

  /**
   * Used by the UI to display folders and packages excluded from the JSure
   * scan.
   * 
   * @return a string with a concise summary of folders and packages excluded
   *         from the JSure scan.
   */
  public String getConciseExcludedFoldersAndPackages() {
    return getConciseFoldersAndPackagesHelper(getExcludedSourceFolders(), getExcludedSourcePackageSpec());
  }

  /**
   * Used by the UI to display folders and packages treated as if they were
   * bytecode (not verified) from the JSure scan.
   * 
   * @return a string with a concise summary of folders and packages treated as
   *         if they were bytecode (not verified) from the JSure scan.
   */
  public String getConciseSourceFoldersLoadedAsBytecode() {
    return getConciseFoldersAndPackagesHelper(getSourceFoldersLoadedAsBytecode(), getSourcePackageSpecLoadedAsBytecode());
  }

  /**
   * @return true if they have the same projects
   */
  public boolean matchProjects(JavaProjectSet<?> other) {
    return projects.keySet().equals(other.projects.keySet());
  }

  public P add(Config cfg) {
    if (f_scanDirName != UNINIT) {
      throw new IllegalStateException("Adding config after run already set: " + f_scanDirName);
    }
    resetOrdering();
    P p = factory.newProject(this, cfg, cfg.getProject(), monitor);
    projects.put(cfg.getProject(), p);
    return p;
  }

  /**
   * Create a new Projects, removing the specified projects
   */
  public JavaProjectSet<P> remove(Collection<String> removed) {
    if (removed == null) {
      return null;
    }
    if (!XUtil.testing) {
      final Iterator<String> it = removed.iterator();
      while (it.hasNext()) {
        String name = it.next();
        if (get(name) == null) {
          // eliminate projects that don't exist
          System.err.println("No such project: " + name);
          it.remove();
        }
      }
    }
    if (removed.isEmpty()) {
      return this;
    }
    JavaProjectSet<P> p = factory.newProjectSet(location, isAuto, new Date(), args);
    for (P old : projects.values()) {
      if (!removed.contains(old.getName())) {
        p.projects.put(old.getName(), old);
      }
    }
    if (p.projects.isEmpty()) {
      return null;
    }
    return p;
  }

  public void computeScan(File dataDir, JavaProjectSet<P> oldProjects) throws Exception {
    if (f_scanDirName != UNINIT) {
      throw new IllegalStateException("Run already set: " + f_scanDirName);
    }
    if (oldProjects != null) {
      setPreviousPartialScan(oldProjects.f_scanDirName);
    }

    final String scanDirName = SLUtility.getScanDirectoryName(getFirstProjectNameAlphaOrNull(), multiProject(), getDate());
    f_scanDirName = scanDirName;
    f_scanDir = new File(dataDir, scanDirName);
    f_scanDir.mkdirs();

    final String resultsName = oldProjects != null ? PersistenceConstants.PARTIAL_RESULTS_ZIP : PersistenceConstants.RESULTS_ZIP;
    f_resultsFile = new File(f_scanDir, resultsName);

    // System.out.println("Contents of projects: "+run);
    final File xml = new File(f_scanDir, PersistenceConstants.PROJECTS_XML);
    final PrintStream pw = new PrintStream(xml);
    try {
      JavaProjectsXMLCreator creator = new JavaProjectsXMLCreator(pw);
      // TODO the problem is that I won't know what the last run was until
      // later ...

      creator.write(this);
    } finally {
      pw.close();
    }
  }

  public void clear() {
    for (P proj : this) {
      proj.clear();
    }
    // Can't clear these, since they're used by the getters
    // args.clear();
    fileMap.clear();
    // ordering.clear();
    // projects.clear();
  }
  
  public void setMonitor(SLProgressMonitor m) {
	// TODO what to do here?
  }
}
