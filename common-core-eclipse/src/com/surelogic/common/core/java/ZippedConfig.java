package com.surelogic.common.core.java;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.resources.*;

import com.surelogic.common.AbstractJavaZip;
import com.surelogic.common.FileUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.FileUtility.UnzipCallback;
import com.surelogic.common.core.SourceZip;
import com.surelogic.common.java.*;

public class ZippedConfig extends Config {
	private static final boolean useSourceZipsDirectly = !XUtil.runJSureInMemory || SystemUtils.IS_OS_WINDOWS;
	public ZippedConfig(String name, File location, boolean isExported, boolean hasJLO) {
		super(name, location, isExported, hasJLO);
	}

	@Override
	protected Config newConfig(String name, File location, boolean isExported, boolean hasJLO) {
		return new ZippedConfig(name, location, isExported, hasJLO);
	}

	@Override
	public void zipSources(File zipDir) throws IOException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProject());
		final SourceZip srcZip = new SourceZip(project);
		File zipFile = new File(zipDir, project.getName() + ".zip");
		if (!zipFile.exists()) {
			zipFile.getParentFile().mkdirs();
			srcZip.generateSourceZip(zipFile.getAbsolutePath(), project);
		} else {
			// System.out.println("Already exists: "+zipFile);
		}
		super.zipSources(zipDir);
	}

	@Override
	public void copySources(File zipDir, File targetDir) throws IOException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProject());
		targetDir.mkdir();

		File projectDir = new File(targetDir, project.getName());
		File zipFile = new File(zipDir, project.getName() + ".zip");
		ZipFile zf = new ZipFile(zipFile);

		// Get class mapping (qname->zip path)
		Properties props = new Properties();
		ZipEntry mapping = zf.getEntry(AbstractJavaZip.CLASS_MAPPING);
		props.load(zf.getInputStream(mapping));

		// Reverse mapping
		final Map<String, List<String>> path2qnames = new HashMap<String, List<String>>();

		// Needed to keep javac from dying on a bad qualified name
		boolean pathsContainDot = false;
		// int count = 0;
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			final String path = (String) e.getValue();
			if (useSourceZipsDirectly) {
				final int len = path.length();
				// Assumes that it ends with '.java'
				// 
				// Not a bug, but it STARTS looking at the index specified 
				//if (path.lastIndexOf('.', len - 5) >= 0) {          
				final int firstDot = path.indexOf('.');
				if (firstDot >= 0 && firstDot < len - 5) {
					pathsContainDot = true;
				}
			}
			List<String> l = path2qnames.get(path);
			if (l == null) {
				l = new ArrayList<String>();
				path2qnames.put(path, l);
			}
			l.add((String) e.getKey());
			// count++;
		}
		// System.out.println(getProject()+": class mapping "+count);
		/*
		 * for(JavaSourceFile f : getFiles()) {
		 * System.out.println(getProject()+": "+f.relativePath); }
		 */
		final List<JavaSourceFile> srcFiles = new ArrayList<JavaSourceFile>();
		final UnzipCallback callback = new UnzipCallback() {
			@Override
			public void unzipped(ZipEntry ze, File f) {
				// Finish setting up srcFiles
				if (ze.getName().endsWith(".java")) {
					final List<String> names = path2qnames.get(ze.getName());
					if (names != null) {
						for (String name : names) {
							/*
                  if (name.contains("$")) {
                    System.out.println("Mapping " + name + " to " + f.getAbsolutePath());
                  }
							 */
							// The last two parameters don't matter because
							// they'll just be thrown away when we call
							// setFiles() below
							srcFiles.add(new JavaSourceFile(name/*
							 * .replace( '$', '.')
							 */, f, null, false, getProject()));
						}
					} else if (ze.getName().endsWith("/package-info.java")) {
						System.out.println("What to do about package-info.java?");
					} else {
						System.err.println("Unable to get qname for " + ze.getName());
					}
				} else {
					// System.out.println("Not a java file: "+ze.getName());
				}
			}
		};
		if (useSourceZipsDirectly && !pathsContainDot) {
			System.out.println("Using source zips directly for "+getProject());
			// OK
			// jar:///C:/Documents%20and%20Settings/UncleBob/lib/vendorA.jar!com/vendora/LibraryClass.class
			final Enumeration<? extends ZipEntry> e = zf.entries();
			final String zipPath = zipFile.getAbsolutePath();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				File f = AbstractJavaZip.makeZipReference(zipPath, ze.getName());
				// System.out.println("URI = "+f.toURI());
				callback.unzipped(ze, f);
			}
		} else {
			FileUtility.unzipFile(zf, projectDir, callback);
			// To prevent issues with importing projects
			File dotProject = new File(projectDir, ".project");
			if (dotProject.exists()) {
				dotProject.renameTo(new File(projectDir, ".project.bak"));
			}
		}
		this.setFiles(srcFiles);
		super.copySources(zipDir, targetDir);
	}      
}
