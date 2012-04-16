package com.surelogic.common.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.i18n.JavaSourceReference;
import com.surelogic.common.logging.SLLogger;

public class JDTUIUtility {
	private static final Method openInEditorMethod;

	static {
		Method m = null;
		try {
			m = JavaUI.class.getMethod("openInEditor", new Class[] {
					IJavaElement.class, boolean.class, boolean.class });
		} catch (final SecurityException e) {
			m = null;
		} catch (final NoSuchMethodException e) {
			m = null;
		}
		openInEditorMethod = m;
	}

	/**
	 * Uses reflection to use the JavaUI method in Eclipse 3.3 to try and open
	 * the {@link IJavaElement} in the editor. The new method allows us to
	 * control if the editor is activated and/or revealed.
	 * <p>
	 * Opens an editor on the given Java element in the active page. Valid
	 * elements are all Java elements that are {@link ISourceReference}. For
	 * elements inside a compilation unit or class file, the parent is opened in
	 * the editor is opened and the element revealed. If there already is an
	 * open Java editor for the given element, it is returned.
	 * 
	 * @param element
	 *            the Java element to open.
	 * @param activate
	 *            if set, the editor will be activated.
	 * @param reveal
	 *            if set, the element will be revealed.
	 */
	public static IEditorPart openInEditor(final IJavaElement element,
			final boolean activate, final boolean reveal) {
		try {
			if (openInEditorMethod != null) {
				final Object rv = openInEditorMethod.invoke(null, new Object[] {
						element, activate, reveal });
				return (IEditorPart) rv;
			} else {
				return JavaUI.openInEditor(element);
			}
		} catch (final CoreException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(161, element), e);
		} catch (final IllegalArgumentException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(161, element), e);
		} catch (final IllegalAccessException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(161, element), e);
		} catch (final InvocationTargetException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(161, element), e);
		}
		return null;
	}

	/**
	 * Tries to open the specified Java element in the editor and highlight the
	 * given line number. If the line number is 0 then the entire passed Java
	 * element is highlighted.
	 * 
	 * @param element
	 *            a Java element, may be {@code null}.
	 * @param lineNumber
	 *            the line number to highlight, or the passed Java element if
	 *            {@code lineNumber <= 0}.
	 * @return @return {@code true} if it was possible to open an editor,
	 *         {@code false} otherwise. A result of {@code false} might indicate
	 *         that the file was opened in an external editor.
	 */
	public static boolean tryToOpenInEditor(final IJavaElement element,
			final int lineNumber) {
		if (element instanceof ISourceReference) {
			try {
				final IEditorPart editorPart = openInEditor(element, false,
						true);
				if (lineNumber > 1) { // only move if not the first line
					final IMarker location = ResourcesPlugin.getWorkspace()
							.getRoot().createMarker("com.surelogic.sierra");
					if (location != null) {
						location.setAttribute(IMarker.LINE_NUMBER, lineNumber);
						IDE.gotoMarker(editorPart, location);
						location.delete();
						return true;
					}
				} else {
					return editorPart != null;
				}
			} catch (final Exception e) {
				SLLogger.getLogger().log(Level.SEVERE,
						I18N.err(132, element, lineNumber), e);
			}
		}
		return false;
	}

	/**
	 * Tries to open the specified Java file in the editor and highlight the
	 * given line number. If the line number is 0 then the entire passed Java
	 * element is highlighted.
	 * 
	 * @param projectName
	 *            the project name the file is contained within. For example,
	 *            <code>JEdit</code>.
	 * @param packageName
	 *            the package name the file is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @param lineNumber
	 *            the line number to highlight, or 0.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditor(final String projectName,
			final String packageName, final String typeName,
			final int lineNumber) {
		final IType element = JDTUtility.findIType(projectName, packageName,
				typeName);
		return tryToOpenInEditor(element, lineNumber);
	}

	/**
	 * Tries to open the specified Java file in the editor and highlight the
	 * passed Java type.
	 * 
	 * @param projectName
	 *            the project name the file is contained within. For example,
	 *            <code>JEdit</code>.
	 * @param packageName
	 *            the package name the file is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditor(final String projectName,
			final String packageName, final String typeName) {
		return tryToOpenInEditor(projectName, packageName, typeName, 0);
	}

	/**
	 * Tries to open the specified Java file in the editor and highlight the
	 * passed Java type.
	 * <p>
	 * This method tries to find a match in all the open Java projects.
	 * 
	 * @param packageName
	 *            the package name the file is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditor(final String packageName,
			final String typeName, final int lineNumber) {
		for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
			if (tryToOpenInEditor(javaProjectName, packageName, typeName,
					lineNumber)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to open the specified Java file in the editor and highlight the
	 * given line number. If the line number is 0 then the file is opened
	 * highlighting the first line in the file.
	 * <p>
	 * This method tries to find a match in all the open Java projects.
	 * 
	 * @param packageName
	 *            the package name the file is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditor(final String packageName,
			final String typeName) {
		for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
			if (tryToOpenInEditor(javaProjectName, packageName, typeName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to open an editor and highlight declaration of the given field
	 * name.
	 * 
	 * @param projectName
	 *            the project name the field is contained within. For example,
	 *            <code>JEdit</code>.
	 * @param packageName
	 *            the package name the field is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @param fieldName
	 *            the name of the field.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditorUsingFieldName(
			final String projectName, final String packageName,
			final String typeName, final String fieldName) {
		final IType element = JDTUtility.findIType(projectName, packageName,
				typeName);
		if (element != null && element.exists()) {
			try {
				for (final IField field : element.getFields()) {
					if (fieldName.equals(field.getElementName())) {
						openInEditor(field, false, true);
						return true;
					}
				}
			} catch (final Exception e) {
				SLLogger.getLogger().log(
						Level.SEVERE,
						I18N.err(136, fieldName, packageName, typeName,
								projectName), e);
			}
		}
		return false;
	}

	/**
	 * Tries to open an editor and highlight declaration of the given method
	 * name. This method can be inaccurate if the targeted type contains
	 * overloaded methods or lots of constructors.
	 * 
	 * @param projectName
	 *            the project name the method is contained within. For example,
	 *            <code>JEdit</code>.
	 * @param packageName
	 *            the package name the method is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @param methodName
	 *            the name of the method.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditorUsingMethodName(
			final String projectName, final String packageName,
			final String typeName, final String methodName) {
		final IType element = JDTUtility.findIType(projectName, packageName,
				typeName);
		if (element != null && element.exists()) {
			try {
				if (methodName.equalsIgnoreCase("<init>")) {
					/*
					 * Just open up the first constructor that we find.
					 */
					for (final IMethod method : element.getMethods()) {
						if (method.isConstructor()) {
							openInEditor(method, false, true);
							return true;
						}
					}
				}
				/*
				 * Open up the first method we find with the right name. This
				 * could be wrong if there are overloaded methods in the class.
				 */
				for (final IMethod method : element.getMethods()) {
					if (methodName.equals(method.getElementName())) {
						openInEditor(method, false, true);
						return true;
					}
				}
			} catch (final Exception e) {
				SLLogger.getLogger().log(
						Level.SEVERE,
						I18N.err(217, methodName, packageName, typeName,
								projectName), e);
			}
		}
		return false;
	}

	/**
	 * Tries to open the specified Java source reference in the editor and
	 * highlight the given line number. If the line number is unknown then the
	 * file is opened highlighting the first line in the file.
	 * 
	 * @param srcRef
	 *            a Java source reference.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditor(final JavaSourceReference srcRef) {
		final String projectName = srcRef.getProjectName();
		if (projectName == null) {
			return tryToOpenInEditor(projectName, srcRef.getPackageName(),
					srcRef.getTypeName(), srcRef.getLineNumber());
		} else {
			return tryToOpenInEditor(srcRef.getPackageName(),
					srcRef.getTypeName(), srcRef.getLineNumber());
		}
	}

	/**
	 * Tries to open an editor and highlight declaration of the given field
	 * name.
	 * <p>
	 * This method tries to find a match in all the open Java projects.
	 * 
	 * @param packageName
	 *            the package name the field is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @param fieldName
	 *            the name of the field.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditorUsingFieldName(
			final String packageName, final String typeName,
			final String fieldName) {
		for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
			if (tryToOpenInEditorUsingFieldName(javaProjectName, packageName,
					typeName, fieldName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to open an editor and highlight declaration of the given method
	 * name. This method can be inaccurate if the targeted type contains
	 * overloaded methods or lots of constructors.
	 * <p>
	 * This method tries to find a match in all the open Java projects.
	 * 
	 * @param packageName
	 *            the package name the method is contained within. For example,
	 *            <code>com.surelogic.sierra</code>. the package name is
	 *            "(default package)" or null then the class is contained within
	 *            the default package.
	 * @param typeName
	 *            the type name, this type may be a nested type of the form
	 *            <code>Outer$Inner</code> or
	 *            <code>Outer$Inner$InnerInner</code> or
	 *            <code>Outer.Inner</code> or
	 *            <code>Outer.Inner.InnerInner</code> (to any depth).
	 * @param methodName
	 *            the name of the method.
	 * @return {@code true} if it was possible to open the editor, {@code false}
	 *         otherwise.
	 */
	public static boolean tryToOpenInEditorUsingMethodName(
			final String packageName, final String typeName,
			final String methodName) {
		for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
			if (tryToOpenInEditorUsingMethodName(javaProjectName, packageName,
					typeName, methodName)) {
				return true;
			}
		}
		return false;
	}
}
