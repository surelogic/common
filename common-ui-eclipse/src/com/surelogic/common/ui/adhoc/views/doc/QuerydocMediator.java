package com.surelogic.common.ui.adhoc.views.doc;

import org.eclipse.swt.browser.Browser;

import com.surelogic.Nullable;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;

public class QuerydocMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final Browser f_browser;

  public QuerydocMediator(AbstractQuerydocView view, Browser browser) {
    f_manager = view.getManager();
    f_browser = browser;
  }

  @Override
  public void init() {
    showQuerydoc(f_manager.getQueryDoc());
    f_manager.addObserver(this);
  }

  @Override
  public void notifyQuerydocValueChange(AdHocQuery query) {
    showQuerydoc(query);
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
  }

  public void setFocus() {
    f_browser.setFocus();
  }

  private void showQuerydoc(@Nullable AdHocQuery query) {
    System.out.println("showing doc for " + query);

  }
}
