package com.surelogic.common.export;

import java.io.File;
import java.io.PrintWriter;

final class CSVTableExporter extends TextFileTableExporer {

	CSVTableExporter(ExportTableDataSource from, File to) {
		super(from, to);
	}

	@Override
	protected void export(PrintWriter to) throws Exception {
		for (String[] row : getSource()) {
			StringBuilder b = new StringBuilder();
			boolean first = true;
			for (String item : row) {
				if (first) {
					first = false;
				} else {
					b.append(',');
				}
				/*
				 * We just quote everything to be safe. Also in CSV you put ""
				 * to represent a double quote within a quoted item.
				 */
				b.append('"').append(doubleQuote(item)).append('"');
			}
			to.println(b.toString());
		}
	}

	private static String doubleQuote(final String s) {
		return s.replaceAll("\"", "\"\"");
	}
}