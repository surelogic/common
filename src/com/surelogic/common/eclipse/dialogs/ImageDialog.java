package com.surelogic.common.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.i18n.I18N;

public final class ImageDialog extends Dialog {

  private final String f_title;
  private final Image f_icon;
  private final Image f_data;

  public ImageDialog(final Shell shell, final Image data, final Image icon,
      final String title) {
    super(shell);
    setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
    setBlockOnOpen(false);

    if (title != null)
      f_title = title;
    else
      f_title = "";
    f_icon = icon;
    if (data == null)
      throw new IllegalArgumentException(I18N.err(44, "data"));
    f_data = data;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(f_title);
    if (f_icon != null)
      newShell.setImage(f_icon);
  }

  @Override
  protected Control createContents(Composite parent) {
    final Label l = new Label(parent, SWT.NONE);
    l.setImage(f_data);
    return parent;
  }
}
