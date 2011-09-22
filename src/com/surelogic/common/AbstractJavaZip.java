package com.surelogic.common;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.tool.ToolProperties;
import com.surelogic.common.xml.XMLUtil;

public abstract class AbstractJavaZip<T> {
	protected static final Logger LOG = SLLogger.getLogger();
	public static final String CLASS_MAPPING = "classMapping.txt";
	public static final String SOURCE_FILES = "sourceFiles.xml";
	public static final String FILE_HASHES = "sourceHashes.txt";
	public static final String FILE_STAMPS = "sourceTimestamps.txt";

	public void generateSourceZipContents(final ZipOutputStream out)
			throws IOException {
		generateSourceZipContents(out, getRoot());
	}

	private static class FileInfo {
		final long timestamp;
		final byte[] hash;
		
		FileInfo(long time, byte[] h) {
			timestamp = time;
			hash = h;
		}
	}
	
	protected static class TempInfo {
		final Map<String, Map<String, String>> fileMap = new TreeMap<String, Map<String, String>>();
		final Map<String, FileInfo> fileInfo = new HashMap<String, FileInfo>();
		
		void generateFileInfo(ZipOutputStream out) throws IOException {
			PrintWriter pw;
			out.putNextEntry(new ZipEntry(FILE_STAMPS));
			pw = new PrintWriter(out);
			for(Map.Entry<String,FileInfo> e : fileInfo.entrySet()) {
				pw.println(e.getKey()+"="+e.getValue().timestamp);
			}
			out.closeEntry();
			
			out.putNextEntry(new ZipEntry(FILE_HASHES));
			pw = new PrintWriter(out);
			for(Map.Entry<String,FileInfo> e : fileInfo.entrySet()) {
				pw.println(e.getKey()+"="+e.getValue().hash);
			}
			out.closeEntry();
		}
	}
	
	public static final String[] CONFIG_FILES = {
		".project", ".classpath", ToolProperties.PROPS_FILE
	};
	
	public void generateSourceZipContents(final ZipOutputStream out,
			final T root) throws IOException {
		for(String name : CONFIG_FILES) {
			T config = getFile(root, name);
			if (isFile(config)) {
				out.putNextEntry(new ZipEntry(name));
				FileUtility.copyToStream(false, name, getFileContents(config), "zip", out, false);
				out.closeEntry();
			}
		}
		
		TempInfo info = new TempInfo();
		addAnnotatedResourcesToZip(out, info, root);

		info.generateFileInfo(out);
		
		out.putNextEntry(new ZipEntry(SOURCE_FILES));
		generateFileList(new PrintWriter(out), info.fileMap);
		out.closeEntry();

		out.putNextEntry(new ZipEntry(CLASS_MAPPING));
		generateClassMappings(new PrintWriter(out), info.fileMap);
		out.closeEntry();
		out.flush(); // unnecessary?
	}

	protected abstract boolean isAccessible(T res);

	protected abstract String getName(T res);

	protected abstract String getFullPath(T res) throws IOException;

	/**
	 * Checks if this resource is a file.
	 * 
	 * @param res
	 *            the resource.
	 * @return {@code true} if the resource is a file, {@code false} otherwise.
	 */
	protected abstract boolean isFile(T res);

	/**
	 * Checks if this resource is a Java source file and if it is returns the
	 * package it is declared within or {@code null} if the resource is not a
	 * Java compilation unit.
	 * 
	 * @param res
	 *            the resource.
	 * @return he package name the source file is declared within using
	 *         <tt>(default)</tt> for the default package or {@code null} if the
	 *         resource is not a Java compilation unit..
	 */

	protected abstract String getJavaPackageNameOrNull(T res);

	protected abstract T getFile(T res, String name);
	
	protected abstract InputStream getFileContents(T res) throws IOException;

	protected abstract T[] getMembers(T res) throws IOException;

	protected abstract String[] getIncludedTypes(T res);

	protected abstract long getTimestamp(T res);
	
	protected final void addAnnotatedFileToZip(final ZipOutputStream out, 
			TempInfo info, final T resource) {
		String pathName;
		try {
			pathName = getFullPath(resource);
		} catch (final IOException e) {
			LOG.log(Level.SEVERE, "Error adding " + getName(resource)
					+ " to ZIP.", e);
			return;
		}
		if (pathName.startsWith("/")) {
			pathName = pathName.substring(1);
		}
		
		final String packageName = getJavaPackageNameOrNull(resource);
		if (packageName == null) {
			return;
		}
		/*
		 * if (resource.toString().contains("pphtml")) {
		 * System.out.println("Looking at: "+resource); }
		 */
		try {
			final InputStream is = getFileContents(resource);
			try {
				out.putNextEntry(new ZipEntry(pathName));
				String className = null;
				byte[] hash = FileUtility.copyToStream(true, getFullPath(resource), is,
						pathName, out, false);
				if (hash != null) {
					long time = getTimestamp(resource);
					info.fileInfo.put(pathName, new FileInfo(time, hash));
				} else {
					return;
				}
				className = getName(resource);
				final String classKey = className;
				// remove ".java"
				className = className.substring(0, className.length() - 5);
				Map<String, String> classNameToSource;
				if (info.fileMap.containsKey(packageName)) {
					classNameToSource = info.fileMap.get(packageName);
				} else {
					classNameToSource = new TreeMap<String, String>();
					info.fileMap.put(packageName, classNameToSource);
				}

				// Changed to map non-main classes
				final String srcPath = "/" + pathName;
				final String zipPath = pathName;
				classNameToSource.put(classKey, srcPath);
				final String[] types = getIncludedTypes(resource);
				if (types == null || types.length == 0) {
					if (className.equals("package-info")) {
						classNameToSource.put(
								packageName + '.' + className, zipPath);
					} else {
						classNameToSource.put(classKey, zipPath);
					}
				} else {
					for (final String t : types) {
						classNameToSource.put(t, zipPath);
					}
				}
				out.closeEntry();
			} finally {
				if (is != null) {
					is.close();
				}
			}
		} catch (final IOException e) {
			LOG.log(Level.SEVERE, "Error adding " + pathName + " to ZIP.",
					e);
			return;
		}
	}
	
	protected void addAnnotatedResourcesToZip(final ZipOutputStream out, 
			TempInfo info, final T resource) {
		if (!isAccessible(resource)) {
			return;
		}
		if (isFile(resource)) {
			addAnnotatedFileToZip(out, info, resource);
		} else { // Resource is an container
			T[] members;
			try {
				members = getMembers(resource);
			} catch (final IOException e) {
				LOG.log(Level.SEVERE, "Error accessing child resources", e);
				return;
			}
			for (final T member : members) {
				addAnnotatedResourcesToZip(out, info, member);
			}
		}
	}

	// keywords, Strings, comments
	@SuppressWarnings("unused")
	private String syntaxHighlight(final String escape) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Tags for source files
	 */
	public static final String SRCFILES_TAG = "sourceFiles";
	public static final String PACKAGE_TAG = "package";
	public static final String CLASS_TAG = "class";
	private static final String PACKAGE_PREFIX = PACKAGE_TAG + " name=\"";
	private static final String PACKAGE_SUFFIX = "\">";
	public static final String PACKAGE_FORMAT = "\t<" + PACKAGE_PREFIX + "%s"
			+ PACKAGE_SUFFIX + "\n";
	private static final String CLASS_NAME_PREFIX = CLASS_TAG + " name=\"";
	private static final String CLASS_NAME_SUFFIX = "\" source=\"";
	private static final String CLASS_SRC_SUFFIX = "\"/>";
	public static final String CLASS_FORMAT = "\t\t<" + CLASS_NAME_PREFIX
			+ "%s" + CLASS_NAME_SUFFIX + "%s" + CLASS_SRC_SUFFIX + "\n";

	private void generateFileList(final PrintWriter pw, 
			final Map<String, Map<String, String>> fileMap) {
		pw.println(XMLUtil.openNode(SRCFILES_TAG));
		final Iterator<Map.Entry<String, Map<String, String>>> packageIter = fileMap
				.entrySet().iterator();
		while (packageIter.hasNext()) {
			final Map.Entry<String, Map<String, String>> packageEntry = packageIter
					.next();
			pw.format(PACKAGE_FORMAT, packageEntry.getKey());
			final Map<String, String> classMap = packageEntry.getValue();
			final Iterator<Map.Entry<String, String>> classIter = classMap
					.entrySet().iterator();
			while (classIter.hasNext()) {
				final Map.Entry<String, String> classEntry = classIter.next();
				if (classEntry.getKey().endsWith(".java")) {
					pw.format(CLASS_FORMAT, classEntry.getKey(),
							classEntry.getValue());
				}
			}
			pw.println("\t" + XMLUtil.closeNode(PACKAGE_TAG));
		}
		pw.println(XMLUtil.closeNode(SRCFILES_TAG));
		pw.flush();
	}

	private static void matchInLine(final BufferedReader br, final String tag)
			throws IOException {
		final String line = br.readLine();
		if (line == null || !line.contains(tag)) {
			throw new IOException("Couldn't find " + tag);
		}
	}

	private static void readPackage(final BufferedReader br,
			final Map<String, Map<String, String>> fileMap, String line)
			throws IOException {
		int start = line.indexOf(PACKAGE_PREFIX);
		if (start < 0) {
			throw new IOException("Couldn't find " + PACKAGE_PREFIX);
		}
		start += PACKAGE_PREFIX.length();
		// String temp = line.substring(start);
		int end = line.indexOf(PACKAGE_SUFFIX, start);
		final String pkg = line.substring(start, end);

		// Read classes
		final Map<String, String> map = new HashMap<String, String>();
		while ((line = br.readLine()) != null) {
			if (line.contains(PACKAGE_TAG)) {
				break; // Done with the package
			}
			start = line.indexOf(CLASS_NAME_PREFIX);
			start += CLASS_NAME_PREFIX.length();
			end = line.indexOf(CLASS_NAME_SUFFIX, start);
			final String name = line.substring(start, end);
			start = end + CLASS_NAME_SUFFIX.length();
			end = line.indexOf(CLASS_SRC_SUFFIX, start);
			String path = line.substring(start, end);
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			map.put(name, path);
		}
		fileMap.put(pkg, map);
	}

	private static Map<String, Map<String, String>> readFileList(
			final BufferedReader br) throws IOException {
		final Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
		matchInLine(br, SRCFILES_TAG);
		String line;
		while ((line = br.readLine()) != null) {
			if (line.contains(SRCFILES_TAG)) {
				return fileMap; // Done with the file
			}
			readPackage(br, fileMap, line);
		}
		return fileMap;
	}

	public static Map<String, Map<String, String>> readSourceFileMappings(
			final ZipFile zf) throws IOException {
		final ZipEntry ze = zf.getEntry(SOURCE_FILES);
		final InputStream in = zf.getInputStream(ze);
		final BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return readFileList(br);
	}

	private void generateClassMappings(final PrintWriter pw,
			final Map<String, Map<String, String>> fileMap) {
		// int count = 0;
		for (final Map.Entry<String, Map<String, String>> e : fileMap
				.entrySet()) {
			for (final Map.Entry<String, String> e2 : e.getValue().entrySet()) {
				if (!e2.getKey().endsWith(".java")) {
					pw.println(e2.getKey() + "=" + e2.getValue());
					// count++;
				}
			}
		}
		// System.out.println("Class mapping#: "+count);
		pw.flush();
	}

	private static Map<String, String> readClassMappings(final BufferedReader br)
			throws IOException {
		final Map<String, String> map = new HashMap<String, String>();
		String line;
		while ((line = br.readLine()) != null) {
			final int separator = line.indexOf('=');
			final String key = line.substring(0, separator);
			final String val = line.substring(separator + 1, line.length());
			map.put(key, val);
		}
		return map;
	}

	public static Map<String, String> readClassMappings(final ZipFile zf)
			throws IOException {
		final ZipEntry ze = zf.getEntry(CLASS_MAPPING);
		if (ze == null) {
			return Collections.emptyMap();
		}
		final InputStream in = zf.getInputStream(ze);
		final BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return readClassMappings(br);
	}

	protected abstract T getRoot();

	public String[] getAnalyses() {
		return new String[0];
	}
}
