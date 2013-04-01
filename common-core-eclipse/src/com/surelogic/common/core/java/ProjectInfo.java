package com.surelogic.common.core.java;

import java.io.File;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.java.*;
import com.surelogic.common.tool.SureLogicToolsFilter;
import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;

public abstract class ProjectInfo<P extends ISLJavaProject> {
	public final IProject project;
	final List<ICompilationUnit> allCompUnits;
	final Set<ICompilationUnit> cuDelta = new HashSet<ICompilationUnit>();
	final Set<IResource> removed = new HashSet<IResource>();
	/**
	 * All comp units includes delta?
	 */
	boolean updated = true;
	boolean active = true;

	protected ProjectInfo(IProject p, List<ICompilationUnit> cus) {
		project = p;
		allCompUnits = new ArrayList<ICompilationUnit>(cus);
	}

	public void setActive(boolean value) {
		active = value;
	}

	public boolean isActive() {
		return active;
	}

	public boolean hasDeltas() {
		return !cuDelta.isEmpty();
	}

	public void registerDelta(List<ICompilationUnit> cus) {
		if (!cus.isEmpty()) {
			cuDelta.addAll(cus);
			updated = false;
		}
	}

	public void registerResourcesDelta(List<Pair<IResource, Integer>> resources) {
		for (Pair<IResource, Integer> p : resources) {
			if (p.second() == IResourceDelta.REMOVED && p.first().getName().endsWith(".java")) {
				removed.add(p.first());
				updated = false;
			}
		}
	}

	private boolean needsUpdate() {
		return !updated && !cuDelta.isEmpty();
	}

	Iterable<ICompilationUnit> getAllCompUnits() {
		if (needsUpdate()) {
			update(allCompUnits, cuDelta, removed);
		}
		return allCompUnits;
	}

	Iterable<IResource> getRemovedResources() {
		return removed;
	}

	Iterable<ICompilationUnit> getDelta() {
		if (needsUpdate()) {
			Iterable<ICompilationUnit> result = new ArrayList<ICompilationUnit>(cuDelta);
			update(allCompUnits, cuDelta, removed);
			return result;
		}
		return allCompUnits;
	}    

	/**
	 * Adds itself to projects to make sure that it's not created multiple times
	 */
	public Config makeConfig(final JavaProjectSet<P> projects, boolean all) throws JavaModelException {
		final IJavaProject jp = JDTUtility.getJavaProject(project.getName());
		if (jp == null) {
			return null;
		}
		scanForJDK(projects, jp);

		final File location = EclipseUtility.resolveIPath(project.getLocation());
		Config config = new ZippedConfig(project.getName(), location, false, containsJavaLangObject(jp));
		projects.add(config);
		setOptions(config);

		for (IResource res : getRemovedResources()) {
			final File f = res.getLocation().toFile();
			config.addRemovedFile(f);
		}
		for (JavaSourceFile jsf : convertCompUnits(config, all ? getAllCompUnits() : getDelta())) {
			config.addFile(jsf);
		}
		addDependencies(projects, config, project, false);
		return config;
	}

	private void setOptions(Config config) {
		final IJavaProject jp = JDTUtility.getJavaProject(config.getProject());
		if (config.getLocation() != null) {
			/*
			 * Moved to clearProjectInfo() // TODO Is this right for multi-project
			 * configurations? ModuleRules.clearSettings();
			 * ModuleRules.clearAsSourcePatterns();
			 * ModuleRules.clearAsNeededPatterns();
			 */

			final IFile propsFile = jp.getProject().getFile(SureLogicToolsPropertiesUtility.PROPS_FILE);
			final Properties props = SureLogicToolsPropertiesUtility.readFileOrNull(propsFile.getLocation().toFile());
			if (props != null) {
				for (Map.Entry<Object, Object> p : props.entrySet()) {
					// System.out.println("Tool set "+p.getKey()+" = "+p.getValue());
					config.setOption(p.getKey().toString(), p.getValue());
				}
			}
			setProjectSpecificProperties(config);
		}
		// Reordered to avoid conflicts
		int version = JDTUtility.getMajorJavaSourceVersion(jp);
		config.setOption(Config.SOURCE_LEVEL, version);
		// System.out.println(config.getProject()+": set to level "+version);
	}

	protected void setProjectSpecificProperties(Config config) {
		// Nothing to do yet
	}
	
	void addDependencies(JavaProjectSet<P> projects, Config config, IProject p, boolean addSource) throws JavaModelException {
		final IJavaProject jp = JDTUtility.getJavaProject(p.getName());
		// TODO what export rules?
		final P jre = scanForJDK(projects, jp);
		final boolean hasSourceForJLO = containsJavaLangObject(jp);
		System.out.println("Project " + jp);

		for (IClasspathEntry cpe : jp.getResolvedClasspath(true)) {
			System.out.println("\tCPE = " + cpe);
			// TODO ignorable since they'll be handled by the compiler
			// cpe.getAccessRules();
			// cpe.combineAccessRules();

			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE:
				if (addSource) {
					addSourceFiles(config, cpe);
				}
				config.addToClassPath(config);
				// TODO makeRelativeTo is a 3.5 API
				final IPath projectPath = p.getFullPath();
				final String pathToSrc = cpe.getPath().makeRelativeTo(projectPath).toString();				
				final String pathToBin;
				if (cpe.getOutputLocation() != null) {
					pathToBin = cpe.getOutputLocation().makeRelativeTo(projectPath).toString();	
				} else {
					// TODO what if there's more than one?
					pathToBin = jp.getOutputLocation().makeRelativeTo(projectPath).toString();	
				}								
				config.addToClassPath(new SrcEntry(config, pathToSrc, pathToBin));
				break;
			case IClasspathEntry.CPE_LIBRARY:
				// System.out.println("Adding "+cpe.getPath()+" for "+p.getName());
				final File f = EclipseUtility.resolveIPath(cpe.getPath());

				// Check if the jar is already in some other project (e.g, the JRE)
				String mapped = projects.checkMapping(f);
				if (hasSourceForJLO && mapped != null && jre != null && mapped.equals(jre.getName())) {
					mapped = null; // treat this jar as if it's part of this project
				}
				if (mapped != null) {
					P mappedProj = projects.get(mapped);
					if (mappedProj == null) {
						// Make project for jar
						mappedProj = makeJarConfig(projects, f, mapped);
					}
					config.addToClassPath(mappedProj.getConfig());
				} else {
					config.addJar(f, cpe.isExported());
				}
				break;
			case IClasspathEntry.CPE_PROJECT:
				final String projName = cpe.getPath().lastSegment();
				final P jcp = projects.get(projName);
				if (jcp != null) {
					// Already created
					config.addToClassPath(jcp.getConfig());
					break;
				}
				final IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
				final ProjectInfo<P> info = getProjectInfo(proj);
				final Config dep;
				if (info != null) {
					final boolean hasDeltas = info.hasDeltas();
					dep = info.makeConfig(projects, hasDeltas);
				} else {
					final File location = EclipseUtility.resolveIPath(proj.getLocation());
					dep = new ZippedConfig(projName, location, cpe.isExported(),
							containsJavaLangObject(JDTUtility.getJavaProject(projName)));
					projects.add(dep);
					setOptions(dep);
				}
				config.addToClassPath(dep);

				if (info == null) {
					addDependencies(projects, dep, proj, true);
				}
				break;
			default:
				System.out.println("Unexpected: " + cpe);
			}
		}
		if (jre != null && !hasSourceForJLO) {
			// Add JRE if not already added
			boolean hasJRE = false;
			for (IClassPathEntry e : config.getClassPath()) {
				if (e.equals(jre.getConfig())) {
					hasJRE = true;
					break;
				}
			}
			if (!hasJRE) {
				System.out.println("Adding missing JRE: " + jre.getName());
				config.addToClassPath(jre.getConfig());
			}
		}
		projects.resetOrdering();
	}      

	protected abstract ProjectInfo<P> getProjectInfo(IProject proj);
	
	private void addSourceFiles(Config config, IClasspathEntry cpe) {
		// TODO handle multiple deltas?
		/*
		 * final File dir = EclipseUtility.resolveIPath(cpe.getPath()); final
		 * File[] excludes = new File[cpe.getExclusionPatterns().length]; int i=0;
		 * for(IPath xp : cpe.getExclusionPatterns()) { excludes[i] =
		 * EclipseUtility.resolveIPath(xp); i++; }
		 */
		IContainer root = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(cpe.getPath());
		final IResource[] excludes = new IResource[cpe.getExclusionPatterns().length];
		int i = 0;
		for (IPath xp : cpe.getExclusionPatterns()) {
			excludes[i] = root.findMember(xp);
			i++;
		}
		addJavaFiles(root, config, excludes);
	}

	private void addJavaFiles(IContainer dir, Config config, IResource... excluded) {
		try {
			addJavaFiles("", dir, config, excluded);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void addJavaFiles(String pkg, IContainer dir, Config config, IResource[] excluded) throws CoreException {
		for (IResource x : excluded) {
			if (dir.equals(x)) {
				return;
			}
		}
		if (dir == null || !dir.exists()) {
			return;
		}
		// System.out.println("Scanning "+dir.getAbsolutePath());
		boolean added = false;
		for (IResource r : dir.members()) {
			if (r instanceof IFile && r.getName().endsWith(".java")) {
				final ICompilationUnit icu = JavaCore.createCompilationUnitFrom((IFile) r);
				if ((icu != null) && (icu.getJavaProject().isOnClasspath(icu))) {
					final File f = r.getLocation().toFile();
					// System.out.println("Found source file: "+f.getPath());
					/*
					 * String typeName = f.getName().substring(0, f.getName().length() -
					 * 5); String qname = pkg.length() == 0 ? typeName : pkg + '.' +
					 * typeName; config.addFile(new JavaSourceFile(qname, f, f
					 * .getAbsolutePath()));
					 */
					final String path = f.getAbsolutePath();
					/*
					 * TODO Problem due to hashing conflict?
					 * 
					 * for(IType t : icu.getAllTypes()) { final String qname =
					 * t.getFullyQualifiedName(); config.addFile(new
					 * JavaSourceFile(qname, f, path)); }
					 */
					final String qname = computeQualifiedName(icu);

					// TODO Used when there's no project info
					config.addFile(new JavaSourceFile(qname, f, path, false, project.getName()));

					if (!added) {
						added = true;
						/*
						 * if (debug) { System.out.println("Found java files in "+pkg); }
						 */
						config.addPackage(pkg);
					}
				}
			}
			if (r instanceof IContainer) {
				final String newPkg = pkg == "" ? r.getName() : pkg + '.' + r.getName();
				addJavaFiles(newPkg, (IContainer) r, config, excluded);
			}
		}
	}

	/**
	 * Create a project/config for a shared jar
	 */
	private P makeJarConfig(JavaProjectSet<P> projects, File f, String name) {
		System.out.println("Creating shared jar: " + name);
		// Use its containing directory as a location
		final Config config = new Config(name, f.getParentFile(), true, false);
		config.addJar(f, true);
		return projects.add(config);
	}

	/**
	 * Look for a container in the raw classpath that corresponds to a JRE
	 */
	private P scanForJDK(JavaProjectSet<P> projects, IJavaProject jp) throws JavaModelException {
		if (jp == null) {
			return null;
		}
		for (IClasspathEntry cpe : jp.getRawClasspath()) {
			System.out.println("Scanning: "+cpe);
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
				final String path = cpe.getPath().toPortableString();
				//IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(cpe); 
				if (path.startsWith(Config.JRE_NAME)) {
					final IClasspathContainer cc = JavaCore.getClasspathContainer(cpe.getPath(), jp);
					if (cc == null) {
						// Creating project from sun.boot.classpath
						P jcp = projects.get(path);
						if (jcp == null) {
							final String classpath = System.getProperty("sun.boot.class.path");
							System.out.println("sun.boot.class.path = " + classpath);

							final Config config = new Config(path, null, true, true);
							for (String jar : classpath.split(File.pathSeparator)) {
								final File f = new File(jar);
								config.addJar(f, true);
								projects.mapToProject(f, path);
							}
							setDefaultJRE(path);
							jcp = projects.add(config);
						}
						return jcp;
					} else {
						P jcp = findJRE(projects, cc);
						if (jcp == null) {
							jcp = projects.add(makeConfig(projects, cc));
						}
						return jcp;
					}
				}
			}
		}
		return null;
	}

	protected void setDefaultJRE(String name) {
		// Nothing to do
	}
	
	private P findJRE(JavaProjectSet<P> projects, final IClasspathContainer cc) {
		final String name = cc.getPath().toPortableString();
		P jcp = projects.get(name);
		if (jcp == null) {
			// Not found by name, so check for existing JREs
			for (P p : projects) {
				if (p.getName().startsWith(Config.JRE_NAME) && compareJREs(p.getConfig(), cc)) {
					return p;
				}
			}
		}
		return jcp;
	}

	private boolean compareJREs(Config c, final IClasspathContainer cc) {
		final IClasspathEntry[] cpes = cc.getClasspathEntries();
		int i = 0;
		for (IClassPathEntry e : c.getClassPath()) {
			if (i >= cpes.length) {
				return false;
			}
			final IClasspathEntry cpe = cpes[i];
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				final File f = EclipseUtility.resolveIPath(cpe.getPath());
				if (!(e instanceof JarEntry)) {
					return false;
				}
				JarEntry j = (JarEntry) e;
				if (!f.equals(j.getPath())) {
					return false;
				}
				break;
			default:
				return false;
			}
			i++;
		}
		return true;
	}    

	/**
	 * Make a Config for the JRE
	 */
	private Config makeConfig(JavaProjectSet<P> projects, final IClasspathContainer cc) {
		final String name = cc.getPath().toPortableString();
		final Config config = new Config(name, null, true, true);
		for (IClasspathEntry cpe : cc.getClasspathEntries()) {
			switch (cpe.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				final File f = EclipseUtility.resolveIPath(cpe.getPath());
				// System.out.println("Adding "+f+" for "+cc.getDescription());
				config.addJar(f, true);
				projects.mapToProject(f, name);
				break;
			default:
				throw new IllegalStateException("Got entryKind: " + cpe.getEntryKind());
			}
		}
		setDefaultJRE(name);
		return config;
	}

	/**
	 * Either add/remove as needed
	 * 
	 * @param removed2
	 */
	void update(Collection<ICompilationUnit> all, Collection<ICompilationUnit> cus, Set<IResource> removed) {
		// Filter out removed files
		final Iterator<ICompilationUnit> it = all.iterator();
		while (it.hasNext()) {
			final ICompilationUnit cu = it.next();
			if (removed.contains(cu.getResource())) {
				it.remove();
			}
		}
		// Add in changed ones
		for (ICompilationUnit cu : cus) {
			// TODO use a Set instead?
			if (cu.getResource().exists()) {
				if (!all.contains(cu)) {
					all.add(cu);
					// System.out.println("Added:   "+cu.getHandleIdentifier());
				} else {
					// System.out.println("Exists:  "+cu.getHandleIdentifier());
				}
			} else {
				all.remove(cu);
				// System.out.println("Deleted: "+cu.getHandleIdentifier());
			}
		}
		updated = true;
	}

	public static Collection<JavaSourceFile> convertCompUnits(Config config, final Iterable<ICompilationUnit> cus)
			throws JavaModelException {
		final List<JavaSourceFile> files = new ArrayList<JavaSourceFile>();
		// setup filter
		final String[] excludedSourceFolders = config.getListOption(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_FOLDER);
		final String[] excludedPackagePatterns = config.getListOption(SureLogicToolsPropertiesUtility.SCAN_EXCLUDE_SOURCE_PACKAGE);
		final SureLogicToolsFilter filter = SureLogicToolsPropertiesUtility
				.getFilterFor(excludedSourceFolders, excludedPackagePatterns);
		for (ICompilationUnit icu : cus) {
			// Check if legal
			final String name = icu.getElementName();
			if (!SLUtility.PACKAGE_INFO_JAVA.equals(name) && !SLUtility.isValidJavaIdentifier(name.substring(0, name.length()-5))) {
				continue;
			}

			final IPath path = icu.getResource().getFullPath();
			final IPath loc = icu.getResource().getLocation();
			final File f = loc.toFile();
			final String qname;
			String packageName = "";
			for (IPackageDeclaration pd : icu.getPackageDeclarations()) {
				packageName = pd.getElementName();
				config.addPackage(pd.getElementName());
			}
			if (f.exists()) {
				qname = computeQualifiedName(icu);
			} else { // Removed
				qname = f.getName();
			}
			boolean excludeFilterMatchesTreatAsBinary = filter.matches(path.toFile().getAbsolutePath(), packageName);
			files.add(new JavaSourceFile(qname, f, path.toPortableString(), excludeFilterMatchesTreatAsBinary, config.getProject()));
		}
		return files;
	}    

	public static String computeQualifiedName(ICompilationUnit icu) throws JavaModelException {
		String qname = null;
		for (IType t : icu.getTypes()) {
			qname = t.getFullyQualifiedName();
			/*
			 * if (qname.endsWith("SingleSignOnEntry")) {
			 * System.out.println("Looking at "+qname); }
			 */
			final int flags = t.getFlags();
			if (Flags.isPublic(flags)) {
				// This is the only public top-level type
				break;
			} else {
				// System.out.println("Got a non-public type: "+qname);
			}
		}
		if (qname == null) {
			// Backup method: unreliable since the qname may not match the
			// filename
			String pkg = null;
			for (IPackageDeclaration pd : icu.getPackageDeclarations()) {
				pkg = pd.getElementName();
				break;
			}
			qname = icu.getElementName();
			if (qname.endsWith(".java")) {
				qname = qname.substring(0, qname.length() - 5);
			}
			if (pkg != null) {
				qname = pkg + '.' + qname;
			}
		}
		return qname;
	}
	
	/**
	 * @return true if the given project contains java.lang.Object in source form
	 * @throws JavaModelException
	 */
	static boolean containsJavaLangObject(IJavaProject jp) throws JavaModelException {
		IType t = jp.findType(SLUtility.JAVA_LANG_OBJECT);
		return t.getCompilationUnit() != null;
	}
}
