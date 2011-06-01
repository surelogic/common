package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.serviceability.ProblemReportMessage;
import com.surelogic.common.serviceability.TipMessage;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;

public class SendServiceMessageWizard extends Wizard {

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
	public static void openProblemReport(String product,
			String imageSymbolicName) {
		openProblemReport(null, product, imageSymbolicName);
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
	public static void openProblemReport(Shell shell, String product,
			String imageSymbolicName) {
		ProblemReportMessage prm = new ProblemReportMessage();
		if (product == null)
			product = "UNKNOWN";
		prm.setProduct(product);
		openHelper(shell, prm, imageSymbolicName);
	}

	/**
	 * Used to open the Send Tip for Improvement dialog using the shell being
	 * used by the active workbench window.
	 * 
	 * @param product
	 *            the SureLogic tool this tip for improvement is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 */
	public static void openTip(String product, String imageSymbolicName) {
		openTip(null, product, imageSymbolicName);
	}

	/**
	 * Used to open the Send Tip for Improvement dialog.
	 * 
	 * @param shell
	 *            a shell.
	 * @param product
	 *            the SureLogic tool this tip for improvement is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 */
	public static void openTip(Shell shell, String product,
			String imageSymbolicName) {
		TipMessage tip = new TipMessage();
		if (product == null)
			product = "UNKNOWN";
		tip.setProduct(product);
		openHelper(shell, tip, imageSymbolicName);
	}

	private static void openHelper(Shell shell, Message message,
			final String imageSymbolicName) {
		SendServiceMessageWizard wizard = new SendServiceMessageWizard(message);
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

	private final Message f_data;

	private SendServiceMessageCollectInformationPage f_collect;
	private SendServiceMessagePreviewPage f_preview;

	private SendServiceMessageWizard(Message data) {
		f_data = data;
		f_data.setDirty();
		f_data.setIdeVersion(JDTUtility.getProductInfo());
		String title = I18N.msg(f_data.propPfx() + "title");
		setWindowTitle(f_data.getProduct() + " " + title);
		setNeedsProgressMonitor(true);
		setHelpAvailable(false);
	}

	@Override
	public void addPages() {
		f_collect = new SendServiceMessageCollectInformationPage(f_data);
		addPage(f_collect);
		f_preview = new SendServiceMessagePreviewPage(f_data);
		addPage(f_preview);
	}

	@Override
	public boolean canFinish() {
		return f_data.minimumDataEntered();
	}

	@Override
	public boolean performFinish() {
		// TODO send to server
		return true;
	}
}
