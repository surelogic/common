package com.surelogic.common.java;

import java.io.*;

public abstract class AbstractJavaFile implements IJavaFile {
	public final String qname;
	public final File file;
	final String project;
	
	AbstractJavaFile(String name, File f, String proj) {	
		if (name != null && name.startsWith(".")) {
			throw new IllegalArgumentException();
		}
		qname = name;
		file = f;		
		project = proj;
	}
	
	public final String getQualifiedName() {
		return qname;
	}
	
	public final String getProject() {
		return project;
	}
	
	// Default implementation
	public File getFile() {
		return file;
	}	

	// Default implementation
	public InputStream getStream() throws IOException {
		return new FileInputStream(file);
	}
}
