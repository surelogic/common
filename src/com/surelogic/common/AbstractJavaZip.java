package com.surelogic.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.XMLUtil;

public abstract class AbstractJavaZip<T> {
	protected static final Logger LOG = SLLogger.getLogger();
	
	public void generateSourceZipContents(ZipOutputStream out)    
	throws IOException {
		generateSourceZipContents(out, getRoot(), true);
	}

	public void generateSourceZipContents(ZipOutputStream out, T root, boolean includeScript) 
	throws IOException {
		Map<String,Map<String,String>> fileMap = new TreeMap<String,Map<String,String>>();
		addAnnotatedResourcesToZip(out, fileMap, root, includeScript);

		out.putNextEntry(new ZipEntry("sourceFiles.xml"));
		generateFileList(new PrintWriter(out), fileMap);
		out.closeEntry();
		
		out.putNextEntry(new ZipEntry("classMapping.xml"));
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
			Map<String,Map<String,String>> fileMap, T resource, boolean includeScript) {

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

				out.putNextEntry(new ZipEntry(pathName + ".html"));
				pw.println("<html><head>");
				pw.println("<style type=\"text/css\">");
				pw.println("BODY { margin: 0px }");
				pw.println("SPAN.highlight { position:absolute; width:100%; color:white; background-color:teal; }");
				pw.println("</style>");
				pw.println("</head><body><pre>");
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
							final String srcPath = "/" + pathName + ".html";
							final String zipPath = pathName + ".html";
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
					String lineNum = new Integer(reader.getLineNumber()).toString();
					pw.println("<span id="
							+ lineNum
							+ ">"
							+ "<a name="
							+ lineNum
							+ ">"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;".substring(6 * Math.min(lineNum
									.length(), 4)) // pad line numbers to 4 digits
									+ lineNum + "</a> " + XMLUtil.escape(line) + "</span>");
				}
				pw.println("</pre>");
				if (includeScript) {
				  pw.println("<script><!--");
				  pw.println("if(parent.currentLine > 0) window.onload = parent.highlightLine;");
				  pw.println("parent.setFileHeader(\"" + packageString + "\", \"" + className + "\");");
				  pw.println("--></script>");
				}
				pw.print("</body></html>");
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
			  addAnnotatedResourcesToZip(out, fileMap, members[i], includeScript);
			}
		}
	}

	/*
	 * Tags for source files
	 */
	public static final String SRCFILES_TAG   = "sourceFiles";
	public static final String PACKAGE_TAG    = "package";
	public static final String CLASS_TAG      = "class";
	public static final String PACKAGE_FORMAT = "\t<"+PACKAGE_TAG+" name=\"%s\">\n";
	public static final String CLASS_FORMAT   = "\t\t<"+CLASS_TAG+" name=\"%s\" source=\"%s\"/>\n";  

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
		
	private void generateClassMappings(PrintWriter pw,
			                                  Map<String, Map<String, String>> fileMap) {
		for(Map.Entry<String, Map<String, String>> e : fileMap.entrySet()) {
			for(Map.Entry<String, String> e2 : e.getValue().entrySet()) {
				if (!e2.getKey().endsWith(".java")) {
					pw.println(e2.getKey()+"="+e2.getValue());
				}
			}
		}
		pw.flush();
	}
	
	protected abstract T getRoot();
	public String[] getAnalyses() {
		return new String[0];
	}
}
