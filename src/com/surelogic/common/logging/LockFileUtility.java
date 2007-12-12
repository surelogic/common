package com.surelogic.common.logging;

import java.io.File;
import java.io.IOException;

/**
 * The utility for creating a lock file for a particular project
 * 
 * Current not in use
 * 
 * @author Tanmay.Sinha
 * 
 */
@Deprecated
public class LockFileUtility {

	private static final LockFileUtility INSTANCE = new LockFileUtility();

	public static LockFileUtility getInstance() {
		return INSTANCE;
	}

	/**
	 * returns true if it created a new lock file else returns false
	 * 
	 * @param productName
	 * @param projectName
	 * @param folderPath
	 * @return
	 * @throws IOException
	 */
	public boolean obtainLockFile(final String productName, String projectName,
	    final String folderPath) throws IOException {

		File lockFile = new File(folderPath + File.separator + productName
				+ "-" + projectName + ".lock");

		if (lockFile.createNewFile()) {
			return true;
		} else {
			return false;
		}

	}

	public boolean clearLockFile(final String productName, String projectName,
	    final String folderPath) {
	  final File lockFile = new File(folderPath + File.separator + productName
				+ "-" + projectName + ".lock");

		if (lockFile.delete()) {
			return true;
		} else {
			return false;
		}
	}
}
