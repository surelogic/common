package com.surelogic.common.java;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.surelogic.common.jobs.SLProgressMonitor;

public interface IJavaFactory<P extends ISLJavaProject> {		
	P newProject(JavaProjectSet<P> projects, Config config, String name, SLProgressMonitor monitor);
	JavaProjectSet<P> newProjectSet(File loc, boolean isAuto, Date time, Map<String, Object> args);
}
