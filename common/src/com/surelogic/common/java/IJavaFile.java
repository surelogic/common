package com.surelogic.common.java;

import java.io.*;

public interface IJavaFile {
	enum Type { SOURCE, CLASS, CLASS_FROM_JAR, CLASS_FOR_SRC }
	Type getType();
	String getQualifiedName();
	String getProject();
	boolean isSource();
	/**
	 * @return a File, possibly non-existent, but encoded to find in a jar
	 */
	File getFile();
	// Note: This may not allow for reusing the ZipFile
	InputStream getStream() throws IOException;
}
