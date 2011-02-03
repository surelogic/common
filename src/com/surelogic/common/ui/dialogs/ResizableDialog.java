package com.surelogic.common.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract dialog that can be resized and maximized and persists that size
 * and location across sessions. Suitable for subclassing.
 * <p>
 * This code is from pages 414-416 of <i>Eclipse: Building Commercial-Quality
 * Plug-Ins</i> (2nd edition) by Eric Clayberg and Dan Rubel.
 */
public abstract class ResizableDialog extends Dialog {
	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_WIDTH = "width";
	private static final String TAG_HEIGHT = "height";

	/**
	 * Used to track the current dialog bounds so that it can be saved when the
	 * dialog has been closed.
	 */
	protected Rectangle cachedBounds;

	/**
	 * Creates a dialog instance. Note that the window will have no visual
	 * representation (no widgets) until it is told to open. By default,
	 * <code>open</code> blocks for dialogs.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level
	 *            shell
	 */
	public ResizableDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Creates a dialog instance. Note that the window will have no visual
	 * representation (no widgets) until it is told to open. By default,
	 * <code>open</code> blocks for dialogs.
	 * 
	 * @param parentShell
	 *            object that returns the current parent shell (not
	 *            <code>null</code>)
	 */
	public ResizableDialog(IShellProvider parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Persistance of size and location
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Answer the dialog settings used to store the last location and size of
	 * the dialog.
	 */
	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Load and answer the previous location and size of the dialog.
	 * 
	 * @return the bounds or <code>null</code> if not found
	 */
	private Rectangle loadBounds() {
		IDialogSettings settings = getDialogSettings();
		try {
			return new Rectangle(settings.getInt(TAG_X),
					settings.getInt(TAG_Y), settings.getInt(TAG_WIDTH),
					settings.getInt(TAG_HEIGHT));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Save the current location and size of the dialog.
	 * 
	 * @param bounds
	 *            the bounds (not <code>null</code>)
	 */
	private void saveBounds(Rectangle bounds) {
		IDialogSettings settings = getDialogSettings();
		settings.put(TAG_X, bounds.x);
		settings.put(TAG_Y, bounds.y);
		settings.put(TAG_WIDTH, bounds.width);
		settings.put(TAG_HEIGHT, bounds.height);
	}

	/**
	 * Returns the initial size to use for the shell by first trying to load a
	 * previously saved size, and failing that falling through to the default
	 * implementation.
	 * 
	 * @return the initial size of the shell
	 */
	protected Point getInitialSize() {

		// Track the current dialog bounds.
		getShell().addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent arg0) {
				cachedBounds = getShell().getBounds();
			}

			public void controlResized(ControlEvent arg0) {
				cachedBounds = getShell().getBounds();
			}
		});

		// Answer the size from the previous incarnation.
		Rectangle b1 = getShell().getDisplay().getBounds();
		Rectangle b2 = loadBounds();
		if (b2 != null)
			return new Point(b1.width < b2.width ? b1.width : b2.width,
					b1.height < b2.height ? b1.height : b2.height);

		return super.getInitialSize();
	}

	/**
	 * Returns the initial location to use for the shell by first trying to load
	 * a previously saved size, and failing that falling through to the default
	 * implementation.
	 * 
	 * @param initialSize
	 *            the initial size of the shell, as returned by
	 *            <code>getInitialSize</code>.
	 * @return the initial location of the shell
	 */
	protected Point getInitialLocation(Point initialSize) {

		// Answer the location from the previous incarnation.
		Rectangle displayBounds = getShell().getDisplay().getBounds();
		Rectangle bounds = loadBounds();
		if (bounds != null) {
			int x = bounds.x;
			int y = bounds.y;
			int maxX = displayBounds.x + displayBounds.width - initialSize.x;
			int maxY = displayBounds.y + displayBounds.height - initialSize.y;
			if (x > maxX)
				x = maxX;
			if (y > maxY)
				y = maxY;
			if (x < displayBounds.x)
				x = displayBounds.x;
			if (y < displayBounds.y)
				y = displayBounds.y;
			return new Point(x, y);
		}

		return super.getInitialLocation(initialSize);
	}

	/**
	 * Closes this window, disposes its shell, removes this window from its
	 * window manager (if it has one), and saves the previous location and size
	 * for the next time this type of dialog is opened.
	 * 
	 * @return <code>true</code> if the window is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 */
	public boolean close() {
		boolean closed = super.close();
		if (closed && cachedBounds != null)
			saveBounds(cachedBounds);
		return closed;
	}
}
