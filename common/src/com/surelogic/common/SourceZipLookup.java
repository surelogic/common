package com.surelogic.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Encapsulates the lookup of files from a number of source zips, 
 * caching the class mappings.  
 * 
 * @author Edwin
 */
public class SourceZipLookup<T> {
	private final Map<String,Mappings> projects = new HashMap<String, Mappings>();
	
	public SourceZipLookup(Iterable<File> zips) throws IOException {
		for (final File f : zips) {
			String project = f.getName();
			if (project.endsWith(".zip")) {
				project = project.substring(0, project.length()-4);
			}
			projects.put(project, new Mappings(f));
		}
	}
	
	public T lookup(String project, String qname) throws IOException {
		T rv = null;
		if (project != null) {
			Mappings m = projects.get(project);
			if (m != null) {
				rv = foundMappings(qname, m);				
				if (rv != null) {
					return rv;
				}
			}
		}
		for(Mappings m : projects.values()) {
			if (m != null) {
				rv = foundMappings(qname, m);
				if (rv != null) {
					return rv;
				}
			}
		}
		return rv;
	}

	private T foundMappings(String qname, Mappings m) throws IOException {
		String path = m.getRelativePathInZip(qname);
		final ZipFile zf = new ZipFile(m.zip);
		try {
			return found(zf, path);
		} finally {
			zf.close();
		}
	}
	
	protected T found(ZipFile zf, String path) throws IOException {		
		return null;
	}
	
	private static class Mappings {
		final File zip;
		final Map<String,String> qnameToRelativePath;	
		
		Mappings(File zip) throws IOException { 			
			final ZipFile zf = new ZipFile(zip);
			try {
				this.zip = zip;
				qnameToRelativePath = AbstractJavaZip.readClassMappings(zf);
			} finally {
				zf.close();
			}			
		}
		
		String getRelativePathInZip(String qname) {
			return qnameToRelativePath.get(qname);
		}
	}
	
	/**
	 * Implementation that returns the lines of the file specified
	 */
	public static class Lines extends SourceZipLookup<String[]> {
		public Lines(Iterable<File> zips) throws IOException {
			super(zips);
		}
		
		protected String[] found(ZipFile zf, String path) throws IOException {		
			final ZipEntry ze = zf.getEntry(path);
			InputStream in = zf.getInputStream(ze);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			List<String> lines = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			return lines.toArray(new String[lines.size()]);
		}
	}
}
