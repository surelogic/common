package com.surelogic.common;

import java.io.File;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;

public final class FileUtility {
  private static final Logger LOG = SLLogger.getLogger("common");
  
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
				  LOG.warning("Could not delete: "+file.getAbsolutePath());
				}
			}
		}
		return (path.delete());
	}
}
