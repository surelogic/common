package com.surelogic.common;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.XMLUtil;

public abstract class AbstractJavaZip<T> {
	protected static final Logger LOG = SLLogger.getLogger();
	public static final String CLASS_MAPPING = "classMapping.txt";
	public static final String SOURCE_FILES = "sourceFiles.xml";	
	
	public void generateSourceZipContents(ZipOutputStream out)    
	throws IOException {
		generateSourceZipContents(out, getRoot());
	}

	public void generateSourceZipContents(ZipOutputStream out, T root) 
	throws IOException {
		Map<String,Map<String,String>> fileMap = new TreeMap<String,Map<String,String>>();
		addAnnotatedResourcesToZip(out, fileMap, root);

		out.putNextEntry(new ZipEntry(SOURCE_FILES));
		generateFileList(new PrintWriter(out), fileMap);
		out.closeEntry();
		
		out.putNextEntry(new ZipEntry(CLASS_MAPPING));
		generateClassMappings(new PrintWriter(out), fileMap);
		out.closeEntry();
		out.flush(); // unnecessary?			
	}

	protected abstract boolean isAccessible(T res);
	protected abstract String getName(T res);
	protected abstract String getFullPath(T res) throws IOException;
	protected abstract boolean isFile(T res);
	protected abstract boolean isJavaSourceFile(T res);
	protected abstract InputStream getFileContents(T res) throws IOException;
	protected abstract T[] getMembers(T res) throws IOException;
	protected abstract String[] getIncludedTypes(T res);
	
	public void addAnnotatedResourcesToZip(ZipOutputStream out, 
			Map<String,Map<String,String>> fileMap, T resource) {

		if(!isAccessible(resource)) return;
		String pathName;
		try {
			pathName = getFullPath(resource);
		} catch(IOException e) {
			LOG.severe("Error adding " + getName(resource) + " to ZIP.");
			e.printStackTrace();
			return;
		}
		if(pathName.startsWith("/")) pathName = pathName.substring(1);
		if(isFile(resource)) {
			if (!isJavaSourceFile(resource)) {
				return;
			}
			try {
				InputStream is = getFileContents(resource);
				LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
				// Can't use XMLWriter because tag attribute order is not fixed
				PrintWriter pw = new PrintWriter(out); 

				out.putNextEntry(new ZipEntry(pathName));
				String line;
				String packageString = null, className = null;
				while ((line = reader.readLine()) != null) {
					if (packageString == null) {
						String trimmed = line.trim();
						if (trimmed.startsWith("package")) {
							packageString = trimmed.substring(7, trimmed.indexOf(';')).trim();
							String packageKey = packageString;
							className = getName(resource);
							String classKey = className;
							// remove ".java"
							className = className.substring(0, className.length() - 5);
							Map<String,String> classNameToSource;
							if (fileMap.containsKey(packageString)) {
								classNameToSource = fileMap.get(packageKey);
							} else {
								classNameToSource = new TreeMap<String,String>();
								fileMap.put(packageKey, classNameToSource);
							}					
							// Changed to map non-main classes
							final String srcPath = "/" + pathName;
							final String zipPath = pathName;
							classNameToSource.put(classKey, srcPath);
							/* FIX
							final ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
							if (icu == null) {
								classNameToSource.put(classKey, zipPath);
							} else {
								try {
									for(IType t : icu.getAllTypes()) {										
										classNameToSource.put(t.getFullyQualifiedName('$'), zipPath);
									}
								} catch (JavaModelException e) {
									e.printStackTrace();
								}
							}
							*/
							String[] types = getIncludedTypes(resource);
							if (types == null) {
								classNameToSource.put(classKey, zipPath);
							} else {
								for(String t : types) {										
									classNameToSource.put(t, zipPath);
								}
							}
						}
					}
					pw.println(line);
				}
				pw.flush();
				out.closeEntry();
			} catch (IOException e) {
				LOG.severe("Error adding " + pathName + " to ZIP.");
				e.printStackTrace();
			}
		}
		else { // Resource is an container
			T[] members;
			try {
				members = getMembers(resource);
			} catch (IOException e) {
				LOG.severe("Error accessing child resources");
				e.printStackTrace();
				return;
			}
			for(int i=0; i < members.length; i++) {
			  addAnnotatedResourcesToZip(out, fileMap, members[i]);
			}
		}
	}

	// keywords, Strings, comments
	private String syntaxHighlight(String escape) {
		// TODO Auto-generated method stub
		return null;
	}



	/*
	 * Tags for source files
	 */
	public static final String SRCFILES_TAG   = "sourceFiles";
	public static final String PACKAGE_TAG    = "package";
	public static final String CLASS_TAG      = "class";
	private static final String PACKAGE_PREFIX = PACKAGE_TAG+" name=\"";
	private static final String PACKAGE_SUFFIX = "\">";
	public static final String PACKAGE_FORMAT = "\t<"+PACKAGE_PREFIX+"%s"+PACKAGE_SUFFIX+"\n";
	private static final String CLASS_NAME_PREFIX = CLASS_TAG+" name=\"";
	private static final String CLASS_NAME_SUFFIX = "\" source=\"";
	private static final String CLASS_SRC_SUFFIX  = "\"/>";
	public static final String CLASS_FORMAT   = "\t\t<"+CLASS_NAME_PREFIX+"%s"+CLASS_NAME_SUFFIX+"%s"+CLASS_SRC_SUFFIX+"\n";  

	private void generateFileList(PrintWriter pw, Map<String, Map<String, String>> fileMap) {
		pw.println(XMLUtil.openNode(SRCFILES_TAG));
		Iterator<Map.Entry<String, Map<String, String>>> packageIter = fileMap.entrySet().iterator();
		while (packageIter.hasNext()) {
			Map.Entry<String, Map<String, String>> packageEntry = packageIter.next();
			pw.format(PACKAGE_FORMAT, packageEntry.getKey());
			Map<String, String> classMap = packageEntry.getValue();
			Iterator<Map.Entry<String, String>> classIter = classMap.entrySet().iterator();
			while (classIter.hasNext()) {
				Map.Entry<String, String> classEntry = classIter.next();
				if (classEntry.getKey().endsWith(".java")) {
					pw.format(CLASS_FORMAT, classEntry.getKey(), classEntry.getValue());
				}
			}
			pw.println("\t"+XMLUtil.closeNode(PACKAGE_TAG));
		}
		pw.println(XMLUtil.closeNode(SRCFILES_TAG));
		pw.flush();
	}
	
	private static void matchInLine(BufferedReader br, String tag) throws IOException {
		String line = br.readLine();
		if (line == null || !line.contains(tag)) {
			throw new IOException("Couldn't find "+tag);
		}
	}
	
	private static void readPackage(BufferedReader br, Map<String, Map<String, String>> fileMap, 
			                        String line) throws IOException {
		int start = line.indexOf(PACKAGE_PREFIX);
		if (start < 0) {
			throw new IOException("Couldn't find "+PACKAGE_PREFIX);
		}
		start += PACKAGE_PREFIX.length();
		String temp = line.substring(start);
		int end = line.indexOf(PACKAGE_SUFFIX, start);
		final String pkg = line.substring(start, end);
		
		// Read classes
		Map<String,String> map = new HashMap<String,String>();
		while ((line = br.readLine()) != null) {
			if (line.contains(PACKAGE_TAG)) {
				break; // Done with the package
			}
			start = line.indexOf(CLASS_NAME_PREFIX); 
			start += CLASS_NAME_PREFIX.length();
			end   = line.indexOf(CLASS_NAME_SUFFIX, start);
			final String name = line.substring(start, end);
			start = end + CLASS_NAME_SUFFIX.length();
			end   = line.indexOf(CLASS_SRC_SUFFIX, start);
			String path = line.substring(start, end);
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			map.put(name, path);
		}
		fileMap.put(pkg, map);
	}
	
	private static Map<String, Map<String, String>> readFileList(BufferedReader br) throws IOException {
		Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
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
		
	public static Map<String, Map<String, String>> readSourceFileMappings(ZipFile zf) throws IOException {
		ZipEntry ze = zf.getEntry(SOURCE_FILES);
		InputStream in = zf.getInputStream(ze);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return readFileList(br);
	}
	
	private void generateClassMappings(PrintWriter pw, Map<String, Map<String, String>> fileMap) {
		for(Map.Entry<String, Map<String, String>> e : fileMap.entrySet()) {
			for(Map.Entry<String, String> e2 : e.getValue().entrySet()) {
				if (!e2.getKey().endsWith(".java")) {
					pw.println(e2.getKey()+"="+e2.getValue());
				}
			}
		}
		pw.flush();
	}
	
	private static Map<String, String> readClassMappings(BufferedReader br) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String line;
		while ((line = br.readLine()) != null) {
			final int separator = line.indexOf('=');
			final String key = line.substring(0, separator);
			final String val = line.substring(separator+1, line.length());
			map.put(key, val);
		}
		return map;
	}
	
	public static Map<String, String> readClassMappings(ZipFile zf) throws IOException {
		ZipEntry ze = zf.getEntry(CLASS_MAPPING);
		InputStream in = zf.getInputStream(ze);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return readClassMappings(br);
	}
	
	protected abstract T getRoot();
	public String[] getAnalyses() {
		return new String[0];
	}
}
