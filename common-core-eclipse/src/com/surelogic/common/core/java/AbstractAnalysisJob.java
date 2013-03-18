package com.surelogic.common.core.java;

import java.io.File;
import java.io.IOException;

import com.surelogic.common.java.*;
import com.surelogic.common.jobs.*;
import com.surelogic.common.jobs.remote.AbstractLocalSLJob;

public abstract class AbstractAnalysisJob<P extends JavaProjectSet<?>> extends AbstractScanDirJob<P> {
	protected final boolean useSeparateJVM;

	protected AbstractAnalysisJob(P projects, File target, File zips, boolean useSeparateJVM) {
		super("Running JSure on " + projects.getLabel(), projects, target, zips);
		this.useSeparateJVM = useSeparateJVM;
	}

	/**
	 * Called before analysis
	 */
	protected abstract void init(SLProgressMonitor monitor) throws Exception;

	/**
	 * Run in the same VM
	 */
	protected abstract boolean analyzeInVM() throws Exception;
	protected abstract AbstractLocalSLJob makeLocalJob() throws Exception;
	
	protected abstract void handleSuccess();
	protected abstract void handleFailure();
	protected abstract void handleCrash(SLProgressMonitor monitor, SLStatus status);
	
	/**
	 * Called after analysis, no matter what happens
	 */
	protected abstract void endAnalysis(SLProgressMonitor monitor);

	/**
	 * Called after endAnalysis() if everything's successful
	 */
	protected abstract void finish(SLProgressMonitor monitor);
	
	@Override
	public SLStatus run(SLProgressMonitor monitor) {
		System.out.println("Starting analysis for " + projects.getLabel());
		final long start = System.currentTimeMillis();
		try {
			for (Config config : projects.getConfigs()) {
				config.copySources(zipDir, targetDir);
			}
		} catch (IOException e) {
			return SLStatus.createErrorStatus("Problem while copying sources", e);
		}
		final long end = System.currentTimeMillis();
		System.out.println("Copying sources = " + (end - start) + " ms");

		try {
			boolean ok = false;
			init(monitor);

			if (useSeparateJVM) {
				AbstractLocalSLJob job = makeLocalJob();
				SLStatus status = job.run(monitor);
				if (status == SLStatus.OK_STATUS) {
					ok = true;

					/*
					 * // Normally done by Javac, but needs to be repeated // locally if
					 * (oldProjects != null && noConflict) { final Projects merged =
					 * projects.merge(oldProjects); ProjectsDrop.ensureDrop(merged); //
					 * System.out.println("Merged projects: "+merged.getLabel()); } else
					 * { ProjectsDrop.ensureDrop(projects); }
					 */
				} else if (status != SLStatus.CANCEL_STATUS && status.getSeverity() == SLSeverity.ERROR) {
					handleCrash(monitor, status);
				}
			} else {
				ok = analyzeInVM();
			}
			if (ok) {
				handleSuccess();
			} else {
				handleFailure();
				return SLStatus.CANCEL_STATUS;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			handleFailure();
			handleCrash(monitor, SLStatus.createErrorStatus("Problem while running JSure", e));
			return SLStatus.CANCEL_STATUS;
		} finally {
			endAnalysis(monitor);
		}
		finish(monitor);

		return SLStatus.OK_STATUS;
	}
}
