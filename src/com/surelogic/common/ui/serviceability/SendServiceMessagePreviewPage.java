package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.serviceability.Message;

public class SendServiceMessagePreviewPage extends WizardPage {

	SendServiceMessagePreviewPage(Message data) {
		super("preview");
		f_data = data;
	}

	private final Message f_data;

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getDefault().getSystemColor(
				SWT.COLOR_BLUE));
		setControl(container);

		setTitle(I18N.msg(f_data.propPfx() + "preview.msg.title"));
		setMessage(I18N.msg(f_data.propPfx() + "preview.msg"),
				IMessageProvider.INFORMATION);
	}
}
