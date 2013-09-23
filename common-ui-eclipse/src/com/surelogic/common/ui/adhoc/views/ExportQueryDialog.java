package com.surelogic.common.ui.adhoc.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocPersistence;
import com.surelogic.common.adhoc.AdHocQuery;

public final class ExportQueryDialog extends Dialog {

  final AdHocManager f_manager;

  private Mediator f_mediator = null;

  public ExportQueryDialog(final Shell parentShell, final AdHocManager manager) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    f_manager = manager;
  }

  @Override
  protected Control createDialogArea(final Composite parent) {
    final Composite container = (Composite) super.createDialogArea(parent);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    container.setLayout(gridLayout);

    final Button exportAllToggle = new Button(container, SWT.CHECK);
    exportAllToggle.setText("Export all queries");
    exportAllToggle.setSelection(true);
    exportAllToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

    final Group queryGroup = new Group(container, SWT.NONE);
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
    data.heightHint = 300;
    queryGroup.setLayoutData(data);
    queryGroup.setText("Available Queries");
    queryGroup.setLayout(new FillLayout());
    queryGroup.setEnabled(false);

    final Table queryTable = new Table(queryGroup, SWT.CHECK);
    queryTable.setEnabled(false);

    for (final AdHocQuery query : f_manager.getQueryList()) {
      final TableItem item = new TableItem(queryTable, SWT.NONE);
      item.setText(query.getDescription());
      item.setData(query);
    }

    final Label directions = new Label(container, SWT.NONE);
    directions.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    directions.setText("To file:");

    final Text fileNameText = new Text(container, SWT.SINGLE);
    fileNameText.setText(System.getProperty("user.home") + System.getProperty("file.separator") + "adhoc.xml");
    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    data.widthHint = 300;
    fileNameText.setLayoutData(data);

    final Button browseButton = new Button(container, SWT.PUSH);
    browseButton.setText("Browse...");
    browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

    f_mediator = new Mediator(exportAllToggle, queryGroup, queryTable, fileNameText, browseButton);
    f_mediator.init();
    return container;
  }

  @Override
  protected void configureShell(final Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Export Queries");
  }

  @Override
  protected void okPressed() {
    if (f_mediator != null) {
      f_mediator.doExport();
    }
    super.okPressed();
  }

  public void setOKEnabled(final boolean enabled) {
    final Button ok = getButton(IDialogConstants.OK_ID);
    ok.setEnabled(enabled);
  }

  class Mediator {

    private final Listener f_setDialogState = new Listener() {
      @Override
      public void handleEvent(final Event event) {
        setDialogState();
      }
    };

    final Button f_exportAllToggle;

    final Group f_queryGroup;

    final Table f_queryTable;

    final Text f_fileNameText;

    final Button f_browseButton;

    FileDialog fd = null;

    Mediator(final Button exportAllToggle, final Group queryGroup, final Table queryTable, final Text fileNameText,
        final Button browseButton) {
      f_exportAllToggle = exportAllToggle;
      f_queryGroup = queryGroup;
      f_queryTable = queryTable;
      f_fileNameText = fileNameText;
      f_browseButton = browseButton;
    }

    void init() {
      f_exportAllToggle.addListener(SWT.Selection, f_setDialogState);
      f_fileNameText.addListener(SWT.Modify, f_setDialogState);
      f_browseButton.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(final Event event) {
          if (fd == null) {
            fd = new FileDialog(getShell(), SWT.SAVE);
            fd.setText("Destination File");
            fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
            fd.setFilterNames(new String[] { "XML Files (*.xml)", "All Files (*.*)" });
          }
          final String fileName = f_fileNameText.getText();
          final int i = fileName.lastIndexOf(System.getProperty("file.separator"));
          if (i != -1) {
            final String path = fileName.substring(0, i);
            fd.setFilterPath(path);
            if (i + 1 < fileName.length()) {
              final String file = fileName.substring(i + 1);
              fd.setFileName(file);
            }
          }
          final String selectedFilename = fd.open();
          if (selectedFilename != null) {
            f_fileNameText.setText(selectedFilename);
          }
        }
      });
    }

    void doExport() {
      final String fileName = f_fileNameText.getText();
      final File exportFile = new File(fileName);
      final List<AdHocQuery> selectedQueries;
      if (f_exportAllToggle.getSelection()) {
        selectedQueries = null;
      } else {
        selectedQueries = new ArrayList<AdHocQuery>();
        for (final TableItem item : f_queryTable.getItems()) {
          if (item.getChecked())
            selectedQueries.add((AdHocQuery) item.getData());
        }
      }
      AdHocPersistence.exportDefaultFile(f_manager, selectedQueries, exportFile);
    }

    void setDialogState() {
      final String fileName = f_fileNameText.getText();
      setOKEnabled(!("".equals(fileName)));
      final boolean queryEnabled = !f_exportAllToggle.getSelection();
      f_queryGroup.setEnabled(queryEnabled);
      f_queryTable.setEnabled(queryEnabled);
    }
  }
}
