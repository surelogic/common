package com.surelogic.common.core.java;

import java.io.File;
import java.io.IOException;

import com.surelogic.common.XUtil;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.java.*;
import com.surelogic.common.jobs.*;

public class CopyProjectsJob extends AbstractScanDirJob<JavaProjectSet> {
	private final SLJob afterJob;

	public CopyProjectsJob(JavaProjectSet projects, File target, File zips, SLJob after) {
		super("Copying project info for " + projects.getLabel(), projects, target, zips);
		afterJob = after;
	}

	@Override
	public SLStatus run(SLProgressMonitor monitor) {
		monitor.begin(3);
		final long start = System.currentTimeMillis();
		try {
			for (Config config : projects.getConfigs()) {
				if (monitor.isCanceled()) {
					return SLStatus.CANCEL_STATUS;
				}
				config.zipSources(zipDir);
			}
		} catch (IOException e) {
			return SLStatus.createErrorStatus("Problem while zipping sources", e);
		}
		monitor.worked(1);
		final long zip = System.currentTimeMillis();
		try {
			for (Config config : projects.getConfigs()) {
				if (monitor.isCanceled()) {
					return SLStatus.CANCEL_STATUS;
				}
				config.relocateJars(targetDir);
			}
		} catch (IOException e) {
			return SLStatus.createErrorStatus("Problem while copying jars", e);
		}
		final long end = System.currentTimeMillis();
		monitor.worked(1);
		System.out.println("Zipping         = " + (zip - start) + " ms");
		System.out.println("Relocating jars = " + (end - zip) + " ms");

		if (monitor.isCanceled()) {
			return SLStatus.CANCEL_STATUS;
		}
		if (afterJob != null) {
			if (XUtil.testing) {
				afterJob.run(monitor);
			} else {
				// was Util important?
				EclipseUtility.toEclipseJob(afterJob, projects.getClass().getName()).schedule();
			}
		}
		monitor.worked(1);
		return SLStatus.OK_STATUS;
	}
}
