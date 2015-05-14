package com.surelogic.common.jobs.remote;

import java.io.*;

import com.surelogic.common.java.*;
import com.surelogic.common.jobs.*;

/**
 * A job that runs in another JVM as specified by a run directory
 * 
 * @author edwin
 */
public class RemoteScanJob<PS extends JavaProjectSet<P>, P extends ISLJavaProject> extends AbstractRemoteSLJob {
  public static final String RUN_DIR_PROP = "sl.run.dir";
  public static final String TEMP_PREFIX = "running_or_crashed_";

  private final IJavaFactory<P> javaFactory;

  protected RemoteScanJob(IJavaFactory<P> f) {
    javaFactory = f;
  }

  @Override
  protected final SLJob init(BufferedReader br, Monitor mon) throws Throwable {
    out.println("Running " + getClass().getSimpleName());
    init();

    final String runPath = System.getProperty(RUN_DIR_PROP, System.getProperty("user.dir"));
    if (runPath == null) {
      throw new IllegalStateException("No run directory");
    }
    out.println("runPath = " + runPath);

    // Load up projects
    final File runDir = new File(runPath);
    // TODO check if runDir exists?
    try {
      out.println("Creating run: " + runDir.getName());
      final JavaProjectsXMLReader<P> reader = new JavaProjectsXMLReader<>(javaFactory);
      @SuppressWarnings("unchecked")
      final PS projects = (PS) reader.readProjectsXML(runDir);
      projects.setScanDir(runDir);
      out.println("projects = " + projects.getLabel());
      return finishInit(runDir, projects);
    } catch (Throwable t) {
      mon.failed("Unable to create SureLogic job", t);
      return null;
    }
  }

  // Things to do before the projects file is loaded up
  protected void init() {
    // Nothing to do yet
  }

  protected SLJob finishInit(File runDir, PS projects) throws Throwable {
    out.println("Doing nothing");
    return new AbstractSLJob("Does nothing") {
      public SLStatus run(SLProgressMonitor monitor) {
        return SLStatus.OK_STATUS;
      }
    };
  }

  public SLStatus runLocally(File runDir, PS projects, SLProgressMonitor monitor) throws Throwable {
    SLJob job = finishInit(runDir, projects);
    SLStatus status = job.run(monitor);
    return status;
  }
}
