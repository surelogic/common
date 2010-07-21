package com.surelogic.common.html;

import java.util.HashMap;
import java.util.Map;

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

	/**
	 * Extracts parameters of the from <tt>?param1=value&param2=value2</tt> from
	 * a String. This is a simple parse that does not handle encodings. The
	 * result is a map with the parameter as the key to lookup a value. If
	 * duplicate parameters exist the last value is the resulting value in the
	 * returned map.
	 * 
	 * @param value
	 *            the string to extract parameters from
	 * @return a (possibly empty) map of the parameters.
	 */
	public static Map<String, String> extractParametersFromURL(
			final String value) {
		final Map<String, String> result = new HashMap<String, String>();
		int start = 0;
		int qIdx = value.indexOf('?', start);
		if (qIdx == -1)
			qIdx = value.indexOf('&', start);
		while (true) {
			int eqIdx = value.indexOf('=', start);
			if (qIdx == -1 || eqIdx == -1)
				break;
			qIdx++;
			if (qIdx == eqIdx)
				break;
			/*
			 * Extract key
			 */
			final String key = value.substring(qIdx, eqIdx);
			/*
			 * Extract value
			 */
			eqIdx++;
			if (eqIdx == value.length()) {
				result.put(key, "");
				break;
			} else {
				qIdx = value.indexOf('&', eqIdx);
				if (qIdx == -1) {
					result.put(key, value.substring(eqIdx));
					break;
				} else {
					result.put(key, value.substring(eqIdx, qIdx));
					start = qIdx;
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		final Map<String, String> m = extractParametersFromURL("jur?gg=78&f=2&f=3&g=999&h=");
		System.out.println(m);
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
