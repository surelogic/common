package com.surelogic.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public final class FontUtility {
	private static final FontData[] fontData = Display.getDefault()
			.getSystemFont().getFontData();

	private FontUtility() {
		// No instance
	}

	public static Font getDefaultBoldFont() {

		if (fontData[0] != null) {
			fontData[0].setStyle(SWT.BOLD);
		}
		return new Font(Display.getCurrent(), fontData[0]);
	}

	public static Font getDefaultItalicFont() {

		if (fontData[0] != null) {
			fontData[0].setStyle(SWT.ITALIC);
		}
		return new Font(Display.getCurrent(), fontData[0]);
	}

	public static Font getDefaultBoldItalicFont() {

		if (fontData[0] != null) {
			fontData[0].setStyle(SWT.ITALIC | SWT.BOLD);
		}
		return new Font(Display.getCurrent(), fontData[0]);
	}
}
