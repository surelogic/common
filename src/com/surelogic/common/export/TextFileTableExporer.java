package com.surelogic.common.export;

import java.io.File;
import java.io.PrintWriter;

abstract class TextFileTableExporer extends FileTableExporter {

	protected TextFileTableExporer(ExportTableDataSource from, File to) {
		super(from, to);
	}

	public final void export() throws Exception {
		final PrintWriter to = new PrintWriter(getExportFile());
		try {
			export(to);
		} finally {
			to.close();
		}
	}

	abstract protected void export(PrintWriter to) throws Exception;
}
