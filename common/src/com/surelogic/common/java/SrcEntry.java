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
	
	private final Config project;
	private final String projectRelativePathToSrc;
	private final String projectRelativePathToBin;
	
	public SrcEntry(boolean exported, Config c, String pathToSrc, String pathToBin) {
		super(exported); 
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
		File zipFile = computeZipFile(new File(loader.getRunDir(), PersistenceConstants.ZIPS_DIR));
		System.out.println("Trying to find "+zipFile.getAbsolutePath());
		if (zipFile.isFile()) {
			//final String jarPath = zipFile.getAbsolutePath();
			System.out.println("Found "+zipFile);
			try {
				// Not exactly what initForJar() does
				final ZipFile zf = new ZipFile(zipFile);    	
				Enumeration<? extends ZipEntry> e = zf.entries();
				while (e.hasMoreElements()) {
					ZipEntry ze = e.nextElement();
					String name = ze.getName();
					//System.out.println("SrcEntry Looking at "+name);
					if (name.endsWith(".class")) {    			
						final String qname = JarEntry.convertClassToQname(name);
						//int lastDot  = qname.lastIndexOf('.');
						//String pkg   = lastDot < 0 ? "" : qname.substring(0, lastDot);
						//jp.addPackage(pkg);
						loader.map(jp.getName(), new JarredClassFile(qname, zipFile, project.getProject(), ze.getName()) {
							@Override
							public Type getType() {
								return Type.CLASS_FOR_SRC;
							}
						});
					}
				}
				System.out.println(jp.getName()+": Done initializing with "+zipFile);
				zf.close();
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
		final File zipFile = computeZipFile(zipDir);
		if (!zipFile.exists()) {
			zipFile.getParentFile().mkdirs();
			FileUtility.zipDir(binDir, zipFile);
			System.out.println("Zipped into "+zipFile);
		} else {
			// System.out.println("Already exists: "+zipFile);
		}				
		super.zipSources(zipDir);
	}
	
	private File computeZipFile(File zipDir) {
		return new File(zipDir, project.getProject()+'.'+projectRelativePathToBin.replace('/', '.') + ".zip");
	}
}
