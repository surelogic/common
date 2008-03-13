package com.surelogic.common;

import java.io.File;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class FileUtility {

	private FileUtility() {
		// no instances
	}

	/**
	 * Tries to create the specified directory in the file system unless it
	 * already exists.
	 * 
	 * @param path
	 *            the desired directory.
	 * @returns <tt>true</tt> if the directory exited or was created, along
	 *          with all necessary parent directories; <tt>false</tt>
	 *          otherwise.
	 * 
	 */
	static public boolean createDirectory(final String path) {
		File p = new File(path);
		if (p.exists()) {
			return true;
		} else {
			boolean success = p.mkdirs();
			if (!success) {
				SLLogger.getLogger().warning(I18N.err(30, path));
			}
			return success;
		}
	}

	/**
	 * Tries to delete a directory and all its contents.
	 * 
	 * @param path
	 *            the directory to delete
	 * @return <tt>true</tt> if and only if the directory is successfully
	 *         deleted; <tt>false</tt> otherwise.
	 * 
	 */
	static public boolean deleteDirectoryAndContents(final File path) {
		boolean success;
		if (path.exists()) {
			for (File file : path.listFiles()) {
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
		success = path.delete();
		if (!success) {
			SLLogger.getLogger().warning(I18N.err(11, path.getAbsolutePath()));
		}
		return success;
	}

	/**
	 * This method gets the path to the Sierra data directory. It ensures the
	 * directory exists.
	 * <p>
	 * It first tries to use the value of the <tt>SLSierraDataDirectory</tt>
	 * property. If that property is not defined (or it is not valid) then
	 * <tt>~/.sierra-data</tt> is used. Finally, if that directory is invalid
	 * then the value of <tt>java.io.tmpdir</tt> is used.
	 * 
	 * @return the path to the Sierra data directory. No trailing <tt>/</tt>
	 *         is included.
	 */
	static public String getSierraDataDirectory() {
		String dir = System.getProperty("SLSierraDataDirectory");
		/*
		 * The property was set so see if it makes sense.
		 */
		if (dir != null) {
			if (createDirectory(dir))
				return dir;
			SLLogger.getLogger().warning(I18N.err(31, dir));
		}
		dir = System.getProperty("user.home") + File.separator + ".sierra-data";
		if (createDirectory(dir))
			return dir;
		SLLogger.getLogger().severe(I18N.err(32, dir));
		return System.getProperty("java.io.tmpdir");
	}
}
