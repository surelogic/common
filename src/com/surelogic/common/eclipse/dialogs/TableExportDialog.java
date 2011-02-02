package com.surelogic.common.eclipse.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.core.jobs.TableExportJob;
import com.surelogic.common.export.ExportFactory;
import com.surelogic.common.export.ExportTableDataSource;
import com.surelogic.common.export.ITableExporter;

public final class TableExportDialog extends Dialog {

	private final ExportTableDataSource f_source;
	private final String f_dialogTitle;
	private final String accessKey;
	
	private Text f_exportFilenameText;
	private Button f_csvFormat;
	private Button f_htmlFormat;

	public TableExportDialog(Shell shell, String dialogTitle,
			ExportTableDataSource source, String key) {
		super(shell);
		assert source != null;
		f_source = source;
		if (dialogTitle == null) {
			f_dialogTitle = "Export";
		} else {
			f_dialogTitle = dialogTitle;
		}
		accessKey = key;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_EXPORT));
		newShell.setText(f_dialogTitle);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		directions.setText("Select the desired export"
				+ " format and the destination filename");

		final Group g = new Group(panel, SWT.NONE);
		g.setText("Export Format");
		g.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
		final RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		rl.wrap = false;
		g.setLayout(rl);

		f_csvFormat = new Button(g, SWT.RADIO);
		f_csvFormat.setText("Comma Separated Values (CSV)");
		f_csvFormat.setSelection(true);
		f_csvFormat.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				changeFileExtension("html", "csv");
			}
		});

		f_htmlFormat = new Button(g, SWT.RADIO);
		f_htmlFormat.setText("HTML Table (viewable in a web browser)");
		f_htmlFormat.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				changeFileExtension("csv", "html");
			}
		});

		Label buildfilenameLabel = new Label(panel, SWT.NONE);
		buildfilenameLabel.setText("Export file:");

		f_exportFilenameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		f_exportFilenameText.setText(System.getProperty("user.home")
				+ System.getProperty("file.separator") + "export.csv");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_exportFilenameText.setLayoutData(data);

		final Button browseButton = new Button(panel, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		browseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			public void handleEvent(Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.SAVE);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.csv", "*.html",
							"*.*" });
					fd.setFilterNames(new String[] { "CSV Files (*.csv)",
							"HTML Files (*.html)", "All Files (*.*)" });
				}
				final String fileName = f_exportFilenameText.getText();
				int i = fileName.lastIndexOf(System
						.getProperty("file.separator"));
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
					f_exportFilenameText.setText(selectedFilename);
				}
			}
		});

		return panel;
	}

	@Override
	protected void okPressed() {
		final File to = new File(f_exportFilenameText.getText());
		if (to != null) {
			final ITableExporter exporter;
			if (f_csvFormat.getSelection()) {
				// CSV format
				exporter = ExportFactory.asCSV(f_source, to);
			} else {
				// HTML format
				exporter = ExportFactory.asHTML(f_source, to);
			}
			final TableExportJob job = new TableExportJob(exporter, accessKey);
			job.schedule();
		}
		super.okPressed();
	}

	private void changeFileExtension(String from, String to) {
		StringBuilder b = new StringBuilder(f_exportFilenameText.getText());
		if (b.toString().endsWith(from)) {
			b.replace(b.length() - from.length(), b.length(), to);
		}
		f_exportFilenameText.setText(b.toString());
	}
}
