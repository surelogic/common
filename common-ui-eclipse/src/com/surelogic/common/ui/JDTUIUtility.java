package com.surelogic.common.ui;

import java.util.logging.Level;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.SLUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.i18n.JavaSourceReference;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ref.IJavaRef;

public class JDTUIUtility {

  /**
   * Tries to open the specified Java element in the editor and highlight the
   * given line number. If the line number is 0 then the entire passed Java
   * element is highlighted.
   * 
   * @param element
   *          a Java element, may be {@code null}.
   * @param lineNumber
   *          the line number to highlight, or the passed Java element if
   *          {@code lineNumber <= 0}.
   * @return {@code true} if it was possible to open an editor, {@code false}
   *         otherwise. A result of {@code false} might indicate that the file
   *         was opened in an external editor.
   */
  public static boolean tryToOpenInEditor(final IJavaElement element, final int lineNumber) {
    if (element instanceof ISourceReference) {
      try {
        final IEditorPart editorPart = JavaUI.openInEditor(element, false, true);
        if (lineNumber > 1) { // only move if not the first line
          final IMarker location = ResourcesPlugin.getWorkspace().getRoot().createMarker(SLUtility.ECLIPSE_MARKER_TYPE_NAME);
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
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(132, element, "line " + lineNumber), e);
      }
    }
    return false;
  }

  /**
   * Tries to open the specified Java code reference in the editor and highlight
   * the referenced code.
   * 
   * @param javaRef
   *          a location in Java code.
   * @return {@code true} if it was possible to open an editor, {@code false}
   *         otherwise. A result of {@code false} might indicate that the file
   *         was opened in an external editor.
   */
  public static boolean tryToOpenInEditor(final IJavaRef javaRef) {
    if (javaRef == null)
      return false;

    final String typeName = javaRef.getTypeNameOrNull();
    if (typeName != null) {
      final IType element = JDTUtility.findIType(javaRef.getEclipseProjectName(), javaRef.getPackageName(), typeName);
      if (element == null) {
        SLLogger.getLogger().warning(I18N.err(269, javaRef));
        return false;
      }

      if (!element.isBinary()) {
        // Warn if the reference is not from Java source code
        if (javaRef.getWithin() != IJavaRef.Within.JAVA_FILE) {
          SLLogger.getLogger().warning(I18N.err(267, element, javaRef));
        }
        /*
         * Source code
         */
        try {
          final IEditorPart editorPart = JavaUI.openInEditor(element, false, true);

          final int offset = javaRef.getOffset();
          final int length = javaRef.getLength();
          final int lineNumber = javaRef.getLineNumber();
          if (offset != -1 && length != -1) {
            /*
             * Use offset and length if at all possible.
             */
            final IMarker location = ResourcesPlugin.getWorkspace().getRoot().createMarker(SLUtility.ECLIPSE_MARKER_TYPE_NAME);
            if (location != null) {
              location.setAttribute(IMarker.CHAR_START, offset);
              location.setAttribute(IMarker.CHAR_END, offset + length);
              IDE.gotoMarker(editorPart, location);
              location.delete();
            }
          } else if (lineNumber != -1) {
            /*
             * Use line number if we must
             */
            final IMarker location = ResourcesPlugin.getWorkspace().getRoot().createMarker(SLUtility.ECLIPSE_MARKER_TYPE_NAME);
            if (location != null) {
              location.setAttribute(IMarker.LINE_NUMBER, lineNumber);
              IDE.gotoMarker(editorPart, location);
              location.delete();
              return true;
            }
          }
          return editorPart != null;
        } catch (final Exception e) {
          SLLogger.getLogger().log(Level.SEVERE, I18N.err(132, element, javaRef), e);
        }
        return false;
      } else {
        /*
         * Binary file
         */
        // Warn if the reference is not from a JAR or class file
        if (javaRef.getWithin() == IJavaRef.Within.JAVA_FILE) {
          SLLogger.getLogger().warning(I18N.err(268, element, javaRef));
        }
        System.out.println("TODO -- Can't handle binary yet -- need to open promises XML editor");
        return false;
      }
    } else {
      // package only
      // TODO
      return false;
    }
  }

  /**
   * Tries to open the specified Java file in the editor and highlight the given
   * line number. If the line number is 0 then the entire passed Java element is
   * highlighted.
   * 
   * @param projectName
   *          the project name the file is contained within. For example,
   *          <code>JEdit</code>.
   * @param packageName
   *          the package name the file is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param lineNumber
   *          the line number to highlight, or 0.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditor(final String projectName, final String packageName, final String typeName,
      final int lineNumber) {
    final IType element = JDTUtility.findIType(projectName, packageName, typeName);
    return tryToOpenInEditor(element, lineNumber);
  }

  /**
   * Tries to open the specified Java file in the editor and highlight the
   * passed Java type.
   * 
   * @param projectName
   *          the project name the file is contained within. For example,
   *          <code>JEdit</code>.
   * @param packageName
   *          the package name the file is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditor(final String projectName, final String packageName, final String typeName) {
    return tryToOpenInEditor(projectName, packageName, typeName, 0);
  }

  /**
   * Tries to open the specified Java file in the editor and highlight the
   * passed Java type.
   * <p>
   * This method tries to find a match in all the open Java projects.
   * 
   * @param packageName
   *          the package name the file is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param lineNumber
   *          the line number to highlight, or 0.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditor(final String packageName, final String typeName, final int lineNumber) {
    for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
      if (tryToOpenInEditor(javaProjectName, packageName, typeName, lineNumber)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to open the specified Java file in the editor and highlight the given
   * line number. If the line number is 0 then the file is opened highlighting
   * the first line in the file.
   * <p>
   * This method tries to find a match in all the open Java projects.
   * 
   * @param packageName
   *          the package name the file is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditor(final String packageName, final String typeName) {
    for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
      if (tryToOpenInEditor(javaProjectName, packageName, typeName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to open an editor and highlight declaration of the given field name.
   * 
   * @param projectName
   *          the project name the field is contained within. For example,
   *          <code>JEdit</code>.
   * @param packageName
   *          the package name the field is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param fieldName
   *          the name of the field.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditorUsingFieldName(final String projectName, final String packageName, final String typeName,
      final String fieldName) {
    final IType element = JDTUtility.findIType(projectName, packageName, typeName);
    if (element != null && element.exists()) {
      try {
        for (final IField field : element.getFields()) {
          if (fieldName.equals(field.getElementName())) {
            JavaUI.openInEditor(field, false, true);
            return true;
          }
        }
      } catch (final Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(136, fieldName, packageName, typeName, projectName), e);
      }
    }
    return false;
  }

  /**
   * Tries to open an editor and highlight declaration of the given method name.
   * This method can be inaccurate if the targeted type contains overloaded
   * methods or lots of constructors.
   * 
   * @param projectName
   *          the project name the method is contained within. For example,
   *          <code>JEdit</code>.
   * @param packageName
   *          the package name the method is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param methodName
   *          the name of the method.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditorUsingMethodName(final String projectName, final String packageName, final String typeName,
      final String methodName) {
    final IType element = JDTUtility.findIType(projectName, packageName, typeName);
    if (element != null && element.exists()) {
      try {
        if (methodName.equalsIgnoreCase("<init>")) {
          /*
           * Just open up the first constructor that we find.
           */
          for (final IMethod method : element.getMethods()) {
            if (method.isConstructor()) {
              JavaUI.openInEditor(method, false, true);
              return true;
            }
          }
        }
        /*
         * Open up the first method we find with the right name. This could be
         * wrong if there are overloaded methods in the class.
         */
        for (final IMethod method : element.getMethods()) {
          if (methodName.equals(method.getElementName())) {
            JavaUI.openInEditor(method, false, true);
            return true;
          }
        }
      } catch (final Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(217, methodName, packageName, typeName, projectName), e);
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
   *          a Java source reference.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditor(final JavaSourceReference srcRef) {
    final String projectName = srcRef.getProjectName();
    if (projectName == null) {
      return tryToOpenInEditor(projectName, srcRef.getPackageName(), srcRef.getTypeName(), srcRef.getLineNumber());
    } else {
      return tryToOpenInEditor(srcRef.getPackageName(), srcRef.getTypeName(), srcRef.getLineNumber());
    }
  }

  /**
   * Tries to open an editor and highlight declaration of the given field name.
   * <p>
   * This method tries to find a match in all the open Java projects.
   * 
   * @param packageName
   *          the package name the field is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param fieldName
   *          the name of the field.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditorUsingFieldName(final String packageName, final String typeName, final String fieldName) {
    for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
      if (tryToOpenInEditorUsingFieldName(javaProjectName, packageName, typeName, fieldName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to open an editor and highlight declaration of the given method name.
   * This method can be inaccurate if the targeted type contains overloaded
   * methods or lots of constructors.
   * <p>
   * This method tries to find a match in all the open Java projects.
   * 
   * @param packageName
   *          the package name the method is contained within. For example,
   *          <code>com.surelogic.sierra</code>. the package name is
   *          "(default package)" or null then the class is contained within the
   *          default package.
   * @param typeName
   *          the type name, this type may be a nested type of the form
   *          <code>Outer$Inner</code> or <code>Outer$Inner$InnerInner</code> or
   *          <code>Outer.Inner</code> or <code>Outer.Inner.InnerInner</code>
   *          (to any depth).
   * @param methodName
   *          the name of the method.
   * @return {@code true} if it was possible to open the editor, {@code false}
   *         otherwise.
   */
  public static boolean tryToOpenInEditorUsingMethodName(final String packageName, final String typeName, final String methodName) {
    for (final String javaProjectName : JDTUtility.getJavaProjectNames()) {
      if (tryToOpenInEditorUsingMethodName(javaProjectName, packageName, typeName, methodName)) {
        return true;
      }
    }
    return false;
  }
}
