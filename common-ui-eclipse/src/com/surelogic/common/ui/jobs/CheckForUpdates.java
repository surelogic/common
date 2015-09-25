package com.surelogic.common.ui.jobs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.BalloonUtility;

/**
 * This job checks the published tool release date on the SureLogic website and
 * compares it to the installed tool release date and then, if their is an
 * update available, notifies the tool user with a dialog.
 * <p>
 * Calling code should simply invoke the {@link #go()} convenience method.
 */
public final class CheckForUpdates extends Job {

  public CheckForUpdates() {
    super("Checking if updates are available for the SureLogic tools");
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    final String downloadUrl = I18N.msg("web.tool-version-file.url", SLUtility.SERVICEABILITY_SERVER);
    monitor.beginTask("Checking current tool release version on website", 6);
    try {
      final URL url = new URL(downloadUrl);
      monitor.worked(1);
      ImmutableList<String> lines = Resources.asCharSource(url, Charset.defaultCharset()).readLines();
      monitor.worked(1);
      if (!lines.isEmpty()) {
        final String webRelease = lines.get(0).trim();
        final Version web;
        try {
          web = Version.parseVersion(webRelease);
        } catch (Exception e) {
          SLLogger.getLogger().log(Level.WARNING, I18N.err(372, webRelease, "web"), e);
          return Status.OK_STATUS;
        }
        monitor.worked(1);
        final String installedRelease = EclipseUtility.getSureLogicToolsVersion();
        final Version installed;
        try {
          installed = Version.parseVersion(installedRelease);
        } catch (Exception e) {
          SLLogger.getLogger().log(Level.WARNING, I18N.err(372, installedRelease, "installed"), e);
          return Status.OK_STATUS;
        }
        monitor.worked(1);
        final int installedCompariableValue = EclipseUtility.getCompariableValueFor(installed);
        final int webCompariableValue = EclipseUtility.getCompariableValueFor(web);
        monitor.worked(1);
        if (webCompariableValue > installedCompariableValue) {
          /*
           * An update is available at the SureLogic website.
           */
          final String simpleWebRelease = web.getMajor() + "." + web.getMinor() + "." + web.getMicro();
          final String title = I18N.msg("web.check-for-update.title");
          final String msg = I18N.msg("web.check-for-update.msg", simpleWebRelease);
          BalloonUtility.showMessage(title, msg);
        }
      }
    } catch (MalformedURLException e) {
      SLLogger.getLogger().log(Level.WARNING, I18N.err(370, downloadUrl), e);
    } catch (IOException ignore) {
      /*
       * This indicates we could not read the file from the SureLogic website
       * for some reason. This is not really a problem, the tool user could just
       * have their Internet turned off.
       */
    } finally {
      monitor.done();
    }
    return Status.OK_STATUS;
  }

  /**
   * Schedules a check for updates job 20 seconds from the call. This call does
   * not block, it will return immediately. Will prompt the tool user if a newer
   * version exists.
   */
  public static void go() {
    final Job job = new CheckForUpdates();
    job.setSystem(true);
    job.schedule(TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS));
  }
}
