package com.surelogic.common.export;

/**
 * Abstract base class for all export formats.
 */
abstract class TableExporter implements ITableExporter {

	private final ExportTableDataSource f_source;

	public final ExportTableDataSource getSource() {
		return f_source;
	}

	protected TableExporter(ExportTableDataSource from) {
		assert from != null;
		f_source = from;
	}
}
