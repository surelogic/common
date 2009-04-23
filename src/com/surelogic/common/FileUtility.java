package com.surelogic.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLSeverity;
import com.surelogic.common.jobs.SLStatus;
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
	 * @returns {@code true} if the directory existed or was created, along with
	 *          all necessary parent directories; {@code false} otherwise.
	 * 
	 */
	public static boolean createDirectory(final String path) {
		if (path == null) {
			throw new IllegalArgumentException(I18N.err(44, "path"));
		}
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
	 *          with all necessary parent directories; {@code false} otherwise.
	 * 
	 */
	public static boolean createDirectory(final File path) {
		if (path == null) {
			throw new IllegalArgumentException(I18N.err(44, "path"));
		}
		if (path.exists()) {
			return true;
		} else {
			final boolean success = path.mkdirs();
			if (!success) {
				/*
				 * Check if the reason we could not create the directory is
				 * because it already exists.
				 */
				if (path.isDirectory())
					return true;
				SLLogger.getLogger().warning(
						I18N.err(30, path.getAbsolutePath()));
			}
			return success;
		}
	}

	/**
	 * Tries to perform a recursive deletion on the passed path. If the path is
	 * a file it is deleted, if the path is a directory then the directory and
	 * all its contents are deleted.
	 * 
	 * @param path
	 *            the file or directory to delete.
	 * @return {@code true} if and only if the directory is successfully
	 *         deleted, {@code false} otherwise.
	 * 
	 */
	public static boolean recursiveDelete(final File path) {
		boolean success;
		if (path.isDirectory()) {
			final File[] files = path.listFiles();
			if (files != null) {
				for (final File file : files) {
					success = recursiveDelete(file);
					if (!success) {
						SLLogger.getLogger().warning(
								I18N.err(11, file.getAbsolutePath()));
					}
				}
			}
		}
		success = path.delete();
		if (!success) {
			SLLogger.getLogger().warning(I18N.err(11, path.getAbsolutePath()));
			path.deleteOnExit();
		}
		return success;
	}

	/**
	 * This method performs a rough calculation of the space being used by the
	 * passed file or directory. The calculation is recursive and includes
	 * sub-directories.
	 * 
	 * @param path
	 *            the file or directory to compute the size of.
	 * @return the size in bytes of the passed file or directory.
	 */
	public static long recursiveSizeInBytes(final File path) {
		long result = 0L;
		if (path.isDirectory()) {
			final File[] files = path.listFiles();
			if (files != null) {
				for (final File file : files) {
					result += recursiveSizeInBytes(file);
				}
			}
		} else {
			result = path.length();
		}
		return result;
	}

	/**
	 * This method produces a human readable size such as <tt>50 Bytes</tt> or
	 * <tt>45.2 MB</tt> or <tt>5.1 GB</tt> from a given size in bytes value.
	 * 
	 * @param bytes
	 *            a size in bytes.
	 * @return the human readable string.
	 */
	public static String bytesToHumanReadableString(final long bytes) {
		final String[] labels = { "Bytes", "KB", "MB", "GB" };
		int labelIndex = 0;
		long size = bytes;
		int digit = 0;
		while (size > 1024 && labelIndex < 3) {
			final long oldSize = size;
			size = oldSize / 1024L;
			digit = (int) (((oldSize % 1024L) / (double) 1024.0) * (double) 10.0);
			labelIndex++;
		}
		if (labelIndex == 0 || digit == 0)
			return size + " " + labels[labelIndex];
		else
			return size + "." + digit + " " + labels[labelIndex];
	}

	/**
	 * Copies the contents of a {@link URL} to a file.
	 * 
	 * @param source
	 *            the stream to copy.
	 * @param to
	 *            the target file.
	 * @return {@code true} if and only if the copy is successful, {@code false}
	 *         otherwise.
	 */
	public static boolean copy(final URL source, final File to) {
		boolean success = true;
		try {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = source.openStream();
				is = new BufferedInputStream(is, 8192);
				os = new FileOutputStream(to);

				final byte[] buf = new byte[8192];
				int num;
				while ((num = is.read(buf)) >= 0) {
					os.write(buf, 0, num);
				}
			} finally {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			}
		} catch (final IOException e) {
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
	 * @return {@code true} if and only if the copy is successful, {@code false}
	 *         otherwise.
	 */
	public static boolean copy(final File from, final File to) {
		try {
			final URL source = from.toURI().toURL();
			return copy(source, to);
		} catch (final MalformedURLException e) {
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
	 * @throws IllegalStateException
	 *             if something goes wrong.
	 */
	public static String getFileContents(final File textFile) {
		final String lf = System.getProperty("line.separator");
		final StringBuilder b = new StringBuilder();
		try {
			final BufferedReader r = new BufferedReader(
					new FileReader(textFile));
			boolean first = true;
			while (true) {
				final String s = r.readLine();
				if (s == null) {
					break;
				}
				if (first) {
					first = false;
				} else {
					b.append(lf);
				}
				b.append(s);
			}
			r.close();
		} catch (final IOException e) {
			final String msg = I18N.err(117, textFile.getAbsolutePath());
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			throw new IllegalStateException(msg, e);
		}
		return b.toString();
	}

	/**
	 * Puts the contents of string into a file.
	 * 
	 * @param textFile
	 *            a text file. If this file exists its contents will be
	 *            replaced, if not it will be created.
	 * @param text
	 *            the file's contents.
	 * @throws IllegalStateException
	 *             if something goes wrong.
	 */
	public static void putFileContents(final File textFile, final String text) {
		try {
			final BufferedWriter r = new BufferedWriter(
					new FileWriter(textFile));
			r.write(text);
			r.close();
		} catch (final IOException e) {
			final String msg = I18N.err(31, textFile.getAbsolutePath());
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	private static File dataAnchor = 
		new File(System.getProperty("user.home")+File.separator+".sierra-data");
	
	public static void setSierraDataDirectoryAnchor(File dir) {
		if (dir != null && dir.exists() && dir.isDirectory()) {
			dataAnchor = dir;
		} else {
			throw new IllegalArgumentException("Bad directory: "+dir);
		}
	}
	
	/**
	 * This method returns the anchor for the Sierra data directory. Clients
	 * typically will not use this method to get the Sierra data directory,
	 * instead they would use the method {@link #getSierraDataDirectory()}.
	 * 
	 * @return the non-null anchor for the Flashlight data directory.
	 * 
	 * @see #getDataDirectory(File)
	 * 
	 * @see #getSierraDataDirectory()
	 */
	public static File getSierraDataDirectoryAnchor() {
		return dataAnchor;
	}

	/**
	 * This method gets the Sierra data directory. It ensures the returned
	 * directory exists.
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
	public static File getSierraDataDirectory() {
		return getDataDirectory(getSierraDataDirectoryAnchor());
	}

	/**
	 * This method gets the Sierra local team server data directory. It ensures
	 * the returned directory exists.
	 * 
	 * @return the Sierra local team server data directory.
	 * @see #getDataDirectory(File)
	 */
	public static File getSierraLocalTeamServerDirectory() {
		final File data = new File(getSierraDataDirectory(), "server");
		return getDataDirectory(data);
	}

	/**
	 * This method gets the Sierra team server cache directory. It ensures the
	 * returned directory exists.
	 * <p>
	 * Note that this method is <i>not</i> just used for a local Sierra team
	 * server. It is used by all Sierra team server instances.
	 * 
	 * @return the Sierra team server cache directory.
	 * @see #getDataDirectory(File)
	 */
	public static File getSierraTeamServerCacheDirectory() {
		final File data = new File(System.getProperty("java.io.tmpdir")
				+ File.separator + "sierra-cache");
		return getDataDirectory(data);
	}

	private static boolean DISALLOW_FILE_ANCHOR = true;
	
	/**
	 * This method determines a directory where data is stored based upon a
	 * passed anchor path. This anchor path is passed in <tt>anchor</tt> and is
	 * either a directory or file that references a directory. It ensures the
	 * returned directory exists.
	 * <ul>
	 * <li>If <tt>anchor</tt> does not exist. Then a directory is created using
	 * <tt>anchor</tt> and returned. If the creation fails for any reason then
	 * <tt>System.getProperty("java.io.tmpdir")</tt> is returned (and an error
	 * is logged).</li>
	 * <li>If <tt>anchor</tt> exists and is a directory, then that directory is
	 * returned.</li>
	 * <li>If <tt>anchor</tt> exists and is a file, then the contents of that
	 * file are read. The resulting string is assumed to be a path to the data
	 * directory. The resulting string is converted to a {@link File} object and
	 * a recursive call to this method is made.</li>
	 * </ul>
	 * 
	 * @return the path to the data directory.
	 * @throws IllegalArgumentException
	 *             if <tt>anchor</tt> is {@code null}.
	 */
	public static File getDataDirectory(final File anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException(I18N.err(44, "anchor"));
		}
		final File tmp = new File(System.getProperty("java.io.tmpdir"));
		if (anchor.exists()) {
			if (anchor.isFile()) {
				if (DISALLOW_FILE_ANCHOR) {
					throw new IllegalArgumentException("Disallowing file anchor "+anchor);
				}
				/*
				 * The contents of the file contain the path to the data
				 * directory.
				 */
				final String referencedPath = getFileContents(anchor).trim();
				final File dataPath = new File(referencedPath);
				if (dataPath.isFile()) {
					SLLogger.getLogger().severe(
							I18N.err(157, referencedPath, anchor
									.getAbsolutePath(), tmp.getAbsolutePath()));
					return tmp;
				} else {
					return getDataDirectory(dataPath);
				}
			} else {
				/*
				 * Return the data directory.
				 */
				return anchor;
			}
		} else {
			/*
			 * The data directory needs to be created.
			 */
			if (createDirectory(anchor)) {
				return anchor;
			} else {
				SLLogger.getLogger().severe(
						I18N.err(32, anchor.getAbsolutePath(), tmp
								.getAbsolutePath()));
				return tmp;
			}
		}
	}

	/**
	 * This method constructs a job to move a data directory from its current
	 * location to another.
	 * <p>
	 * The destination is deleted if it is not a directory or if
	 * <tt>moveOldToNew</tt> is {@code true} and there is some existing data to
	 * move.
	 * 
	 * @param anchor
	 *            the anchor path, see {@link #getDataDirectory(File)}.
	 * @param destination
	 *            the new data directory.
	 * @param moveOldToNew
	 *            {@code true} if it is desired to delete any contents at
	 *            <tt>destination</tt> and overwrite them with the contents of
	 *            the old data directory, {@link false} otherwise. The contents
	 *            will only be deleted if there is some existing data to move or
	 *            if <tt>destination</tt> is not a directory.
	 * @param optionalStartUp
	 *            a job run before the data directory is moved, {@code null}
	 *            indicates no job is needed. This job is typically used to
	 *            release resources in the data directory.
	 * @param optionalFinishUp
	 *            a job run after the data directory is moved, {@code null}
	 *            indicates no job is needed. This job is typically used to
	 *            re-attach to resources in the data directory.
	 * @return an {@link SLJob} to move the data directory.
	 */
	public static SLJob moveDataDirectory(final File anchor,
			final File destination, final boolean moveOldToNew,
			final SLJob optionalStartUp, final SLJob optionalFinishUp) {
		final File existing = getDataDirectory(anchor);
		final SLJob job = new AbstractSLJob(I18N.msg(
				"common.jobs.name.moveDataDirectory", existing
						.getAbsolutePath(), destination.getAbsolutePath())) {

			public SLStatus run(final SLProgressMonitor monitor) {
				monitor.begin();
				boolean success;
				try {
					/*
					 * Optionally run a start-up job to setup for this move.
					 * Resources in the data directory might need to be
					 * released.
					 */
					if (optionalStartUp != null) {
						final SLStatus startResult = AbstractSLJob.invoke(
								optionalStartUp, monitor, 1);
						if (startResult.getSeverity() != SLSeverity.OK) {
							return startResult;
						}
						if (monitor.isCanceled()) {
							return SLStatus.CANCEL_STATUS;
						}
					}

					if (destination.exists()
							&& (moveOldToNew && existing.exists() || !destination
									.isDirectory())) {
						/*
						 * Only clear out the destination directory if existing
						 * data exists to move into it OR if the destination
						 * path isn't a directory.
						 * 
						 * We don't need to be deleting stuff for no reason.
						 */
						success = FileUtility.recursiveDelete(destination);
						if (!success) {
							/*
							 * Failed to clear out the destination directory.
							 */
							final int code = 92;
							final String msg = I18N.err(code, destination
									.getAbsolutePath());
							return SLStatus.createErrorStatus(code, msg);
						}
					}

					if (moveOldToNew && existing.exists()) {
						success = existing.renameTo(destination);
						if (!success) {
							/*
							 * Failed to move the existing directory to its
							 * destination.
							 */
							final int code = 95;
							final String msg = I18N.err(code, existing
									.getAbsolutePath(), destination
									.getAbsolutePath());
							return SLStatus.createErrorStatus(code, msg);
						}
					}

					/*
					 * Create the destination data directory, if it doesn't
					 * already exist.
					 */
					if (!destination.exists()) {
						success = FileUtility.createDirectory(destination);
						if (!success) {
							/*
							 * Failed to create the destination data directory.
							 */
							final int code = 156;
							final String msg = I18N.err(code, destination
									.getAbsolutePath());
							return SLStatus.createErrorStatus(code, msg);
						}

					}

					/*
					 * Point the anchor to the destination data directory unless
					 * the anchor is the destination data directory.
					 */
					if (!DISALLOW_FILE_ANCHOR && 
						!anchor.getAbsolutePath().equals(destination.getAbsolutePath())) {
						FileUtility.putFileContents(anchor, destination
								.getAbsolutePath());
					}

					/*
					 * Optionally run a finish-up job to setup for this move.
					 * Resources in the data directory might need to be attached
					 * to.
					 */
					if (optionalFinishUp != null) {
						final SLStatus startResult = AbstractSLJob.invoke(
								optionalFinishUp, monitor, 1);
						if (startResult.getSeverity() != SLSeverity.OK) {
							return startResult;
						}
					}
				} finally {
					monitor.done();
				}
				return SLStatus.OK_STATUS;
			}
		};
		return job;
	}

	/**
	 * Convenience method to move a data directory when no start or finish jobs
	 * are needed. This method has the same effect as calling:
	 * 
	 * <pre>
	 * moveDataDirectory(anchor, destination, moveOldToNew, null, null)
	 * </pre>
	 * 
	 * @see #moveDataDirectory(File, File, boolean, SLJob, SLJob)
	 */
	public static SLJob moveDataDirectory(final File anchor,
			final File destination, final boolean moveOldToNew) {
		return moveDataDirectory(anchor, destination, moveOldToNew, null, null);
	}

	public static void zipDir(File tempDir, File zipFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zipDir(tempDir, tempDir, zos);
		zos.close();
	}

	public static void zipDir(File baseDir, File zipDir, ZipOutputStream zos)
			throws IOException {
		// get a listing of the directory content
		File[] dirList = zipDir.listFiles();
		byte[] readBuffer = new byte[4096];
		int bytesIn = 0;
		// loop through dirList, and zip the files
		for (File f : dirList) {
			if (f.isDirectory()) {
				// if the File object is a directory, call this
				// function again to add its content recursively
				zipDir(baseDir, f, zos);
				// loop again
				continue;
			}
			// if we reached here, the File object f was not a directory
			// create a FileInputStream on top of f
			FileInputStream fis = new FileInputStream(f);
			// create a new zip entry
			String path = f.getAbsolutePath();
			String name;
			if (path.startsWith(baseDir.getAbsolutePath())) {
				name = path.substring(baseDir.getAbsolutePath().length() + 1);
			} else {
				name = path;
			}
			ZipEntry anEntry = new ZipEntry(name);
			// place the zip entry in the ZipOutputStream object
			zos.putNextEntry(anEntry);
			// now write the content of the file to the ZipOutputStream
			while ((bytesIn = fis.read(readBuffer)) != -1) {
				zos.write(readBuffer, 0, bytesIn);
			}
			// close the Stream
			fis.close();
		}
	}
}
