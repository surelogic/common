package com.surelogic.common.ui.adhoc.views.doc;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.adhoc.AdHocManager;

public abstract class AbstractQuerydocView extends ViewPart {

  public abstract AdHocManager getManager();

  @Override
  public void createPartControl(Composite parent) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

}
