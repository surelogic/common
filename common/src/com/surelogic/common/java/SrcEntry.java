package com.surelogic.common.java;

import java.io.File;
import java.io.IOException;

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
		// Nothing to do?
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
		File zipFile = new File(zipDir, project.getProject()+'.'+projectRelativePathToBin.replace('/', '.') + ".zip");
		if (!zipFile.exists()) {
			zipFile.getParentFile().mkdirs();
			FileUtility.zipDir(binDir, zipFile);
		} else {
			// System.out.println("Already exists: "+zipFile);
		}				
		super.zipSources(zipDir);
	}	
}
