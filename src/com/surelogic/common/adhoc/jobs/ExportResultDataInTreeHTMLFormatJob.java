package com.surelogic.common.adhoc.jobs;

import java.io.File;
import java.io.PrintWriter;

import com.surelogic.common.FileUtility;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.model.Cell;
import com.surelogic.common.xml.Entities;

public class ExportResultDataInTreeHTMLFormatJob extends
		ExportResultDataInHTMLFormatJob {

	private final boolean f_hasTable;
	private String[] f_headers;
	private final String[] f_treeState;

	private final static String jsPath = "/com/surelogic/common/js/";
	private final static String jsScript = "outline.js";

	public ExportResultDataInTreeHTMLFormatJob(
			final AdHocQueryResultSqlData data, final File file) {
		super(data, file);
		final int lastTreeIndex = data.getModel().getLastTreeIndex();
		f_treeState = lastTreeIndex == -1 ? new String[0] : new String[data
				.getModel().getLastTreeIndex()];
		f_hasTable = !data.getModel().isPureTree();
		addImage("arrow_down.gif");
		addImage("arrow_right.gif");
	}

	@Override
	protected void writeHeader(final PrintWriter writer, final String[] headers) {
		writer.println("<html><head><script src=\"outline.js\" ></script>");
		addStyle(writer);
		writer.println("</head><body onload=\"outlineInit()\" >");
		if (f_hasTable) {
			// Initialize table headers
			f_headers = new String[headers.length - f_treeState.length];
			for (int i = 0; i < headers.length - f_treeState.length; i++) {
				f_headers[i] = headers[i + f_treeState.length];
			}
		}
		firstRow = true;
	}

	private boolean firstRow;

	@Override
	protected void writeRow(final PrintWriter writer, final Cell[] row) {
		final StringBuilder b = new StringBuilder();
		boolean treeChanged = false;
		if (firstRow) {
			for (int i = 0; i < f_treeState.length; i++) {
				f_treeState[i] = row[i].getText();
				if (i == 0) {
					b.append("<ul class=\"outline\">");
				} else {
					b.append("<ul>");
				}
				b.append("<li>");
				addCellText(row[i], b);
			}
			treeChanged = true;
			firstRow = false;
		} else {
			for (int i = 0; i < f_treeState.length; i++) {
				final String cellText = row[i].getText();
				if (!cellText.equals(f_treeState[i])) {
					treeChanged = true;
					// This is a new entry in this list
					f_treeState[i] = cellText;
					// Close out any existing lists or tables
					if (f_hasTable) {
						b.append("</tbody></table></li></ul>");
					}
					for (int j = i + 1; j < f_treeState.length; j++) {
						b.append("</li>");
						b.append("</ul>");
					}
					b.append("</li><li>");
					addCellText(row[i], b);
					// Now add the rest of the list items and break
					for (int j = i + 1; j < f_treeState.length; j++) {
						final String liText = row[j].getText();
						f_treeState[j] = liText;
						b.append("<ul><li>");
						addCellText(row[j], b);
					}
					break;
				}
			}
		}
		// Add the table portion
		if (f_hasTable) {
			if (treeChanged) {
				writeTableHeader(b);
			}
			b.append("<tr>");
			for (int i = f_treeState.length; i < row.length; i++) {
				b.append("<td>");
				addCellText(row[i], b);
				b.append("</td>");
			}
			b.append("</tr>");
		}
		writer.println(b);
	}

	@Override
	@SuppressWarnings("unused")
	protected void writeHtmlFooter(final PrintWriter writer) {
		if (f_hasTable) {
			writer.print("</tbody></table>");
		}
		for (final String element : f_treeState) {
			writer.print("</ul>");
		}
		writer.println("</body></html>");
		// Copy our outline.js file
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		FileUtility.copy(cl.getResource(jsPath + jsScript), new File(
				f_parentDir, jsScript));
	}

	private StringBuilder writeTableHeader(final StringBuilder b) {
		b.append("<ul><li><table><thead><tr>");
		for (final String h : f_headers) {
			b.append("<th>");
			Entities.addEscaped(h, b);
			b.append("</th>");
		}
		b.append("</tr></thead><tbody>");
		return b;
	}

}
