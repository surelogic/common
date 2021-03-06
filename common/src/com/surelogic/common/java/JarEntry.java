package com.surelogic.common.java;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.*;

import com.surelogic.common.*;
import com.surelogic.common.xml.XmlCreator;

/**
 * Despite the name, this handles both jars and directories
 * @author Edwin
 */
public class JarEntry extends AbstractClassPathEntry {
	private final Config project;
	private File path;
	private File origPath;
	
	JarEntry(Config p, File f, boolean isExported) {
		this(p, f, f, isExported);
	}
	
	public JarEntry(Config p, File f, File orig, boolean isExported) {
		super(isExported);
		project = p;
		path = f;
		origPath = orig;
	}
	
	public File getPath() {
		return path;
	}
	
	@Override
  public void outputToXML(XmlCreator.Builder proj) {
		XmlCreator.Builder b = proj.nest(PersistenceConstants.JAR);
		b.addAttribute(PersistenceConstants.PATH, path.getAbsolutePath());
		b.addAttribute(PersistenceConstants.ORIG_PATH, origPath.getAbsolutePath());
		b.addAttribute(PersistenceConstants.IS_EXPORTED, isExported());
		b.end();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof JarEntry) {
			JarEntry j2 = (JarEntry) o;
			if (!getPath().equals(j2.getPath())) {
				// Deal w/ copied jars?!?
				if (!getPath().getName().equals(j2.getPath().getName())) {
					return false;
				}
				if (getPath().isDirectory() || j2.getPath().isDirectory()) {
					return false;
				}
				if (getPath().length() != j2.getPath().length()) {
					return false;
				}
			}
			return isExported() == j2.isExported();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	@Override
	public String toString() {
		return path.getAbsolutePath();
	}
	
	@Override
  public void init(ISLJavaProject jp, IJavacClassParser loader) throws IOException {
		if (!isExported() && jp.getConfig() != project) {
			// Not supposed to be exported
			return;
		}
    	if (path.exists()) {
    		if (path.isDirectory()) {
    			initForJarDir(jp, loader, path, null);
    		} 
    		else {
    			initForJar(jp, loader, path);
    		}
    	}
    	jp.mapJar(path, origPath);
	}
	
	private void initForJarDir(ISLJavaProject jp, IJavacClassParser loader, File dir, String pkgPrefix) 
	throws IOException {
		System.out.println("Scanning "+dir.getAbsolutePath());
		int i=0;
		for(File f : dir.listFiles()) {
			boolean createdPkg = false;
			if (f.isDirectory()) {
				initForJarDir(jp, loader, f, pkgPrefix == null ? f.getName() : pkgPrefix+'.'+f.getName());
			}
			else if (f.getName().endsWith(".class")) {
				final String name  = f.getName().substring(0, f.getName().length()-6).replace('$', '.');				
				final String qname = pkgPrefix == null ? name : pkgPrefix+'.'+name;
				loader.map(jp.getName(), new AbstractJavaBinaryFile(qname, f, project.getProject()) {
					public Type getType() {
						return Type.CLASS;
					}					
				});
				if (!createdPkg) {
					jp.addPackage(pkgPrefix == null ? "" : pkgPrefix, Config.Type.BINARY);
				}
				System.out.println("\tMapping "+qname);
				i++;
			}
			else if (f.getName().endsWith(".jar")) {
				initForJar(jp, loader, f);
			}
		}
		if (i > 0) {
			System.out.println("Mapped "+i+" classes in "+dir.getAbsolutePath());
		}
	}

	private void initForJar(ISLJavaProject jp, IJavacClassParser loader, final File jar) 
			throws IOException {
		try {
			//final String jarPath = jar.getAbsolutePath();
			final ZipFile zf = new ZipFile(jar);    	
			Enumeration<? extends ZipEntry> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				String name = ze.getName();
				if (name.endsWith(".class")) {    			
					if (name.lastIndexOf('$') >= 0) {
						// Skipping nested/local classes
						continue;
					}
					String qname = convertClassToQname(name);
					if (!SLUtility.isValidDotSeparatedJavaIdentifier(qname)) {
						continue;
					}
					int lastDot  = qname.lastIndexOf('.');
					String pkg   = lastDot < 0 ? "" : qname.substring(0, lastDot);
					jp.addPackage(pkg, Config.Type.BINARY);
					/*
    			if (SLUtility.JAVA_LANG_OBJECT.equals(qname) || "java.lang.Enum".equals(qname)) {
    				System.out.println(jp.getName()+": mapping "+qname+" to "+jar.getAbsolutePath());    			
    			}
					 */
					loader.map(jp.getName(), new JarredClassFile(qname, jar, project.getProject(), name));
				}
			}
			System.out.println(jp.getName()+": Done initializing with "+jar+" : "+jar.length());
			zf.close();
		} catch(ZipException e) {
			System.out.println("Zip exception with "+jar);
		}
	}

	public static String convertClassToQname(String name) {
		String raw   = name.substring(0, name.length()-6);
		String qname = raw.replace('/', '.');
		return qname;
	}
	
	@Override
  public void relocateJars(File targetDir) throws IOException {
		final File target = computeTargetName(targetDir, path.getParentFile(), path.getName());
		if (target == null) {
			// Nothing to do now
			return;
		}
		target.getParentFile().mkdirs();

		if (!target.exists()) {
			if (path.isFile()) {
				boolean success = FileUtility.copy(path, target);
				if (!success) {
					throw new IOException("Unable to copy "+path+" to "+target);
				}
			} else {
				File[] files = path.listFiles();
				if (files.length != 0) {
					FileUtility.zipDir(path, target);
				} else {
					return;
				}
			}
		} else {
			// Already copied, so just use what's there
		}
		origPath = path;
        path = target;
	}

	// TODO what if there are duplicates on the path? (e.g. rt.jar)
	private File computeTargetName(File targetDir, File parent, String name) {
		final int hash = path.getAbsolutePath().hashCode();
		File target;
		if (path.isFile()) {
			// For a jar file
			final int lastDot      = name.lastIndexOf('.');
			if (lastDot < 0) {
				throw new IllegalStateException("No extension: "+name);
			}
			final String prefix    = name.substring(0, lastDot);
			final String extension = name.substring(lastDot);
			target = new File(targetDir, project.getProject()+'/'+prefix+'_'+hash+extension);      
		} else { // a directory
			target = new File(targetDir, project.getProject()+'/'+name+'_'+hash+".zip");   
		}
		//System.out.println("Relocating "+path+" to "+target);		
		if (target.exists()) {
			// Check if it's really the same file
			if (target.length() == path.length()) {
				return target;
			}
			
			//System.out.println("Target already exists: "+target);
			if (parent == null) {
				return null;
			}
			// Try using a somewhat longer name
			return computeTargetName(targetDir, parent.getParentFile(), parent.getName()+'_'+name);
		}
		return target;
	}
	
	@Override
	public File getFileForClassPath() {
		return path;
	}
}
