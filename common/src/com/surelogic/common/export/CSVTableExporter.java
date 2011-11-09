package com.surelogic.common.export;

import java.io.File;
import java.io.PrintWriter;

final class CSVTableExporter extends TextFileTableExporer {

	CSVTableExporter(ExportTableDataSource from, File to) {
		super(from, to);
	}

	@Override
	protected void export(PrintWriter to) throws Exception {
    final StringBuilder b = new StringBuilder();
		for (String[] row : getSource()) {
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
			b.setLength(0);
		}
	}

	private static String doubleQuote(final String s) {
		return s.replaceAll("\"", "\"\"");
	}
}