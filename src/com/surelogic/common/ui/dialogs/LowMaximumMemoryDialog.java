package com.surelogic.common.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.core.preferences.CommonCorePreferencesUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.SWTUtility;

public final class LowMaximumMemoryDialog extends Dialog {

	private final long f_maxMemoryMB;
	private final long f_maxPermGenMB;

	private static final int f_widthHint = 350;

	public LowMaximumMemoryDialog(final long maxMemoryMB, final long permGenMB) {
		super(SWTUtility.getShell());
		f_maxMemoryMB = maxMemoryMB;
		f_maxPermGenMB = permGenMB;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO));
		newShell.setText("Low Maximum Memory Warning");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		banner.setImage(SLImages
				.getImage(CommonImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite msgPanel = new Composite(panel, SWT.NONE);
		msgPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		gridLayout = new GridLayout();
		msgPanel.setLayout(gridLayout);

		int numWarnings = 0;
		if (f_maxMemoryMB < SLEclipseStatusUtility.LOW_MEM_THRESHOLD) {
			createLabel(msgPanel,
					I18N.msg("sierra.eclipse.lowMemoryWarning1", f_maxMemoryMB));
			numWarnings++;
		}
		if (f_maxPermGenMB < SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD) {
			createLabel(msgPanel, I18N.msg("sierra.eclipse.lowPermGenWarning1",
					f_maxPermGenMB));
			numWarnings++;
		}
		if (numWarnings == 1) {
			createLabel(msgPanel, I18N.msg("sierra.eclipse.lowMemoryWarning2"));
		} else {
			createLabel(msgPanel, I18N.msg("sierra.eclipse.lowMemoryWarnings2"));
		}
		Group recommendations = new Group(msgPanel, SWT.NONE);
		recommendations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		gridLayout = new GridLayout();
		recommendations.setLayout(gridLayout);
		recommendations.setText(I18N.msg("sierra.eclipse.lowMemoryWarning3"));

		if (f_maxMemoryMB < SLEclipseStatusUtility.LOW_MEM_THRESHOLD) {
			final int mem = SLEclipseStatusUtility.LOW_MEM_THRESHOLD;
			createLabel(recommendations,
					I18N.msg("sierra.eclipse.lowMemoryWarning4", mem, mem));
		}
		if (f_maxPermGenMB < SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD) {
			final int mem = SLEclipseStatusUtility.LOW_PERMGEN_THRESHOLD;
			createLabel(recommendations,
					I18N.msg("sierra.eclipse.lowPermGenWarning4", mem, mem));
		}
		createLabel(msgPanel, I18N.msg("sierra.eclipse.lowMemoryWarning5"));
		createLabel(msgPanel, I18N.msg("sierra.eclipse.lowMemoryWarning6"));

		final Button check = new Button(msgPanel, SWT.CHECK);
		GridData data = new GridData(SWT.DEFAULT, SWT.BOTTOM, false, true);
		check.setLayoutData(data);
		check.setText("Please do not show this warning again");
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				CommonCorePreferencesUtility
						.setWarnAboutLowMaximumMemory(!check.getSelection());
			}
		});

		return panel;
	}

	private void createLabel(Composite parent, String text) {
		Label msg = new Label(parent, SWT.WRAP);
		GridData data = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		data.widthHint = f_widthHint;
		msg.setLayoutData(data);
		msg.setText(text);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}
}
