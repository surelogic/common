package com.surelogic.common.java;

import java.io.File;

/**
 * Named to distinguish it from Eclipse's IJavaProject
 */
public interface ISLJavaProject {
	void addPackage(String pkg);
	String getName();
	void mapJar(File path, File origPath);
	Config getConfig();
	void clear();
}
