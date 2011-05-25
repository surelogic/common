package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SendProblemReportWizard extends Wizard implements INewWizard {

	private SendProblemReportCollectInformationPage collectInformationPage;
	private SendProblemReportPreviewPage previewInformationPage;

	@Override
	public void addPages() {
		setWindowTitle("Send Problem Report");
		collectInformationPage = new SendProblemReportCollectInformationPage();
		addPage(collectInformationPage);
		previewInformationPage = new SendProblemReportPreviewPage();
		addPage(previewInformationPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}
