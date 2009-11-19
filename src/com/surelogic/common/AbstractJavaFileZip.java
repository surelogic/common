package com.surelogic.common;

import java.io.*;

public abstract class AbstractJavaFileZip extends AbstractJavaZip<File> {

	@Override
	protected InputStream getFileContents(File res) throws IOException {
		return new FileInputStream(res);
	}

	@Override
	protected String getFullPath(File res) throws IOException {
		return res.getCanonicalPath();
	}

	private static final File[] noFiles = new File[0];
	
	@Override
	protected File[] getMembers(File res) throws IOException {
		if (res.isDirectory()) {
			return res.listFiles();
		}
		return noFiles;
	}

	@Override
	protected String getName(File res) {
		return res.getName();
	}

	@Override
	protected boolean isAccessible(File res) {
		return true;
	}

	@Override
	protected boolean isFile(File res) {
		return res.isFile();
	}

	@Override
	protected String getJavaPackageNameOrNull(File res) {
		throw new UnsupportedOperationException("not implemented yet");
	}
}
