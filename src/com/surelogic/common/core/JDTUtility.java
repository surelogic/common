package com.surelogic.common.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.tool.ToolProperties;

/**
 * A collection of useful JDT spells.
 */
public final class JDTUtility {

	/**
	 * Adds an entry to the classpath of the passed Eclipse Java project. The
	 * entry is placed as the last entry in the project's classpath.
	 * 
	 * @param javaProject
	 *            an Eclipse Java project.
	 * @param path
	 *            a path or Jar file.
	 * @return {@code true} if the addition was successful, {@code false}
	 *         otherwise. A log entry is made if the addition failed.
	 * 
	 * @throws IllegalArgumentException
	 *             if either parameter is {@code null}.
	 */
	public static boolean addToEndOfClasspath(final IJavaProject javaProject,
			final IPath path) {
		if (javaProject == null)
			throw new IllegalArgumentException(I18N.err(44, "javaProject"));
		if (path == null)
			throw new IllegalArgumentException(I18N.err(44, "path"));
		try {
			final IClasspathEntry[] orig = javaProject.getRawClasspath();
			final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

			for (IClasspathEntry e : orig) {
				entries.add(e);
			}
			entries.add(JavaCore.newLibraryEntry(path, null, null,
					new IAccessRule[0], new IClasspathAttribute[0], false));

			javaProject.setRawClasspath(entries
					.toArray(new IClasspathEntry[entries.size()]), null);
			return true;
		} catch (JavaModelException jme) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					I18N.err(219, path.toString(), javaProject.getProject()
							.getName()), jme);
		}
		return false;
	}

	/**
	 * Removes an entry from the classpath of an Eclipse Java project. If the
	 * passed path is not in the classpath then no changes are made to the
	 * project's classpath.
	 * 
	 * @param javaProject
	 *            an Eclipse Java project.
	 * @param path
	 *            a path or Jar file.
	 * @return {@code true} if the addition was successful, {@code false}
	 *         otherwise. A log entry is made if the addition failed.
	 * 
	 * @throws IllegalArgumentException
	 *             if either parameter is {@code null}.
	 */
	public static boolean removeFromClasspath(IJavaProject javaProject,
			IPath path) {
		if (javaProject == null)
			throw new IllegalArgumentException(I18N.err(44, "javaProject"));
		if (path == null)
			throw new IllegalArgumentException(I18N.err(44, "path"));
		try {
			final IClasspathEntry[] orig = javaProject.getRawClasspath();
			final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

			boolean removed = false;
			for (IClasspathEntry e : orig) {
				if (path.equals(e.getPath()))
					removed = true;
				else
					entries.add(e);
			}
			if (removed)
				javaProject.setRawClasspath(entries
						.toArray(new IClasspathEntry[entries.size()]), null);
			return true;
		} catch (JavaModelException jme) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					I18N.err(220, path.toString(), javaProject.getProject()
							.getName()), jme);
		}
		return false;
	}

	/**
	 * An abstract base class for matching classpath entries.
	 * 
	 * @see JDTUtility#isOnClasspath(IJavaProject, IPathFilter)
	 */
	public static abstract class IPathFilter {
		public abstract boolean match(IPath path);

		public boolean stopAfterMatch() {
			return true;
		}
	}

	/**
	 * Checks if the passed pathname is on the classpath of the passed Eclipse
	 * Java project.
	 * 
	 * @param javaProject
	 *            an Eclipse Java project.
	 * @param pathname
	 *            a path.
	 * @return {@code true} if the path is found on the classpath, {@code false}
	 *         otherwise.
	 */
	public static boolean isOnClasspath(IJavaProject javaProject,
			final IPath pathname) {
		if (javaProject == null)
			throw new IllegalArgumentException(I18N.err(44, "javaProject"));
		if (pathname == null)
			throw new IllegalArgumentException(I18N.err(44, "pathname"));
		return isOnClasspath(javaProject, new IPathFilter() {
			public boolean match(IPath path) {
				return pathname.equals(path);
			}
		});
	}

	/**
	 * Checks if the passed file is on the classpath of the passed Eclipse Java
	 * project.
	 * 
	 * @param javaProject
	 *            an Eclipse Java project.
	 * @param file
	 *            a Jar file.
	 * @return {@code true} if the file is found on the classpath, {@code false}
	 *         otherwise.
	 */
	public static boolean isOnClasspath(IJavaProject javaProject,
			final IFile file) {
		if (javaProject == null)
			throw new IllegalArgumentException(I18N.err(44, "javaProject"));
		if (file == null)
			throw new IllegalArgumentException(I18N.err(44, "file"));
		return isOnClasspath(javaProject, new IPathFilter() {
			public boolean match(IPath path) {
				return file.getFullPath().equals(path);
			}
		});
	}

	/**
	 * Checks if the anything that matches with the passed {@link IPathFilter}
	 * is on the classpath of the passed Eclipse Java project.
	 * 
	 * @param javaProject
	 *            an Eclipse Java project.
	 * @param matcher
	 *            an implementation of {@link IPathFilter}.
	 * @return {@code true} if a match is found on the classpath, {@code false}
	 *         otherwise.
	 * 
	 * @see IPathFilter
	 */
	public static boolean isOnClasspath(IJavaProject javaProject,
			IPathFilter matcher) {
		if (javaProject == null)
			throw new IllegalArgumentException(I18N.err(44, "javaProject"));
		if (matcher == null)
			throw new IllegalArgumentException(I18N.err(44, "matcher"));
		boolean rv = false;
		try {
			for (IClasspathEntry e : javaProject.getRawClasspath()) {
				if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					if (matcher.match(e.getPath())) {
						if (matcher.stopAfterMatch()) {
							return true;
						} else {
							rv = true;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			// Ignore this problem
		}
		return rv;
	}

	/**
	 * This is a method that tries to read the Eclipse or RAD product version
	 * information so that it can be included in a report. The build id is also
	 * included if it is not {@code null}.
	 * 
	 * @return information about the Eclipse or RAD product version.
	 */
	public static String getProductInfo() {
		String result = "unknown";
		final IProduct product = Platform.getProduct();
		if (product != null) {
			final String description = product.getDescription();
			if (description != null) {
				int index = description.indexOf("ersion");
				if (index != -1) {
					index = description.indexOf('\n', index);
					if (index != -1) {
						result = description.substring(0, index);
						result = result.replaceAll("[\r\n]", " ");
					}
				}
			}
		}
		final String eclipseBuildId = System.getProperty("eclipse.buildId");
		if (eclipseBuildId != null) {
			result = result + " (Build id: " + eclipseBuildId + ")";
		}
		return result;
	}

	/**
	 * Gets a reference to the Eclipse log file. This file is under the
	 * workspace in <tt>.metadata/.log</tt>.
	 * <p>
	 * It is possible that this file may not exist. This is the case if the user
	 * deletes the log entries from the <b>Error Log</b> view. If this occurs a
	 * warning is logged.
	 * 
	 * @return a reference to the Eclipse log file.
	 */
	public static File getEclipseLogFile() {
		final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IPath log = wsRoot.getLocation().addTrailingSeparator().append(
				".metadata/.log");
		final File logFile = log.makeAbsolute().toFile();
		if (!logFile.exists()) {
			final String msg = I18N.err(137, logFile.getAbsolutePath());
			SLLogger.getLogger().log(Level.WARNING, msg, new Exception(msg));
		}
		return logFile;
	}

	/**
	 * Gets the source-level Java version of the passed project
	 * 
	 * @param jp
	 *            a Java project.
	 * @return the source-level Java version.
	 */
	public static String getJavaSourceVersion(final IJavaProject jp) {
		final String javaVersion = jp.getOption(
				"org.eclipse.jdt.core.compiler.source", true);
		return javaVersion;
	}

	/**
	 * Get the major Java version for the source level
	 */
	public static int getMajorJavaSourceVersion(final IJavaProject jp) {
		final String javaVersion = getJavaSourceVersion(jp);
		return Integer.parseInt(javaVersion.substring(2, 3));
	}

	/**
	 * Gets the {@link ICompilationUnit} associated with the passed information
	 * or {@code null} if neither can be found.
	 * 
	 * @param projectName
	 *            the project name the element is contained within. For example,
	 *            <code>JEdit</code>.
	 * @param packageName
	 *            the package name the element is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @return the {@link ICompilationUnit} associated with the passed
	 *         information or {@code null} if neither can be found.
	 */
	public static ICompilationUnit findICompilationUnit(
			final String projectName, final String packageName,
			final String typeName) {
		final IType type = findIType(projectName, packageName, typeName);
		if (type != null) {
			final ICompilationUnit element = type.getCompilationUnit();
			return element;
		} else {
			return null;
		}
	}

	/**
	 * Gets the {@link IType} element associated with the passed information or
	 * {@code null} if it cannot be found.
	 * 
	 * @param projectName
	 *            the project name the element is contained within. For example,
	 *            <code>JEdit</code>.  If null, try all projects
	 * @param packageName
	 *            the package name the element is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @return the Java element associated with the passed information or
	 *         {@code null} if no Java element can be found.
	 */
	public static IType findIType(final String projectName,
			final String packageName, final String typeName) {
		try {
			final int occurrenceCount = getAnonymousOccurrenceCount(typeName);
			final String baseTypeName = stripOffAnonymousOccurranceCount(typeName);
			final String className = baseTypeName.replace("$", ".");

			final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace()
					.getRoot();
			final IJavaModel model = JavaCore.create(wsRoot);
			if (model != null) {
				for (final IJavaProject project : model.getJavaProjects()) {
					if (projectName == null || project.getElementName().equals(projectName)) {
						String packageNameHolder = null;
						if (!(packageName == null)
								&& !packageName
										.equals(SLUtility.JAVA_DEFAULT_PACKAGE)) {
							packageNameHolder = packageName;
						}
						final IType type = project.findType(packageNameHolder,
								className, new NullProgressMonitor());

						if (type != null && type.exists()
								&& occurrenceCount > 0) {
							return lookupAnonymous(type, occurrenceCount);
						} else {
							return type;
						}
					}
				}
			}
		} catch (final Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(135, packageName, typeName, projectName), e);
		}
		return null;
	}

	private static final Pattern ANON = Pattern.compile("\\$\\d+");

	/**
	 * Determines the occurrence count from an anonymous type name. For example,
	 * the type {@code Hello$1} would return 1.
	 * <p>
	 * This method assumes that {@code $} is used as the separator.
	 * 
	 * @param typeName
	 *            the type name.
	 * @return the occurrence count or 0 if none.
	 */
	private static int getAnonymousOccurrenceCount(final String typeName) {
		final Matcher matcher = ANON.matcher(typeName);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * Strips off the anonymous type occurrence information from this type name.
	 * If the name has no anonymous information then it is returned unmodified.
	 * For example, the type {@code Hello$1} would return {@code Hello}.
	 * <p>
	 * This method assumes that {@code $} is used as the separator.
	 * 
	 * @param typeName
	 *            the type name.
	 * @return the type name with any anonymous type occurrence information
	 *         removed.
	 */
	private static String stripOffAnonymousOccurranceCount(final String typeName) {
		return ANON.matcher(typeName).replaceAll("");
	}

	/**
	 * This method tries to find an anonymous class within the passed type.
	 * 
	 * @param within
	 *            the type to search.
	 * @param occurrenceCount
	 *            the position of the anonymous class.
	 * @return the anonymous class or, <tt>within</tt> if not found.
	 * @throws JavaModelException
	 *             if something goes wrong.
	 */
	private static IType lookupAnonymous(final IType within,
			final int occurrenceCount) throws JavaModelException {
		final List<IType> anonymousTypes = new ArrayList<IType>();
		for (final IJavaElement child : within.getChildren()) {
			queryListOfAnonymous(child, anonymousTypes);
		}

		if (anonymousTypes.size() >= occurrenceCount) {
			final IType result = anonymousTypes.get(occurrenceCount - 1);
			return result;
		} else {
			return within;
		}
	}

	/**
	 * This method determines the ordered list of anonymous classes declared
	 * within the passed type.
	 * 
	 * @param within
	 *            the type to search.
	 * @param mutableList
	 *            a mutable list to add discovered anonymous classes to.
	 * @throws JavaModelException
	 *             if something goes wrong.
	 */
	private static void queryListOfAnonymous(final IJavaElement within,
			final List<IType> mutableList) throws JavaModelException {
		if (within instanceof IType) {
			final IType t = (IType) within;
			if (t.isAnonymous()) {
				mutableList.add(t);
			} else {
				// named type...bail out (no searching in this)
				return;
			}
		} else {
			if (within instanceof IParent) {
				final IParent p = (IParent) within;
				for (final IJavaElement child : p.getChildren()) {
					queryListOfAnonymous(child, mutableList);
				}
			}
		}
	}

	/**
	 * Gets a list of names for all the open Java projects in the workspace.
	 * 
	 * @return a list of names for all the open Java projects in the workspace.
	 */
	public static List<String> getJavaProjectNames() {
		final List<String> projectNames = new ArrayList<String>();
		for (final IJavaProject project : getJavaProjects()) {
			final String projectName = project.getElementName();
			if (projectName != null) {
				projectNames.add(projectName);
			}
		}
		return projectNames;
	}

	/**
	 * Gets a list of all the open Java projects in the workspace.
	 * 
	 * @return a list of all the open Java projects in the workspace.
	 */
	public static List<IJavaProject> getJavaProjects() {
		final List<IJavaProject> projectNames = new ArrayList<IJavaProject>();
		try {
			final IWorkspace ws = ResourcesPlugin.getWorkspace();
			final IWorkspaceRoot wsRoot = ws.getRoot();
			final IJavaModel model = JavaCore.create(wsRoot);
			for (final IJavaProject project : model.getJavaProjects()) {
				if (project != null) {
					projectNames.add(project);
				}
			}
		} catch (final JavaModelException e) {
			final String msg = I18N.err(79);
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
		}
		Collections.sort(projectNames, new Comparator<IJavaProject>() {
			public int compare(final IJavaProject o1, final IJavaProject o2) {
				return o1.getElementName().compareToIgnoreCase(
						o2.getElementName());
			}
		});
		return projectNames;
	}

	public static List<IProject> getProjects() {
		List<IJavaProject> projs = getJavaProjects();
		List<IProject> rv = new ArrayList<IProject>();
		for (IJavaProject p : projs) {
			rv.add(p.getProject());
		}
		return rv;
	}

	/**
	 * Gets the {@link IJavaProject} reference for the passed project name or
	 * {@code null} if there is no Java project using that name.
	 * 
	 * @param projectName
	 *            a project name.
	 * @return the {@link IJavaProject} reference for the passed project name or
	 *         {@code null} if there is no Java project using that name.
	 */
	public static IJavaProject getJavaProject(final String projectName) {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot wsRoot = ws.getRoot();
		final IJavaModel model = JavaCore.create(wsRoot);
		final IJavaProject jp = model.getJavaProject(projectName);
		if (jp.exists()) {
			return jp;
		}
		return null;
	}

	private static boolean noCompilationErrors(final IResource resource,
			final IProgressMonitor monitor) throws CoreException {
		final IMarker[] problems = resource.findMarkers(
				// IMarker.PROBLEM,
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);

		// check if any of these have a severity attribute that indicates an
		// error
		for (final IMarker marker : problems) {
			if (monitor.isCanceled()) {
				return false;
			}
			if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_INFO)) {
				final Logger log = SLLogger.getLogger();
				log.info("***** MARKER Message: "
						+ marker.getAttribute(IMarker.MESSAGE));
				log.info("***** MARKER Line #: "
						+ marker.getAttribute(IMarker.LINE_NUMBER));
				log.info("***** MARKER File: "
						+ marker.getAttribute(IMarker.LOCATION));
				log.info("***** MARKER Message: "
						+ marker.getAttribute(IMarker.MESSAGE));
				return false; // we found an error (bail out)
			}
		}
		return true;
	}

	/**
	 * Check if the compilation state of an {@link IJavaProject} has errors. FIX
	 * Originally copied from double-checker
	 * 
	 * @param javaProject
	 *            the {@link IJavaProject}to check for errors
	 * @param monitor
	 * @return <code>true</code> if the project has no compilation errors,
	 *         <code>false</code> if errors exist or the project has never been
	 *         built
	 * @throws CoreException
	 *             if we have trouble getting the project's {@link IMarker}list
	 */
	public static boolean noCompilationErrors(final IJavaProject javaProject,
			final IProgressMonitor monitor) throws CoreException {
		if (javaProject.hasBuildState()) {
			return noCompilationErrors(javaProject.getProject(), monitor);
		} else if (hasNoSource(javaProject)) {
			return true;
		} else {
			SLLogger.getLogger().warning(
					I18N.err(83, javaProject.getElementName()));
		}
		return false;
	}

	/**
	 * Checks if the passed project has any source code on its classpath.
	 * 
	 * @param p
	 *            a Java project.
	 * @return {@code true} if the passed project has any source code on its
	 *         classpath, {@code false} otherwise.
	 * @throws JavaModelException
	 *             if something goes wrong.
	 */
	private static boolean hasNoSource(final IJavaProject p)
			throws JavaModelException {
		for (final IClasspathEntry cpe : p.getRawClasspath()) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the passed compilation unit contains no compilation errors.
	 * 
	 * @param cu
	 *            a compilation unit.
	 * @return {@code true} if the passed compilation unit contains no
	 *         compilation errors, {@code false} otherwise.
	 * @throws CoreException
	 *             if something goes wrong.
	 */
	public static boolean noCompilationErrors(final ICompilationUnit cu,
			final IProgressMonitor monitor) throws CoreException {
		final boolean result = false; // assume it has errors or is not built
		final IJavaProject javaProject = cu.getJavaProject();
		if (javaProject.hasBuildState()) {
			return noCompilationErrors(cu.getCorrespondingResource(), monitor);
		} else {
			SLLogger.getLogger().warning(
					I18N.err(84, javaProject.getElementName(), cu.toString()));
		}
		return result;
	}

	/**
	 * Checks if the passed set contains no compilation errors.
	 * 
	 * @param <T>
	 *            either a {@link IJavaProject} or an {@link ICompilationUnit}.
	 * @param elements
	 *            to search for compilation errors.
	 * @return {@code true} if the passed collection contains no compilation
	 *         errors, {@code false} otherwise.
	 * @throws CoreException
	 *             if something goes wrong.
	 */
	public static <T extends IJavaElement> boolean noCompilationErrors(
			final Iterable<T> elements, final IProgressMonitor monitor)
			throws CoreException {
		for (final IJavaElement elt : elements) {
			switch (elt.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
				if (!noCompilationErrors((IJavaProject) elt, monitor)) {
					return false;
				}
				break;
			case IJavaElement.COMPILATION_UNIT:
				if (!noCompilationErrors((ICompilationUnit) elt, monitor)) {
					return false;
				}
				break;
			default:
				SLLogger.getLogger().warning(I18N.err(85, elt.toString()));
				return false;
			}
		}
		return true;
	}

	/**
	 * Collects compilation error messages from a collection of
	 * {@link IJavaProject} or {@link ICompilationUnit} instances.
	 * 
	 * @param <T>
	 *            either a {@link IJavaProject} or an {@link ICompilationUnit}.
	 * @param elements
	 *            to search for compilation errors.
	 * @return a (possibly empty) collection of compilation error messages.
	 * @throws CoreException
	 *             if something goes wrong.
	 */
	public static <T extends IJavaElement> Collection<String> findCompilationErrors(
			final Collection<T> elements, final IProgressMonitor monitor)
			throws CoreException {
		if (elements.isEmpty()) {
			return Collections.emptyList();
		}
		final List<String> errors = new ArrayList<String>();
		for (final IJavaElement elt : elements) {
			if (monitor.isCanceled()) {
				return Collections.emptyList();
			}
			switch (elt.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
				if (!noCompilationErrors((IJavaProject) elt, monitor)) {
					errors.add(elt.getElementName());
				}
				break;
			case IJavaElement.COMPILATION_UNIT:
				if (!noCompilationErrors((ICompilationUnit) elt, monitor)) {
					errors.add(elt.getElementName());
				}
				break;
			default:
				SLLogger.getLogger().warning(I18N.err(85, elt.toString()));
				errors.add(elt.getElementName());
			}
		}
		return errors;
	}

	/**
	 * Returns a map of which projects' builds are up-to-date with their source
	 * files and their project dependencies
	 */
	public static Map<IJavaProject, Boolean> projectsUpToDate() {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IJavaModel javaModel = JavaCore.create(root);
		try {
			final IJavaProject[] projects = javaModel.getJavaProjects();
			final Map<IJavaProject, Boolean> status = projectsUpToDate(root,
					projects);
			return status;
		} catch (final JavaModelException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Got exception while checking if projects are up to date",
					e);
			return null;
		}
	}

	private static Map<IJavaProject, Boolean> projectsUpToDate(
			final IWorkspaceRoot root, final IJavaProject[] projects)
			throws JavaModelException {
		final Map<IJavaProject, Boolean> status = new HashMap<IJavaProject, Boolean>();
		for (final IJavaProject p : projects) {
			status.put(p, null);
		}
		projectsUpToDate(root, status);
		return status;
	}

	private static Map<IJavaProject, Boolean> projectsUpToDate(
			final IWorkspaceRoot root, final Iterable<IJavaProject> projects)
			throws JavaModelException {
		final Map<IJavaProject, Boolean> status = new HashMap<IJavaProject, Boolean>();
		for (final IJavaProject p : projects) {
			status.put(p, null);
		}
		projectsUpToDate(root, status);
		return status;
	}

	/**
	 * Checks the projects in the map and returns whether their builds are
	 * up-to-date with their source files and their project dependencies. Also
	 * returns status for those projects.
	 * 
	 * @throws JavaModelException
	 */
	public static boolean projectsUpToDate(
			final Collection<IJavaProject> projects) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			final Map<IJavaProject, Boolean> status = projectsUpToDate(root,
					projects);
			for (final IJavaProject p : projects) {
				final Boolean b = status.get(p);
				if (b == null || !b.booleanValue()) {
					return false;
				}
			}
			return true;
		} catch (final JavaModelException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Got exception while checking if projects are up to date",
					e);
			return false;
		}
	}

	private static void projectsUpToDate(final IWorkspaceRoot root,
			final Map<IJavaProject, Boolean> status) throws JavaModelException {
		final List<IJavaProject> projs = new ArrayList<IJavaProject>(status
				.size());
		for (final Map.Entry<IJavaProject, Boolean> e : status.entrySet()) {
			projs.add(e.getKey());
			e.setValue(null);
		}
		final IProjectChecker checker = new AllChecker();
		for (final IJavaProject p : projs) {
			checkProject(root, status, p, checker);
		}
	}

	/**
	 * Check whether the project's build is up to date with the source files
	 * 
	 * @throws JavaModelException
	 */
	private static boolean checkProject(final IWorkspaceRoot root,
			final Map<IJavaProject, Boolean> status, final IJavaProject p,
			final IProjectChecker checker) throws JavaModelException {
		Boolean rv = status.get(p);
		if (rv == null) {
			// status not computed yet
			// temporarily set to true to stop cycles
			status.put(p, Boolean.TRUE);
			loop: for (final IClasspathEntry cpe : p.getRawClasspath()) {
				switch (cpe.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					if (checker.checkProjectDependencies(p)) {
						// Check/update dependent projects
						final String projName = cpe.getPath().lastSegment();
						final IProject proj = root.getProject(projName);
						final IJavaProject jp = JavaCore.create(proj);
						final boolean built = checkProject(root, status, jp,
								checker);
						if (!built) {
							rv = Boolean.FALSE;
							break loop;
						}
					}
					break;
				case IClasspathEntry.CPE_SOURCE:
					if (!checker.check(root, p, cpe)) {
						rv = Boolean.FALSE;
						break loop;
					}
					break;
				default:
				}
			}
			if (rv == null) {
				rv = Boolean.TRUE;
			}
			status.put(p, rv);
		}
		return rv.booleanValue();
	}

	private static interface IProjectChecker {
		boolean check(IWorkspaceRoot root, IJavaProject p, IClasspathEntry cpe)
				throws JavaModelException;

		boolean checkProjectDependencies(IJavaProject p);
	}

	static abstract class AbstractChecker implements IProjectChecker {
		protected void init(final IWorkspaceRoot root, final IJavaProject p,
				final IClasspathEntry cpe) {
			// Do nothing
		}

		public boolean check(final IWorkspaceRoot root, final IJavaProject p,
				final IClasspathEntry cpe) throws JavaModelException {
			init(root, p, cpe);

			IPath out = cpe.getOutputLocation();
			if (out == null) {
				out = p.getOutputLocation();
			}
			for (final IPackageFragmentRoot pkgRoot : p
					.findPackageFragmentRoots(cpe)) {
				for (final IJavaElement e : pkgRoot.getChildren()) {
					final IPackageFragment pkg = (IPackageFragment) e;
					if (stopProcessing()) {
						return false;
					}
					for (final ICompilationUnit cu : pkg.getCompilationUnits()) {
						final long cuTime = cu.getCorrespondingResource()
								.getLocalTimeStamp();
						if (!processTypes(cu, cuTime)) {
							continue;
						}
						for (final IType t : cu.getTypes()) {
							final String name = t.getFullyQualifiedName()
									.replace('.', '/')
									+ ".class";
							final IPath path = out.append(name);
							final long clTime = root.getFile(path)
									.getLocalTimeStamp();
							if (!check(cu, cuTime, clTime)) {
								return false;
							}
						}
					}
				}
			}
			return !stopProcessing();
		}

		protected boolean stopProcessing() {
			return false;
		}

		/**
		 * Returns whether check() should look at the class files
		 */
		protected boolean processTypes(final ICompilationUnit cu,
				final long cuTime) {
			return true;
		}

		/**
		 * Compare timestamps with its class files
		 */
		protected boolean check(final ICompilationUnit cu, final long cuTime,
				final long clTime) {
			return true;
		}
	}

	static class AllChecker extends AbstractChecker {
		long dotClassPathTime;
		long dotProjectTime;

		@Override
		protected void init(final IWorkspaceRoot root, final IJavaProject jp,
				final IClasspathEntry cpe) {
			final IProject p = jp.getProject();
			dotClassPathTime = p.getFile(".classpath").getLocalTimeStamp();
			dotProjectTime = p.getFile(".project").getLocalTimeStamp();
		}

		@Override
		protected boolean check(final ICompilationUnit cu, final long cuTime,
				final long clTime) {
			final Logger log = SLLogger.getLogger();
			if (cuTime > clTime) {
				if (log.isLoggable(Level.FINE)) {
					log.fine(cu.getElementName() + " is newer");
				}
				return false;
			}
			if (dotClassPathTime > clTime) {
				if (log.isLoggable(Level.FINE)) {
					log.fine(".classpath is newer");
				}
				return false;
			}
			if (dotProjectTime > clTime) {
				if (log.isLoggable(Level.FINE)) {
					log.fine(".project is newer");
				}
				return false;
			}
			return true;
		}

		public boolean checkProjectDependencies(final IJavaProject p) {
			return true;
		}
	}

	public static boolean compUnitsUpToDate(
			final Collection<ICompilationUnit> elements) {
		// FIX too conservative?
		final List<IJavaProject> projects = new ArrayList<IJavaProject>();
		for (final ICompilationUnit icu : elements) {
			final IJavaProject p = icu.getJavaProject();
			if (!projects.contains(p)) {
				projects.add(p);
			}
		}
		return projectsUpToDate(projects);
	}

	/**
	 * Collects all the comp units newer than the cutoffs
	 */
	static class NewerThanCollector extends AbstractChecker {
		final Map<IJavaProject, Date> cutoffs;
		final Set<ICompilationUnit> units = new HashSet<ICompilationUnit>();
		long cutoff = 0;

		NewerThanCollector(final Map<IJavaProject, Date> times) {
			cutoffs = times;
		}

		@Override
		public boolean check(final IWorkspaceRoot root, final IJavaProject p,
				final IClasspathEntry cpe) throws JavaModelException {
			final Date date = cutoffs.get(p);
			if (date == null) {
				SLLogger.getLogger().severe(
						"No last-scan time found for " + p.getElementType());
				return true;
			}
			cutoff = date.getTime();
			return super.check(root, p, cpe);
		}

		public boolean checkProjectDependencies(final IJavaProject p) {
			return false;
		}

		@Override
		protected boolean processTypes(final ICompilationUnit cu,
				final long cuTime) {
			if (cuTime > cutoff) {
				units.add(cu);
				SLLogger.getLogger().fine(
						cu.getElementName() + " is newer than cutoff: "
								+ units.size());
				return false;
			}
			return true;
		}

		@Override
		protected boolean check(final ICompilationUnit cu, final long cuTime,
				final long clTime) {
			if (clTime > cutoff) {
				units.add(cu);
				SLLogger.getLogger().fine(
						cu.getElementName() + " is newer than cutoff: "
								+ units.size());
			}
			return true;
		}

		public Collection<ICompilationUnit> list() {
			return units;
		}
	}

	public static Collection<ICompilationUnit> modifiedCompUnits(
			final Map<IJavaProject, Date> times, final IProgressMonitor monitor) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			final Map<IJavaProject, Boolean> status = new HashMap<IJavaProject, Boolean>();
			for (final IJavaProject p : times.keySet()) {
				status.put(p, null);
			}
			final NewerThanCollector collector = new NewerThanCollector(times);
			for (final IJavaProject p : times.keySet()) {
				checkProject(root, status, p, collector);
				monitor.worked(1);
			}
			return collector.list();
		} catch (final JavaModelException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Got exception while looking for modified comp units", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Check if the project's sources has been modified since
	 * 
	 * @param jp
	 * @param lastModified
	 * @return
	 */
	public static boolean projectUpdatedSince(final IJavaProject jp,
			final long time) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final Map<IJavaProject, Boolean> status = new HashMap<IJavaProject, Boolean>();
		try {
			return !checkProject(root, status, jp, new AbstractChecker() {
				private boolean updated = false;

				public boolean checkProjectDependencies(final IJavaProject p) {
					return false;
				}

				@Override
				protected boolean stopProcessing() {
					return updated;
				}

				@Override
				protected boolean processTypes(final ICompilationUnit cu,
						final long cuTime) {
					if (cuTime > time) {
						updated = true;
					}
					return false;
				}
			});
		} catch (final JavaModelException e) {
			return true;
		}
	}

	public static JavaProjectResources collectAllResources(IJavaProject jp, JavaProjectResources.Filter filter) {
		JavaProjectResources result = new JavaProjectResources(jp, filter);
		try {
			collectAllResources(result, jp.getProject());
		} catch (CoreException e) {
			e.printStackTrace(); // TODO
			return null;
		}
		return result;
	}

	private static void collectAllResources(JavaProjectResources jpr,
			IContainer p) throws CoreException {
		for (IResource res : p.members()) {
			if (res instanceof IContainer) {
				collectAllResources(jpr, (IContainer) res);
			} else if (res instanceof IFile) {
				jpr.resources.add(res);
				if (jpr.project.isOnClasspath(res)) {
					IFile f = (IFile) res;
					IJavaElement ije = JavaCore.create(f);
					if (ije instanceof ICompilationUnit) {
						jpr.cus.add((ICompilationUnit) ije);
					}
				}
			} else {
				System.out.println("Ignoring: " + res);
			}
		}
	}

	public interface CompUnitFilter {
		boolean matches(ICompilationUnit icu) throws JavaModelException;
	}
	
	public static CompUnitFilter getFilter(IProject p, String[] paths, String[] pkgs) {
		final Set<String> unique = new HashSet<String>();
		for (String path : paths) {
			unique.add(path);
		}		
		final IPath[] excludePaths = new IPath[unique.size()];
		int i = 0;
		for (String path : unique) {
			excludePaths[i] = p.getFullPath().append(path);
			i++;
		}
	
		/*
		for(String pkg : pkgs) {
			if (pkg.contains("rendering")) {
				System.out.println("Got package pattern: "+pkg);
			}
		}
		*/
		final Pattern[] excludePatterns = ToolProperties.makePackageMatchers(pkgs);
		return new CompUnitFilter() {
			public boolean matches(ICompilationUnit icu)
					throws JavaModelException {
				for (IPackageDeclaration pd : icu.getPackageDeclarations()) {
					final String pkg = pd.getElementName();
					/*
					if (pkg.contains("rendering")) {
						System.out.println("Got package: "+pkg);
					}
					*/
					for (Pattern p : excludePatterns) {
						if (p.matcher(pkg).matches()) {
							System.out.println("Excluding: "+icu.getHandleIdentifier());
							return true;
						}
					}
				}
				for (IPath p : excludePaths) {
					if (p.isPrefixOf(icu.getPath())) {
						System.out.println("Excluding due to "+p+": "+icu.getHandleIdentifier());
						return true;
					}
				}
				return false;
			}
		};
	}
	
	private JDTUtility() {
		// utility
	}

}
