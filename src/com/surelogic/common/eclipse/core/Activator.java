package com.surelogic.common.eclipse.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.surelogic.common.eclipse.core.logging.EclipseHandler;
import com.surelogic.common.logging.SLLogger;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	private static Activator plugin;

	public Activator() {
		if (plugin != null)
			throw new IllegalStateException(Activator.class.getName()
					+ " instance already exits, it should be a singleton.");
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		/*
		 * Configure the SLLogger to write to the Eclipse log.
		 * 
		 * The EclipseHandler also adjusts the Level of logging based upon the
		 * debug trace settings for this plug-in.
		 */
		SLLogger.addHandler(new EclipseHandler());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		try {
			// SLImages.dispose();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Gets the identifier for this plug in.
	 * 
	 * @return an identifier, such as <tt>com.surelogic.common</tt>. In rare
	 *         cases, for example bad plug in XML, it may be {@code null}.
	 * @see Bundle#getSymbolicName()
	 */
	public String getPlugInId() {
		return plugin.getBundle().getSymbolicName();
	}

	public String getDirectoryOf(final String plugInId) {
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
	 * @return A comma-separated list of plug in identifiers needed to run the
	 *         given one, including itself
	 */
	public Set<String> getDependencies(final String plugInId) {
		final Bundle bundle = Platform.getBundle(plugInId);
		if (bundle == null) {
			return Collections.emptySet();
		}
		return getDependencies(bundle, new HashSet<String>());
	}

	/**
	 * Returns the set of dependencies for this plug in.
	 * 
	 * @param b
	 *            Not a checked plug in
	 * @param checked
	 *            The set of plug ins that we're already checked
	 */
	private Set<String> getDependencies(Bundle b, Set<String> checked) {
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
}
