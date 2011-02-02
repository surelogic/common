package com.surelogic.common.eclipse;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.AbstractJavaZip;
import com.surelogic.common.logging.SLLogger;

public class SourceZip extends AbstractJavaZip<IResource> {

	protected static Logger LOG = SLLogger.getLogger();

	private final IResource root;

	public SourceZip(IResource root) {
		this.root = root;
	}

	public void generateSourceZip(String name, IResource res)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(name);
		//OutputStream fos = FileUtility.getOutputStream(new File(name));
		ZipOutputStream z = new ZipOutputStream(fos);
		generateSourceZipContents(z, res);
		z.close();
	}

	public void generateSourceZipContents(ZipOutputStream out, IResource res)
			throws IOException {
		super.generateSourceZipContents(out, res);
	}

	@Override
	protected long getTimestamp(IResource res) {
		return res.getModificationStamp();
	}
	
	@Override
	protected InputStream getFileContents(IResource res) throws IOException {
		IFile file = (IFile) res;
		try {
			return file.getContents();
		} catch (CoreException e) {
			LOG.severe("Error adding " + file.getName() + " to ZIP.");
			final IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
	}

	@Override
	protected String getFullPath(IResource res) throws IOException {
		return res.getFullPath().toString();
	}

	@Override
	protected String[] getIncludedTypes(IResource res) {
		IFile file = (IFile) res;
		final ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		if (icu != null) {
			if (!icu.getJavaProject().isOnClasspath(icu)) {
				return null;
			}
			try {
				IType[] types = icu.getAllTypes();
				String[] names = new String[types.length];
				int i = 0;
				for (IType t : icu.getAllTypes()) {
					names[i] = t.getFullyQualifiedName('$');
					i++;
				}
				return names;
			} catch (JavaModelException e) {
				LOG.log(Level.SEVERE, "Problem getting included types", e);
			}
		}
		return null;
	}

	@Override
	protected IResource[] getMembers(IResource res) throws IOException {
		try {
			return ((IContainer) res).members();
		} catch (CoreException e) {
			LOG.severe("Error accessing child resources");
			final IOException io = new IOException();
			io.initCause(e);
			throw io;
		}
	}

	@Override
	protected String getName(IResource res) {
		return res.getName();
	}

	@Override
	protected IResource getRoot() {
		return root;
	}

	@Override
	protected boolean isAccessible(IResource res) {
		return res.isAccessible();
	}

	@Override
	protected boolean isFile(IResource res) {
		return (res.getType() == IResource.FILE);
	}

	@Override
	protected String getJavaPackageNameOrNull(IResource res) {
		if (res != null && res.getType() == IResource.FILE
				&& "java".equalsIgnoreCase(res.getFileExtension())) {
			final IFile file = (IFile) res;
			final ICompilationUnit icu = JavaCore
					.createCompilationUnitFrom(file);
			if ((icu != null) && (icu.getJavaProject().isOnClasspath(icu))) {
				try {
					final IPackageDeclaration[] pkgDecls = icu
							.getPackageDeclarations();
					if (pkgDecls.length > 0) {
						final IPackageDeclaration pkgDecl = pkgDecls[0];
						return pkgDecl.getElementName();
					} else {
						/*
						 * No package declaration in the compilation unit so it
						 * is in the default package.
						 */
						return "(default)";
					}
				} catch (JavaModelException e) {
					// fall through to the return below
					return null;
				}
			} else {
				//System.out.println("Not on classpath: "+icu.getHandleIdentifier());
				return null;
			}
		}
		return null;
	}
}
