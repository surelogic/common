package com.surelogic.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

import com.surelogic.NonNull;
import com.surelogic.Regions;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public class LibResources {

  /**
   * The current version of the promises Jar files. *
   * <p>
   * If you change this you <i>must</i> add the current name to the
   * {@link #PROMISES_JAR_OLD_VERSIONS} and {@link #PROMISES8_JAR_OLD_VERSIONS}
   * array.
   */
  public static final String PROMISES_VERSION = "5.6.0";

  /**
   * The name of the current promises Jar file for Java 1.5 and above.
   */
  public static final String PROMISES_JAR = "promises-" + PROMISES_VERSION + ".jar";

  /**
   * The name of the current promises Jar file for Java 8 and above.
   */
  public static final String PROMISES8_JAR = "promises8-" + PROMISES_VERSION + ".jar";

  /**
   * Holds the names of the old library files that may need to be upgraded to
   * the new promises Jar file for Java 1.5 and above.
   */
  public static final String[] PROMISES_JAR_OLD_VERSIONS = { "promises.jar", "promises-3.0.0.jar", "promises-3.1.0.jar",
      "promises-3.2.0.jar", "promises-3.2.1.jar", "promises-3.2.2.jar", "promises-4.0.0.jar", "promises-4.0.1.jar",
      "promises-4.0.2.jar", "promises-4.3.0.jar", "promises-4.3.1.jar", "promises-4.3.3.jar", "promises-4.3.4.jar",
      "promises-4.4.0.jar", "promises-5.0.0.jar", "promises-5.1.0.jar", "promises-5.2.0.jar", "promises-5.5.0.jar",
      "promises-5.5.1.jar" };

  /**
   * Holds the names of the old library files that may need to be upgraded to
   * the new promises Jar file for Java 8 and above.
   */
  public static final String[] PROMISES8_JAR_OLD_VERSIONS = {};

  public static final String PATH = "/lib/runtime/";
  public static final String PROMISES_JAR_PATHNAME = PATH + PROMISES_JAR;
  public static final String PROMISES8_JAR_PATHNAME = PATH + PROMISES8_JAR;
  public static final String PROMISES_PKG = "com.surelogic";

  public static URL getPromisesJarURL() {
    return LibResources.class.getResource(PROMISES_JAR_PATHNAME);
  }

  public static URL getPromises8JarURL() {
    return LibResources.class.getResource(PROMISES8_JAR_PATHNAME);
  }

  public static InputStream getPromisesJar() throws IOException {
    final URL url = getPromisesJarURL();
    final InputStream is = url.openStream();
    return is;
  }

  public static InputStream getPromises8Jar() throws IOException {
    final URL url = getPromises8JarURL();
    final InputStream is = url.openStream();
    return is;
  }

  /**
   * Gets all the promise annotation classes declared in the package
   * <tt>com.surelogic</tt> within the promises JAR that ships with the
   * SureLogic tools. No filtering is done except that only annotations are
   * included.
   * <p>
   * If this method fails the returned list will be empty and a severe error
   * will be sent to the log.
   * 
   * @return a list of annotations in the promises jar.
   * 
   * @see #isMultipleAnnotationPromise(Class)
   * @see #getPromiseClassesWithoutMultipleAnnotationPromises()
   */
  @NonNull
  public static ArrayList<Class<?>> getPromiseClasses() {
    ArrayList<Class<?>> result = new ArrayList<>();
    try {
      final String pfx = "com/surelogic/";
      final String ext = ".class";
      final JarInputStream jis = new JarInputStream(getPromisesJar());
      try {
        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
          final String jarEntryName = je.getName();
          if (jarEntryName != null) {
            if (jarEntryName.startsWith(pfx) && jarEntryName.endsWith(ext)) {
              final String className = jarEntryName.substring(pfx.length(), jarEntryName.length() - ext.length());
              final String fullyQualifiedClassName = PROMISES_PKG + "." + className;
              Class<?> c = Class.forName(fullyQualifiedClassName);
              if (c.isAnnotation())
                result.add(c);
            }
          }
        }
      } finally {
        jis.close();
      }
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(327, PROMISES_JAR_PATHNAME), e);
    }
    return result;
  }

  /**
   * This method filters the results of {@link #getPromiseClasses()} using
   * {@link #isMultipleAnnotationPromise(Class)} to remove promises that are
   * used only to allow multiple annotations to be annotated on a declaration
   * prior to Java 8. For example {@link Regions}.
   * 
   * @return a list of annotations in the promises jar.
   * 
   * @see #isMultipleAnnotationPromise(Class)
   * @see #getPromiseClasses()
   */
  public static ArrayList<Class<?>> getPromiseClassesWithoutMultipleAnnotationPromises() {
    ArrayList<Class<?>> result = getPromiseClasses();
    try {
      for (Iterator<Class<?>> i = result.iterator(); i.hasNext();) {
        final Class<?> promiseClass = i.next();
        if (isMultipleAnnotationPromise(promiseClass)) {
          i.remove();
        }
      }
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(328, PROMISES_JAR_PATHNAME), e);
    }
    return result;
  }

  /**
   * Checks if a promise is most likely used only to allow multiple annotations
   * to be annotated on a declaration prior to Java 8. For example
   * {@link Regions}.
   * <p>
   * <i>Implementation note:</i> This method uses a heuristic that checks if
   * there is a method called <tt>value</tt> that returns an array and that the
   * return type is within the package <tt>com.surelogic</tt> (another
   * annotation). This should be precise but may need to be updated in the
   * future.
   * 
   * @param promise
   *          the promise annotation class.
   * @return {@code true} if the passed promise is most likely used only to
   *         allow multiple annotations to be annotated on a declaration prior
   *         to Java 8, {@code false} if it is a normal promise.
   */
  public static boolean isMultipleAnnotationPromise(Class<?> promise) {
    for (Method m : promise.getMethods()) {
      if ("value".equals(m.getName())) {
        final Class<?> returnType = m.getReturnType();
        if (returnType != null) {
          if (returnType.isArray()) {
            if (returnType.getCanonicalName().startsWith(PROMISES_PKG))
              return true;
          }
        }
      }
    }
    return false;
  }
}
