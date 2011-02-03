package com.surelogic.common.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A helper class to redirect the global Eclipse cut/copy/paste to SWT Text
 * controls if they have the focus.
 * <p>
 * This was created to meet Ready for Rational UI - Views 3.5.18, global actions
 * from window menu or toolbar.
 */
public abstract class FocusAction extends Action {

	private final Control f_target;

	protected FocusAction(final Control target) {
		f_target = target;
	}

	protected abstract void takeActionOnText(final Text c);

	protected void takeActionOnFocus(Control c) {
		if (c instanceof Composite) {
			Composite comp = (Composite) c;
			for (Control child : comp.getChildren()) {
				takeActionOnFocus(child);
			}
		} else if (c instanceof Text) {
			Text t = (Text) c;
			if (t.isFocusControl())
				takeActionOnText(t);
		}
	}

	@Override
	public void run() {
		takeActionOnFocus(f_target);
	}
}
