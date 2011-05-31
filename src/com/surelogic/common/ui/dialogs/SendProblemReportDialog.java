package com.surelogic.common.ui.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.jobs.EclipseJob;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.serviceability.ServiceUtility;
import com.surelogic.common.ui.BalloonUtility;
import com.surelogic.common.ui.SLImages;

/**
 * Dialog send a problem report to SureLogic.
 */
public final class SendProblemReportDialog extends TitleAreaDialog {

	/**
	 * Used to open the dialog.
	 * 
	 * @param shell
	 *            a shell.
	 * @param aboutTool
	 *            the SureLogic tool this report is about.
	 */
	public static void open(final Shell shell, final String aboutTool,
			final String imageSymbolicName) {
		final SendProblemReportDialog dialog = new SendProblemReportDialog(
				shell, aboutTool, imageSymbolicName);
		dialog.open();
	}

	private static final int CONTENTS_WIDTH_HINT = 700;
	private static final int TIP_HEIGHT_HINT = 200;

	private final String f_aboutTool;
	private final String f_imageSymbolicName;
	private Mediator f_mediator = null;

	public SendProblemReportDialog(Shell parentShell, String aboutTool,
			String imageSymbolicName) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.MAX | SWT.CLOSE | SWT.MODELESS
				| SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
		f_aboutTool = aboutTool == null ? "" : aboutTool;
		f_imageSymbolicName = imageSymbolicName;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (f_imageSymbolicName != null)
			newShell.setImage(SLImages.getImage(f_imageSymbolicName));
		String title = I18N.msg("common.send.problemReport.dialog.title");
		if (!"".equals(f_aboutTool)) {
			title = f_aboutTool + " " + title;
		}
		newShell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);

		final Composite panel = new Composite(contents, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = CONTENTS_WIDTH_HINT;
		panel.setLayoutData(data);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(I18N.msg("common.send.problemReport.dialog.info"));
		info.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2,
				1));

		final Label email = new Label(panel, SWT.RIGHT);
		email.setText(I18N.msg("common.send.problemReport.dialog.email"));
		email.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text emailText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		emailText.setText(CommonCorePreferencesUtility.getServicabilityEmail());
		emailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label name = new Label(panel, SWT.RIGHT);
		name.setText(I18N.msg("common.send.problemReport.dialog.name"));
		name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text nameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		nameText.setText(CommonCorePreferencesUtility.getServicabilityName());
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button sendVersion = new Button(panel, SWT.CHECK);
		sendVersion.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));
		sendVersion.setText(I18N
				.msg("common.send.problemReport.dialog.sendVersion"));
		sendVersion.setSelection(true);

		final Button sendEclipseLog = new Button(panel, SWT.CHECK);
		sendEclipseLog.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT,
				false, false, 2, 1));
		sendEclipseLog.setText(I18N
				.msg("common.send.problemReport.dialog.sendEclipseLog"));
		sendEclipseLog.setSelection(true);

		final Label space = new Label(panel, SWT.NONE);
		space.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));

		final Label summary = new Label(panel, SWT.RIGHT);
		summary.setText(I18N.msg("common.send.problemReport.dialog.summary"));
		summary
				.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text summaryText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		summaryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label tip = new Label(panel, SWT.NONE);
		tip.setText(I18N.msg("common.send.problemReport.dialog.tip"));
		tip.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false,
				2, 1));

		final Text tipText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.heightHint = TIP_HEIGHT_HINT;
		tipText.setLayoutData(data);

		final Button preview = new Button(panel, SWT.PUSH);
		preview.setText(I18N.msg("common.send.problemReport.dialog.preview"));
		preview.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, true, false,
				2, 1));

		setTitle(I18N.msg("common.send.problemReport.dialog.msg.title"));
		setMessage(I18N.msg("common.send.problemReport.dialog.msg"),
				IMessageProvider.INFORMATION);
		Dialog.applyDialogFont(panel);

		f_mediator = new Mediator(emailText, nameText, summaryText, tipText,
				sendVersion, sendEclipseLog, preview);
		f_mediator.init();

		return contents;
	}

	@Override
	protected void okPressed() {
		if (f_mediator != null) {
			f_mediator.okPressed();
		}
		super.okPressed();
	}

	private final class Mediator {

		private final Text f_email;
		private final Text f_name;
		private final Text f_summary;
		private final Text f_tip;
		private final Button f_sendVersion;
		private final Button f_sendEclipseLog;
		private final Button f_previewButton;

		public Mediator(Text email, Text name, Text summary, Text tip,
				Button sendVersion, Button sendEclipseLog, Button previewButton) {
			f_email = email;
			f_name = name;
			f_summary = summary;
			f_tip = tip;
			f_sendVersion = sendVersion;
			f_sendEclipseLog = sendEclipseLog;
			f_previewButton = previewButton;
		}

		public void init() {
			final Listener modifyListener = new Listener() {
				public void handleEvent(Event event) {
					setButtonState();
				}
			};
			f_summary.addListener(SWT.Modify, modifyListener);
			f_tip.addListener(SWT.Modify, modifyListener);
			/*
			 * We have to run this a bit later so that the OK button is created.
			 */
			f_tip.getDisplay().asyncExec(new Runnable() {
				public void run() {
					setButtonState();
				}
			});
			f_previewButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					ShowTextDialog.showText(getShell(), I18N
							.msg("common.send.problemReport.dialog.title"),
							getMsg());
				}
			});
		}

		private void setButtonState() {
			final boolean tipTyped = f_tip.getText().length() != 0;
			final boolean summaryTyped = f_summary.getText().length() != 0;
			final boolean hasText = tipTyped && summaryTyped;
			getButton(IDialogConstants.OK_ID).setEnabled(hasText);
			f_previewButton.setEnabled(hasText);
		}

		public String getMsg() {
			File ideLogFile = f_sendEclipseLog.getSelection() ? JDTUtility
					.getEclipseLogFile() : null;
			// return ServiceUtility.composeAProblemReport(f_aboutTool, f_email
			// .getText(), f_name.getText(), f_summary.getText(), f_tip
			// .getText(), f_sendVersion.getSelection(), JDTUtility
			// .getProductInfo(), ideLogFile);
			return "";
		}

		public void okPressed() {
			CommonCorePreferencesUtility.setServicabilityEmail(f_email
					.getText());
			CommonCorePreferencesUtility.setServicabilityName(f_name.getText());

			final String msg = getMsg();
			final SLJob job = ServiceUtility.sendToSureLogic(msg,
					new Runnable() {
						public void run() {
							BalloonUtility
									.showMessage(
											I18N
													.msg("common.send.problemReport.sent.title"),
											I18N
													.msg("common.send.problemReport.sent.message"));
						}
					});
			EclipseJob.getInstance().schedule(job);
		}
	}
}
