package com.surelogic.common.ui.dialogs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.surelogic.Utility;
import com.surelogic.common.FileUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;

@Utility
public final class DialogUtility {

  /**
   * Used by
   * {@link DialogUtility#copyZipResourceToUsersDiskDialogInteractionHelper(String, String, ZipResourceFactory, String)}
   * to construct an input stream for a Zip resource in a SureLogic Eclipse
   * plugin.
   */
  public interface ZipResourceFactory {
    /**
     * Getter for the input stream for a Zip resource in a SureLogic Eclipse
     * plugin.
     * 
     * @return a input stream to a Zip file.
     * @throws IOException
     *           if something goes wrong.
     */
    InputStream getInputStream() throws IOException;
  }

  /**
   * Handles dialog interaction for copying a Zip out of a SureLogic Eclipse
   * plugin to the user's disk. This is used to save Ant tasks and Maven
   * plugins.
   * 
   * @param source
   *          a factory for the {@link InputStream} to the Zip inside the
   *          SureLogic Eclipse plugin.
   * @param target
   *          a target file name to save the copy to. A dialog prompts the user
   *          for the path where to put this file.
   * @param zipNameForDebugging
   *          the simple name of the Zip file obtained through <tt>source</tt>,
   *          this is used for debug output if something goes wrong.
   * @param resourcePrefix
   *          the prefix for strings gotten from {@link I18N#msg(String)} calls
   *          to fill in dialog text.
   */
  public static void copyZipResourceToUsersDiskDialogInteractionHelper(ZipResourceFactory source, String target,
      String zipNameForDebugging, String resourcePrefix) {
    DirectoryDialog dialog = new DirectoryDialog(EclipseUIUtility.getShell());
    dialog.setText(I18N.msg(resourcePrefix + ".saveAs.title"));
    dialog.setMessage(I18N.msg(resourcePrefix + ".saveAs.msg", target));
    final String result = dialog.open();
    boolean copySuccessful = false;
    Exception ioException = null;
    if (result != null) {
      final File file = new File(result, target);
      try {
        if (file.exists()) {
          MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg(resourcePrefix + ".saveAs.failed.title"),
              I18N.msg(resourcePrefix + ".saveAs.exists.msg", file.getPath()));
          return;
        }
        copySuccessful = FileUtility.copy(zipNameForDebugging, source.getInputStream(), file);
      } catch (final Exception e) {
        ioException = e;
      }
      if (copySuccessful) {
        MessageDialog.openInformation(EclipseUIUtility.getShell(), I18N.msg(resourcePrefix + ".saveAs.confirm.title"),
            I18N.msg(resourcePrefix + ".saveAs.confirm.msg", file.getPath()));
      } else {
        final int err = 225;
        final String msg = I18N.err(225, zipNameForDebugging, file.getAbsolutePath());
        final IStatus reason = SLEclipseStatusUtility.createErrorStatus(err, msg, ioException);
        ErrorDialogUtility.open(EclipseUIUtility.getShell(), I18N.msg(resourcePrefix + ".saveAs.failed.title"), reason, true);
      }
    }
  }

  // Suppress default constructor for noninstantiability
  private DialogUtility() {
    throw new AssertionError();
  }
}
