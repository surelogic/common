package com.surelogic.common.html;

/**
 * Provides a set of convenience methods for creating HTML pages.
 * <p>
 * Moved into this package from
 * <code>org.eclipse.jface.internal.text.revisions</code>.
 * </p>
 */
public final class SimpleHTMLPrinter {

	private SimpleHTMLPrinter() {
		// utility
	}

	private static String replace(final String text, final char c,
			final String s) {

		int previous = 0;
		int current = text.indexOf(c, previous);

		if (current == -1) {
			return text;
		}

		final StringBuffer buffer = new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

	public static String convertToHTMLContent(String content) {
		content = replace(content, '&', "&amp;"); //$NON-NLS-1$
		content = replace(content, '"', "&quot;"); //$NON-NLS-1$
		content = replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	public static void addPageProlog(final StringBuilder buffer,
			final String styleSheet) {

		final StringBuilder pageProlog = new StringBuilder();

		pageProlog.append("<html>"); //$NON-NLS-1$

		appendStyleSheet(pageProlog, styleSheet);

		pageProlog.append("<body>"); //$NON-NLS-1$

		buffer.append(pageProlog.toString());
	}

	private static void appendStyleSheet(final StringBuilder buffer,
			final String styleSheet) {
		if (styleSheet == null) {
			return;
		}

		buffer.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$
		buffer.append(styleSheet);
		buffer.append("</style></head>"); //$NON-NLS-1$
	}

	public static void addPageEpilog(final StringBuilder buffer) {
		buffer.append("</body></html>"); //$NON-NLS-1$
	}
}
