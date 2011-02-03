package com.surelogic.common.ui.tooltip;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.surelogic.common.ui.HTMLPrinter;

public class ToolTipInformationControl extends AbstractInformationControl {

	private static final int WIDTH = 500;
	private static final int HEIGHT = 200;

	private Browser tipBrowser;
	private boolean completed;
	private boolean browserHasContent;

	private final ListenerList/* <IInputChangedListener> */inputChangeListeners = new ListenerList(
			ListenerList.IDENTITY);

	public ToolTipInformationControl(final Shell parentShell,
			final String statusFieldText) {
		super(parentShell, statusFieldText);
		create();
	}

	public ToolTipInformationControl(final Shell parentShell) {
		super(parentShell, true);
		create();
	}

	public ToolTipInformationControl(final Shell parentShell,
			final ToolBarManager toolBar) {
		super(parentShell, toolBar);
		create();
	}

	@Override
	protected void createContent(final Composite parent) {
		tipBrowser = new Browser(parent, SWT.NONE);
		
		// tipBrowser.setJavascriptEnabled(false);

		final Display display = getShell().getDisplay();
		tipBrowser.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		tipBrowser.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		tipBrowser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(final ProgressEvent event) {
				completed = true;
			}
		});

		tipBrowser.addOpenWindowListener(new OpenWindowListener() {
			public void open(final WindowEvent event) {
				event.required = true; // Cancel opening of new windows
			}
		});

		// Replace browser's built-in context menu with none
		tipBrowser.setMenu(new Menu(getShell(), SWT.NONE));

		// createTextLayout();
		setSize(WIDTH, HEIGHT);
	}

	public void setTip(String tipText) {

		browserHasContent = tipText != null && tipText.length() > 0;

		if (!browserHasContent) {
			tipText = "<html><body ></html>"; //$NON-NLS-1$
		}
		final boolean RTL = (getShell().getStyle() & SWT.RIGHT_TO_LEFT) != 0;
		final boolean resizable = isResizable();

		// The default "overflow:auto" would not result in a predictable width
		// for the client area
		// and the re-wrapping would cause visual noise
		String[] styles = null;
		if (RTL && resizable) {
			styles = new String[] {
					"direction:rtl;", "overflow:scroll;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (RTL && !resizable) {
			styles = new String[] {
					"direction:rtl;", "overflow:hidden;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (!resizable) {
			// XXX: In IE, "word-wrap: break-word;" causes bogus wrapping even
			// in non-broken words :-(see e.g. Javadoc of String).
			// Re-check whether we really still need this now that the Javadoc
			// Hover header already sets this style.
			styles = new String[] { "overflow:hidden;"/*
													 * ,
													 * "word-wrap: break-word;"
													 */}; //$NON-NLS-1$
		} else {
			styles = new String[] { "overflow:scroll;" }; //$NON-NLS-1$
		}

		final StringBuffer buffer = new StringBuffer(tipText);
		HTMLPrinter.insertStyles(buffer, styles);
		tipText = buffer.toString();

		/*
		 * XXX: Should add some JavaScript here that shows something like
		 * "(continued...)" or "..." at the end of the visible area when the
		 * page overflowed with "overflow:hidden;".
		 */

		completed = false;
		tipBrowser.setText(tipText);

		final Object[] listeners = inputChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IInputChangedListener) listeners[i]).inputChanged(tipText);
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		Shell shell = getShell();
		if (shell.isVisible() == visible) {
			return;
		}

		if (!visible) {
			super.setVisible(false);
			setTip(null);
			return;
		}

		/*
		 * The Browser widget flickers when made visible while it is not
		 * completely loaded. The fix is to delay the call to setVisible until
		 * either loading is completed (see ProgressListener in constructor), or
		 * a timeout has been reached.
		 */
		final Display display = shell.getDisplay();

		// Make sure the display wakes from sleep after timeout:
		display.timerExec(100, new Runnable() {
			public void run() {
				completed = true;
			}
		});

		while (!completed) {
			// Drive the event loop to process the events required to load the
			// browser widget's contents:
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell = getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}

		/*
		 * Avoids flickering when replacing hovers, especially on Vista in
		 * ON_CLICK mode. Causes flickering on GTK. Carbon does not care.
		 */
		if ("win32".equals(SWT.getPlatform())) {
			shell.moveAbove(null);
		}

		super.setVisible(true);
	}

	public boolean hasContents() {
		return browserHasContent;
	}

	@Override
	public void setSize(final int width, final int height) {
		tipBrowser.setRedraw(false); // avoid flickering
		try {
			super.setSize(width, height);
		} finally {
			tipBrowser.setRedraw(true);
		}
	}

	/**
	 * Sets the location for a hovering shell
	 * 
	 * @param shell
	 *            the object that is to hover
	 * @param position
	 *            the position of a widget to hover over
	 * @return the top-left location for a hovering box
	 */
	public void setHoverLocation(final Point position) {
		final Shell shell = getShell();
		final Rectangle displayBounds = shell.getDisplay().getBounds();
		final Rectangle shellBounds = shell.getBounds();
		shellBounds.x = Math.max(Math.min(position.x, displayBounds.width
				- shellBounds.width), 0);
		shellBounds.y = Math.max(Math.min(position.y + 16, displayBounds.height
				- shellBounds.height), 0);
		shell.setBounds(shellBounds);
	}

	public boolean mouseIsOver(final Widget widget, final int x, final int y) {
		final Rectangle bounds = getShell().getBounds();
		final Rectangle region = new Rectangle(-10, -10, bounds.width + 20,
				bounds.height + 20);
		final Display d = getShell().getDisplay();
		final Point p = d.map((Control) widget, getShell(), new Point(x, y));
		return region.contains(p);
	}
}
