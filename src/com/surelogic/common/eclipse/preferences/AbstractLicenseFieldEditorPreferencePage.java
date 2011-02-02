package com.surelogic.common.eclipse.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.common.eclipse.dialogs.ManageLicensesDialog;
import com.surelogic.common.i18n.I18N;

/**
 * Abstract preference page class that adds a button to the preference page to
 * open the SureLogic license management dialog for the user.
 * <p>
 * Use this class when your preference page would extend
 * {@link FieldEditorPreferencePage}.
 */
public abstract class AbstractLicenseFieldEditorPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AbstractLicenseFieldEditorPreferencePage() {
		super(SWT.NONE);
	}

	protected AbstractLicenseFieldEditorPreferencePage(int style) {
		super(style);
	}

	@Override
	protected void contributeButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		final Button licenseButton = new Button(parent, SWT.PUSH);
		licenseButton.setText(I18N.msg("common.manage.licenses.dialog.title"));
		licenseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ManageLicensesDialog.open(getShell());
			}
		});
	}
}
