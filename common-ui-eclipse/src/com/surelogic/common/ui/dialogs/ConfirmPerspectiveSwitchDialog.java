package com.surelogic.common.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.i18n.I18N;

public final class ConfirmPerspectiveSwitchDialog extends MessageDialog {

  boolean f_rememberMyDecision = false;

  /**
   * Gets if the user has asked that his or her decision be remembered in the
   * future rather than being prompted by this dialog.
   * 
   * @return {@code true} if the user has asked that his or her decision be
   *         remembered in the future rather than being prompted by this dialog,
   *         {@code false} otherwise.
   */
  public boolean getRememberMyDecision() {
    return f_rememberMyDecision;
  }

  @Override
  protected Control createCustomArea(Composite parent) {
    final Button rememberMyDecision = new Button(parent, SWT.CHECK);
    rememberMyDecision.setText(I18N.msg("common.confirm.perspective.switch.dialog.remember"));
    rememberMyDecision.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        f_rememberMyDecision = rememberMyDecision.getSelection();
      }
    });
    return super.createCustomArea(parent);
  }

  public ConfirmPerspectiveSwitchDialog(Shell parentShell, Image dialogTitleImage, String dialogMessage) {
    super(parentShell, I18N.msg("common.confirm.perspective.switch.dialog.title"), dialogTitleImage, dialogMessage, QUESTION,
        new String[] { "Yes", "No" }, 0);
  }
}
