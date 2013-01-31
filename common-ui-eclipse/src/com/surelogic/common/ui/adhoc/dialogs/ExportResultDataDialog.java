package com.surelogic.common.ui.adhoc.dialogs;

import java.io.File;

import org.eclipse.core.runtime.jobs.Job;
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
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.jobs.ExportResultDataInCSVFormatJob;
import com.surelogic.common.adhoc.jobs.ExportResultDataInTableHTMLFormatJob;
import com.surelogic.common.adhoc.jobs.ExportResultDataInTreeHTMLFormatJob;
import com.surelogic.common.adhoc.jobs.ExportResultDataJob;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;

public final class ExportResultDataDialog extends Dialog {

	public static void open(final AdHocQueryResultSqlData data) {
		if (data == null)
			throw new IllegalArgumentException(I18N.err(44, "data"));
		final Dialog dialog = new ExportResultDataDialog(
				EclipseUIUtility.getShell(), data);
		dialog.open();
	}

	private final AdHocQueryResultSqlData data;

	private Text f_exportFilenameText;
	private Button f_csvFormat;
	private Button f_htmlFormat;
	private Button f_htmlTreeFormat;

	public ExportResultDataDialog(final Shell parent,
			final AdHocQueryResultSqlData data) {
		super(parent);
		this.data = data;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_EXPORT));
		newShell.setText("Export Findings");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);
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
			@Override
      public void handleEvent(final Event event) {
				changeFileExtension("html", "csv");
			}
		});
		f_htmlFormat = new Button(g, SWT.RADIO);
		f_htmlFormat.setText("HTML Table");
		f_htmlFormat.setSelection(false);
		f_htmlFormat.addListener(SWT.Selection, new Listener() {
			@Override
      public void handleEvent(final Event event) {
				changeFileExtension("csv", "html");
			}
		});
		f_htmlTreeFormat = new Button(g, SWT.RADIO);
		f_htmlTreeFormat.setText("HTML Tree");
		f_htmlTreeFormat.setSelection(false);
		f_htmlTreeFormat.addListener(SWT.Selection, new Listener() {
			@Override
      public void handleEvent(final Event event) {
				changeFileExtension("csv", "html");
			}
		});

		final Label buildfilenameLabel = new Label(panel, SWT.NONE);
		buildfilenameLabel.setText("Export file:");

		f_exportFilenameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		f_exportFilenameText.setText(System.getProperty("user.home")
				+ System.getProperty("file.separator") + "data.csv");
		final GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_exportFilenameText.setLayoutData(data);

		final Button browseButton = new Button(panel, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		browseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			@Override
      public void handleEvent(final Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.SAVE);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.csv", "*.xml",
							"*.*" });
					fd.setFilterNames(new String[] { "CSV Files (*.csv)",
							"XML Files (*.xml)", "All Files (*.*)" });
				}
				final String fileName = f_exportFilenameText.getText();
				final int i = fileName.lastIndexOf(System
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
		final File exportfile = new File(f_exportFilenameText.getText());
		if (exportfile != null) {
			ExportResultDataJob job;
			if (f_csvFormat.getSelection()) {
				// CSV format
				job = new ExportResultDataInCSVFormatJob(data, exportfile);
			} else if (f_htmlFormat.getSelection()) {
				job = new ExportResultDataInTableHTMLFormatJob(data, exportfile);
			} else {
				job = new ExportResultDataInTreeHTMLFormatJob(data, exportfile);
			}
			final Job eJob = EclipseUtility.toEclipseJob(job);
			eJob.setUser(true);
			eJob.schedule();
		}
		super.okPressed();
	}

	private void changeFileExtension(final String from, final String to) {
		final StringBuilder b = new StringBuilder(
				f_exportFilenameText.getText());
		if (b.toString().endsWith(from)) {
			b.replace(b.length() - from.length(), b.length(), to);
		}
		f_exportFilenameText.setText(b.toString());
	}

}
