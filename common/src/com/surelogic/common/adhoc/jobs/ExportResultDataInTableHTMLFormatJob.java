package com.surelogic.common.adhoc.jobs;

import java.io.File;
import java.io.PrintWriter;

import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.model.Cell;
import com.surelogic.common.xml.Entities;

public class ExportResultDataInTableHTMLFormatJob extends
		ExportResultDataInHTMLFormatJob {

	public ExportResultDataInTableHTMLFormatJob(
			final AdHocQueryResultSqlData data, final File file) {
		super(data, file);
	}

	@Override
	protected void writeHtmlFooter(final PrintWriter writer) {
		writer.println("</tbody></table></body></html>");
	}

	@Override
	protected void writeHeader(final PrintWriter writer, final String[] headers) {
		writer.println("<html><head>");
		addStyle(writer);
		writer.println("</head><body><table><thead>");
		final StringBuilder b = new StringBuilder();
		for (final String h : headers) {
			b.append("<th>");
			Entities.addEscaped(h, b);
			b.append("</th>");
		}
		writer.println(b);
		writer.println("</thead><tbody>");
	}

	@Override
	protected void writeRow(final PrintWriter writer, final Cell[] row) {
		final StringBuilder b = new StringBuilder();
		b.append("<tr>");
		for (final Cell c : row) {
			b.append("<td>");
			addCellText(c, b);
			b.append("</td>");
		}
		b.append("</tr>");
		writer.println(b);
	}

}
