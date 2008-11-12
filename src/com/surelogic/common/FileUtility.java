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

/**
 * A utility to help with file operations.
 */
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
		if (path == null)
			throw new IllegalArgumentException(I18N.err(44, "path"));
		final File p = new File(path);
		return createDirectory(p);
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
	static public boolean createDirectory(final File path) {
		if (path == null)
			throw new IllegalArgumentException(I18N.err(44, "path"));
		if (path.exists()) {
			return true;
		} else {
			boolean success = path.mkdirs();
			if (!success) {
				SLLogger.getLogger().warning(
						I18N.err(30, path.getAbsolutePath()));
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
		final String lf = System.getProperty("line.separator");
		final StringBuilder b = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(new FileReader(textFile));
			while (true) {
				String s = r.readLine();
				if (s == null)
					break;
				b.append(s);
				b.append(lf);
			}
			r.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(117, textFile.getAbsolutePath()), e);
		}
		return b.toString();
	}

	/**
	 * This method returns the anchor for the Sierra data directory. Clients
	 * typically will not use this method to get the Sierra data directory,
	 * instead they would use the method {@link #getSierraDataDirectory()}.
	 * 
	 * @return the non-null anchor for the Flashlight data directory.
	 * @see #getDataDirectory(File)
	 * @see #getSierraDataDirectory()
	 */
	static public File getSierraDataDirectoryAnchor() {
		return new File(System.getProperty("user.home") + File.separator
				+ ".sierra-data");
	}

	/**
	 * This method gets the Sierra data directory. It ensures the directory
	 * exists.
	 * <p>
	 * This method is the same as calling
	 * 
	 * <pre>
	 * getDataDirectory(getSierraDataDirectoryAnchor())
	 * </pre>
	 * 
	 * @return the Sierra data directory.
	 * @see #getDataDirectory(File)
	 * @see #getSierraDataDirectoryAnchor()
	 */
	static public File getSierraDataDirectory() {
		return getDataDirectory(getSierraDataDirectoryAnchor());
	}

	/**
	 * This method gets the Sierra local team server data directory. It ensures
	 * the directory exists.
	 * 
	 * @return the Sierra local team server data directory.
	 * @see #getDataDirectory(File)
	 */
	static public File getSierraLocalTeamServerDirectory() {
		final File data = new File(getSierraDataDirectory(), "server");
		return getDataDirectory(data);
	}

	/**
	 * This method gets the Sierra team server cache directory. It ensures the
	 * directory exists.
	 * <p>
	 * Note that this method is <i>not</i> just used for a local Sierra team
	 * server. It is used by all Sierra team server instances.
	 * 
	 * @return the Sierra team server cache directory.
	 * @see #getDataDirectory(File)
	 */
	static public File getSierraTeamServerCacheDirectory() {
		final File data = new File(System.getProperty("java.io.tmpdir")
				+ File.separator + "sierra-cache");
		return getDataDirectory(data);
	}

	/**
	 * This method determines a directory where data is stored based upon a
	 * passed anchor path. This anchor path is passed in <tt>data</tt> and is
	 * either a directory or file that references a directory. It ensures the
	 * directory exists.
	 * <ul>
	 * <li>If <tt>data</tt> does not exist. Then a directory is created using
	 * <tt>data</tt> and returned. If the creation fails for any reason then
	 * <tt>System.getProperty("java.io.tmpdir")</tt> is returned (and an error
	 * is logged).</li>
	 * <li>If <tt>data</tt> exists and is a directory, then that directory is
	 * returned.</li>
	 * <li>If <tt>data</tt> exists and is a file, then the contents of that file
	 * are read. The resulting string is assumed to be a path to the data
	 * directory. The resulting string is converted to a {@link File} object and
	 * a recursive call to this method is made.</li>
	 * </ul>
	 * 
	 * @return the path to the data directory.
	 * @throws IllegalArgumentException
	 *             if <tt>data</tt> is {@code null}.
	 */
	static public File getDataDirectory(final File data) {
		if (data == null)
			throw new IllegalArgumentException(I18N.err(44, "data"));
		if (data.exists()) {
			if (data.isFile()) {
				/*
				 * The contents of the file contain the path to the data
				 * directory.
				 */
				final String referencedPath = getFileContents(data).trim();
				final File dataPath = new File(referencedPath);
				return getDataDirectory(dataPath);
			} else {
				/*
				 * Return the data directory.
				 */
				return data;
			}
		} else {
			/*
			 * The data directory needs to be created.
			 */
			if (createDirectory(data)) {
				return data;
			} else {
				final File tmp = new File(System.getProperty("java.io.tmpdir"));
				SLLogger.getLogger().severe(
						I18N.err(32, data.getAbsolutePath(), tmp
								.getAbsolutePath()));
				return tmp;
			}
		}
	}
}
