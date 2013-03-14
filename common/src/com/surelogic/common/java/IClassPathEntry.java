package com.surelogic.common.java;

import java.io.*;
import java.net.URI;

import com.surelogic.common.xml.XmlCreator;

/**
 * An entry noting where some code is ordered in a Config's classpath
 * 
 * @author edwin
 */
public interface IClassPathEntry {
	/**
     *  whether a classpath entry is exported to dependent projects (or not)
     */
	boolean isExported();
	
	void init(IJavaProject jp, IJavacClassParser loader) throws IOException; 
	
	/**
	 * 
	 */
	void zipSources(File zipDir) throws IOException;
	
	/**
	 * 
	 */
	void copySources(File zipDir, File targetDir) throws IOException;
	
	/**
	 * 
	 */
	JavaSourceFile mapPath(URI path);

	/**
	 * 
	 */
	void relocateJars(File targetDir) throws IOException;
	
	void outputToXML(XmlCreator.Builder b);
	
	/**
	 * @return a File suitable for adding to the classpath, or null
	 */
	File getFileForClassPath();
}
