package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;

public class SendProblemReportCollectInformationPage extends WizardPage {

	SendProblemReportCollectInformationPage() {
		super("collectInformation");
	}

	private static final int CONTENTS_WIDTH_HINT = 400;
	private static final int TIP_HEIGHT_HINT = 150;
	
	private Mediator f_mediator = null;

	@Override
	public void createControl(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		setControl(panel);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(I18N.msg("common.send.problemReport.wizard.info"));
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		info.setLayoutData(data);

		final Label email = new Label(panel, SWT.RIGHT);
		email.setText(I18N.msg("common.send.problemReport.wizard.email"));
		email.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text emailText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		emailText.setText(CommonCorePreferencesUtility.getServicabilityEmail());
		emailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label name = new Label(panel, SWT.RIGHT);
		name.setText(I18N.msg("common.send.problemReport.wizard.name"));
		name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text nameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		nameText.setText(CommonCorePreferencesUtility.getServicabilityName());
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button sendVersion = new Button(panel, SWT.CHECK);
		sendVersion.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));
		sendVersion.setText(I18N
				.msg("common.send.problemReport.wizard.sendVersion"));
		sendVersion.setSelection(true);

		final Button sendEclipseLog = new Button(panel, SWT.CHECK);
		sendEclipseLog.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT,
				false, false, 2, 1));
		sendEclipseLog.setText(I18N
				.msg("common.send.problemReport.wizard.sendEclipseLog"));
		sendEclipseLog.setSelection(true);

		final Label space = new Label(panel, SWT.NONE);
		space.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));

		final Label summary = new Label(panel, SWT.RIGHT);
		summary.setText(I18N.msg("common.send.problemReport.wizard.summary"));
		summary.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text summaryText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		summaryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label tip = new Label(panel, SWT.WRAP);
		tip.setText(I18N.msg("common.send.problemReport.wizard.tip"));
		data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		tip.setLayoutData(data);

		final Text tipText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.heightHint = TIP_HEIGHT_HINT;
		tipText.setLayoutData(data);

		parent.pack();
		
		f_mediator = new Mediator(emailText, nameText, summaryText, tipText,
				sendVersion, sendEclipseLog);
		f_mediator.init();

		setTitle(I18N.msg("common.send.problemReport.wizard.msg.title"));
		setMessage(I18N.msg("common.send.problemReport.wizard.msg"),
				IMessageProvider.INFORMATION);
	}
	
	private final class Mediator {

		private final Text f_email;
		private final Text f_name;
		private final Text f_summary;
		private final Text f_tip;
		private final Button f_sendVersion;
		private final Button f_sendEclipseLog;

		public Mediator(Text email, Text name, Text summary, Text tip,
				Button sendVersion, Button sendEclipseLog) {
			f_email = email;
			f_name = name;
			f_summary = summary;
			f_tip = tip;
			f_sendVersion = sendVersion;
			f_sendEclipseLog = sendEclipseLog;
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
		}

		private void setButtonState() {
			final boolean tipTyped = f_tip.getText().length() != 0;
			final boolean summaryTyped = f_summary.getText().length() != 0;
			final boolean hasText = tipTyped && summaryTyped;
			setPageComplete(hasText);
		}

//		public String getMsg() {
//			File ideLogFile = f_sendEclipseLog.getSelection() ? JDTUtility
//					.getEclipseLogFile() : null;
//			return ServiceUtility.composeAProblemReport(f_aboutTool, f_email
//					.getText(), f_name.getText(), f_summary.getText(), f_tip
//					.getText(), f_sendVersion.getSelection(), JDTUtility
//					.getProductInfo(), ideLogFile);
//		}
//
//		public void okPressed() {
//			CommonCorePreferencesUtility.setServicabilityEmail(f_email
//					.getText());
//			CommonCorePreferencesUtility.setServicabilityName(f_name.getText());
//
//			final String msg = getMsg();
//			final SLJob job = ServiceUtility.sendToSureLogic(msg,
//					new Runnable() {
//						public void run() {
//							BalloonUtility
//									.showMessage(
//											I18N
//													.msg("common.send.problemReport.sent.title"),
//											I18N
//													.msg("common.send.problemReport.sent.message"));
//						}
//					});
//			EclipseJob.getInstance().schedule(job);
//		}
	}
}
