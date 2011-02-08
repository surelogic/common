package com.surelogic.common.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;

public class EclipseUtility {

	/**
	 * Defines the SureLogic preference node that is used by the preference API
	 * defined below. This API can be used in UI and Non-UI Eclipse code to
	 * manage Eclipse preferences for SureLogic tools.
	 * <p>
	 * To obtain a version of the SureLogic preferences usable in the Eclipse
	 * UI, e.g., in the implementation of a properties dialog, see
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
			(new DefaultScope()).getNode(PREFERENCES_NODE).flush();
			(new InstanceScope()).getNode(PREFERENCES_NODE).flush();
		} catch (BackingStoreException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(218, PREFERENCES_NODE), e);
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

	/*
	 * Boolean preference API
	 */

	public static boolean getBooleanPreference(String key) {
		return Platform.getPreferencesService().getBoolean(PREFERENCES_NODE,
				key, BOOLEAN_DEFAULT_DEFAULT, null);
	}

	public static void setBooleanPreference(String key, boolean value) {
		(new InstanceScope()).getNode(PREFERENCES_NODE).putBoolean(key, value);
	}

	public static boolean getDefaultBooleanPreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).getBoolean(key,
				BOOLEAN_DEFAULT_DEFAULT);
	}

	public static void setDefaultBooleanPreference(String key, boolean value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).putBoolean(key, value);
	}

	/*
	 * Double preference API
	 */

	public static double getDoublePreference(String key) {
		return Platform.getPreferencesService().getDouble(PREFERENCES_NODE,
				key, DOUBLE_DEFAULT_DEFAULT, null);
	}

	public static void setDoublePreference(String key, double value) {
		(new InstanceScope()).getNode(PREFERENCES_NODE).putDouble(key, value);
	}

	public static double getDefaultDoublePreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).getDouble(key,
				DOUBLE_DEFAULT_DEFAULT);
	}

	public static void setDefaultDoublePreference(String key, double value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).putDouble(key, value);
	}

	/*
	 * Float preference API
	 */

	public static float getFloatPreference(String key) {
		return Platform.getPreferencesService().getFloat(PREFERENCES_NODE, key,
				FLOAT_DEFAULT_DEFAULT, null);
	}

	public static void setFloatPreference(String key, float value) {
		(new InstanceScope()).getNode(PREFERENCES_NODE).putFloat(key, value);
	}

	public static float getDefaultFloatPreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).getFloat(key,
				FLOAT_DEFAULT_DEFAULT);
	}

	public static void setDefaultFloatPreference(String key, float value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).putFloat(key, value);
	}

	/*
	 * Int preference API
	 */

	public static int getIntPreference(String key) {
		return Platform.getPreferencesService().getInt(PREFERENCES_NODE, key,
				INT_DEFAULT_DEFAULT, null);
	}

	public static void setIntPreference(String key, int value) {
		(new InstanceScope()).getNode(PREFERENCES_NODE).putInt(key, value);
	}

	public static int getDefaultIntPreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).getInt(key,
				INT_DEFAULT_DEFAULT);
	}

	public static void setDefaultIntPreference(String key, int value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).putInt(key, value);
	}

	/*
	 * Long preference API
	 */

	public static long getLongPreference(String key) {
		return Platform.getPreferencesService().getLong(PREFERENCES_NODE, key,
				LONG_DEFAULT_DEFAULT, null);
	}

	public static void setLongPreference(String key, long value) {
		(new InstanceScope()).getNode(PREFERENCES_NODE).putLong(key, value);
	}

	public static long getDefaultLongPreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).getLong(key,
				LONG_DEFAULT_DEFAULT);
	}

	public static void setDefaultLongPreference(String key, long value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).putLong(key, value);
	}

	/*
	 * String preference API
	 */

	public static String getStringPreference(String key) {
		return Platform.getPreferencesService().getString(PREFERENCES_NODE,
				key, STRING_DEFAULT_DEFAULT, null);
	}

	public static void setStringPreference(String key, String value) {
		/*
		 * Avoid a NPE if the passed value is null, assume that this means to
		 * use the default-default
		 */
		if (value == null)
			value = STRING_DEFAULT_DEFAULT;
		(new InstanceScope()).getNode(PREFERENCES_NODE).put(key, value);
	}

	public static String getDefaultStringPreference(String key) {
		return (new DefaultScope()).getNode(PREFERENCES_NODE).get(key,
				STRING_DEFAULT_DEFAULT);
	}

	public static void setDefaultStringPreference(String key, String value) {
		(new DefaultScope()).getNode(PREFERENCES_NODE).put(key, value);
	}

	/**
	 * Gets the directory where the passed plug-in identifier is located. Works
	 * for both directories and Jar files.
	 * 
	 * @param plugInId
	 *            the id of an installed plug-in.
	 * @return the path to the plug-in or Jar.
	 */
	public static String getDirectoryOf(final String plugInId) {
		final Bundle bundle = Platform.getBundle(plugInId);
		if (bundle == null) {
			throw new IllegalStateException("null bundle returned for "
					+ plugInId);
		}

		final URL relativeURL = bundle.getEntry("");
		try {
			URL commonPathURL = FileLocator.resolve(relativeURL);
			final String commonDirectory = commonPathURL.getPath();
			if (commonDirectory.startsWith("file:")
					&& commonDirectory.endsWith(".jar!/")) {
				// Jar file
				return commonDirectory.substring(5,
						commonDirectory.length() - 2);
			}
			return commonDirectory;
		} catch (Exception e) {
			throw new IllegalStateException(
					"failed to resolve a path for the URL " + relativeURL);
		}
	}

	/**
	 * Gets a list of plug-in identifiers needed to run the given plug-in id,
	 * including itself.
	 * 
	 * @param plugInId
	 *            the id of an installed plug-in.
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
	 *            Not a checked plug-in
	 * @param checked
	 *            The set of plug-ins that we're already checked
	 */
	private static Set<String> getDependencies(Bundle b, Set<String> checked) {
		checked.add(b.getSymbolicName());

		Dictionary<String, String> d = b.getHeaders();
		String deps = d.get("Require-Bundle");
		if (deps != null) {
			String lastId = null;
			List<String> ids = new ArrayList<String>();
			List<String> optional = null;
			final StringTokenizer st = new StringTokenizer(deps, ";, ");
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				if ("resolution:=optional".equals(id) && lastId != null) {
					if (optional == null) {
						optional = new ArrayList<String>();
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
				if (bundle == null && !optional.contains(id)) {
					throw new IllegalArgumentException("Couldn't find bundle "
							+ id + " required for " + b.getSymbolicName());
				}
				getDependencies(bundle, checked);
			}
		}
		return checked;
	}

	/**
	 * Gets the path to the open Eclipse workspace.
	 * 
	 * @return the path to the open Eclipse workspace.
	 */
	public static File getWorkspacePath() {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		final IPath location = root.getLocation();
		if (location == null) {
			throw new IllegalStateException(I18N.err(44,
					"IWorkspaceRoot.getLocation()"));
		}
		return location.toFile();
	}

	/**
	 * Constructs a path within the current workspace with the passed fragment
	 * added to it.
	 * <p>
	 * This method is often used to construct locations for tool data
	 * directories. For example, {@code getADataDirectoryPath(".flashlight")}
	 * would be the default location of the Flashlight data directory.
	 * 
	 * @param fragment
	 *            the last fragment to add to the workspace path.
	 * @return the constructed data directory path.
	 */
	public static String getADataDirectoryPath(String fragment) {
		final File root = EclipseUtility.getWorkspacePath();
		final File path = new File(root, fragment);
		return path.getAbsolutePath();
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
	 * Checks if any active job is assignment-compatible with the passed type.
	 * 
	 * @param type
	 *            a type of job.
	 * @return {@code true} if any active job is assignment-compatible with the
	 *         passed type, {@code false} otherwise.
	 */
	public static boolean isActiveJobOfType(final Class<? extends Job> type) {
		if (type == null) {
			return false;
		}
		final IJobManager manager = Job.getJobManager();
		for (final Job job : manager.find(null)) {
			if (type.isInstance(job)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Counts the number of active jobs that are assignment-compatible with the
	 * passed type.
	 * 
	 * @param type
	 *            a type of job.
	 * @return the count of active jobs that are assignment-compatible with the
	 *         passed type.
	 */
	public static int getActiveJobCountOfType(final Class<? extends Job> type) {
		int result = 0;
		if (type != null) {
			final IJobManager manager = Job.getJobManager();
			for (final Job job : manager.find(null)) {
				if (type.isInstance(job)) {
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
	 *            the name of the project.
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
			IResource res = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(path);
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
	 *            the folder path.
	 * @param monitor
	 *            a progress monitor to track progress.
	 * @throws CoreException
	 *             if something goes wrong within Eclipse.
	 * @see java.io.File#mkdirs()
	 */
	public static void createFolderHelper(final IFolder folder,
			final IProgressMonitor monitor) throws CoreException {
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
	 * This method is a bit of a hack in the sense that a complete version
	 * number is only returned outside of development, i.e., in a real release.
	 * So for a real release expect a string similar to
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
	 *            a plug-in to query.
	 * @return a string that represents the version of the passed activator.
	 */
	public static String getVersion(final Plugin activator) {
		final String rawVersionS = (String) Activator.getDefault().getBundle()
				.getHeaders().get(BUNDLE_VERSION);
		if (rawVersionS.endsWith(DOT_QUALIFIER)) {
			return rawVersionS.substring(0, rawVersionS.length()
					- DOT_QUALIFIER.length());
		}
		return rawVersionS;
	}

	/**
	 * Gets the date that the version of the passed activator was released or
	 * today's date.
	 * <p>
	 * This method uses {@link #getVersion(AbstractUIPlugin)} to obtain a
	 * version string similar to
	 * 
	 * <pre>
	 * 3.1.1.201001151440
	 * </pre>
	 * 
	 * The <tt>"20100115"</tt> portion of the string (after the last
	 * <tt>"."</tt>) is then parsed as a date using the pattern
	 * <tt>"yyyyMMdd"</tt>.
	 * 
	 * @param activator
	 *            a plug-in to query.
	 * @return the date the version was released or today's date.
	 */
	public static Date getReleaseDate(final Plugin activator) {
		final String rawVersionS = (String) Activator.getDefault().getBundle()
				.getHeaders().get(BUNDLE_VERSION);
		if (rawVersionS.endsWith(DOT_QUALIFIER))
			return new Date();

		final int lastDotIndex = rawVersionS.lastIndexOf('.');
		if (lastDotIndex == -1)
			return new Date();
		if (lastDotIndex + 9 > rawVersionS.length())
			return new Date();

		final String dateS = rawVersionS.substring(lastDotIndex + 1,
				lastDotIndex + 9);

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		try {
			final Date result = dateFormat.parse(dateS);
			return result;
		} catch (ParseException ignore) {
		}
		return new Date();

	}

	/**
	 * Constructs an Eclipse job to lookup the release date of the passed
	 * plug-in and set it as the product release date of the passed product.
	 * 
	 * @param product
	 *            a product.
	 * @param activator
	 *            a plug-in.
	 * @return an Eclipse job.
	 */
	public static Job getProductReleaseDateJob(final SLLicenseProduct product,
			final Plugin activator) {
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
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		return job;
	}

	public static void main(String[] args) {
		System.out.println(SLUtility.toStringHumanDay(getReleaseDate(null)));
	}

	public static IProject unzipToWorkspace(final File projectZip)
			throws CoreException, IOException {
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

	public static IProject unzipToWorkspace(final URL zip)
			throws CoreException, IOException {
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

	public static IProject unzipToWorkspace(final String name,
			final InputStream is) throws IOException, CoreException {
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
	 *            The project directory
	 * @throws CoreException
	 */
	public static IProject importProject(final File projectRoot)
			throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = newProjectDescription(workspace,
				new File(projectRoot, ".project"));
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
				description
						.setLocation(new Path(projectRoot.getAbsolutePath()));
			}
			proj.create(description, new NullProgressMonitor());
			proj.open(new NullProgressMonitor());
			SLLogger.getLogger().info(
					"Created and opened project '" + proj + "'");
		}
		return proj;
	}

	private static IProjectDescription newProjectDescription(
			final IWorkspace workspace, final File projectFile) {
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
	 *            a view identifier
	 * @return code true} if the view identified by the given id exists,
	 *         {@code false} otherwise.
	 */
	public static boolean bundleExists(final String id) {
		return Platform.getBundle(id) != null;
	}

	private EclipseUtility() {
		// no instances
	}
}
