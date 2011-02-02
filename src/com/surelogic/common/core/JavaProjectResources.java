package com.surelogic.common.core;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;

public class JavaProjectResources {
	public final IJavaProject project;
	public final List<IResource> resources = new ArrayList<IResource>();
    public final List<ICompilationUnit> cus = new ArrayList<ICompilationUnit>();
    
    JavaProjectResources(IJavaProject p) {
    	project = p;
    }
}
