package com.surelogic.common;

import java.io.File;
import java.util.Collections;

public interface ISourceZipFileHandles {
	Iterable<File> getSourceZips();
	File getSourceZipForProject(String proj);
	
	static final ISourceZipFileHandles EMPTY = new ISourceZipFileHandles() {
		public Iterable<File> getSourceZips() {
		     return Collections.emptyList();
		}

		public File getSourceZipForProject(String proj) {
			return null;
		}		
	};
}
