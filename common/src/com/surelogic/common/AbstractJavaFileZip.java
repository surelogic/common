package com.surelogic.common;

import java.io.*;

public abstract class AbstractJavaFileZip extends AbstractJavaZip<File> {

	@Override
	protected final InputStream getFileContents(File res) throws IOException {
		return new FileInputStream(res);
	}

	@Override
	protected String getFullPath(File res) throws IOException {
		return res.getCanonicalPath();
	}
	
	@Override
	protected final File[] getMembers(File res) throws IOException {
		if (res.isDirectory()) {
			return res.listFiles();
		}
		return FileUtility.noFiles;
	}

	@Override
	protected final String getName(File res) {
		return res.getName();
	}

	@Override
	protected final boolean isAccessible(File res) {
		return true;
	}

	@Override
	protected final boolean isFile(File res) {
		return res.isFile();
	}

    @Override
    protected final long getTimestamp(final File res) {
        return res.lastModified();
    }
	
    @Override
    protected final File getFile(final File res, final String name) {
        return new File(res, name);
    }
    
	@Override
	protected String getJavaPackageNameOrNull(File res) {
		throw new UnsupportedOperationException("not implemented yet");
	}
}
