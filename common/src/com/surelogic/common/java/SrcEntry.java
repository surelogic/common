package com.surelogic.common.java;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.surelogic.common.FileUtility;
import com.surelogic.common.xml.XmlCreator;

/**
 * Used to record the source path
 * 
 * @author Edwin
 */
public class SrcEntry extends AbstractClassPathEntry {
	public static final String ZIP_BINARIES = "zip.binaries";
	
	@SuppressWarnings("unused")
	private final Config project;
	private final String projectRelativePathToSrc;
	private final String projectRelativePathToBin;
	private File zipFile = null;
	
	public SrcEntry(Config c, String pathToSrc, String pathToBin) {
		super(true); // TODO is this right?
		project = c;
		projectRelativePathToSrc = pathToSrc;
		projectRelativePathToBin = pathToBin;
	}

	public String getProjectRelativePath() {
		return projectRelativePathToSrc;
	}
	
	@Override
	public void init(ISLJavaProject jp, IJavacClassParser loader)
			throws IOException {
		if (zipFile != null) {
			final String jarPath = zipFile.getAbsolutePath();
			try {
				// Not exactly what initForJar() does
				final ZipFile zf = new ZipFile(zipFile);    	
				Enumeration<? extends ZipEntry> e = zf.entries();
				while (e.hasMoreElements()) {
					ZipEntry ze = e.nextElement();
					String name = ze.getName();
					if (name.endsWith(".class")) {    			
						final String qname = JarEntry.convertClassToQname(name);
						//int lastDot  = qname.lastIndexOf('.');
						//String pkg   = lastDot < 0 ? "" : qname.substring(0, lastDot);
						//jp.addPackage(pkg);
						loader.mapBinary(jp.getName(), qname, project.getProject(), jarPath);
					}
				}
				System.out.println(jp.getName()+": Done initializing with "+zipFile);
			} catch(ZipException e) {
				System.out.println("Zip exception with "+zipFile);
			}
		}
	}
	
	@Override
  public void outputToXML(XmlCreator.Builder proj) {
		XmlCreator.Builder b = proj.nest(PersistenceConstants.SRC);
		b.addAttribute(PersistenceConstants.PATH, projectRelativePathToSrc);
		if (projectRelativePathToBin != null) {				
			b.addAttribute(PersistenceConstants.BIN_PATH, projectRelativePathToBin);
		}
		b.addAttribute(PersistenceConstants.IS_EXPORTED, isExported());
		b.end();
	}
	
	@Override
	public int hashCode() {
		return projectRelativePathToSrc.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SrcEntry) {
			SrcEntry s2 = (SrcEntry) o;
			return projectRelativePathToSrc.equals(s2.projectRelativePathToSrc);
		}
		return false;
	}
	
	@Override
	public void zipSources(File zipDir) throws IOException {		
		final File binDir = new File(project.getLocation(), projectRelativePathToBin);
		zipFile = new File(zipDir, project.getProject()+'.'+projectRelativePathToBin.replace('/', '.') + ".zip");
		if (!zipFile.exists()) {
			zipFile.getParentFile().mkdirs();
			FileUtility.zipDir(binDir, zipFile);
		} else {
			// System.out.println("Already exists: "+zipFile);
		}				
		super.zipSources(zipDir);
	}
}
