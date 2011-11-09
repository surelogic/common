package com.surelogic.common.export;

import java.io.File;

abstract class FileTableExporter extends TableExporter {

	private final File f_exportFile;

	public final File getExportFile() {
		return f_exportFile;
	}

	protected FileTableExporter(ExportTableDataSource from, File to) {
		super(from);
		assert to != null;
		f_exportFile = to;
	}
}
