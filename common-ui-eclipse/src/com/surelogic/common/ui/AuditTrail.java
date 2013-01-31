package com.surelogic.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class AuditTrail extends ScrolledComposite {

	private final Composite f_contents;

	private final Color f_stripe = new Color(PlatformUI.getWorkbench()
			.getDisplay(), 238, 216, 198);

	public AuditTrail(Composite parent) {
		super(parent, SWT.V_SCROLL);
		addListener(SWT.Resize, new Listener() {
			@Override
      public void handleEvent(Event e) {
				reflow();
			}
		});

		f_contents = new Composite(this, SWT.NONE);
		setContent(f_contents);
		setExpandVertical(true);
		setExpandHorizontal(true);
		setAlwaysShowScrollBars(true);

	}

	public void removeAll() {
		Control[] controls = f_contents.getChildren();

		for (Control c : controls) {
			c.dispose();
		}
	}

	public void addEntry(final String title, final String text) {
		final Group entry = new Group(f_contents, SWT.NONE);
		entry.setText(title);
		final FillLayout layout = new FillLayout();
		entry.setLayout(layout);
		final Text entryText = new Text(entry, SWT.MULTI | SWT.WRAP);
		entryText.setText(text);
		entryText.setEditable(false);
		Color c = f_contents.getBackground();
		entryText.setBackground(c);
		entryText.setBackground(f_stripe);
		reflow();
	}

	public void reflow() {
		final Rectangle viewportSize = getClientArea();

		final int entryWidth = viewportSize.width - 10;

		int yPos = 0;
		for (Control entry : f_contents.getChildren()) {
			final Point entrySize = entry.computeSize(entryWidth, SWT.DEFAULT);
			entry.setBounds(0, yPos, entrySize.x, entrySize.y);
			yPos += entrySize.y;
		}
		f_contents.setSize(viewportSize.width, yPos);

		setMinHeight(yPos);
		updatePageIncrement();
	}

	private void updatePageIncrement() {
		ScrollBar vbar = getVerticalBar();
		if (vbar != null) {
			Rectangle clientArea = getClientArea();
			int increment = clientArea.height - 5;
			vbar.setPageIncrement(increment);
		}
	}

	@Override
	public void dispose() {
		try {
			f_stripe.dispose();
		} finally {
			super.dispose();
		}
	}
}
