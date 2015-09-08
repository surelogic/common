package com.surelogic.common.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.FileUtility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.jobs.EclipseAccessKeysJob;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AggregateSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;

@Utility
public class EclipseUtility {

  public static final String DOT_PROJECT = ".project";

  /**
   * Defines the SureLogic preference node that is used by the preference API
   * defined below. This API can be used in UI and Non-UI Eclipse code to manage
   * Eclipse preferences for SureLogic tools.
   * <p>
   * To obtain a version of the SureLogic preferences usable in the Eclipse UI,
   * e.g., in the implementation of a properties dialog, see
   * <tt>EclipseUIUtility.getPreferences()</tt>.
   * <p>
   * To be compatible with the Eclipse UI preferences we set default values in
   * the {@link DefaultScope} and current values in the {@link InstanceScope}.
   * This ensures that UI code accessing preferences via
   * <tt>EclipseUIUtility.getPreferences()</tt> will get the same results
   * callers of the preference API defined in this class.
   */
  public static final String PREFERENCES_NODE = "com.surelogic.common.core.preferences";

  /**
   * Persists any preference changes to the disk. Only called from
   * {@link Activator#stop(org.osgi.framework.BundleContext)}.
   */
  static void persistPreferences() {
    try {
      DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).flush();
      InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).flush();
    } catch (BackingStoreException e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(218, PREFERENCES_NODE), e);
    }
  }

  /**
   * The default-default value for boolean preferences (<code>false</code>).
   */
  public static final boolean BOOLEAN_DEFAULT_DEFAULT = false;

  /**
   * The default-default value for double preferences (<code>0.0</code>).
   */
  public static final double DOUBLE_DEFAULT_DEFAULT = 0.0;

  /**
   * The default-default value for float preferences (<code>0.0f</code>).
   */
  public static final float FLOAT_DEFAULT_DEFAULT = 0.0f;

  /**
   * The default-default value for int preferences (<code>0</code>).
   */
  public static final int INT_DEFAULT_DEFAULT = 0;

  /**
   * The default-default value for long preferences (<code>0L</code>).
   */
  public static final long LONG_DEFAULT_DEFAULT = 0L;

  /**
   * The default-default value for String preferences (<code>""</code>).
   */
  public static final String STRING_DEFAULT_DEFAULT = "";

  /**
   * The default-default value for String list preferences (empty list).
   */
  public static final List<String> STRING_LIST_DEFAULT_DEFAULT = Collections.emptyList();

  /*
   * Boolean preference API
   */

  public static boolean getBooleanPreference(String key) {
    return Platform.getPreferencesService().getBoolean(PREFERENCES_NODE, key, BOOLEAN_DEFAULT_DEFAULT, null);
  }

  public static void setBooleanPreference(String key, boolean value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).putBoolean(key, value);
  }

  public static boolean getDefaultBooleanPreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).getBoolean(key, BOOLEAN_DEFAULT_DEFAULT);
  }

  public static void setDefaultBooleanPreference(String key, boolean value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).putBoolean(key, value);
  }

  /*
   * Double preference API
   */

  public static double getDoublePreference(String key) {
    return Platform.getPreferencesService().getDouble(PREFERENCES_NODE, key, DOUBLE_DEFAULT_DEFAULT, null);
  }

  public static void setDoublePreference(String key, double value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).putDouble(key, value);
  }

  public static double getDefaultDoublePreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).getDouble(key, DOUBLE_DEFAULT_DEFAULT);
  }

  public static void setDefaultDoublePreference(String key, double value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).putDouble(key, value);
  }

  /*
   * Float preference API
   */

  public static float getFloatPreference(String key) {
    return Platform.getPreferencesService().getFloat(PREFERENCES_NODE, key, FLOAT_DEFAULT_DEFAULT, null);
  }

  public static void setFloatPreference(String key, float value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).putFloat(key, value);
  }

  public static float getDefaultFloatPreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).getFloat(key, FLOAT_DEFAULT_DEFAULT);
  }

  public static void setDefaultFloatPreference(String key, float value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).putFloat(key, value);
  }

  /*
   * Int preference API
   */

  public static int getIntPreference(String key) {
    return Platform.getPreferencesService().getInt(PREFERENCES_NODE, key, INT_DEFAULT_DEFAULT, null);
  }

  public static void setIntPreference(String key, int value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).putInt(key, value);
  }

  public static int getDefaultIntPreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).getInt(key, INT_DEFAULT_DEFAULT);
  }

  public static void setDefaultIntPreference(String key, int value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).putInt(key, value);
  }

  /*
   * Long preference API
   */

  public static long getLongPreference(String key) {
    return Platform.getPreferencesService().getLong(PREFERENCES_NODE, key, LONG_DEFAULT_DEFAULT, null);
  }

  public static void setLongPreference(String key, long value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).putLong(key, value);
  }

  public static long getDefaultLongPreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).getLong(key, LONG_DEFAULT_DEFAULT);
  }

  public static void setDefaultLongPreference(String key, long value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).putLong(key, value);
  }

  /*
   * String preference API
   */

  public static String getStringPreference(String key) {
    return Platform.getPreferencesService().getString(PREFERENCES_NODE, key, STRING_DEFAULT_DEFAULT, null);
  }

  public static void setStringPreference(String key, String value) {
    /*
     * Avoid a NPE if the passed value is null, assume that this means to use
     * the default-default
     */
    if (value == null)
      value = STRING_DEFAULT_DEFAULT;
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).put(key, value);
  }

  public static String getDefaultStringPreference(String key) {
    return DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).get(key, STRING_DEFAULT_DEFAULT);
  }

  public static void setDefaultStringPreference(String key, String value) {
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).put(key, value);
  }

  /*
   * String list preference API
   */

  public static List<String> getStringListPreference(String key) {
    final String encodedList = Platform.getPreferencesService().getString(PREFERENCES_NODE, key, null, null);
    if (encodedList == null || "".equals(encodedList))
      return STRING_LIST_DEFAULT_DEFAULT;
    else
      return SLUtility.decodeStringList(encodedList);
  }

  public static void setStringListPreference(String key, List<String> value) {
    final String encodedList;
    if (value == null || value.isEmpty())
      encodedList = ""; // null is not allowed
    else
      encodedList = SLUtility.encodeStringList(value);
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).put(key, encodedList);
  }

  public static List<String> getDefaultStringListPreference(String key) {
    final String encodedList = DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).get(key, null);
    if (encodedList == null)
      return STRING_LIST_DEFAULT_DEFAULT;
    else
      return SLUtility.decodeStringList(encodedList);
  }

  public static void setDefaultStringListPreference(String key, List<String> value) {
    final String encodedList;
    if (value == null || value.isEmpty())
      encodedList = null;
    else
      encodedList = SLUtility.encodeStringList(value);
    DefaultScope.INSTANCE.getNode(PREFERENCES_NODE).put(key, encodedList);
  }

  /*
   * Observing preference API
   */

  /**
   * Adds a listener to the instance scope of the preferences we use.
   * 
   * @param value
   *          the preference change listener to register
   */
  public static void addPreferenceChangeListener(@NonNull IPreferenceChangeListener value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).addPreferenceChangeListener(value);
  }

  /**
   * Removes a listener to the instance scope of the preferences we use.
   * 
   * @param value
   *          the preference change listener to remove
   */
  public static void removePreferenceChangeListener(@NonNull IPreferenceChangeListener value) {
    InstanceScope.INSTANCE.getNode(PREFERENCES_NODE).removePreferenceChangeListener(value);
  }

  /**
   * Copies the contents of a {@link URL} to a workspace file.
   * 
   * @param source
   *          the stream to copy.
   * @param to
   *          the target file.
   * @return {@code true} if and only if the copy is successful, {@code false}
   *         otherwise.
   */
  public static boolean copy(final InputStream source, final IFile to) {
    try {
      // Make sure Eclipse is up-to-date with the file system
      to.refreshLocal(IResource.DEPTH_ONE, null);

      if (to.exists()) {
        to.setContents(source, IResource.NONE, null);
      } else {
        to.create(source, IResource.NONE, null);
      }
    } catch (CoreException e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(223, to.getName()), e);
    }
    return true;
  }

  /**
   * Gets the directory where the passed plug-in identifier is located. Works
   * for both directories and Jar files.
   * 
   * @param pluginId
   *          the id of an installed plug-in.
   * @return the path to the plug-in or Jar.
   * 
   * @throws IllegalArgumentException
   *           if the Eclipse platform doesn't know about the passed plugin
   *           identifier, the file or directory doesn't exist.
   */
  public static File getInstallationDirectoryOf(final String pluginId) {
    try {
      final Bundle bundle = Platform.getBundle(pluginId);
      if (bundle == null) {
        throw new IllegalArgumentException(I18N.err(343, pluginId));
      }
      File result = FileLocator.getBundleFile(bundle);
      return result;
    } catch (Exception e) {
      throw new IllegalArgumentException(I18N.err(344, pluginId), e);
    }
  }

  /**
   * Gets the URL where the passed plug-in identifier is located. Works for both
   * directories and Jar files.
   * 
   * @param pluginId
   *          the id of an installed plug-in.
   * @return the path to the plug-in or Jar.
   * 
   * @throws IllegalArgumentException
   *           if the Eclipse platform doesn't know about the passed plugin
   *           identifier, or there is some problem determining the URL.
   */
  public static URL getInstallationURLOf(final String pluginId) {
    final File f = getInstallationDirectoryOf(pluginId);
    try {
      return f.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(I18N.err(344, pluginId), e);
    }
  }

  /**
   * Gets a list of plug-in identifiers needed to run the given plug-in id,
   * including itself.
   * 
   * @param plugInId
   *          the id of an installed plug-in.
   * @return A comma-separated list of plug-in identifiers needed to run the
   *         given plug-in id, including itself.
   */
  public static Set<String> getDependencies(final String plugInId) {
    final Bundle bundle = Platform.getBundle(plugInId);
    if (bundle == null) {
      return Collections.emptySet();
    }
    return getDependencies(bundle, new HashSet<String>());
  }

  /**
   * Returns the set of dependencies for the given plug-in bundle.
   * 
   * @param b
   *          Not a checked plug-in
   * @param checked
   *          The set of plug-ins that we're already checked
   */
  private static Set<String> getDependencies(Bundle b, Set<String> checked) {
    if (b == null) {
      return checked;
    }
    checked.add(b.getSymbolicName());

    Dictionary<String, String> d = b.getHeaders();
    String deps = d.get("Require-Bundle");
    if (deps != null) {
      String lastId = null;
      final List<String> ids = new ArrayList<>();
      List<String> optional = null;
      final StringTokenizer st = new StringTokenizer(deps, ";, ");
      while (st.hasMoreTokens()) {
        String id = st.nextToken();
        if ("resolution:=optional".equals(id) && lastId != null) {
          if (optional == null) {
            optional = new ArrayList<>();
          }
          optional.add(lastId);
        }
        if (id.indexOf('=') >= 0 || id.indexOf('"') >= 0) {
          // Ignore any property stuff
          // (e.g. version info)
          // System.out.println("Ignoring: "+id);
          continue;
        }
        lastId = id;
        ids.add(id);
      }
      for (String id : ids) {
        // System.out.println("Considering: "+id);
        if (checked.contains(id)) {
          continue;
        }
        final Bundle bundle = Platform.getBundle(id);
        if (bundle == null) {
          if (!optional.contains(id)) {
            throw new IllegalArgumentException("Couldn't find bundle " + id + " required for " + b.getSymbolicName());
          } else {
            continue;
          }
        }
        getDependencies(bundle, checked);
      }
    }
    return checked;
  }

  public static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  /**
   * Gets the path to the open Eclipse workspace.
   * 
   * @return the path to the open Eclipse workspace.
   * @throws IllegalStateException
   *           if something goes wrong.
   */
  @NonNull
  public static File getWorkspacePath() {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();
    final IPath location = root.getLocation();
    if (location == null) {
      throw new IllegalStateException(I18N.err(44, "IWorkspaceRoot.getLocation()"));
    }
    return location.toFile();
  }

  /**
   * Constructs a {@link File} within the current workspace with the passed path
   * fragment added to it.
   * <p>
   * This method is often used to construct locations for tool data directories.
   * 
   * @param fragmentToAdd
   *          a path fragment to add to the workspace path.
   * @return the constructed path.
   * @throws Exception
   *           if something goes wrong.
   */
  public static File getWorkspaceRelativeAsFile(String fragmentToAdd) {
    final File root = EclipseUtility.getWorkspacePath();
    final File path = new File(root, fragmentToAdd);
    return path;
  }

  /**
   * Gets the {@link File} for the Flashlight data directory.
   * 
   * @return the Flashlight data directory.
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static File getFlashlightDataDirectory() {
    return EclipseUtility.getWorkspaceRelativeAsFile(FileUtility.FLASHLIGHT_DATA_PATH_FRAGMENT);
  }

  /**
   * Gets the {@link File} for the JSure data directory.
   * 
   * @return the JSure data directory.
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static File getJSureDataDirectory() {
    return EclipseUtility.getWorkspaceRelativeAsFile(FileUtility.JSURE_DATA_PATH_FRAGMENT);
  }

  /**
   * Gets the {@link File} for the JSecure data directory.
   * 
   * @return the JSecure data directory.
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static File getJSecureDataDirectory() {
    return EclipseUtility.getWorkspaceRelativeAsFile(FileUtility.JSECURE_DATA_PATH_FRAGMENT);
  }

  /**
   * Gets the {@link File} for the Sierra data directory.
   * 
   * @return the Sierra data directory.
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static File getSierraDataDirectory() {
    return EclipseUtility.getWorkspaceRelativeAsFile(FileUtility.SIERRA_DATA_PATH_FRAGMENT);
  }

  /**
   * Gets the {@link File} for the Sierra scans directory.
   * 
   * @return the Sierra scans directory.
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static File getSierraScanDirectory() {
    return new File(getSierraDataDirectory(), FileUtility.SIERRA_SCAN_PATH_FRAGMENT);
  }

  /**
   * Try to create an appropriate IFile object
   */
  public static IFile resolveIFile(String path) {
    try {
      final IWorkspace workspace = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot root = workspace.getRoot();
      if (!path.startsWith("file:")) {
        return root.getFile(new Path(path));
      }
      final URI loc = new URI(path);
      // Try to use Eclipse to find the right thing
      IFile[] files = root.findFilesForLocationURI(loc);
      for (IFile file : files) {
        if (file.exists()) {
          return file;
        }
      }
      if (loc.getAuthority() == null) {
        return null;
      }
      if (loc.getAuthority().length() != 0) {
        return null;
      }
      // Assume that the path is under the workspace
      final File rootFile = getWorkspacePath();
      File f = new File(loc);
      IPath p = new Path(computePath(rootFile, f).toString());
      return root.getFile(p);
    } catch (IllegalArgumentException e) {
      return null;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private static StringBuilder computePath(File root, File here) {
    if (root.equals(here)) {
      return new StringBuilder("/");
    }
    StringBuilder sb = computePath(root, here.getParentFile());
    if (sb != null) {
      // Check for ending slash
      if (sb.charAt(sb.length() - 1) != '/') {
        sb.append('/');
      }
      return sb.append(here.getName());
    }
    return null;
  }

  /**
   * Counts the number of active jobs that are assignment-compatible with the
   * passed type.
   * 
   * @param type
   *          a type of job.
   * @return the count of active jobs that are assignment-compatible with the
   *         passed type.
   */
  public static int getActiveJobCountWithName(final String name) {
    int result = 0;
    if (name != null) {
      final IJobManager manager = Job.getJobManager();
      for (final Job job : manager.find(null)) {
        if (name.equals(job.getName())) {
          result++;
        }
      }
    }
    return result;
  }

  /**
   * Tries to lookup an {@link IProject} with the given name.
   * 
   * @param name
   *          the name of the project.
   * @return the {@link IProject}, or {code null} if none.
   */
  public static IProject getProject(final String name) {
    if (name.contains("/")) {
      return null;
    }
    final IWorkspace ws = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot wsRoot = ws.getRoot();
    return wsRoot.getProject(name);
  }

  /**
   * Resolves IPath objects into File objects
   */
  public static File resolveIPath(IPath path) {
    File loc = path.toFile();
    if (!loc.exists()) {
      IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
      if (res == null) {
        return null;
      }
      loc = res.getLocation().toFile();
    }
    return loc;
  }

  /**
   * Helper method: it recursively creates a folder path.
   * 
   * @param folder
   *          the folder path.
   * @param monitor
   *          a progress monitor to track progress.
   * @throws CoreException
   *           if something goes wrong within Eclipse.
   * @see java.io.File#mkdirs()
   */
  public static void createFolderHelper(final IFolder folder, final IProgressMonitor monitor) throws CoreException {
    if (!folder.exists()) {
      final IContainer parent = folder.getParent();
      if (parent instanceof IFolder && !((IFolder) parent).exists()) {
        createFolderHelper((IFolder) parent, monitor);
      }
      folder.create(false, true, monitor);
    }
  }

  private static final String BUNDLE_VERSION = "Bundle-Version";
  private static final String DOT_QUALIFIER = ".qualifier";

  /**
   * Gets the version of the passed activator as read from its bundle headers.
   * <p>
   * This method is a bit of a hack in the sense that a complete version number
   * is only returned outside of development, i.e., in a real release. So for a
   * real release expect a string similar to
   * 
   * <pre>
   * 3.1.1.201001151440
   * </pre>
   * 
   * However, in a development or meta-Eclipse a string similar to
   * <tt>3.1.1.qualifier</tt> is returned from the bundle headers which we
   * truncate to
   * 
   * <pre>
   * 3.1.1
   * </pre>
   * 
   * @param activator
   *          a plug-in to query.
   * @return a string that represents the version of the passed activator.
   */
  public static String getVersion(final Plugin activator) {
    final String rawVersionS = (String) Activator.getDefault().getBundle().getHeaders().get(BUNDLE_VERSION);
    if (rawVersionS.endsWith(DOT_QUALIFIER)) {
      return rawVersionS.substring(0, rawVersionS.length() - DOT_QUALIFIER.length());
    }
    return rawVersionS;
  }

  /**
   * Gets the version of Eclipse, such as <tt>"4.4.0.20140612-0500"</tt>.
   * Currently this method looks for the definition of the Eclipse product in
   * the extension registry, and if that fails it uses the version of the
   * org.eclipse.platform plug-in which should match.
   * 
   * @return the version of the Eclipse or <tt>unknown</tt> if the version
   *         cannot be determined.
   */
  public static String getEclipseVersion() {
    @Nullable
    String result = null;
    @NonNull
    final String product = System.getProperty("eclipse.product", "org.eclipse.platform.ide");
    final IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.core.runtime.products");
    if (point != null) {
      final IExtension[] extensions = point.getExtensions();
      if (extensions != null)
        for (IExtension ext : extensions) {
          if (product.equals(ext.getUniqueIdentifier())) {
            final IContributor contributor = ext.getContributor();
            if (contributor != null) {
              Bundle bundle = Platform.getBundle(contributor.getName());
              if (bundle != null) {
                result = bundle.getVersion().toString();
              }
            }
          }
        }
    }
    if (result == null) // if all of the above didn't work
      result = getVersion(Platform.getBundle("org.eclipse.platform"));
    return result;
  }

  /**
   * Gets the version of the passed bundle, such as <tt>4.5.0</tt>.
   * 
   * @param bundle
   *          a bundle.
   * @return the version of the passed bundle, such as <tt>4.5.0</tt>, or
   *         <tt>unknown</tt> if the version cannot be determined or the passed
   *         bundle is {@code null}.
   */
  public static String getVersion(@Nullable Bundle bundle) {
    String result = "unknown";
    if (bundle != null) {
      Version v = bundle.getVersion();
      if (v != null)
        result = v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
    }
    return result;
  }

  /**
   * Gets the date that the version of the passed activator was released or
   * today's date.
   * <p>
   * This method uses {@link #getVersion(AbstractUIPlugin)} to obtain a version
   * string similar to
   * 
   * <pre>
   * 3.1.1.201001151440
   * </pre>
   * 
   * The <tt>"20100115"</tt> portion of the string (after the last <tt>"."</tt>)
   * is then parsed as a date using the pattern <tt>"yyyyMMdd"</tt>.
   * 
   * @param activator
   *          a plug-in to query.
   * @return the date the version was released or today's date.
   */
  public static Date getReleaseDate(final Plugin activator) {
    final String rawVersionS = (String) Activator.getDefault().getBundle().getHeaders().get(BUNDLE_VERSION);
    if (rawVersionS.endsWith(DOT_QUALIFIER))
      return new Date();

    final int lastDotIndex = rawVersionS.lastIndexOf('.');
    if (lastDotIndex == -1)
      return new Date();
    if (lastDotIndex + 9 > rawVersionS.length())
      return new Date();

    final String dateS = rawVersionS.substring(lastDotIndex + 1, lastDotIndex + 9);

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    try {
      final Date result = dateFormat.parse(dateS);
      return result;
    } catch (ParseException ignore) {
    }
    return new Date();
  }

  /**
   * Constructs an Eclipse job to lookup the release date of the passed plug-in
   * and set it as the product release date of the passed product.
   * 
   * @param product
   *          a product.
   * @param activator
   *          a plug-in.
   * @return an Eclipse job.
   */
  public static Job getProductReleaseDateJob(final SLLicenseProduct product, final Plugin activator) {
    if (product == null)
      throw new IllegalArgumentException(I18N.err(44, "product"));
    if (activator == null)
      throw new IllegalArgumentException(I18N.err(44, "activator"));

    final Job job = new Job("Looking up " + product + " release date") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), 2);
        try {
          Date releaseDate = getReleaseDate(activator);
          monitor.worked(1);
          SLLicenseUtility.setReleaseDateFor(product, releaseDate);
          // special handling for Flashlight for Android version
          if (product == SLLicenseProduct.FLASHLIGHT)
            SLLicenseUtility.setReleaseDateFor(SLLicenseProduct.FLASHLIGHT_ANDROID, releaseDate);
          monitor.worked(1);
        } finally {
          monitor.done();
        }
        return Status.OK_STATUS;
      }
    };
    return job;
  }

  public static IProject unzipToWorkspace(final File projectZip) throws CoreException, IOException {
    final String zipName = projectZip.getName();
    final String name;
    if (zipName.lastIndexOf('.') >= 0) {
      // Remove suffix
      name = zipName.substring(0, zipName.lastIndexOf('.'));
    } else {
      name = zipName;
    }
    return unzipToWorkspace(name, new FileInputStream(projectZip));
  }

  public static IProject unzipToWorkspace(final URL zip) throws CoreException, IOException {
    final String zipPath = zip.getPath();
    final int lastSlash = zipPath.lastIndexOf('/');
    final String zipName = zipPath.substring(lastSlash + 1);
    final String name;
    if (zipName.lastIndexOf('.') >= 0) {
      // Remove suffix
      name = zipName.substring(0, zipName.lastIndexOf('.'));
    } else {
      name = zipName;
    }
    return unzipToWorkspace(name, zip.openStream());
  }

  public static IProject unzipToWorkspace(final String name, final InputStream is) throws IOException, CoreException {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final File root = workspace.getRoot().getLocation().toFile();
    final File project = new File(root, name);
    if (project.exists()) {
      return null;
    }

    final ZipInputStream zis = new ZipInputStream(is);
    final byte[] buf = new byte[32768];
    ZipEntry ze = zis.getNextEntry();
    while (ze != null) {
      final File f = new File(root, ze.getName());
      if (f.exists()) {
        return null;
      }
      if (ze.isDirectory()) {
        f.mkdirs();
      } else {
        f.getParentFile().mkdirs();

        // Copy the data
        // System.out.println("Creating " + f);
        OutputStream out = new FileOutputStream(f);
        out = new BufferedOutputStream(out, 32768);
        int numRead;
        while ((numRead = zis.read(buf)) > 0) {
          out.write(buf, 0, numRead);
        }
        out.close();
      }
      zis.closeEntry();
      ze = zis.getNextEntry();
    }
    return importProject(project);
  }

  /**
   * Assumes that .project is "just" in the project directory
   * 
   * @param projectRoot
   *          The project directory
   * @throws CoreException
   */
  public static IProject importProject(final File projectRoot) throws CoreException {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IProjectDescription description = newProjectDescription(workspace, new File(projectRoot, DOT_PROJECT));
    final String projectName;
    if (description != null) {
      projectName = description.getName();
    } else {
      projectName = projectRoot.getName();
    }
    final IProject proj = workspace.getRoot().getProject(projectName);
    if (!proj.exists()) {
      if (description == null) {
        description = workspace.newProjectDescription(projectName);
        description.setLocation(new Path(projectRoot.getAbsolutePath()));
      }
      proj.create(description, new NullProgressMonitor());
      proj.open(new NullProgressMonitor());
      SLLogger.getLogger().fine("Created and opened project '" + proj + "'");
    }
    return proj;
  }

  private static IProjectDescription newProjectDescription(final IWorkspace workspace, final File projectFile) {
    // If there is no file or the user has already specified forget it
    if (projectFile == null) {
      return null;
    }
    final IPath path = new Path(projectFile.getPath());

    IProjectDescription newDescription = null;

    try {
      newDescription = workspace.loadProjectDescription(path);
    } catch (final CoreException exception) {
      // no good couldn't get the name
    }
    return newDescription;
  }

  /**
   * Checks if a bundle identified by the given id exists.
   * 
   * @param viewId
   *          a view identifier
   * @return code true} if the view identified by the given id exists,
   *         {@code false} otherwise.
   */
  public static boolean bundleExists(final String id) {
    return Platform.getBundle(id) != null;
  }

  /**
   * Checks if the Flashlight Android plug-in is installed into Eclipse. This
   * done via a call to {@link #bundleExists(String)} with the correct plug-in
   * identifier.
   * 
   * @return {@code true} if it is installed, {@code false} otherwise.
   */
  public static boolean isFlashlightAndroidInstalled() {
    return bundleExists("com.surelogic.flashlight.android");
  }

  /**
   * Checks if the JSure client plug-in is installed into Eclipse. This done via
   * a call to {@link #bundleExists(String)} with the correct plug-in
   * identifier.
   * 
   * @return {@code true} if it is installed, {@code false} otherwise.
   */
  public static boolean isJSureInstalled() {
    return bundleExists("com.surelogic.jsure.client.eclipse");
  }

  /**
   * Checks if the Sierra local team server plug-in is installed into Eclipse.
   * This done via a call to {@link #bundleExists(String)} with the correct
   * plug-in identifier.
   * 
   * @return {@code true} if it is installed, {@code false} otherwise.
   */
  public static boolean isLocalTeamServerInstalled() {
    return bundleExists("com.surelogic.sierra.eclipse.teamserver");
  }

  /**
   * Wraps an IDE independent job with an optional set of access keys so that it
   * can scheduled within the Eclipse jobs system. Jobs with the same access
   * keys will proceed in serial order.
   * 
   * @param job
   *          the IDE independent job.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. May
   *          be empty. If no access keys are passed no serialization rule will
   *          be setup.
   * 
   * @return an Eclipse job that can be submitted.
   * 
   * @throws IllegalArgumentException
   *           if job is {@code null}.
   */
  @NonNull
  public static Job toEclipseJob(@NonNull final SLJob job, String... accessKeys) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));
    final Job result = new EclipseAccessKeysJob(job.getName(), accessKeys) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        final SLStatus status = job.run(new SLProgressMonitorWrapper(monitor, job.getName(), job.getObservers()));
        return SLEclipseStatusUtility.convert(status);
      }
    };
    result.addJobChangeListener(new SLJobChangeAdapter(job));
    return result;
  }

  /**
   * Wraps an IDE independent job with an Eclipse workspace resource so that it
   * can scheduled within the Eclipse jobs system. Jobs with the same workspace
   * resource keys will proceed in serial order.
   * 
   * @param job
   *          the IDE independent job.
   * @param resource
   *          a workspace resource. Workspace jobs with the same resource will
   *          proceed in serial order.
   * @return an Eclipse workspace job that can be submitted.
   * 
   * @throws IllegalArgumentException
   *           if job or resource is {@code null}.
   */
  @NonNull
  public static WorkspaceJob toWorkspaceJob(@NonNull final SLJob job, @NonNull final IResource resource) {
    if (job == null)
      throw new IllegalArgumentException(I18N.err(44, "job"));
    if (resource == null)
      throw new IllegalArgumentException(I18N.err(44, "resource"));
    final WorkspaceJob result = new WorkspaceJob(job.getName()) {
      @Override
      public IStatus runInWorkspace(IProgressMonitor monitor) {
        final SLStatus status = job.run(new SLProgressMonitorWrapper(monitor, job.getName(), job.getObservers()));
        return SLEclipseStatusUtility.convert(status);
      }
    };
    result.setRule(resource);
    result.addJobChangeListener(new SLJobChangeAdapter(job));
    return result;
  }

  /**
   * Wraps an IDE independent job with the Eclipse workspace root. No other
   * workspace resource jobs will run at the same time as this one.
   * 
   * @param job
   *          the IDE independent job.
   * @return an Eclipse workspace job that can be submitted.
   * 
   * @throws IllegalArgumentException
   *           if job is {@code null}.
   */
  @NonNull
  public static WorkspaceJob toEntireWorkspaceJob(@NonNull final SLJob job) {
    final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
    final WorkspaceJob result = toWorkspaceJob(job, workspace);
    return result;
  }

  /**
   * Checks if there is an active {@link SLJob} of the passed type being managed
   * by Eclipse. This method will go through any wrappers and
   * {@link AggregateSLJob} instances to find the job.
   * <p>
   * Note that this method only finds {@link SLJob} instances.
   * 
   * @param type
   *          of {@link SLJob} to search for.
   * @return {@code true} if an {@link SLJob} is active of the passed type,
   *         {@code false} otherwise.
   */
  public static boolean isActiveOfType(final Class<? extends SLJob> type) {
    if (type == null) {
      return false;
    }
    return !getActiveJobsOfType(type).isEmpty();
  }

  /**
   * Gets all active {@link SLJob} instances of the passed type being managed by
   * Eclipse. This method will go through any wrappers and
   * {@link AggregateSLJob} instances to find the job.
   * <p>
   * Note that this method only returns {@link SLJob} instances.
   * 
   * @param type
   *          a type of {@link SLJob}.
   * @return all active {@link SLJob} instances of the passed type. May be
   *         empty.
   */
  @NonNull
  public static <T extends SLJob> List<T> getActiveJobsOfType(final Class<T> type) {
    if (type == null) {
      return Collections.emptyList();
    }
    final List<T> result = new ArrayList<>();
    for (SLJob jobInEclipse : f_jobsPassedToEclipse) {
      getThroughAggregateJobByTypeHelper(jobInEclipse, type, result);
    }
    return result;
  }

  private static <T extends SLJob> void getThroughAggregateJobByTypeHelper(final SLJob job, final Class<T> type,
      List<T> mutableResult) {
    if (job instanceof AggregateSLJob) {
      for (final SLJob subJob : ((AggregateSLJob) job).getAggregatedJobs()) {
        getThroughAggregateJobByTypeHelper(subJob, type, mutableResult);
      }
    } else {
      if (type.isInstance(job)) {
        @SuppressWarnings("unchecked")
        T jobOfInterest = (T) job;
        mutableResult.add(jobOfInterest);
      }
    }
  }

  /**
   * Gets all active {@link SLJob} instances with the passed name being managed
   * by Eclipse. This method will go through any wrappers and
   * {@link AggregateSLJob} instances to find the job.
   * <p>
   * Note that this method only returns {@link SLJob} instances.
   * 
   * @param name
   *          a job name.
   * @return all active {@link SLJob} instances with the passed name. May be
   *         empty.
   */
  public static List<SLJob> getActiveJobsWithName(final String name) {
    if (name == null) {
      return Collections.emptyList();
    }
    final List<SLJob> result = new ArrayList<>();
    for (SLJob jobInEclipse : f_jobsPassedToEclipse) {
      getThroughAggregateJobByNameHelper(jobInEclipse, name, result);
    }
    return result;
  }

  private static void getThroughAggregateJobByNameHelper(final SLJob job, final String name, List<SLJob> mutableResult) {
    if (job instanceof AggregateSLJob) {
      for (final SLJob subJob : ((AggregateSLJob) job).getAggregatedJobs()) {
        getThroughAggregateJobByNameHelper(subJob, name, mutableResult);
      }
    } else {
      if (name.equals(job.getName()))
        mutableResult.add(job);
    }
  }

  /**
   * Tracks {@link SLJob} instances wrapped to Eclipse jobs. The
   * {@link SLJobChangeAdapter} manages the contents of this list.
   */
  static final CopyOnWriteArraySet<SLJob> f_jobsPassedToEclipse = new CopyOnWriteArraySet<>();

  /**
   * An Eclipse job change adapter that updates the set of jobs passed to
   * Eclipse.
   * <p>
   * Sadly, Eclipse wraps their own jobs several times so it is not possible to
   * pull an SLJob out of an Eclipse job (well at least without reflection
   * dependent upon the internals of Eclipse).
   */
  final static class SLJobChangeAdapter extends JobChangeAdapter {

    final SLJob f_job;

    SLJobChangeAdapter(SLJob job) {
      if (job == null)
        throw new IllegalArgumentException(I18N.err(44, "job"));
      f_job = job;
    }

    @Override
    public void done(IJobChangeEvent event) {
      f_jobsPassedToEclipse.remove(f_job);
    }

    @Override
    public void scheduled(IJobChangeEvent event) {
      f_jobsPassedToEclipse.add(f_job);
    }
  }

  private EclipseUtility() {
    // no instances
  }
}
