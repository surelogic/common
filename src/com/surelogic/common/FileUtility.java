package com.surelogic.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

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
	 * @returns <tt>true</tt> if the directory existed or was created, along
	 *          with all necessary parent directories; <tt>false</tt> otherwise.
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
	 *         deleted, <tt>false</tt> otherwise.
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
	 * Copies the contents of a {@link URL} to a file.
	 * 
	 * @param source
	 *            the stream to copy.
	 * @param to
	 *            the target file.
	 * @return <tt>true</tt> if and only if the copy is successful,
	 *         <tt>false</tt> otherwise.
	 */
	static public boolean copy(URL source, File to) {
		boolean success = true;
		try {
			InputStream is = null;
			try {
				is = source.openStream();
				is = new BufferedInputStream(is, 8192);
				final OutputStream os = new FileOutputStream(to);

				final byte[] buf = new byte[8192];
				int num;
				while ((num = is.read(buf)) >= 0) {
					os.write(buf, 0, num);
				}
			} finally {
				if (is != null)
					is.close();
			}
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(112, source.toString(), to.getAbsolutePath()), e);
			success = false;
		}
		return success;
	}

	/**
	 * Copies the contents of one file to another file.
	 * 
	 * @param from
	 *            the source file to copy.
	 * @param to
	 *            the target file.
	 * @return <tt>true</tt> if and only if the copy is successful,
	 *         <tt>false</tt> otherwise.
	 */
	static public boolean copy(File from, File to) {
		try {
			URL source = from.toURI().toURL();
			return copy(source, to);
		} catch (MalformedURLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(113, from.getAbsolutePath()), e);
		}
		return false;
	}

	/**
	 * Gets the contents of a text file and returns it as a string.
	 * 
	 * @param textFile
	 *            a text file.
	 * @return the file's contents.
	 */
	static public String getFileContents(final File textFile) {
		StringBuilder b = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(new FileReader(textFile));
			while (true) {
				String s = r.readLine();
				if (s == null)
					break;
				b.append(s);
				b.append("\n");
			}
			r.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(117, textFile.getAbsolutePath()), e);
		}
		return b.toString();
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
	 * @return the path to the Sierra data directory. No trailing <tt>/</tt> is
	 *         included.
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

	/**
	 * This method gets the path to the Sierra local team server data directory.
	 * It ensures the directory exists.
	 * 
	 * @return the path to the Sierra data directory. No trailing <tt>/</tt> is
	 *         included.
	 */
	static public String getSierraLocalTeamServerDirectory() {
		final String dir = getSierraDataDirectory() + File.separator + "server";
		if (createDirectory(dir))
			return dir;
		SLLogger.getLogger().severe(I18N.err(92, dir));
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * This method gets the path to the Sierra team server cache directory. It
	 * ensures the directory exists.
	 * 
	 * @return the path to the Sierra team server cache directory. No trailing
	 *         <tt>/</tt> is included.
	 */
	static public String getSierraTeamServerCacheDirectory() {
		final String tmpdir = System.getProperty("java.io.tmpdir");
		final String dir = tmpdir + File.separator + "sierra-cache";
		if (createDirectory(dir))
			return dir;
		SLLogger.getLogger().severe(I18N.err(95, dir));
		return tmpdir;
	}

	/**
	 * This method gets the path to the Flashlight data directory. It ensures
	 * the directory exists.
	 * <p>
	 * It first tries to use the value of the <tt>SLFlashlightDataDirectory</tt>
	 * property. If that property is not defined (or it is not valid) then
	 * <tt>~/.flashlight-data</tt> is used. Finally, if that directory is
	 * invalid then the value of <tt>java.io.tmpdir</tt> is used.
	 * 
	 * @return the path to the Flashlight data directory. No trailing <tt>/</tt>
	 *         is included.
	 */
	static public String getFlashlightDataDirectory() {
		String dir = System.getProperty("SLFlashlightDataDirectory");
		/*
		 * The property was set so see if it makes sense.
		 */
		if (dir != null) {
			if (createDirectory(dir))
				return dir;
			SLLogger.getLogger().warning(I18N.err(31, dir));
		}
		dir = System.getProperty("user.home") + File.separator
				+ ".flashlight-data";
		if (createDirectory(dir))
			return dir;
		SLLogger.getLogger().severe(I18N.err(32, dir));
		return System.getProperty("java.io.tmpdir");
	}
}
