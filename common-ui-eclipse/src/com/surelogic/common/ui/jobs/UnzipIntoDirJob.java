package com.surelogic.common.ui.jobs;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.FileUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;

public final class UnzipIntoDirJob extends Job {

  public UnzipIntoDirJob(@NonNull String resourcePrefix, @NonNull File zipFile, @NonNull File targetDir,
      @Nullable Runnable runWhenDone) {
    super(I18N.msg(resourcePrefix + ".job"));
    if (resourcePrefix == null)
      throw new IllegalArgumentException(I18N.err(44, "resourcePrefix"));
    f_res = resourcePrefix;
    if (zipFile == null)
      throw new IllegalArgumentException(I18N.err(44, "zipFile"));
    if (!zipFile.isFile())
      throw new IllegalArgumentException(I18N.err(74, zipFile.getAbsolutePath()));
    f_zipFile = zipFile;
    if (targetDir == null)
      throw new IllegalArgumentException(I18N.err(44, "targetDir"));
    f_targetDir = targetDir;
    f_runWhenDone = runWhenDone;
  }

  @NonNull
  final String f_res;

  /**
   * The zip file should exist on the disk.
   */
  @NonNull
  final File f_zipFile;

  /**
   * The target dir probably should not exist on the disk. This job tries to
   * create it.
   */
  @NonNull
  final File f_targetDir;

  @Nullable
  Runnable f_runWhenDone;

  @Override
  protected IStatus run(final IProgressMonitor pm) {
    // create target dir
    if (!FileUtility.createDirectory(f_targetDir)) {
      return SLEclipseStatusUtility.createErrorStatus(341, I18N.err(341, f_targetDir.getAbsolutePath()));
    }
    // unzip contents
    try {
      FileUtility.unzipFile(f_zipFile, f_targetDir);
    } catch (IOException e) {
      return SLEclipseStatusUtility.createErrorStatus(342,
          I18N.err(342, f_zipFile.getAbsolutePath(), f_targetDir.getAbsolutePath()), e);
    }
    // notify user
    SLUIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg(f_res + ".success.title"),
            I18N.msg(f_res + ".success.msg", f_zipFile.getName()));
        return Status.OK_STATUS;
      }
    };
    job.schedule();
    // run optional end job
    if (f_runWhenDone != null)
      f_runWhenDone.run();
    return Status.OK_STATUS;
  }
}
