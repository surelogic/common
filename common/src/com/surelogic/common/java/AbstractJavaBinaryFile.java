package com.surelogic.common.java;

import java.io.File;

public abstract class AbstractJavaBinaryFile extends AbstractJavaFile {
	AbstractJavaBinaryFile(String name, File f, String proj) {
		super(name, f, proj);
	}

	public boolean isSource() {
		return false;
	}
}
