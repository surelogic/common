package com.surelogic.common.ui.serviceability;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.ui.printing.SLPrintingUtility;

public class SendServiceMessagePreviewPage extends WizardPage {

	SendServiceMessagePreviewPage(Message data) {
		super("preview");
		f_data = data;
	}

	private final Message f_data;
	private Text f_descriptionText;

	@Override
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		setControl(panel);

		GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		f_descriptionText = new Text(panel, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		f_descriptionText.setFont(JFaceResources.getTextFont());
		f_descriptionText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				f_data.setMessage(f_descriptionText.getText());
			}
		});
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_descriptionText.setLayoutData(data);

		Button print = new Button(panel, SWT.PUSH);
		print.setText("Print...");
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		print.setLayoutData(data);
		print.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String text = f_descriptionText.getText();
				SLPrintingUtility.printText(text);
			}
		});

		setTitle(I18N.msg(f_data.propPfx() + "preview.msg.title"));
		setMessage(I18N.msg(f_data.propPfx() + "preview.msg"),
				IMessageProvider.INFORMATION);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			f_data.generateMessage(false);
			f_descriptionText.setText(f_data.getMessage());
		}
		super.setVisible(visible);
	}
}
