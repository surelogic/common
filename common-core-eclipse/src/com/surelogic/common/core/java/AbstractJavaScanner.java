package com.surelogic.common.core.java;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.Pair;
import com.surelogic.common.XUtil;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.java.*;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;

/**
 * Framework for setting up a scan directory and running an analysis
 * 
 * @author edwin
 */
public abstract class AbstractJavaScanner<PS extends JavaProjectSet<P>, P extends ISLJavaProject> {	
	final IJavaFactory<P> javaFactory;
	private final Map<IProject, ProjectInfo<P>> projectMap = new HashMap<IProject, ProjectInfo<P>>();
	private final Map<String, Object> args = new HashMap<String, Object>();
	
	protected AbstractJavaScanner(IJavaFactory<P> factory) {
		javaFactory = factory;
	}
	
	/**
	 * @return the root data directory
	 */
	protected File prepForScan(PS newProjects, SLProgressMonitor monitor, boolean useSeparateJVM) throws Exception {
		// final boolean hasDeltas = info.hasDeltas();
		makeProjects(newProjects, monitor);
		
		final File dataDir = getDataDirectory();
		final PS oldProjects = useSeparateJVM ? null : null;// (Projects)
		// ProjectsDrop.getProjects();
		if (oldProjects != null) {
			System.out.println("Old projects = " + oldProjects.getLabel());
		}
		newProjects.computeScan(dataDir, oldProjects);
		return dataDir;
	}

	protected abstract File getDataDirectory();
	protected abstract void markAsRunning(File runDir);	
	protected abstract AbstractAnalysisJob<PS> makeAnalysisJob(PS projects, File target, File zips, boolean useSeparateJVM);

	protected void scheduleScanForExecution(PS projects, SLJob copy) throws Exception {
		EclipseUtility.toEntireWorkspaceJob(copy).schedule();
	}
	
	protected final void startScan(PS newProjects, SLProgressMonitor monitor, boolean useSeparateJVM) {
		try {
			final File dataDir = prepForScan(newProjects, monitor, useSeparateJVM);
			final File runDir = new File(dataDir, newProjects.getRun());
			final File zips = new File(runDir, PersistenceConstants.ZIPS_DIR);
			final File target = new File(runDir, PersistenceConstants.SRCS_DIR);
			target.mkdirs();
			markAsRunning(runDir);

			AbstractAnalysisJob<?> analysis = makeAnalysisJob(newProjects, target, zips, useSeparateJVM);
			SLJob copy = new CopyProjectsJob(newProjects, target, zips, analysis);
			scheduleScanForExecution(newProjects, copy);      
		} catch (Exception e) {
			System.err.println("Unable to make config for JSure");
			e.printStackTrace();
			if (XUtil.testing) {
				throw (RuntimeException) e;
			}
			return;
		}
	}
	
	/**
	 * If true, create common projects for shared jars Otherwise, jars in
	 * different are treated as if they're completely unique
	 * 
	 * Creating separate projects for shared jars doesn't work, due to
	 * dependencies on other jars, esp. the JRE
	 */
	private static final boolean shareCommonJars = false;

	private void findSharedJars(final PS projects) {
		if (!shareCommonJars) {
			return;
		}

		try { 
			final Map<File,File> shared = new HashMap<File, File>();
			for(IJavaProject p : JDTUtility.getJavaProjects()) { 
				for(IClasspathEntry	cpe : p.getResolvedClasspath(true)) { 
					switch (cpe.getEntryKind()) { 
					case IClasspathEntry.CPE_LIBRARY:
						final IPath path = cpe.getPath(); 
						final File f = EclipseUtility.resolveIPath(path); 
						if (shared.containsKey(f)) {
							//System.out.println("Repeated view: "+f); 
							shared.put(f, f); 
						} else if (f != null) { 
							//System.out.println("First view:    "+f); 
							shared.put(f, null); // Seen once 
						} 
					}
				} 
			} 
			// Create mappings for shared jars 
			for(File path : shared.keySet()) { 
				File f = shared.get(path); 
				if (f != null) {
					projects.mapToProject(path, f.getAbsolutePath()); 
				} else { 
					// Ignore jars only seen once 
				} 
			} 
		} catch (JavaModelException e) { 
			return; 
		}     
	}
	
	// TODO how to set up for deltas?
	protected final PS makeProjects(final PS projects, SLProgressMonitor monitor) throws JavaModelException {
		final List<ProjectInfo<P>> infos = new ArrayList<ProjectInfo<P>>(projectMap.values());
		monitor.begin(infos.size() + 2);

		findSharedJars(projects);
		monitor.worked(1);

		for (ProjectInfo<P> info : infos) {
			if (!projects.contains(info.project.getName())) {
				if (info.isActive()) {
					Config c = info.makeConfig(projects, !info.hasDeltas());
					if (c == null) {
						continue;
					}
				} else {
					// Otherwise, it's inactive
					continue;
				}
			} else {
				// Already added as a dependency?
				info.setActive(true);
			}
			ISLJavaProject proj = projects.get(info.project.getName());
			/*
			 * if (proj == null) { continue; }
			 */
			Config config = proj.getConfig();
			config.setAsSource();
			monitor.worked(1);
		}

		// Remove inactive projects?
		for (ProjectInfo<P> info : infos) {
			if (!info.isActive()) {
				projectMap.remove(info.project);
			}
		}
		monitor.worked(1);
		return projects;
	}	
	
	protected void clearProjectInfo() {
		projectMap.clear();
	}
			
	protected final ProjectInfo<P> getProjectInfo(IProject proj) {
		return projectMap.get(proj);
	}
	
	/**
	 * Register resources
	 */
	@SuppressWarnings({ "rawtypes" })
	public void registerBuild(IProject project, Map args, List<Pair<IResource, Integer>> resources, List<ICompilationUnit> cus) {
		final int k = getBuildKind(args);
		if (k == IncrementalProjectBuilder.CLEAN_BUILD || k == IncrementalProjectBuilder.FULL_BUILD) {
			// TODO what about resources?

			SLLogger.getLogger().fine("Got full build for " + project.getName());
			ProjectInfo<P> info = finishRegisteringFullBuild(project, resources, cus);
			projectMap.put(project, info);
		} else {
			ProjectInfo<P> info = projectMap.get(project);
			if (info == null) {
				throw new IllegalStateException("No full build before this?");
			}
			info.registerDelta(cus);
			info.registerResourcesDelta(resources);
			finishRegisteringIncrementalBuild(resources, cus);
		}
	}
	
	protected abstract ProjectInfo<P> finishRegisteringFullBuild(IProject project, List<Pair<IResource, Integer>> resources, List<ICompilationUnit> cus);
	protected void finishRegisteringIncrementalBuild(List<Pair<IResource, Integer>> resources, List<ICompilationUnit> cus) {
		// Nothing to do
	}
	
	@SuppressWarnings({ "rawtypes" })
	protected static int getBuildKind(Map args) {
		final String kind = (String) args.get(JavaBuild.BUILD_KIND);
		return Integer.parseInt(kind);
	}	
	
	public final void setArg(String key, Object value) {
		args.put(key, value);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void doExplicitBuild(Map args, boolean ignoreNature) {
	    configureBuild(args, ignoreNature);
	}

	@SuppressWarnings({ "rawtypes" })
	public void configureBuild(Map args, boolean ignoreNature) {
		final int k = getBuildKind(args);
		configureBuild(EclipseUtility.getWorkspacePath(),
				(k & IncrementalProjectBuilder.AUTO_BUILD) == IncrementalProjectBuilder.AUTO_BUILD, ignoreNature);
	}
	
	public void configureBuild(File location, boolean isAuto /* IProject p */, boolean ignoreNature) {
		ConfigureJob configure = new ConfigureJob("Configuring JSure build", location, isAuto, args, ignoreNature);

		synchronized (this) {
			// Only if there's no build already
			SLLogger.getLogger().fine("Starting to configure JSure build");
			/*
			 * ProjectsDrop pd = ProjectsDrop.getDrop(); if (pd != null) { for
			 * (JavacProject jp : ((Projects) pd.getIIRProjects())) {
			 * System.out.println("Deactivating " + jp); jp.deactivate(); } }
			 */
			if (XUtil.testing) {
				configure.run(new NullSLProgressMonitor());
			} else {
				EclipseUtility.toEclipseJob(configure).schedule();
			}
		}
	}
	
	public static SLStatus waitForBuild(boolean isAuto) {
		SLLogger.getLogger().fine("Waiting for build: " + isAuto);
		try {
			Object family = isAuto ? ResourcesPlugin.FAMILY_AUTO_BUILD : ResourcesPlugin.FAMILY_MANUAL_BUILD;
			Job[] jobs = Job.getJobManager().find(family);
			if (jobs.length == 0) {
				return SLStatus.OK_STATUS;
			}
			Job.getJobManager().join(family, null);
		} catch (OperationCanceledException e1) {
			return SLStatus.CANCEL_STATUS;
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return SLStatus.OK_STATUS;
	}	
	
	class ConfigureJob extends AbstractSLJob {
		final PS projects;
		final boolean ignoreNature;

		@SuppressWarnings("unchecked")
		ConfigureJob(String name, File location, boolean isAuto, Map<String, Object> args, boolean ignoreNature) {
			super(name);      
			projects = (PS) javaFactory.newProjectSet(location, isAuto, new Date(), new HashMap<String, Object>(args));
			args.clear();
			this.ignoreNature = ignoreNature;
		}

		@Override
		public SLStatus run(SLProgressMonitor monitor) {
			if (XUtil.testing) {
				System.out.println("Do I need to do something here to wait?");
			} else {
				SLStatus s = waitForBuild(projects.isAutoBuild());
				if (s == SLStatus.CANCEL_STATUS) {
					return s;
				}
				// Clear for next build?
			}
			if (!ignoreNature) {
				System.err.println("NOT deactivating projects");
				// Clear projects that are inactive
				/*
				 * for (IJavaProject jp : JDTUtility.getJavaProjects()) { ProjectInfo
				 * info = JavacDriver.this.projects.get(jp .getProject()); if (info !=
				 * null) { info.setActive(Nature.hasNature(jp.getProject()));
				 * 
				 * // Check if it was previously active, but is now a // dependency? } }
				 */
			}
			if (monitor.isCanceled()) {
				return SLStatus.CANCEL_STATUS;
			}
			final boolean runRemote = !XUtil.runJSureInMemory && ignoreNature;
			startScan(projects, monitor, runRemote);
			return SLStatus.OK_STATUS;
		}
	}	
}

