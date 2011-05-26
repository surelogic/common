package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;

public class SendProblemReportWizard extends Wizard implements INewWizard {

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
		String title = I18N.msg("common.send.problemReport.dialog.title");
		if (!"".equals(aboutTool)) {
			title = aboutTool + " " + title;
		}
		final SendProblemReportWizard wizard = new SendProblemReportWizard(
				title);
		final WizardDialog dialog = new WizardDialog(shell, wizard) {
			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				if (imageSymbolicName != null)
					newShell.setImage(SLImages.getImage(imageSymbolicName));

			}
		};
		dialog.open();
	}

	private final String f_windowTitle;

	private SendProblemReportCollectInformationPage f_collectInformationPage;
	private SendProblemReportPreviewPage f_previewInformationPage;

	private SendProblemReportWizard(String windowTitle) {
		f_windowTitle = windowTitle;
	}

	@Override
	public void addPages() {
		setWindowTitle(f_windowTitle);
		f_collectInformationPage = new SendProblemReportCollectInformationPage();
		addPage(f_collectInformationPage);
		f_previewInformationPage = new SendProblemReportPreviewPage();
		addPage(f_previewInformationPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}
