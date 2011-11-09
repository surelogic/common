package com.surelogic.common.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.dialogs.ManageLicensesDialog;

/**
 * Abstract preference page class that adds a button to the preference page to
 * open the SureLogic license management dialog for the user.
 * <p>
 * Use this class when your preference page would extend {@link PreferencePage}.
 */
public abstract class AbstractLicensePreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

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
