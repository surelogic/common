package com.surelogic.common.ui.serviceability;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.serviceability.Message;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.printing.SLPrintingUtility;

public class SendServiceMessagePreviewPage extends WizardPage {

  SendServiceMessagePreviewPage(Message data) {
    super("preview");
    f_data = data;
  }

  final Message f_data;
  Text f_descriptionText;

  @Override
  public void createControl(Composite parent) {
    Composite panel = new Composite(parent, SWT.NONE);
    setControl(panel);

    GridLayout gridLayout = new GridLayout();
    panel.setLayout(gridLayout);

    f_descriptionText = new Text(panel, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    f_descriptionText.setFont(JFaceResources.getTextFont());
    f_descriptionText.addListener(SWT.Modify, new Listener() {
      @Override
      public void handleEvent(Event event) {
        f_data.setMessage(f_descriptionText.getText());
      }
    });
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    f_descriptionText.setLayoutData(data);

    final Link link = new Link(panel, SWT.WRAP);
    link.setText(I18N.msg("common.serviceability.printOrSave"));
    data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    link.setLayoutData(data);
    link.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        if ("print".equals(event.text)) {
          final String text = f_descriptionText.getText();
          SLPrintingUtility.printText(f_data.getMessageTypeString(), text, true);
        } else if ("save".equals(event.text)) {
          final String text = f_descriptionText.getText();

          FileDialog fileDialog = new FileDialog(EclipseUIUtility.getShell(), SWT.SAVE);
          fileDialog.setFilterExtensions(new String[] { "*.txt" });
          final String pathName = fileDialog.open();
          if (pathName != null) {
            File textFile = new File(pathName);
            FileUtility.putStringIntoAFile(textFile, text);
          }
        }
      }
    });

    setTitle(I18N.msg(f_data.propPfx() + "preview.msg.title"));
    setMessage(I18N.msg(f_data.propPfx() + "preview.msg"), IMessageProvider.INFORMATION);
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
