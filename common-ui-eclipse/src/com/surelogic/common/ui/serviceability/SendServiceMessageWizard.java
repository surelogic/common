package com.surelogic.common.ui.serviceability;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.jobs.EclipseJob;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.serviceability.JSureScanFailureMessage;
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.serviceability.MessageWithLog;
import com.surelogic.common.serviceability.ProblemReportMessage;
import com.surelogic.common.serviceability.ServiceUtility;
import com.surelogic.common.serviceability.SierraScanFailureMessage;
import com.surelogic.common.serviceability.TipMessage;
import com.surelogic.common.ui.BalloonUtility;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.common.ui.jobs.SLJobWrapperRunnableWithProgress;

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
		ProblemReportMessage prm = new ProblemReportMessage(
				JDTUtility.getEclipseLogFile());
		if (product == null)
			product = "UNKNOWN";
		prm.setProduct(product);
		startJobToReadInLog(prm);
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
		openTip(null, product, imageSymbolicName, new TipMessage());
	}

	/**
	 * Used to open the Send Tip for Improvement dialog using the shell being
	 * used by the active workbench window. The tip message may be populated
	 * with information.
	 * 
	 * @param product
	 *            the SureLogic tool this tip for improvement is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 * @param tip
	 *            the non-null tip message which may be populated with contents.
	 * 
	 * @throws IllegalArgumentException
	 *             if tip is {@code null}.
	 */
	public static void openTip(String product, String imageSymbolicName,
			TipMessage tip) {
		openTip(null, product, imageSymbolicName, tip);
	}

	/**
	 * Used to open the Send Tip for Improvement dialog. The tip message may be
	 * populated with information.
	 * 
	 * @param shell
	 *            a shell, or {@code null} to use the shell being used by the
	 *            active workbench window.
	 * @param product
	 *            the SureLogic tool this tip for improvement is about.
	 * @param imageSymbolicName
	 *            an image name from {@link CommonImages} to use for the window.
	 *            This image is only displayed on some operating systems (e.g.,
	 *            Windows).
	 * @param tip
	 *            the non-null tip message which may be populated with contents.
	 * 
	 * @throws IllegalArgumentException
	 *             if tip is {@code null}.
	 */
	public static void openTip(Shell shell, String product,
			String imageSymbolicName, TipMessage tip) {
		if (tip == null)
			throw new IllegalArgumentException(I18N.err(44, "tip"));
		if (product == null)
			product = "UNKNOWN";
		tip.setProduct(product);
		openHelper(shell, tip, imageSymbolicName);
	}

	/**
	 * Asks the user if they want to report a JSure scan crash to SureLogic.
	 * <p>
	 * Must be called from the UI thread.
	 * 
	 * @param product
	 *            the SureLogic tool this crash report is about. Must be
	 *            non-null. This is known to be JSure, however, the client may
	 *            include a version as well as a name.
	 * @param status
	 *            the status message about the scan crash. Must be non-null.
	 * @param scanLog
	 *            the scan log file. Must be non-null.
	 */
	public static void openJSureScanCrashReport(String product,
			SLStatus status, File scanLog) {
		JSureScanFailureMessage sfm = new JSureScanFailureMessage(scanLog);
		sfm.setProduct(product);
		String msg = status.getMessage();
		if (msg == null || msg.isEmpty()) {
			msg = sfm.getMessageTypeString();
		} else {
			msg = sfm.getMessageTypeString() + " : " + status.getMessage();
		}
		sfm.setMessage(msg);
		sfm.setDescription(status.toString());
		startJobToReadInLog(sfm);
		openHelper(null, sfm, CommonImages.IMG_JSURE_LOGO);
	}

	/**
	 * Asks the user if they want to report a Sierra scan crash to SureLogic.
	 * <p>
	 * Must be called from the UI thread.
	 * 
	 * @param product
	 *            the SureLogic tool this crash report is about. Must be
	 *            non-null. This is known to be Sierra, however, the client may
	 *            include a version as well as a name.
	 * @param status
	 *            the status message about the scan crash. Must be non-null.
	 * @param scanLog
	 *            the scan log file. Must be non-null.
	 */
	public static void openSierraScanCrashReport(String product,
			SLStatus status, File scanLog) {
		SierraScanFailureMessage sfm = new SierraScanFailureMessage(scanLog);
		sfm.setProduct(product);
		String msg = status.getMessage();
		if (msg == null || msg.isEmpty()) {
			msg = sfm.getMessageTypeString();
		} else {
			msg = sfm.getMessageTypeString() + " : " + status.getMessage();
		}
		sfm.setMessage(msg);
		sfm.setDescription(status.toString());
		startJobToReadInLog(sfm);
		openHelper(null, sfm, CommonImages.IMG_SIERRA_LOGO);
	}

	private static void startJobToReadInLog(MessageWithLog msg) {
		/*
		 * We do this here because code in 'common' can't start jobs.
		 */
		final SLJob job = msg.getReadInLogContentsJob();
		EclipseJob.getInstance().schedule(job, false, true);
	}

	private static void openHelper(Shell shell, Message message,
			final String imageSymbolicName) {
		SendServiceMessageWizard wizard = new SendServiceMessageWizard(message);
		if (shell == null)
			shell = EclipseUIUtility.getShell();
		final WizardDialog dialog = new WizardDialog(shell, wizard) {

			@Override
			protected void setShellStyle(int newShellStyle) {
				// This is a total hack to make the wizard modeless
				super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE
						| SWT.BORDER | SWT.MODELESS | SWT.RESIZE
						| getDefaultOrientation());
			}

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
		CommonCorePreferencesUtility.setServicabilityEmail(f_data.getEmail());
		CommonCorePreferencesUtility.setServicabilityName(f_data.getName());
		f_data.generateMessage(false);
		final String msg = f_data.getMessage();
		final SLJob job = ServiceUtility.sendToSureLogic(msg, new Runnable() {
			public void run() {
				BalloonUtility.showMessage(
						I18N.msg(f_data.propPfx() + "sent.title"),
						I18N.msg(f_data.propPfx() + "sent.message"));
			}
		});
		final SLJobWrapperRunnableWithProgress rwp = new SLJobWrapperRunnableWithProgress(
				job);
		try {
			getContainer().run(true, false, rwp);
		} catch (InvocationTargetException e) {
			ErrorDialogUtility.open(null, null,
					SLEclipseStatusUtility.convert(rwp.getResultAsSLStatus()));
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
}
