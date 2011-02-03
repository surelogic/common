package com.surelogic.common.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.i18n.I18N;

/**
 * A dialog to show the instrumentation log to the user.
 */
public final class ShowTextDialog extends Dialog {

	/**
	 * Opens a dialog to display some text. The dialog will not allow the user
	 * to edit the displayed text. The dialog only has an OK button.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param title
	 *            the dialog title.
	 * @param text
	 *            the text to display.
	 */
	public static void showText(Shell parentShell, String title, String text) {
		final Dialog d = new ShowTextDialog(parentShell, title, text, false);
		d.open();
	}

	/**
	 * Opens a dialog to display and allow changes to, or cut-and-paste, of
	 * text. The dialog has an OK and a Cancel button. If the OK button is
	 * pressed the mutated text is returned. If the Cancel button is pressed
	 * {@code null} is returned.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param title
	 *            the dialog title.
	 * @param text
	 *            the text to display and allow to be edited.
	 * @return the mutated text or, {@code null} if Cancel was pressed instead
	 *         of OK.
	 */
	public static String mutateText(Shell parentShell, String title, String text) {
		final ShowTextDialog d = new ShowTextDialog(parentShell, title, text,
				true);
		d.open();
		return d.f_textAtClose;
	}

	private final String f_title;

	private final String f_text;

	private final boolean f_textIsMutable;

	private Text f_textWidget = null;

	private String f_textAtClose = null;

	private ShowTextDialog(Shell parentShell, String title, String text,
			boolean textIsMutable) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (title == null)
			throw new IllegalArgumentException(I18N.err(44, "title"));
		f_title = title;
		if (text == null)
			throw new IllegalArgumentException(I18N.err(44, "text"));
		f_text = text;
		f_textIsMutable = textIsMutable;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		final Layout layout;
		if (f_textIsMutable) {
			layout = new GridLayout();
		} else {
			layout = new FillLayout();
		}
		c.setLayout(layout);
		f_textWidget = new Text(c, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		if (f_textIsMutable) {
			final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.minimumHeight = 200;
			data.minimumWidth = 400;
			f_textWidget.setLayoutData(data);
		}
		f_textWidget.setFont(JFaceResources.getTextFont());
		f_textWidget.setText(f_text);
		f_textWidget.setEditable(f_textIsMutable);
		return c;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(f_title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (f_textIsMutable)
			super.createButtonsForButtonBar(parent);
		else
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected void okPressed() {
		String text = f_textWidget.getText();
		if (text != null)
			f_textAtClose = text;
		super.okPressed();
	}
}
