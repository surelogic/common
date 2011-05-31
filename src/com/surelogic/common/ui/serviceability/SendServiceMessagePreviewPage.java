package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.serviceability.Message;

public class SendServiceMessagePreviewPage extends WizardPage {

	SendServiceMessagePreviewPage(Message data) {
		super("preview");
		f_data = data;
	}

	private final Message f_data;
	private Text f_descriptionText;

	@Override
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		setControl(panel);
		panel.setLayout(new FillLayout());

		f_descriptionText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		f_descriptionText.setFont(JFaceResources.getTextFont());

		setTitle(I18N.msg(f_data.propPfx() + "preview.msg.title"));
		setMessage(I18N.msg(f_data.propPfx() + "preview.msg"),
				IMessageProvider.INFORMATION);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			f_data.generateMessage();
			f_descriptionText.setText(f_data.getMessage());

		}
		super.setVisible(visible);
	}
}
