package com.surelogic.common.ui.adhoc.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;

public class LotsOfSavedQueriesDialog extends MessageDialog {

	public static boolean show() {
		return show(null);
	}

	public static boolean show(Shell shell) {
		final LotsOfSavedQueriesDialog dialog = new LotsOfSavedQueriesDialog(
				shell == null ? EclipseUIUtility.getShell() : shell);
		dialog.open();
		return dialog.f_rememberMyDecision;
	}

	boolean f_rememberMyDecision = false;

	private LotsOfSavedQueriesDialog(final Shell shell) {
		super(shell, I18N.msg("adhoc.query.dialog.lotsOfSavedQueries.title"),
				SLImages.getImage(CommonImages.IMG_DRUM_EXPLORER), I18N
						.msg("adhoc.query.dialog.lotsOfSavedQueries.msg"),
				MessageDialog.INFORMATION, new String[] { "OK" }, 0);
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		final Button rememberMyDecision = new Button(parent, SWT.CHECK);
		rememberMyDecision.setText(I18N
				.msg("adhoc.query.dialog.lotsOfSavedQueries.remember"));
		rememberMyDecision.setSelection(f_rememberMyDecision);
		rememberMyDecision.addListener(SWT.Selection, new Listener() {
			@Override
      public void handleEvent(Event event) {
				f_rememberMyDecision = rememberMyDecision.getSelection();
			}
		});
		return super.createCustomArea(parent);
	}

}
