package com.surelogic.common.ui.dialogs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.ILicenseObserver;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class NoLicenseDialog implements ILicenseObserver {

  /**
   * Writes messages to the Eclipse log. May be {@code null}.
   */
  final ILicenseObserver f_toEclipseLog;

  /**
   * Constructs a new instance.
   * 
   * @param toEclipseLog
   *          Writes messages to the Eclipse log. May be {@code null}.
   */
  public NoLicenseDialog(ILicenseObserver toEclipseLog) {
    f_toEclipseLog = toEclipseLog;
  }

  @Override
  public void notifyNoLicenseFor(final SLLicenseProduct product) {
    final String subject = product.toString();
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        if (f_toEclipseLog != null)
          f_toEclipseLog.notifyNoLicenseFor(product);
        final String title = I18N.msg("common.manage.licenses.dialog.noLicense.title");
        final String msg = I18N.msg("common.manage.licenses.dialog.noLicense.msg", subject);
        final Shell shell = EclipseUIUtility.getShell();
        if (MessageDialog.openQuestion(shell, title, msg)) {
          ManageLicensesDialog.open(shell);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * Holds the set of license subject that the user has been notified are close
   * to their expiration date. This set should <i>only</i> be accessed within
   * the UI thread.
   */
  final Set<String> f_alreadyNotified = new HashSet<>();

  @Override
  public void notifyExpiration(final SLLicenseProduct product, final Date expiration) {
    final String subject = product.toString();
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        /*
         * Only notify the user once about a particular license.
         */
        if (!f_alreadyNotified.contains(subject)) {
          f_alreadyNotified.add(subject);
          if (f_toEclipseLog != null)
            f_toEclipseLog.notifyExpiration(product, expiration);
          final String title = I18N.msg("common.manage.licenses.dialog.expiration.title");
          final String msg = I18N.msg("common.manage.licenses.dialog.expiration.msg", subject, SLUtility.toStringDay(expiration));
          final Shell shell = EclipseUIUtility.getShell();
          if (MessageDialog.openQuestion(shell, title, msg)) {
            ManageLicensesDialog.open(shell);
          }
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }
}
