package com.surelogic.common.java;

import java.io.File;

/**
 * TODO How to split this code?
 * 
 * @author edwin
 */
public interface IJavacClassParser {
	/**
	 * @return true if the Config has already been handled for the given project
	 */
	boolean ensureInitialized(ISLJavaProject jp, Config config);
	void mapFile(String name, String qname, String pname, JavaSourceFile p);
	void map(String name, String absolutePath, String project, String qname);
	void mapClass(String name, String qname, String project, File f);
}
