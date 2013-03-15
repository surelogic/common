package com.surelogic.common.core.java;

import java.io.*;
import java.util.Map;

import com.surelogic.common.XUtil;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.java.*;
import com.surelogic.common.jobs.*;

/**
 * Framework for setting up a scan directory and running an analysis
 * 
 * @author edwin
 */
public abstract class AbstractJavaScanner<P extends JavaProjectSet> {
	/**
	 * @return the root data directory
	 */
	protected abstract File prepForScan(P projects, SLProgressMonitor monitor, boolean useSeparateJVM) throws Exception;
	protected abstract void markAsRunning(File runDir);	
	protected abstract AbstractAnalysisJob<P> makeAnalysisJob(P projects, File target, File zips, boolean useSeparateJVM);

	protected void scheduleScanForExecution(P projects, Map<String, Object> args, SLJob copy) throws Exception {
		EclipseUtility.toEntireWorkspaceJob(copy).schedule();
	}
	
	protected final void startScan(P newProjects, Map<String, Object> args, SLProgressMonitor monitor, boolean useSeparateJVM) {
		try {
			final File dataDir = prepForScan(newProjects, monitor, useSeparateJVM);
			final File runDir = new File(dataDir, newProjects.getRun());
			final File zips = new File(runDir, PersistenceConstants.ZIPS_DIR);
			final File target = new File(runDir, PersistenceConstants.SRCS_DIR);
			target.mkdirs();
			markAsRunning(runDir);

			AbstractAnalysisJob<?> analysis = makeAnalysisJob(newProjects, target, zips, useSeparateJVM);
			SLJob copy = new CopyProjectsJob(newProjects, target, zips, analysis);
			scheduleScanForExecution(newProjects, args, copy);      
		} catch (Exception e) {
			System.err.println("Unable to make config for JSure");
			e.printStackTrace();
			if (XUtil.testing) {
				throw (RuntimeException) e;
			}
			return;
		}
	}
}
