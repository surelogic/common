package com.surelogic.common.java;

import java.io.File;

public interface IJavaProject {
	void addPackage(String pkg);
	String getName();
	void mapJar(File path, File origPath);
	Config getConfig();
}
