package com.surelogic.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TextEditedListener implements Listener {

	public interface TextEditedAction {
		void textEditedAction(String newText);
	}

	private final TextEditedAction f_action;
	private boolean f_editInProgress = false;

	public TextEditedListener(TextEditedAction action) {
		f_action = action;
	}

	@Override
  public void handleEvent(Event event) {
		if (event.type == SWT.Modify) {
			f_editInProgress = true;
		} else if (event.type == SWT.FocusOut && f_editInProgress) {
			f_editInProgress = false;
			if (event.widget instanceof Text) {
				final String newText = ((Text) event.widget).getText();
				if (f_action != null)
					f_action.textEditedAction(newText);
			}
		}
	}
}
