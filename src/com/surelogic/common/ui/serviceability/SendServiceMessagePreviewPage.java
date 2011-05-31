package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.surelogic.common.serviceability.Message;

public class SendServiceMessagePreviewPage extends WizardPage {

	SendServiceMessagePreviewPage(Message data) {
		super("previewInformation");
		setTitle("Preview your input");
		setDescription("A description of this page");
		f_data = data;
	}

	private final Message f_data;

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getDefault().getSystemColor(
				SWT.COLOR_BLUE));
		setControl(container);
	}
}
