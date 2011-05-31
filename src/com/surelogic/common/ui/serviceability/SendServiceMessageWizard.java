package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.serviceability.ProblemReportMessage;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;

public class SendServiceMessageWizard extends Wizard implements INewWizard {

	/**
	 * Used to open the Send Problem Report dialog using the shell being used by
	 * the active workbench window.
	 * 
	 * @param product
	 *            the SureLogic tool this report is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 */
	public static void open(String product, final String imageSymbolicName) {
		open(null, product, imageSymbolicName);
	}

	/**
	 * Used to open the Send Problem Report dialog.
	 * 
	 * @param shell
	 *            a shell.
	 * @param product
	 *            the SureLogic tool this report is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 */
	public static void open(Shell shell, String product,
			final String imageSymbolicName) {
		final SendServiceMessageWizard wizard = new SendServiceMessageWizard(
				product);
		if (shell == null)
			shell = EclipseUIUtility.getShell();
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
	private final Message f_data;

	private SendServiceMessageCollectInformationPage f_collectInformationPage;
	private SendServiceMessagePreviewPage f_previewInformationPage;

	private SendServiceMessageWizard(String product) {
		if (product == null)
			product = "UNKNOWN";
		String title = I18N.msg("common.send.problemReport.wizard.title");
		if (product != null) {
			title = product + " " + title;
		}
		f_windowTitle = title;
		f_data = new ProblemReportMessage();
		f_data.setProduct(product);
		f_data.setIdeVersion(JDTUtility.getProductInfo());
	}

	@Override
	public void addPages() {
		setWindowTitle(f_windowTitle);
		f_collectInformationPage = new SendServiceMessageCollectInformationPage(
				f_data);
		addPage(f_collectInformationPage);
		f_previewInformationPage = new SendServiceMessagePreviewPage(f_data);
		addPage(f_previewInformationPage);
	}

	@Override
	public boolean performFinish() {
		return f_data.minimumDataEntered();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}
