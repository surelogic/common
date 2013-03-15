package com.surelogic.common.core.java;

import java.io.File;

import com.surelogic.common.java.JavaProjectSet;
import com.surelogic.common.jobs.AbstractSLJob;

public abstract class AbstractScanDirJob<P extends JavaProjectSet> extends AbstractSLJob {
	protected final P projects;
	
    /**
     * Where the source files will be copied to
     */
	protected final File targetDir;

    /**
     * Where the source zips will be created
     */
	protected final File zipDir;

    protected AbstractScanDirJob(String name, P projects, File target, File zips) {
      super(name);
      this.projects = projects;
      targetDir = target;
      zipDir = zips;
    }
  }
