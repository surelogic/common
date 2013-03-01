package com.surelogic.common.ui.adhoc.views.doc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.adhoc.AdHocManager;

public abstract class AbstractQuerydocView extends ViewPart {

  private QuerydocMediator f_mediator = null;

  public abstract AdHocManager getManager();

  @Override
  public void createPartControl(Composite parent) {
    parent.setLayout(new FillLayout());
    final Browser browser = new Browser(parent, SWT.NONE);

    // tipBrowser.setJavascriptEnabled(false);

    final Display display = parent.getDisplay();
    browser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    browser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

    // Replace browser's built-in context menu with none
    browser.setMenu(new Menu(parent.getShell(), SWT.NONE));

    f_mediator = new QuerydocMediator(this, browser);
    f_mediator.init();
  }

  @Override
  public void setFocus() {
    if (f_mediator != null) {
      f_mediator.setFocus();
    }
  }

  @Override
  public void dispose() {
    if (f_mediator != null) {
      f_mediator.dispose();
      f_mediator = null;
    }
    super.dispose();
  }

}
