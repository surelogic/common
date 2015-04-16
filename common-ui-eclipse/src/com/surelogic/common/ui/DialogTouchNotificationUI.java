package com.surelogic.common.ui;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.license.ILicenseObserver;
import com.surelogic.common.ui.dialogs.NoLicenseDialog;

public class DialogTouchNotificationUI extends SLEclipseStatusUtility.LogTouchNotificationUI {

  @Override
  public ILicenseObserver getLicenseObserver() {
    return new NoLicenseDialog(new SLEclipseStatusUtility.LogOutputLicenseObserver());
  }
}
