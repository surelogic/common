package com.surelogic.common.core;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;

/* A temporary class to hold the resources and compilation units
 * from a project
 */
public class JavaProjectResources {
    public interface Filter {
        
    }
    
	//TODO why are all these public -- seems like a bad API
	public final IJavaProject project;
	public final Filter filter;
	
	public final List<IResource> resources = new ArrayList<IResource>();
    public final List<ICompilationUnit> cus = new ArrayList<ICompilationUnit>();
    
    JavaProjectResources(IJavaProject p, Filter f) {
    	project = p;
    	filter = f;
    }
}
