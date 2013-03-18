package com.surelogic.common.java;

import java.io.File;

public interface IJavacClassParser {
	boolean ensureInitialized(ISLJavaProject jp, Config config);
	void mapFile(String name, String qname, String pname, JavaSourceFile p);
	void map(String name, String absolutePath, String project, String qname);
	void mapClass(String name, String qname, String project, File f);

	String convertClassToQname(String name);


}
