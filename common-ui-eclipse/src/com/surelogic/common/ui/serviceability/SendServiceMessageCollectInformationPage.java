package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
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
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.serviceability.MessageWithLog;

public class SendServiceMessageCollectInformationPage extends WizardPage {

	SendServiceMessageCollectInformationPage(Message data) {
		super("collect");
		f_data = data;
	}

	private static final int CONTENTS_WIDTH_HINT = 400;
	private static final int TIP_HEIGHT_HINT = 150;

	private final Message f_data;

	@Override
	public void createControl(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		setControl(panel);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(I18N.msg(f_data.propPfx() + "info"));
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		info.setLayoutData(data);

		final Label email = new Label(panel, SWT.RIGHT);
		email.setText(I18N.msg(f_data.propPfx() + "email"));
		email.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text emailText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		emailText.setText(CommonCorePreferencesUtility.getServicabilityEmail());
		emailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label name = new Label(panel, SWT.RIGHT);
		name.setText(I18N.msg(f_data.propPfx() + "name"));
		name.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text nameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		nameText.setText(CommonCorePreferencesUtility.getServicabilityName());
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button sendVersion = new Button(panel, SWT.CHECK);
		sendVersion.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));
		sendVersion.setText(I18N.msg(f_data.propPfx() + "sendVersion"));
		sendVersion.setSelection(true);

		final Button sendEclipseLog;
		if (f_data instanceof MessageWithLog) {
			final MessageWithLog mwl = (MessageWithLog) f_data;
			sendEclipseLog = new Button(panel, SWT.CHECK);
			sendEclipseLog.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT,
					false, false, 2, 1));
			sendEclipseLog
					.setText(I18N.msg(f_data.propPfx() + "sendEclipseLog"));
			sendEclipseLog.setSelection(mwl.getSendLog());
		} else {
			sendEclipseLog = null;
		}

		final Label space = new Label(panel, SWT.NONE);
		space.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false, 2, 1));

		final Label summary = new Label(panel, SWT.RIGHT);
		summary.setText(I18N.msg(f_data.propPfx() + "summary"));
		summary.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text summaryText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		if (f_data.getMessage() != null)
			summaryText.setText(f_data.getMessage());
		summaryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label description = new Label(panel, SWT.WRAP);
		description.setText(I18N.msg(f_data.propPfx() + "desc"));
		data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		description.setLayoutData(data);

		final Text descriptionText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		descriptionText.setFont(JFaceResources.getTextFont());
		if (f_data.getDescription() != null)
			descriptionText.setText(f_data.getDescription());
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.heightHint = TIP_HEIGHT_HINT;
		descriptionText.setLayoutData(data);

		final Runnable updatePageComplete = new Runnable() {
			public void run() {
				f_data.setEmail(emailText.getText());
				f_data.setName(nameText.getText());
				f_data.setSummary(summaryText.getText());
				f_data.setDescription(descriptionText.getText());
				f_data.setSendVersionInfo(sendVersion.getSelection());
				if (f_data instanceof MessageWithLog) {
					final MessageWithLog mwl = (MessageWithLog) f_data;
					mwl.setSendLog(sendEclipseLog.getSelection());
				}

				setPageComplete(f_data.minimumDataEntered());
			}
		};

		final Listener listener = new Listener() {
			public void handleEvent(Event event) {
				updatePageComplete.run();
			}
		};

		emailText.addListener(SWT.Modify, listener);
		nameText.addListener(SWT.Modify, listener);
		summaryText.addListener(SWT.Modify, listener);
		descriptionText.addListener(SWT.Modify, listener);
		sendVersion.addListener(SWT.Selection, listener);
		if (f_data instanceof MessageWithLog) {
			sendEclipseLog.addListener(SWT.Selection, listener);
		}
		/*
		 * We have to run this a bit later so that the OK button is created.
		 */
		emailText.getDisplay().asyncExec(updatePageComplete);

		setTitle(I18N.msg(f_data.propPfx() + "msg.title"));
		setMessage(I18N.msg(f_data.propPfx() + "msg"),
				IMessageProvider.INFORMATION);
	}
}
