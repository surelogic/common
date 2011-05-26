package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class SendProblemReportPreviewPage extends WizardPage {

	SendProblemReportPreviewPage() {
		super("previewInformation");
		setTitle("Preview your input");
		setDescription("A description of this page");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getDefault().getSystemColor(
				SWT.COLOR_BLUE));
		setControl(container);
	}
}
