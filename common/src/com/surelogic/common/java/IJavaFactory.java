package com.surelogic.common.java;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.surelogic.common.jobs.SLProgressMonitor;

public interface IJavaFactory<P extends ISLJavaProject> {		
	P newProject(JavaProjectSet<P> projects, Config config, String name, SLProgressMonitor monitor);
	JavaProjectSet<P> newProjectSet(File loc, boolean isAuto, Date time, Map<String, Object> args);
	
	static final IJavaFactory<JavaProject> prototype = new IJavaFactory<JavaProject>() {
		public JavaProject newProject(JavaProjectSet<JavaProject> projects,
				Config config, String name, SLProgressMonitor monitor) {
			return new JavaProject(projects, config, name, monitor);
		}
		public JavaProjectSet<JavaProject> newProjectSet(File loc,
				boolean isAuto, Date time, Map<String, Object> args) {
			return new JavaProjectSet<JavaProject>(this, loc, isAuto, time, args);
		}
	};
}
