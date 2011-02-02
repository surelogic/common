package com.surelogic.common.eclipse.core.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;

public abstract class AbstractPrefInitializer extends AbstractPreferenceInitializer {
	protected static String getDefaultDataDirectory(String fragment) {
		final File root = EclipseUtility.getWorkspacePath();
		final File path = new File(root, fragment);
		return path.getAbsolutePath();
	}
	
	protected static void ensureDataDirectoryExists(String path) {
		if (path == null) {
			throw new IllegalStateException(I18N.err(44, "path"));
		}
		final File dataDir = new File(path);
		if (!FileUtility.createDirectory(dataDir)) {
			throw new RuntimeException("Unable to create " + path);
		}
	}
}
