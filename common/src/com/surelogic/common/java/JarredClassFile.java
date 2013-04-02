package com.surelogic.common.java;

import java.io.*;
import java.util.zip.*;

public class JarredClassFile extends AbstractJavaBinaryFile {
	final String pathInJar;
	
	JarredClassFile(String qname, File f, String proj, String path) {
		super(qname, f, proj);
		pathInJar = path;
	}
	
	/* This gets the File for the zip, not the class
	 * 
	 * TODO fix in JavacClassParser
	 * 
	public File getFile() {
		return null;
	}
	*/

	public InputStream getStream() throws IOException {
		final ZipFile zf = new ZipFile(file);
		final ZipEntry e = zf.getEntry(pathInJar);
		return zf.getInputStream(e);
	}
	
	public Type getType() {
		return Type.CLASS_FROM_JAR;
	}
}
