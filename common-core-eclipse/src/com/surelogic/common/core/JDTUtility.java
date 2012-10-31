package com.surelogic.common.core;

import java.io.File;
import java.lang.reflect.Method;
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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ref.DeclVisitor;
import com.surelogic.common.ref.IDecl;
import com.surelogic.common.ref.IDeclField;
import com.surelogic.common.ref.IDeclFunction;
import com.surelogic.common.ref.IDeclPackage;
import com.surelogic.common.ref.IDeclParameter;
import com.surelogic.common.ref.IDeclType;
import com.surelogic.common.ref.IDeclTypeParameter;
import com.surelogic.common.ref.IJavaRef;

/**
 * A collection of useful JDT spells.
 */
@Utility
public final class JDTUtility {

  /**
   * Adds an entry to the classpath of the passed Eclipse Java project. The
   * entry is placed as the last entry in the project's classpath.
   * 
   * @param javaProject
   *          an Eclipse Java project.
   * @param path
   *          a path or Jar file.
   * @return {@code true} if the addition was successful, {@code false}
   *         otherwise. A log entry is made if the addition failed.
   * 
   * @throws IllegalArgumentException
   *           if either parameter is {@code null}.
   */
  public static boolean addToEndOfClasspath(final IJavaProject javaProject, final IPath path) {
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
      entries.add(JavaCore.newLibraryEntry(path, null, null, new IAccessRule[0], new IClasspathAttribute[0], false));

      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
      return true;
    } catch (JavaModelException jme) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(219, path.toString(), javaProject.getProject().getName()), jme);
    }
    return false;
  }

  /**
   * Removes an entry from the classpath of an Eclipse Java project. If the
   * passed path is not in the classpath then no changes are made to the
   * project's classpath.
   * 
   * @param javaProject
   *          an Eclipse Java project.
   * @param path
   *          a path or Jar file.
   * @return {@code true} if the addition was successful, {@code false}
   *         otherwise. A log entry is made if the addition failed.
   * 
   * @throws IllegalArgumentException
   *           if either parameter is {@code null}.
   */
  public static boolean removeFromClasspath(IJavaProject javaProject, IPath path) {
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
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
      return true;
    } catch (JavaModelException jme) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(220, path.toString(), javaProject.getProject().getName()), jme);
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
   *          an Eclipse Java project.
   * @param pathname
   *          a path.
   * @return {@code true} if the path is found on the classpath, {@code false}
   *         otherwise.
   */
  public static boolean isOnClasspath(IJavaProject javaProject, final IPath pathname) {
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
   *          an Eclipse Java project.
   * @param file
   *          a Jar file.
   * @return {@code true} if the file is found on the classpath, {@code false}
   *         otherwise.
   */
  public static boolean isOnClasspath(IJavaProject javaProject, final IFile file) {
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
   * Checks if the anything that matches with the passed {@link IPathFilter} is
   * on the classpath of the passed Eclipse Java project.
   * 
   * @param javaProject
   *          an Eclipse Java project.
   * @param matcher
   *          an implementation of {@link IPathFilter}.
   * @return {@code true} if a match is found on the classpath, {@code false}
   *         otherwise.
   * 
   * @see IPathFilter
   */
  public static boolean isOnClasspath(IJavaProject javaProject, IPathFilter matcher) {
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
   * Gets a reference to the Eclipse log file. This file is under the workspace
   * in <tt>.metadata/.log</tt>.
   * <p>
   * It is possible that this file may not exist. This is the case if the user
   * deletes the log entries from the <b>Error Log</b> view. If this occurs a
   * warning is logged.
   * 
   * @return a reference to the Eclipse log file.
   */
  public static File getEclipseLogFile() {
    final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    final IPath log = wsRoot.getLocation().addTrailingSeparator().append(".metadata/.log");
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
   *          a Java project.
   * @return the source-level Java version.
   */
  public static String getJavaSourceVersion(final IJavaProject jp) {
    final String javaVersion = jp.getOption("org.eclipse.jdt.core.compiler.source", true);
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
   * Gets the {@link ICompilationUnit} associated with the passed information or
   * {@code null} if neither can be found.
   * 
   * @param projectName
   *          the project name the element is contained within. For example,
   *          <code>JEdit</code>.
   * @param packageName
   *          the package name the element is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @return the {@link ICompilationUnit} associated with the passed information
   *         or {@code null} if neither can be found.
   */
  public static ICompilationUnit findICompilationUnit(final String projectName, final String packageName, final String typeName) {
    final IType type = findIType(projectName, packageName, typeName);
    if (type != null) {
      final ICompilationUnit element = type.getCompilationUnit();
      return element;
    } else {
      return null;
    }
  }

  /**
   * Gets the enclosing {@link ICompilationUnit} of the passed Java element.
   * 
   * @param e
   *          a Java element.
   * @return a {@link ICompilationUnit}, or {@code null} if the Java element is
   *         not in a <tt>.java</tt> file.
   */
  @Nullable
  public static ICompilationUnit getEnclosingICompilationUnitOrNull(IJavaElement e) {
    while (e != null) {
      if (e instanceof ICompilationUnit)
        return (ICompilationUnit) e;
      e = e.getParent();
    }
    return null;
  }

  /**
   * Gets the enclosing {@link IClassFile} of the passed Java element.
   * 
   * @param e
   *          a Java element.
   * @return a {@link IClassFile}, or {@code null} if the Java element is not in
   *         a <tt>.class</tt> file.
   */
  @Nullable
  public static IClassFile getEnclosingIClassFileOrNull(IJavaElement e) {
    while (e != null) {
      if (e instanceof IClassFile)
        return (IClassFile) e;
      e = e.getParent();
    }
    return null;
  }

  /**
   * Gets the {@link IType} element associated with the passed information or
   * {@code null} if it cannot be found.
   * 
   * @param projectName
   *          the project name the element is contained within. For example,
   *          <code>JEdit</code>. If null, or starts with
   *          {@link SLUtility#LIBRARY_PROJECT}, try all projects
   * @param packageName
   *          the package name the element is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          {@link SLUtility#JAVA_DEFAULT_PACKAGE} or null then the class is
   *          contained within the default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @return the Java element associated with the passed information or
   *         {@code null} if no Java element can be found.
   */
  public static IType findIType(final String projectName, final String packageName, final String typeName) {
    try {
      final int occurrenceCount = getAnonymousOccurrenceCount(typeName);
      final String baseTypeName = stripOffAnonymousOccurranceCount(typeName);
      final String className = baseTypeName.replace("$", ".");

      final boolean searchAllProjects = projectName == null || projectName.startsWith(SLUtility.LIBRARY_PROJECT);
      final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
      final IJavaModel model = JavaCore.create(wsRoot);
      if (model != null) {
        for (final IJavaProject project : model.getJavaProjects()) {
          if (searchAllProjects || projectName.equals(project.getElementName())) {
            String packageNameHolder = null;
            if (!(packageName == null) && !packageName.equals(SLUtility.JAVA_DEFAULT_PACKAGE)) {
              packageNameHolder = packageName;
            }
            final IType type = project.findType(packageNameHolder, className, new NullProgressMonitor());

            if (type != null && type.exists()) {
              if (occurrenceCount > 0) {
                return lookupAnonymous(type, occurrenceCount);
              } else {
                return type;
              }
            }
          }
        }
      }
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(135, packageName, typeName, projectName), e);
    }
    return null;
  }

  /**
   * Tries to find the corresponding {@link IJavaElement} to the passed
   * {@link IJavaRef}.
   * 
   * @param javaRef
   *          a Java code reference.
   * @return the corresponding {@link IJavaElement} or {@code null} if none can
   *         be found.
   * 
   * @see #findJavaElement(IJavaRef)
   */
  @Nullable
  public static IJavaElement findJavaElementOrNull(final IJavaRef javaRef) {
    return findJavaElement(javaRef).first();
  }

  /**
   * Tries to find the corresponding {@link IJavaElement} to the passed
   * {@link IJavaRef}. The Eclipse Java element is returned if it can be found,
   * and a confidence from 1 to 0 is found, where 1 indicates a perfect match
   * and 0 indicates no match.
   * 
   * @param javaRef
   *          a Java code reference.
   * @return a pair: (first) the corresponding {@link IJavaElement} or
   *         {@code null}, (second) the confidence of the match [1,0] where
   *         higher is better.
   */
  @NonNull
  public static Pair<IJavaElement, Double> findJavaElement(final IJavaRef javaRef) {
    if (javaRef == null)
      return new Pair<IJavaElement, Double>(null, Double.valueOf(0));
    System.out.println("findJavaElementOrNull(" + javaRef + ")");

    final IDecl decl = javaRef.getDeclaration();

    String projectName = javaRef.getEclipseProjectNameOrNull();
    final boolean searchAllProjects = projectName == null || projectName.startsWith(SLUtility.LIBRARY_PROJECT);
    if (searchAllProjects)
      projectName = null;

    try {
      double confidence = 0;
      IJavaElement best = null;
      List<IPackageFragment> pkgs = null;
      for (final IJavaProject prj : getProjectsToSearchByName(projectName)) {
        JavaElementMatcher matcher = new JavaElementMatcher(prj);
        decl.acceptRootToThis(matcher);
        final IJavaElement matcherResult = matcher.getResult();
        if (matcherResult != prj) {
          final double matcherConfidence = matcher.getConfidence();
          if (matcherConfidence > confidence) {
            best = matcherResult;
            confidence = matcherConfidence;
            pkgs = matcher.getPackageFragments();
          }
        }
      }
      if (best == null)
        return new Pair<IJavaElement, Double>(null, Double.valueOf(0));
      /*
       * Special case for package-info files (a better answer).
       */
      if (decl.getKind() == IDecl.Kind.PACKAGE && best instanceof IPackageFragment) {
        for (IPackageFragment pkg : pkgs) {
          final ICompilationUnit cu = pkg.getCompilationUnit(SLUtility.PACKAGE_INFO + ".java");
          if (cu != null && cu.exists())
            return new Pair<IJavaElement, Double>(cu, Double.valueOf(1)); // package-info.java
          final IClassFile cf = pkg.getClassFile(SLUtility.PACKAGE_INFO + ".class");
          if (cf != null && cf.exists())
            return new Pair<IJavaElement, Double>(cf, Double.valueOf(1)); // package-info.class
        }
      }
      System.out.println(" found (" + confidence + ") -> " + best);
      return new Pair<IJavaElement, Double>(best, Double.valueOf(confidence));
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.WARNING, I18N.err(156, javaRef), e);
    }
    return new Pair<IJavaElement, Double>(null, Double.valueOf(0));
  }

  /**
   * This class matches up an {@link IDecl} to a corresponding
   * {@link IJavaElement}, whenever possible.
   * <p>
   * Always use {@link IDecl#acceptRootToThis(DeclVisitor)} for this visitor.
   */
  private static class JavaElementMatcher extends DeclVisitor {

    @NonNull
    IJavaElement current;
    /**
     * Count of things we found.
     */
    int foundCount = 0;
    /**
     * Count of things we looked for.
     */
    int lookedForCount = 0;
    /**
     * We need this because the package can be under multiple package fragment
     * roots.
     */
    @NonNull
    List<IPackageFragment> packageMatches = new ArrayList<IPackageFragment>();

    public JavaElementMatcher(IJavaProject projectToSearch) {
      current = projectToSearch;
    }

    /**
     * Gets the result&mdash;an Eclipse Java element.
     * <p>
     * If the result is a {@link IPackageFragment} use
     * {@link #getPackageFragments()} to get all matches to examine (this method
     * returns the last found). There can be more than one location in an
     * Eclipse project where the package exists (e.g., a src and test source
     * folder that have the same packages).
     * 
     * @return an Eclipse Java element.
     */
    IJavaElement getResult() {
      return current;
    }

    /**
     * A list of all package fragments found. There can be more than one
     * location in an Eclipse project where the package exists (e.g., a src and
     * test source folder that have the same packages).
     * 
     * @return a list of package fragments.
     */
    List<IPackageFragment> getPackageFragments() {
      return packageMatches;
    }

    /**
     * A numerical confidence about the result of this search. Greater is
     * better.
     * <p>
     * It is better to test that the {@link #getResult()} ==
     * <tt>projectToSearch</tt> (passed to
     * {@link #JavaElementMatcher(IJavaProject)}) than to check if this method
     * returns 0
     * 
     * @return a numerical confidence about the result of this search. Greater
     *         is better.
     */
    double getConfidence() {
      return (double) foundCount / (double) lookedForCount;
    }

    @Override
    public void visitPackage(IDeclPackage node) {
      // for a IPackageFragment the default package is ""
      String nodePkgName = node.getName().equals(SLUtility.JAVA_DEFAULT_PACKAGE) ? "" : node.getName();
      lookedForCount++;
      boolean found = false;
      for (IJavaElement je : getChildren(current)) {
        if (je instanceof IPackageFragment) {
          if (nodePkgName.equals(je.getElementName())) {
            packageMatches.add((IPackageFragment) je);
            current = je; // the last found
            found = true;
          }
        }
      }
      if (found)
        foundCount++;
    }

    @Override
    public boolean visitTypes(List<IDeclType> types) {
      for (IDeclType type : types) {
        lookedForCount++;
        if (current.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
          // we have to look in all packages found for a top-level type
          for (IPackageFragment pkg : packageMatches) {
            if (matchType(type, pkg)) {
              foundCount++;
              continue;
            }
          }
        } else {
          if (matchType(type, current))
            foundCount++;
        }
      }
      return false;
    }

    boolean matchType(IDeclType node, IJavaElement parent) {
      int anonymousDeclCount = 0;
      for (IJavaElement ije : getChildren(parent)) {
        if (ije instanceof IType) {
          final IType it = (IType) ije;
          try {
            if (it.isAnonymous() && node.getKind() == IDecl.Kind.CLASS && node.getVisibility() == IDecl.Visibility.ANONYMOUS) {
              if (node.getPosition() == anonymousDeclCount) {
                current = it;
                return true;
              }
              anonymousDeclCount++;
              continue;
            }
            if (it.isClass() && node.getKind() == IDecl.Kind.CLASS || it.isInterface() && node.getKind() == IDecl.Kind.INTERFACE
                || it.isEnum() && node.getKind() == IDecl.Kind.ENUM || it.isAnnotation() && node.getKind() == IDecl.Kind.ANNOTATION) {
              if (node.getName().equals(it.getElementName())) {
                current = it;
                return true;
              }
            }
          } catch (JavaModelException ex) {
            SLLogger.getLogger().log(Level.WARNING, I18N.err(290, node), ex);
          }
        }
      }
      return false; // no match found
    }

    @Override
    public void visitField(IDeclField node) {
      lookedForCount++;
      for (IJavaElement je : getChildren(current)) {
        if (je.getElementType() == IJavaElement.FIELD) {
          if (node.getName().equals(je.getElementName())) {
            foundCount++;
            current = je;
            return;
          }
        }
      }
    }

    @Override
    public void visitInitializer(IDecl node) {
      lookedForCount++;
      for (IJavaElement ije : getChildren(current)) {
        if (ije instanceof IInitializer) {
          final IInitializer init = (IInitializer) ije;
          try {
            boolean isStatic = Flags.isStatic(init.getFlags());
            if (node.isStatic() == isStatic) {
              foundCount++;
              current = init;
              return;
            }
          } catch (JavaModelException ex) {
            SLLogger.getLogger().log(Level.WARNING, I18N.err(290, node), ex);
          }
        }
      }
    }

    @Override
    public boolean visitMethod(IDeclFunction node) {
      lookedForCount++;
      for (IJavaElement ije : getChildren(current)) {
        if (ije instanceof IMethod) {
          final IMethod im = (IMethod) ije;
          if (node.getName().equals(im.getElementName())) {
            final String[] paramTypes = im.getParameterTypes();
            final List<IDeclParameter> nodeParams = node.getParameters();
            if (nodeParams.size() == paramTypes.length) {
              boolean matches = true;
              for (int i = 0; i < paramTypes.length; i++) {
                final String eclipseTypeSig = paramTypes[i];
                final String imType = Signature.getSignatureSimpleName(eclipseTypeSig);
                // heuristic match (not exact with arrays at the very least)
                if (!nodeParams.get(i).getTypeOf().getFullyQualified().contains(imType)) {
                  matches = false;
                  break;
                }
              }
              if (matches) {
                foundCount++;
                current = im;
                return false;
              }
            }
          }
        }
      }
      return false;
    }

    @Override
    public boolean visitConstructor(IDeclFunction node) {
      lookedForCount++;
      for (IJavaElement ije : getChildren(current)) {
        if (ije instanceof IMethod) {
          final IMethod im = (IMethod) ije;
          final String[] paramTypes = im.getParameterTypes();
          final List<IDeclParameter> nodeParams = node.getParameters();
          if (nodeParams.size() == paramTypes.length) {
            boolean matches = true;
            for (int i = 0; i < paramTypes.length; i++) {
              final String eclipseTypeSig = paramTypes[i];
              final String imType = Signature.getSignatureSimpleName(eclipseTypeSig);
              // heuristic match (not exact with arrays at the very least)
              if (!nodeParams.get(i).getTypeOf().getFullyQualified().contains(imType)) {
                matches = false;
                break;
              }
            }
            if (matches) {
              foundCount++;
              current = im;
              return false;
            }
          }
        }
      }
      return false;
    }

    @Override
    public void visitParameter(IDeclParameter node, boolean partOfDecl) {
      if (!partOfDecl)
        return;
      lookedForCount++;
      if (current instanceof IMethod) {
        ILocalVariable[] params = getParameters((IMethod) current);
        if (params.length > node.getPosition()) {
          foundCount++;
          current = params[node.getPosition()];
          return;
        }
      }
    }

    /**
     * <b>Remove this method when we support Eclipse 3.7 and above.</b>
     * <p>
     * This is a hack to get the parameters of the passed method. The
     * <tt>getParameters()</tt> method was not added to Eclipse until the 3.7
     * release.
     * 
     * @param m
     *          an Eclipse Java model method
     * @return the method's parameters, or an empty array if none or the method
     *         call doesn't exist in the Eclipse we are running within.
     */
    private ILocalVariable[] getParameters(IMethod m) {
      ILocalVariable[] result = new ILocalVariable[0];
      try {
        Class<?> c = Class.forName("org.eclipse.jdt.core.IMethod");
        if (c.isInstance(m)) {
          Method gp = c.getDeclaredMethod("getParameters");
          result = (ILocalVariable[]) gp.invoke(m);
        }
      } catch (Exception probablyInEclipseLessThan37) {
        // ignore
      }
      return result;
    }

    @Override
    public void visitTypeParameter(IDeclTypeParameter node, boolean partOfDecl) {
      if (!partOfDecl)
        return;
      try {
        lookedForCount++;
        ITypeParameter[] typeParams = null;
        if (current instanceof IType) {
          typeParams = ((IType) current).getTypeParameters();
        }
        if (current instanceof IMethod) {
          typeParams = ((IMethod) current).getTypeParameters();
        }
        if (typeParams == null)
          return;
        if (typeParams.length > node.getPosition()) {
          foundCount++;
          current = typeParams[node.getPosition()];
          return;
        }
      } catch (JavaModelException ex) {
        SLLogger.getLogger().log(Level.WARNING, I18N.err(290, node), ex);
      }
    }

    List<IJavaElement> getChildren(IJavaElement e) {
      List<IJavaElement> result = new ArrayList<IJavaElement>();
      try {
        if (e instanceof IParent) {
          for (IJavaElement child : ((IParent) e).getChildren()) {
            if (instanceOfInterestingType(child))
              result.add(child);
            else
              result.addAll(getChildren(child));
          }
        }
      } catch (JavaModelException ex) {
        SLLogger.getLogger().log(Level.WARNING, I18N.err(290, e), ex);
      }
      return result;
    }

    boolean instanceOfInterestingType(IJavaElement e) {
      return e instanceof IPackageFragment || e instanceof IType || e instanceof IField || e instanceof IMethod
          || e instanceof IInitializer;
    }
  }

  /**
   * Gets a set of Java projects filtered by a name.
   * 
   * @param projectNameOrNull
   *          a project name to match, or {@code null} to search all projects.
   * @return a possibly empty set of Java projects.
   * @throws JavaModelException
   *           if something goes wrong.
   */
  private static List<IJavaProject> getProjectsToSearchByName(@Nullable final String projectNameOrNull) throws JavaModelException {
    final List<IJavaProject> result = new ArrayList<IJavaProject>();
    final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    final IJavaModel model = JavaCore.create(wsRoot);
    if (model != null) {
      for (final IJavaProject project : model.getJavaProjects()) {
        if (projectNameOrNull == null || project.getElementName().equals(projectNameOrNull)) {
          result.add(project);
        }
      }
    }
    return result;
  }

  private static final Pattern ANON = Pattern.compile("\\$\\d+");

  /**
   * Determines the occurrence count from an anonymous type name. For example,
   * the type {@code Hello$1} would return 1.
   * <p>
   * This method assumes that {@code $} is used as the separator.
   * 
   * @param typeName
   *          the type name.
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
   *          the type name.
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
   *          the type to search.
   * @param occurrenceCount
   *          the position of the anonymous class.
   * @return the anonymous class or, <tt>within</tt> if not found.
   * @throws JavaModelException
   *           if something goes wrong.
   */
  private static IType lookupAnonymous(final IType within, final int occurrenceCount) throws JavaModelException {
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
   *          the type to search.
   * @param mutableList
   *          a mutable list to add discovered anonymous classes to.
   * @throws JavaModelException
   *           if something goes wrong.
   */
  private static void queryListOfAnonymous(final IJavaElement within, final List<IType> mutableList) throws JavaModelException {
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
        return o1.getElementName().compareToIgnoreCase(o2.getElementName());
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
   *          a project name.
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

  private static boolean noCompilationErrors(final IResource resource, final IProgressMonitor monitor) throws CoreException {
    final IMarker[] problems = resource.findMarkers(
    // IMarker.PROBLEM,
        IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

    // check if any of these have a severity attribute that indicates an
    // error
    for (final IMarker marker : problems) {
      if (monitor.isCanceled()) {
        return false;
      }
      if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO)) {
        final Logger log = SLLogger.getLogger();
        log.info("***** MARKER Message: " + marker.getAttribute(IMarker.MESSAGE));
        log.info("***** MARKER Line #: " + marker.getAttribute(IMarker.LINE_NUMBER));
        log.info("***** MARKER File: " + marker.getAttribute(IMarker.LOCATION));
        log.info("***** MARKER Message: " + marker.getAttribute(IMarker.MESSAGE));
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
   *          the {@link IJavaProject}to check for errors
   * @param monitor
   * @return <code>true</code> if the project has no compilation errors,
   *         <code>false</code> if errors exist or the project has never been
   *         built
   * @throws CoreException
   *           if we have trouble getting the project's {@link IMarker}list
   */
  public static boolean noCompilationErrors(final IJavaProject javaProject, final IProgressMonitor monitor) throws CoreException {
    if (javaProject.hasBuildState()) {
      return noCompilationErrors(javaProject.getProject(), monitor);
    } else if (hasNoSource(javaProject)) {
      return true;
    } else {
      SLLogger.getLogger().warning(I18N.err(83, javaProject.getElementName()));
    }
    return false;
  }

  /**
   * Checks if the passed project has any source code on its classpath.
   * 
   * @param p
   *          a Java project.
   * @return {@code true} if the passed project has any source code on its
   *         classpath, {@code false} otherwise.
   * @throws JavaModelException
   *           if something goes wrong.
   */
  private static boolean hasNoSource(final IJavaProject p) throws JavaModelException {
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
   *          a compilation unit.
   * @return {@code true} if the passed compilation unit contains no compilation
   *         errors, {@code false} otherwise.
   * @throws CoreException
   *           if something goes wrong.
   */
  public static boolean noCompilationErrors(final ICompilationUnit cu, final IProgressMonitor monitor) throws CoreException {
    final boolean result = false; // assume it has errors or is not built
    final IJavaProject javaProject = cu.getJavaProject();
    if (javaProject.hasBuildState()) {
      return noCompilationErrors(cu.getCorrespondingResource(), monitor);
    } else {
      SLLogger.getLogger().warning(I18N.err(84, javaProject.getElementName(), cu.toString()));
    }
    return result;
  }

  /**
   * Checks if the passed set contains no compilation errors.
   * 
   * @param <T>
   *          either a {@link IJavaProject} or an {@link ICompilationUnit}.
   * @param elements
   *          to search for compilation errors.
   * @return {@code true} if the passed collection contains no compilation
   *         errors, {@code false} otherwise.
   * @throws CoreException
   *           if something goes wrong.
   */
  public static <T extends IJavaElement> boolean noCompilationErrors(final Iterable<T> elements, final IProgressMonitor monitor)
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
   *          either a {@link IJavaProject} or an {@link ICompilationUnit}.
   * @param elements
   *          to search for compilation errors.
   * @return a (possibly empty) collection of compilation error messages.
   * @throws CoreException
   *           if something goes wrong.
   */
  public static <T extends IJavaElement> Collection<String> findCompilationErrors(final Collection<T> elements,
      final IProgressMonitor monitor) throws CoreException {
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
      final Map<IJavaProject, Boolean> status = projectsUpToDate(root, projects);
      return status;
    } catch (final JavaModelException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Got exception while checking if projects are up to date", e);
      return null;
    }
  }

  private static Map<IJavaProject, Boolean> projectsUpToDate(final IWorkspaceRoot root, final IJavaProject[] projects)
      throws JavaModelException {
    final Map<IJavaProject, Boolean> status = new HashMap<IJavaProject, Boolean>();
    for (final IJavaProject p : projects) {
      status.put(p, null);
    }
    projectsUpToDate(root, status);
    return status;
  }

  private static Map<IJavaProject, Boolean> projectsUpToDate(final IWorkspaceRoot root, final Iterable<IJavaProject> projects)
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
  public static boolean projectsUpToDate(final Collection<IJavaProject> projects) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    try {
      final Map<IJavaProject, Boolean> status = projectsUpToDate(root, projects);
      for (final IJavaProject p : projects) {
        final Boolean b = status.get(p);
        if (b == null || !b.booleanValue()) {
          return false;
        }
      }
      return true;
    } catch (final JavaModelException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Got exception while checking if projects are up to date", e);
      return false;
    }
  }

  private static void projectsUpToDate(final IWorkspaceRoot root, final Map<IJavaProject, Boolean> status)
      throws JavaModelException {
    final List<IJavaProject> projs = new ArrayList<IJavaProject>(status.size());
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
  private static boolean checkProject(final IWorkspaceRoot root, final Map<IJavaProject, Boolean> status, final IJavaProject p,
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
            final boolean built = checkProject(root, status, jp, checker);
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
    boolean check(IWorkspaceRoot root, IJavaProject p, IClasspathEntry cpe) throws JavaModelException;

    boolean checkProjectDependencies(IJavaProject p);
  }

  static abstract class AbstractChecker implements IProjectChecker {
    protected void init(final IWorkspaceRoot root, final IJavaProject p, final IClasspathEntry cpe) {
      // Do nothing
    }

    public boolean check(final IWorkspaceRoot root, final IJavaProject p, final IClasspathEntry cpe) throws JavaModelException {
      init(root, p, cpe);

      IPath out = cpe.getOutputLocation();
      if (out == null) {
        out = p.getOutputLocation();
      }
      for (final IPackageFragmentRoot pkgRoot : p.findPackageFragmentRoots(cpe)) {
        for (final IJavaElement e : pkgRoot.getChildren()) {
          final IPackageFragment pkg = (IPackageFragment) e;
          if (stopProcessing()) {
            return false;
          }
          for (final ICompilationUnit cu : pkg.getCompilationUnits()) {
            final long cuTime = cu.getCorrespondingResource().getLocalTimeStamp();
            if (!processTypes(cu, cuTime)) {
              continue;
            }
            for (final IType t : cu.getTypes()) {
              final String name = t.getFullyQualifiedName().replace('.', '/') + ".class";
              final IPath path = out.append(name);
              final long clTime = root.getFile(path).getLocalTimeStamp();
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
    protected boolean processTypes(final ICompilationUnit cu, final long cuTime) {
      return true;
    }

    /**
     * Compare timestamps with its class files
     */
    protected boolean check(final ICompilationUnit cu, final long cuTime, final long clTime) {
      return true;
    }
  }

  static class AllChecker extends AbstractChecker {
    long dotClassPathTime;
    long dotProjectTime;

    @Override
    protected void init(final IWorkspaceRoot root, final IJavaProject jp, final IClasspathEntry cpe) {
      final IProject p = jp.getProject();
      dotClassPathTime = p.getFile(".classpath").getLocalTimeStamp();
      dotProjectTime = p.getFile(".project").getLocalTimeStamp();
    }

    @Override
    protected boolean check(final ICompilationUnit cu, final long cuTime, final long clTime) {
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

  public static boolean compUnitsUpToDate(final Collection<ICompilationUnit> elements) {
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
    public boolean check(final IWorkspaceRoot root, final IJavaProject p, final IClasspathEntry cpe) throws JavaModelException {
      final Date date = cutoffs.get(p);
      if (date == null) {
        SLLogger.getLogger().severe("No last-scan time found for " + p.getElementType());
        return true;
      }
      cutoff = date.getTime();
      return super.check(root, p, cpe);
    }

    public boolean checkProjectDependencies(final IJavaProject p) {
      return false;
    }

    @Override
    protected boolean processTypes(final ICompilationUnit cu, final long cuTime) {
      if (cuTime > cutoff) {
        units.add(cu);
        SLLogger.getLogger().fine(cu.getElementName() + " is newer than cutoff: " + units.size());
        return false;
      }
      return true;
    }

    @Override
    protected boolean check(final ICompilationUnit cu, final long cuTime, final long clTime) {
      if (clTime > cutoff) {
        units.add(cu);
        SLLogger.getLogger().fine(cu.getElementName() + " is newer than cutoff: " + units.size());
      }
      return true;
    }

    public Collection<ICompilationUnit> list() {
      return units;
    }
  }

  public static Collection<ICompilationUnit> modifiedCompUnits(final Map<IJavaProject, Date> times, final IProgressMonitor monitor) {
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
      SLLogger.getLogger().log(Level.SEVERE, "Got exception while looking for modified comp units", e);
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
  public static boolean projectUpdatedSince(final IJavaProject jp, final long time) {
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
        protected boolean processTypes(final ICompilationUnit cu, final long cuTime) {
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

  private static void collectAllResources(JavaProjectResources jpr, IContainer p) throws CoreException {
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

  /**
   * Recursively compute the set of projects and their dependent projects
   * 
   * @throws JavaModelException
   */
  public static Collection<IJavaProject> getAllRequiredProjects(List<IJavaProject> selected) {
    final Set<IJavaProject> required = new HashSet<IJavaProject>();
    for (IJavaProject p : selected) {
      getAllRequiredProjects(required, p);
    }
    return required;
  }

  private static void getAllRequiredProjects(Set<IJavaProject> collected, IJavaProject p) {
    if (p == null) {
      return;
    }
    if (collected.contains(p)) {
      return; // Done with this project
    }
    collected.add(p);

    try {
      for (IClasspathEntry cpe : p.getResolvedClasspath(true)) {
        if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
          final String projName = cpe.getPath().lastSegment();
          IJavaProject dependency = getJavaProject(projName);
          if (dependency == null) {
            throw new IllegalStateException("Unable to find project '" + projName + "'required by " + p.getElementName());
          }
          getAllRequiredProjects(collected, dependency);
        }
      }
    } catch (JavaModelException e) {
      throw new IllegalStateException("Could not resolve classpath for " + p.getElementName());
    }
  }

  private JDTUtility() {
    // utility
  }

}
