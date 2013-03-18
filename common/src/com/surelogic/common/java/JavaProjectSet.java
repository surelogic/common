package com.surelogic.common.java;

import java.io.*;
import java.util.*;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.*;
import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;
import com.surelogic.common.util.FilterIterator;

/**
 * A set for projects specified for a run/scan
 * 
 * @author edwin
 */
public abstract class JavaProjectSet<P extends ISLJavaProject> implements Iterable<P> {
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
	
	public JavaProjectSet(File loc, boolean isAuto, Date d, Map<String, Object> args) {
		location = loc;
		this.isAuto = isAuto;
		this.args = args;
		f_scanDirName = UNINIT;
		date = d;
	}

	/**
	 * Only used by Util and jsure-ant
	 */
	public JavaProjectSet(Config cfg, SLProgressMonitor monitor) {
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
		List<String> folders = new ArrayList<String>();
		for (Config c : getConfigs()) {
			String[] here = c.getListOption(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_FOLDER);
			for (String p : here) {
				folders.add('/' + c.getProject() + '/' + p);
			}
		}
		if (folders.size() == 0) {
			return Collections.emptyList();
		}
		return folders;
	}
	
	/**
	 * Gets the excluded source packages with wildcards.
	 * 
	 * @return the excluded source package spec with wildcards, or an empty list
	 *         if none.
	 */
	public List<String> getExcludedSourcePackageSpec() {
		List<String> pkgs = new ArrayList<String>();
		for (Config c : getConfigs()) {
			String[] here = c.getListOption(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_PACKAGE);
			for (String p : here) {
				pkgs.add(p);
			}
		}
		if (pkgs.size() == 0) {
			return Collections.emptyList();
		}
		return pkgs;
	}
	
	public String getConciseExcludedFoldersAndPackages() {
		final StringBuilder b = new StringBuilder();

		List<String> flds = getExcludedSourceFolders();
		List<String> pkgs = getExcludedSourcePackageSpec();

		if (!flds.isEmpty()) {
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

		if (!pkgs.isEmpty()) {
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
	 * @return true if they have the same projects
	 */
	public boolean matchProjects(JavaProjectSet<P> other) {
		return projects.keySet().equals(other.projects.keySet());
	}
	
	public P add(Config config) {
		// TODO Auto-generated method stub
		return null;
	}
}
