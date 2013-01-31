package com.surelogic.common.ui.tooltip;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Created primarily to be able to customize the widget used by 
 * ColumnViewerToolTipSupport
 * 
 * @author edwin
 */
public class ColumnViewerToolTip extends ColumnViewerToolTipSupport {
	private ColumnViewerToolTip(ColumnViewer viewer, int style,
			                    boolean manualActivation) {
		super(viewer, style, manualActivation);
	}

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 */
	public static void enableFor(ColumnViewer viewer) {
		new ColumnViewerToolTip(viewer,ToolTip.NO_RECREATE,false);
	}
	
	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style style passed to control tool tip behavior
	 * 
	 * @see ToolTip#RECREATE
	 * @see ToolTip#NO_RECREATE
	 */
	public static void enableFor(ColumnViewer viewer, int style) {
		new ColumnViewerToolTip(viewer,style,false);
	}
	
	/**
	 * Copied originally from DefaultToolTip
	 * 
	 * Creates the content are of the the tooltip. By default this creates a
	 * CLabel to display text. To customize the text Subclasses may override the
	 * following methods
	 * <ul>
	 * <li>{@link #getStyle(Event)}</li>
	 * <li>{@link #getBackgroundColor(Event)}</li>
	 * <li>{@link #getForegroundColor(Event)}</li>
	 * <li>{@link #getFont(Event)}</li>
	 * <li>{@link #getImage(Event)}</li>
	 * <li>{@link #getText(Event)}</li>
	 * <li>{@link #getBackgroundImage(Event)}</li>
	 * </ul>
	 * 
	 * @param event
	 *            the event that triggered the activation of the tooltip
	 * @param parent
	 *            the parent of the content area
	 * @return the content area created
	 */
	@Override
  protected Composite createToolTipContentArea(Event event, Composite parent) {
		Image image = getImage(event);
		Image bgImage = getBackgroundImage(event);
		String text = getText(event);
		Color fgColor = getForegroundColor(event);
		Color bgColor = getBackgroundColor(event);
		Font font = getFont(event);

		// TODO Customize this??
		CLabel label = new CLabel(parent, getStyle(event));
		if (text != null) {
			label.setText(text);
		}

		if (image != null) {
			label.setImage(image);
		}

		if (fgColor != null) {
			label.setForeground(fgColor);
		}

		if (bgColor != null) {
			label.setBackground(bgColor);
		}

		if (bgImage != null) {
			label.setBackgroundImage(image);
		}

		if (font != null) {
			label.setFont(font);
		}

		return label;
	}
}
