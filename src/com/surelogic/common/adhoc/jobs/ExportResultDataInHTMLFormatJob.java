package com.surelogic.common.adhoc.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.surelogic.common.ImageWriter;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.model.Cell;
import com.surelogic.common.xml.Entities;

public abstract class ExportResultDataInHTMLFormatJob extends
		ExportResultDataJob {

	protected final File f_parentDir;
	protected final ImageWriter f_writer;

	protected ExportResultDataInHTMLFormatJob(
			final AdHocQueryResultSqlData data, final File file) {
		super(data, file);
		// We keep a folder to store image files relative to this file path
		f_parentDir = file.getParentFile();
		f_writer = new ImageWriter(f_parentDir);
	}

	/**
	 * Optional method. May be called to add a style section to the header.
	 * 
	 * @param writer
	 * @return
	 */
	protected void addStyle(final PrintWriter writer) {
		writer.println("<style>");
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(
								"/com/surelogic/common/adhoc/jobs/style.css")));
		try {
			String str = in.readLine();
			while (str != null) {
				writer.println(str);
				str = in.readLine();
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		writer.println("</style>");
	}

	protected abstract void writeHtmlFooter(final PrintWriter writer);

	@Override
	protected final void writeFooter(final PrintWriter writer) {
		writeHtmlFooter(writer);
		// Copy out all images we used into our folder
		f_writer.writeImages();
	}

	/**
	 * Add an image to the image folder, so that it can be accessed in the HTML
	 * document. These images are copied into the image folder when the footer
	 * is written.
	 * 
	 * @param imageName
	 *            may not be <code>null</code>
	 * @return
	 */
	protected void addImage(final String imageName) {
		f_writer.addImage(imageName);
	}

	/**
	 * Add an image tag the the provided StringBuilder. The image will also be
	 * added to the image folder if need be.
	 * 
	 * @param imageName
	 *            may be <code>null</code>
	 * @param b
	 * @return
	 */
	protected StringBuilder addImageTag(final String imageName,
			final StringBuilder b) {
		if (imageName != null) {
			addImage(imageName);
			b.append("<img src=\"");
			b.append(f_writer.imageLocation(imageName));
			b.append("\" />");
		}
		return b;
	}

	/**
	 * Add the text and optional image of this cell to the StringBuilder. The
	 * image will be added to the image folder if need be and any text will be
	 * properly escaped.
	 * 
	 * @param c
	 * @param b
	 * @return
	 */
	protected StringBuilder addCellText(final Cell c, final StringBuilder b) {
		addImageTag(c.getImageSymbolicName(), b);
		Entities.addEscaped(c.getText(), b);
		return b;
	}

}
