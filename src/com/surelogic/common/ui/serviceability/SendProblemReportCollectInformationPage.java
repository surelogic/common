package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;

public class SendProblemReportCollectInformationPage extends WizardPage {

	public SendProblemReportCollectInformationPage() {
		super("collectInformation");
	}

	private static final int CONTENTS_WIDTH_HINT = 400;
	private static final int TIP_HEIGHT_HINT = 150;

	@Override
	public void createControl(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		setControl(panel);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label info = new Label(panel, SWT.WRAP);
		info.setText(I18N.msg("common.send.problemReport.dialog.info"));
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		info.setLayoutData(data);

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
		summary.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		final Text summaryText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		summaryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label tip = new Label(panel, SWT.WRAP);
		tip.setText(I18N.msg("common.send.problemReport.dialog.tip"));
		data = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
		data.widthHint = CONTENTS_WIDTH_HINT;
		tip.setLayoutData(data);

		final Text tipText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.heightHint = TIP_HEIGHT_HINT;
		tipText.setLayoutData(data);

		parent.pack();

		setTitle(I18N.msg("common.send.problemReport.dialog.msg.title"));
		setMessage(I18N.msg("common.send.problemReport.dialog.msg"),
				IMessageProvider.INFORMATION);
	}
}
