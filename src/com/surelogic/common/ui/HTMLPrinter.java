package com.surelogic.common.ui;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.jobs.SLUIJob;

/**
 * Provides a set of convenience methods for creating HTML pages.
 * <p>
 * Moved into this package from
 * <code>org.eclipse.jface.internal.text.revisions</code>.
 * </p>
 */
public final class HTMLPrinter {

	private static RGB BG_COLOR_RGB = null;

	static {
		final Display display = Display.getDefault();
		if (display != null && !display.isDisposed()) {
			final UIJob job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					BG_COLOR_RGB = display.getSystemColor(
							SWT.COLOR_INFO_BACKGROUND).getRGB();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	private HTMLPrinter() {
		// Nothing to do
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

	public static String read(final Reader rd) {

		final StringBuffer buffer = new StringBuffer();
		final char[] readBuffer = new char[2048];

		try {
			int n = rd.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n = rd.read(readBuffer);
			}
			return buffer.toString();
		} catch (final IOException x) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Error reading in HTML printer", x);
		}

		return null;
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position, final RGB bgRGB, final URL styleSheetURL) {

		if (bgRGB == null) {
			insertPageProlog(buffer, position, styleSheetURL);
		} else {
			final StringBuffer pageProlog = new StringBuffer(300);

			pageProlog.append("<html>"); //$NON-NLS-1$

			appendStyleSheetURL(pageProlog, styleSheetURL);

			pageProlog.append("<body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$

			buffer.insert(position, pageProlog.toString());
		}
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position, final RGB bgRGB, final String styleSheet) {

		if (bgRGB == null) {
			insertPageProlog(buffer, position, styleSheet);
		} else {
			final StringBuffer pageProlog = new StringBuffer(300);

			pageProlog.append("<html>"); //$NON-NLS-1$

			appendStyleSheetURL(pageProlog, styleSheet);

			pageProlog.append("<body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$

			buffer.insert(position, pageProlog.toString());
		}
	}

	public static void insertStyles(final StringBuffer buffer,
			final String[] styles) {
		if (styles == null || styles.length == 0) {
			return;
		}

		final StringBuffer styleBuf = new StringBuffer(10 * styles.length);
		for (int i = 0; i < styles.length; i++) {
			styleBuf.append(" style=\""); //$NON-NLS-1$
			styleBuf.append(styles[i]);
			styleBuf.append('"');
		}

		// Find insertion index
		// a) within existing body tag with trailing space
		int index = buffer.indexOf("<body "); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index + 5, styleBuf);
			return;
		}

		// b) within existing body tag without attributes
		index = buffer.indexOf("<body>"); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index + 5, ' ');
			buffer.insert(index + 6, styleBuf);
			return;
		}
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position, final RGB bgRGB) {
		if (bgRGB == null) {
			insertPageProlog(buffer, position);
		} else {
			final StringBuffer pageProlog = new StringBuffer(60);
			pageProlog.append("<html><body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$
			buffer.insert(position, pageProlog.toString());
		}
	}

	private static void appendStyleSheetURL(final StringBuffer buffer,
			final String styleSheet) {
		if (styleSheet == null) {
			return;
		}

		buffer.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$
		buffer.append(styleSheet);
		buffer.append("</style></head>"); //$NON-NLS-1$
	}

	private static void appendStyleSheetURL(final StringBuffer buffer,
			final URL styleSheetURL) {
		if (styleSheetURL == null) {
			return;
		}

		buffer.append("<head>"); //$NON-NLS-1$

		buffer.append("<LINK REL=\"stylesheet\" HREF= \""); //$NON-NLS-1$
		buffer.append(styleSheetURL);
		buffer.append("\" CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$

		buffer.append("</head>"); //$NON-NLS-1$
	}

	private static void appendColor(final StringBuffer buffer, final RGB rgb) {
		buffer.append('#');
		buffer.append(Integer.toHexString(rgb.red));
		buffer.append(Integer.toHexString(rgb.green));
		buffer.append(Integer.toHexString(rgb.blue));
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position) {
		insertPageProlog(buffer, position, getBgColor());
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position, final URL styleSheetURL) {
		insertPageProlog(buffer, position, getBgColor(), styleSheetURL);
	}

	public static void insertPageProlog(final StringBuffer buffer,
			final int position, final String styleSheet) {
		insertPageProlog(buffer, position, getBgColor(), styleSheet);
	}

	public static void addPageProlog(final StringBuffer buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public static void addPageEpilog(final StringBuffer buffer) {
		buffer.append("</body></html>"); //$NON-NLS-1$
	}

	public static void startBulletList(final StringBuffer buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	public static void endBulletList(final StringBuffer buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	public static void addBullet(final StringBuffer buffer, final String bullet) {
		if (bullet != null) {
			buffer.append("<li>"); //$NON-NLS-1$
			buffer.append(bullet);
			buffer.append("</li>"); //$NON-NLS-1$
		}
	}

	public static void addSmallHeader(final StringBuffer buffer,
			final String header) {
		if (header != null) {
			buffer.append("<h5>"); //$NON-NLS-1$
			buffer.append(header);
			buffer.append("</h5>"); //$NON-NLS-1$
		}
	}

	public static void addParagraph(final StringBuffer buffer,
			final String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	public static void addParagraph(final StringBuffer buffer,
			final Reader paragraphReader) {
		if (paragraphReader != null) {
			addParagraph(buffer, read(paragraphReader));
		}
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position, final RGB bgRGB, final URL styleSheetURL) {

		if (bgRGB == null) {
			insertPageProlog(buffer, position, styleSheetURL);
		} else {
			final StringBuilder pageProlog = new StringBuilder(300);

			pageProlog.append("<html>"); //$NON-NLS-1$

			appendStyleSheetURL(pageProlog, styleSheetURL);

			pageProlog.append("<body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$

			buffer.insert(position, pageProlog.toString());
		}
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position, final RGB bgRGB, final String styleSheet) {

		if (bgRGB == null) {
			insertPageProlog(buffer, position, styleSheet);
		} else {
			final StringBuilder pageProlog = new StringBuilder(300);

			pageProlog.append("<html>"); //$NON-NLS-1$

			appendStyleSheetURL(pageProlog, styleSheet);

			pageProlog.append("<body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$

			buffer.insert(position, pageProlog.toString());
		}
	}

	public static void insertStyles(final StringBuilder buffer,
			final String[] styles) {
		if (styles == null || styles.length == 0) {
			return;
		}

		final StringBuilder styleBuf = new StringBuilder(10 * styles.length);
		for (int i = 0; i < styles.length; i++) {
			styleBuf.append(" style=\""); //$NON-NLS-1$
			styleBuf.append(styles[i]);
			styleBuf.append('"');
		}

		// Find insertion index
		// a) within existing body tag with trailing space
		int index = buffer.indexOf("<body "); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index + 5, styleBuf);
			return;
		}

		// b) within existing body tag without attributes
		index = buffer.indexOf("<body>"); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index + 5, ' ');
			buffer.insert(index + 6, styleBuf);
			return;
		}
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position, final RGB bgRGB) {
		if (bgRGB == null) {
			insertPageProlog(buffer, position);
		} else {
			final StringBuilder pageProlog = new StringBuilder(60);
			pageProlog.append("<html><body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\">"); //$NON-NLS-1$
			buffer.insert(position, pageProlog.toString());
		}
	}

	private static void appendStyleSheetURL(final StringBuilder buffer,
			final String styleSheet) {
		if (styleSheet == null) {
			return;
		}

		buffer.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$
		buffer.append(styleSheet);
		buffer.append("</style></head>"); //$NON-NLS-1$
	}

	private static void appendStyleSheetURL(final StringBuilder buffer,
			final URL styleSheetURL) {
		if (styleSheetURL == null) {
			return;
		}

		buffer.append("<head>"); //$NON-NLS-1$

		buffer.append("<LINK REL=\"stylesheet\" HREF= \""); //$NON-NLS-1$
		buffer.append(styleSheetURL);
		buffer.append("\" CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$

		buffer.append("</head>"); //$NON-NLS-1$
	}

	private static void appendColor(final StringBuilder buffer, final RGB rgb) {
		buffer.append('#');
		buffer.append(Integer.toHexString(rgb.red));
		buffer.append(Integer.toHexString(rgb.green));
		buffer.append(Integer.toHexString(rgb.blue));
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position) {
		insertPageProlog(buffer, position, getBgColor());
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position, final URL styleSheetURL) {
		insertPageProlog(buffer, position, getBgColor(), styleSheetURL);
	}

	public static void insertPageProlog(final StringBuilder buffer,
			final int position, final String styleSheet) {
		insertPageProlog(buffer, position, getBgColor(), styleSheet);
	}

	private static RGB getBgColor() {
		if (BG_COLOR_RGB != null) {
			return BG_COLOR_RGB;
		}
		return new RGB(255, 255, 225); // RGB value of info bg color on
		// WindowsXP

	}

	public static void addPageProlog(final StringBuilder buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public static void addPageEpilog(final StringBuilder buffer) {
		buffer.append("</body></html>"); //$NON-NLS-1$
	}

	public static void startBulletList(final StringBuilder buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	public static void endBulletList(final StringBuilder buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	public static void addBullet(final StringBuilder buffer, final String bullet) {
		if (bullet != null) {
			buffer.append("<li>"); //$NON-NLS-1$
			buffer.append(bullet);
			buffer.append("</li>"); //$NON-NLS-1$
		}
	}

	public static void addSmallHeader(final StringBuilder buffer,
			final String header) {
		if (header != null) {
			buffer.append("<h5>"); //$NON-NLS-1$
			buffer.append(header);
			buffer.append("</h5>"); //$NON-NLS-1$
		}
	}

	public static void addParagraph(final StringBuilder buffer,
			final String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	public static void addParagraph(final StringBuilder buffer,
			final Reader paragraphReader) {
		if (paragraphReader != null) {
			addParagraph(buffer, read(paragraphReader));
		}
	}

	/**
	 * Replaces the following style attributes of the font definition of the
	 * <code>html</code> element:
	 * <ul>
	 * <li>font-size</li>
	 * <li>font-weight</li>
	 * <li>font-style</li>
	 * <li>font-family</li>
	 * </ul>
	 * The font's name is used as font family, a <code>sans-serif</code> default
	 * font family is appended for the case that the given font name is not
	 * available.
	 * <p>
	 * If the listed font attributes are not contained in the passed style list,
	 * nothing happens.
	 * </p>
	 * 
	 * @param styles
	 *            CSS style definitions
	 * @param fontData
	 *            the font information to use
	 * @return the modified style definitions
	 * @since 3.3
	 */
	public static String convertTopLevelFont(String styles,
			final FontData fontData) {
		final boolean bold = (fontData.getStyle() & SWT.BOLD) != 0;
		final boolean italic = (fontData.getStyle() & SWT.ITALIC) != 0;

		// See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=155993
		final String size = Integer.toString(fontData.getHeight())
				+ ("carbon".equals(SWT.getPlatform()) ? "px" : "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final String family = "'" + fontData.getName() + "',sans-serif"; //$NON-NLS-1$ //$NON-NLS-2$
		styles = styles
				.replaceFirst(
						"(html\\s*\\{.*(?:\\s|;)font-size:\\s*)\\d+pt(\\;?.*\\})", "$1" + size + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		styles = styles
				.replaceFirst(
						"(html\\s*\\{.*(?:\\s|;)font-weight:\\s*)\\w+(\\;?.*\\})", "$1" + (bold ? "bold" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		styles = styles
				.replaceFirst(
						"(html\\s*\\{.*(?:\\s|;)font-style:\\s*)\\w+(\\;?.*\\})", "$1" + (italic ? "italic" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		styles = styles
				.replaceFirst(
						"(html\\s*\\{.*(?:\\s|;)font-family:\\s*).+?(;.*\\})", "$1" + family + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return styles;
	}
}
