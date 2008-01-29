package com.surelogic.common;

import java.io.File;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class FileUtility {

	private FileUtility() {
		// no instances
	}

	static public boolean deleteDirectoryAndContents(final File path) {
		if (path.exists()) {
			for (File file : path.listFiles()) {
				boolean success;
				if (file.isDirectory()) {
					success = deleteDirectoryAndContents(file);
				} else {
					success = file.delete();
				}
				if (!success) {
					SLLogger.getLogger().warning(
							I18N.err(11, file.getAbsolutePath()));
				}
			}
		}
		return (path.delete());
	}
}
