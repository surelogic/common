package com.surelogic.common.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.CommonImages;
import com.surelogic.common.FileUtility;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.PossiblyActivatedSLLicense;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseManager;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.ui.SLImages;

final class ManageLicensesMediator implements ILifecycle {

  private final Table f_licenseTable;
  private final Button f_installFromFileButton;
  private final Button f_installFromClipboardButton;
  private final Button f_activateButton;
  private final Button f_renewButton;
  private final Button f_uninstallButton;

  ManageLicensesMediator(Table licenseTable, Button installFromFileButton, Button installFromClipboardButton,
      Button activateButton, Button renewButton, Button uninstallButton) {
    f_licenseTable = licenseTable;
    f_installFromFileButton = installFromFileButton;
    f_installFromClipboardButton = installFromClipboardButton;
    f_activateButton = activateButton;
    f_renewButton = renewButton;
    f_uninstallButton = uninstallButton;
  }

  @Override
  public void init() {
    updateTableContents();

    f_licenseTable.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        updateButtonState();
      }
    });

    f_installFromFileButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        installLicenseFromFile();
      }
    });
    f_installFromClipboardButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        installLicenseFromClipboard();
      }
    });
    f_activateButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final List<PossiblyActivatedSLLicense> selection = getLicenseTableSelection();
        activateRenewLicenses(selection, true);
      }
    });
    f_renewButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final List<PossiblyActivatedSLLicense> selection = getLicenseTableSelection();
        activateRenewLicenses(selection, false);
      }
    });

    f_uninstallButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final List<PossiblyActivatedSLLicense> selection = getLicenseTableSelection();
        uninstallLicenses(selection);
      }
    });
  }

  @Override
  public void dispose() {
    // nothing to do
  }

  private void updateTableContents() {
    f_licenseTable.setRedraw(false);
    f_licenseTable.removeAll();
    for (final PossiblyActivatedSLLicense installedLicense : SLLicenseManager.getInstance().getLicenses()) {
      final TableItem item = new TableItem(f_licenseTable, SWT.NONE);
      final SLLicense license = installedLicense.getSignedSLLicense().getLicense();
      item.setText(0, license.getProduct().toString());
      setLicenseImage(item, installedLicense);
      final String activationExpl = installedLicense.getActivatedExplanation();
      item.setText(1, activationExpl);
      if (!installedLicense.isActivated()) {
        item.setForeground(1, item.getDisplay().getSystemColor(SWT.COLOR_RED));
      }
      item.setText(2, license.getType().toString());
      item.setText(3, installedLicense.getExpirationExplanation());
      String issuedTo = license.getHolder();
      if (issuedTo == null)
        issuedTo = "";
      String id = license.getUuid().toString();
      if (id == null)
        id = "";
      item.setText(4, issuedTo);
      item.setText(5, id);

      item.setData(installedLicense);
    }
    for (TableColumn c : f_licenseTable.getColumns())
      c.pack();
    f_licenseTable.setRedraw(true);
  }

  private void setLicenseImage(final TableItem item, final PossiblyActivatedSLLicense installedLicense) {
    if (installedLicense.isActivated()) {
      if (installedLicense.isExpired())
        item.setImage(0, SLImages.getImage(CommonImages.IMG_LICENSE_GRAY));
      else
        item.setImage(0, SLImages.getImage(CommonImages.IMG_LICENSE));

    } else {
      if (installedLicense.isPastInstallBeforeDate())
        item.setImage(0, SLImages.getImage(CommonImages.IMG_LICENSE_NO_SEAL_GRAY));
      else
        item.setImage(0, SLImages.getImage(CommonImages.IMG_LICENSE_NO_SEAL));
    }
  }

  List<PossiblyActivatedSLLicense> getLicenseTableSelection() {
    TableItem[] selection = f_licenseTable.getSelection();
    if (selection != null && selection.length > 0) {
      final List<PossiblyActivatedSLLicense> result = new ArrayList<>();
      for (TableItem ti : selection) {
        final Object data = ti.getData();
        if (data instanceof PossiblyActivatedSLLicense) {
          final PossiblyActivatedSLLicense license = (PossiblyActivatedSLLicense) data;
          result.add(license);
        }
      }
      return result;
    } else
      return Collections.emptyList();
  }

  void updateButtonState() {
    List<PossiblyActivatedSLLicense> selection = getLicenseTableSelection();
    final boolean somethingSelected = !selection.isEmpty();
    f_uninstallButton.setEnabled(somethingSelected);
    if (somethingSelected) {
      boolean noneActivated = true;
      boolean allPerpetualAndActivated = true;
      for (PossiblyActivatedSLLicense license : selection) {
        if (license.isActivated())
          noneActivated = false;
        else
          allPerpetualAndActivated = false;
        if (license.getSignedSLLicense().getLicense().getType() != SLLicenseType.PERPETUAL)
          allPerpetualAndActivated = false;
      }
      f_activateButton.setEnabled(noneActivated);
      f_renewButton.setEnabled(allPerpetualAndActivated);
    } else {
      f_activateButton.setEnabled(false);
      f_renewButton.setEnabled(false);
    }
  }

  /**
   * Installs a license from a file.
   */
  void installLicenseFromFile() {
    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
    final String title = I18N.msg("common.manage.licenses.dialog.install.file.dialog.title");
    fileDialog.setText(title);
    fileDialog.setFilterPath(System.getProperty("user.home"));
    final String path = fileDialog.open();
    if (path != null) {
      try {
        final String value = FileUtility.getFileContentsAsString(new File(path));
        SLLicenseUtility.tryToInstallLicense(value);
      } catch (Exception e) {
        final int code = 141;
        final String msg = I18N.err(code, path, e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        final SLStatus status = SLStatus.createErrorStatus(code, msg, e);
        final String errorDialogTitle = I18N.msg("common.manage.licenses.dialog.install.failure");
        ErrorDialogUtility.open(getShell(), errorDialogTitle, SLEclipseStatusUtility.convert(status));
      }
      updateTableContents();
      updateButtonState();
    }
  }

  void installLicenseFromClipboard() {
    final Clipboard clipboard = new Clipboard(f_licenseTable.getDisplay());
    String data = (String) clipboard.getContents(TextTransfer.getInstance());
    clipboard.dispose();
    /*
     * Handle the case where the clipboard is empty and data is null. In this
     * case we make data the empty string.
     */
    if (data == null)
      data = "";

    /*
     * Allow the user a chance to view/edit the clipboard data.
     */
    final String value = ShowTextDialog.mutateText(getShell(),
        I18N.msg("common.manage.licenses.dialog.installFromClipboard.dialog.title"), data);
    if (value == null)
      return; // The user canceled the license installation

    try {
      SLLicenseUtility.tryToInstallLicense(value);
    } catch (Exception e) {
      final int code = 145;
      final String msg = I18N.err(code, e.getMessage() == null ? e.getClass().getName() : e.getMessage(), value);
      final SLStatus status = SLStatus.createErrorStatus(code, msg, e);
      final String errorDialogTitle = I18N.msg("common.manage.licenses.dialog.install.failure");
      ErrorDialogUtility.open(getShell(), errorDialogTitle, SLEclipseStatusUtility.convert(status));
    }
    updateTableContents();
    updateButtonState();
  }

  /**
   * Uninstalls a set of licenses.
   * 
   * @param selection
   *          the licenses to uninstall.
   */
  void uninstallLicenses(List<PossiblyActivatedSLLicense> selection) {
    final int count = selection.size();
    if (count < 1)
      return;
    boolean anyActivatedWithANetCheck = false;
    for (PossiblyActivatedSLLicense license : selection) {
      if (license.isActivated() && license.getSignedSLLicense().getLicense().performNetCheck()) {
        anyActivatedWithANetCheck = true;
        break;
      }
    }
    String confirmMsg = I18N.msg("common.manage.licenses.dialog.uninstall.msg", count > 1 ? count + " licenses" : "\""
        + selection.get(0).getSignedSLLicense().getLicense().getProduct().toString() + "\" license");
    if (anyActivatedWithANetCheck)
      confirmMsg = confirmMsg + I18N.msg("common.manage.licenses.dialog.uninstall.netcheckwarn");
    if (!MessageDialog.openConfirm(getShell(), I18N.msg("common.manage.licenses.dialog.uninstall.title"), confirmMsg)) {
      return; // bail
    }
    try {
      SLLicenseUtility.tryToUninstallLicenses(selection);
    } catch (Exception e) {
      final int code = 142;
      final String msg = I18N.err(code, e.getMessage());
      final SLStatus status = SLStatus.createErrorStatus(code, msg, e);
      final String errorDialogTitle = I18N.msg("common.manage.licenses.dialog.uninstall.failure");
      ErrorDialogUtility.open(getShell(), errorDialogTitle, SLEclipseStatusUtility.convert(status));
    }
    updateTableContents();
    updateButtonState();
  }

  /**
   * Activates or renews a set of licenses.
   * 
   * @param selection
   *          the licenses to renew or activate.
   * @param activation
   *          {@true} if this action is a license activation, {@code} false if
   *          this action is a license renewal.
   */
  void activateRenewLicenses(List<PossiblyActivatedSLLicense> selection, boolean activation) {
    final int count = selection.size();
    if (count < 1)
      return;

    final String passive = activation ? "Activation" : "Renewal";
    final String active = activation ? "Activate" : "Renew";

    if (!MessageDialog.openConfirm(
        getShell(),
        I18N.msg("common.manage.licenses.dialog.activaterenew.title", passive),
        I18N.msg("common.manage.licenses.dialog.activaterenew.msg", active,
            count > 1 ? count + " licenses" : "\"" + selection.get(0).getSignedSLLicense().getLicense().getProduct().toString()
                + "\" license"))) {
      return; // bail
    }

    try {
      SLLicenseUtility.tryToActivateRenewLicenses(selection, SLUtility.getMacAddressesOfThisMachine());
    } catch (Exception e) {
      final int code = 144;
      final String msg = I18N.err(code, e.getMessage());
      final SLStatus status = SLStatus.createErrorStatus(code, msg, e);
      final String errorDialogTitle = I18N.msg("common.manage.licenses.dialog.activaterenew.failure", active);
      ErrorDialogUtility.open(getShell(), errorDialogTitle, SLEclipseStatusUtility.convert(status));
    }
    updateTableContents();
    updateButtonState();
  }

  private Shell getShell() {
    return f_licenseTable.getShell();
  }
}
