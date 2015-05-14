package com.surelogic.common.java;

import java.io.*;
import java.net.URI;

import com.surelogic.common.FileUtility;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.xml.XmlCreator;

public final class JavaSourceFile extends AbstractJavaFile {
	public static final String SLASH_PACKAGE_INFO = '/'+SLUtility.PACKAGE_INFO_JAVA;
	
	public final String relativePath;
	public final boolean asBinary;
	
	public JavaSourceFile(String name, File f, String path, boolean asBinary, String proj) {
		super(name, f, proj);
		relativePath = FileUtility.normalizePath(path);
		this.asBinary = asBinary;
	}
	
	@Override
	public String toString() {
		return relativePath == null ? file.toString() : relativePath;
	}
	
	@Override 
	public int hashCode() {
		return file.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof JavaSourceFile) {
			JavaSourceFile f2 = (JavaSourceFile) o;
			if (file.equals(f2.file)) {
				if (!qname.equals(f2.qname) || !relativePath.equals(f2.relativePath)) {
					throw new IllegalStateException();
				}
				return true;
			}			
		}
		return false;
	}

	public Pair<URI, String> getLocation() {
		return new Pair<>(file.toURI(), relativePath);
	}

	public void outputToXML(XmlCreator.Builder proj) {
		XmlCreator.Builder b = proj.nest(PersistenceConstants.FILE);		
		b.addAttribute(PersistenceConstants.PATH, relativePath);
		b.addAttribute(PersistenceConstants.QNAME, qname);
		b.addAttribute(PersistenceConstants.LOCATION, file.getAbsolutePath());
		b.addAttribute(PersistenceConstants.AS_BINARY, asBinary);
		b.end();
	}

	public boolean isSource() {
		return true;
	}

	public Type getType() {
		return Type.SOURCE;
	}
}
