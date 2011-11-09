package com.surelogic.common.export;

import java.io.File;

public final class ExportFactory {

	public static ITableExporter asCSV(ExportTableDataSource from, File to) {
		return new CSVTableExporter(from, to);
	}

	public static ITableExporter asHTML(ExportTableDataSource from, File to) {
		return new HTMLTableExporter(from, to);
	}

	private ExportFactory() {
		// no instances
	}
}
