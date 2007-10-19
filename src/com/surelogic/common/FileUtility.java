package com.surelogic.common;

import java.io.File;

public final class FileUtility {

	private FileUtility() {
		// no instances
	}

	static public boolean deleteDirectoryAndContents(File path) {
		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory()) {
					deleteDirectoryAndContents(file);
				} else {
					file.delete();
				}
			}
		}
		return (path.delete());
	}
}
