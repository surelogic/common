package com.surelogic.common.ui.dialogs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.preferences.AutoPerspectiveSwitchPreferences;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;

public abstract class AbstractConfirmPerspectiveSwitch {
  final AtomicBoolean f_dialogOpen = new AtomicBoolean(false);
  final String perspectiveId;
  final AutoPerspectiveSwitchPreferences prefs;

  protected AbstractConfirmPerspectiveSwitch(String id, AutoPerspectiveSwitchPreferences p) {
    perspectiveId = id;
    prefs = p;
  }

  public final void submitUIJob() {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        /*
         * Ensure that we don't already have this dialog up.
         */
        if (f_dialogOpen.compareAndSet(false, true)) {
          try {
            /*
             * Now prompt the user to change to the new perspective, if we are
             * not already in it.
             */
            final boolean inCodeReviewPerspective = EclipseUIUtility.isPerspectiveOpen(perspectiveId);
            if (!inCodeReviewPerspective) {
              final boolean change = toPerspective(EclipseUIUtility.getShell());
              if (change) {
                EclipseUIUtility.showPerspective(perspectiveId);
              }
            }
          } finally {
            /*
             * The dialog is no longer being shown.
             */
            f_dialogOpen.set(false);
          }
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * Checks if the perspective should be opened.
   * 
   * @param shell
   *          a shell.
   * @return {@code true} if the perspective should be opened, {@code false}
   *         otherwise.
   */
  protected final boolean toPerspective(Shell shell) {
    if (prefs.getPromptPerspectiveSwitch()) {
      ConfirmPerspectiveSwitchDialog dialog = new ConfirmPerspectiveSwitchDialog(shell, SLImages.getImage(getLogo()),
          I18N.msg(getShortPrefix() + "dialog.confirm.perspective.switch"));
      final boolean result = dialog.open() == Window.OK;
      final boolean rememberMyDecision = dialog.getRememberMyDecision();
      if (rememberMyDecision) {
        EclipseUtility.setBooleanPreference(prefs.getPromptPerspectiveSwitchConstant(), !rememberMyDecision);
        EclipseUtility.setBooleanPreference(prefs.getAutoPerspectiveSwitchConstant(), result);
      }
      return result;
    } else {
      return prefs.getAutoPerspectiveSwitch();
    }
  }

  protected abstract String getLogo();

  /**
   * @return one word ending with '.'
   */
  protected abstract String getShortPrefix();
}
