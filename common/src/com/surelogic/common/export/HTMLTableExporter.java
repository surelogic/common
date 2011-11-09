package com.surelogic.common.export;

import java.io.File;
import java.io.PrintWriter;

final class HTMLTableExporter extends TextFileTableExporer {

	protected HTMLTableExporter(ExportTableDataSource from, File to) {
		super(from, to);
	}

	@Override
	protected void export(PrintWriter to) throws Exception {
		to.println("<table>");
		boolean header = true;
		for (String[] row : getSource()) {
			final String col;
			if (header) {
				header = false;
				col = "th";
			} else {
				col = "td";
			}
			to.print("<tr>");
			for (String item : row) {
				to.print("<");
				to.print(col);
				to.print(">");
				textOrNBSP(item);
				to.print("</");
				to.print(col);
				to.print(">");
			}
			to.print("</tr>");
		}
		to.println("</table>");
	}

	private static String textOrNBSP(String text) {
		if (text != null) {
			text = text.trim();
			if (!"".equals(text))
				return text;
		}
		return "&nbsp;";
	}
}
