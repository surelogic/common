package com.surelogic.common.java;

import java.io.File;

/**
 * TODO How to split this code?
 * 
 * @author edwin
 */
public interface IJavacClassParser {
	/**
	 * Returns whether the Config has already been handled for the given project
	 * @return true if already initialized, false if not (and set to be true)
	 */
	boolean ensureInitialized(ISLJavaProject jp, Config config);
	void mapFile(String destProj, String qname, String srcProj, JavaSourceFile p);
	void map(String destProj, String qname, String srcProj, String jarName);
	void mapClass(String destProj, String qname, String srcProj, File f);
}
