package com.surelogic.common.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

import com.surelogic.common.logging.SLLogger;

/**
 * A utility that helps you get resources out of an Eclipse plug-in.
 */
public class Resources {

	public static URL findRoot(Bundle bundle) {
		String s = bundle.getLocation();
		if (s.startsWith("reference:file:")) {
			try {
				return new URL(s.substring("reference:".length()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bundle.getEntry("/");
	}

	public static URL findRoot(Plugin p) {
		return findRoot(p.getBundle());
	}

	public static URL findRoot(String plugInId) {
		final Bundle bundle = Platform.getBundle(plugInId);
		if (bundle == null) {
			throw new IllegalStateException("null bundle returned for "
					+ plugInId);
		}
		return findRoot(bundle);
	}

	public static URL findURL(Plugin p, String pluginPath) {
		try {
			return new URL(findRoot(p), pluginPath);
		} catch (MalformedURLException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"unable to create a URL for the plug-in relative path "
							+ pluginPath, e);
		}
		return null;
	}

	public static Iterable<URL> findURLs(Plugin p, String prefixPath,
			String pattern, boolean recurse) {
		final Enumeration<URL> e = p.getBundle().findEntries(prefixPath,
				pattern, recurse);
		return new Iterable<URL>() {
			public Iterator<URL> iterator() {
				return new Iterator<URL>() {
					public boolean hasNext() {
						return e.hasMoreElements();
					}

					public URL next() {
						return e.nextElement();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public static Iterable<URL> findURLs(Plugin p, String prefixPath,
			String pattern) {
		return findURLs(p, prefixPath, pattern, true);
	}

	private Resources() {
		// no instances
	}
}
