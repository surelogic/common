package com.surelogic.common.adhoc.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.model.AdornedTreeTableModel;
import com.surelogic.common.adhoc.model.Cell;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

public abstract class ExportResultDataJob extends AbstractSLJob {

	private final AdHocQueryResultSqlData f_data;
	private final File f_file;

	protected ExportResultDataJob(final AdHocQueryResultSqlData data,
			final File file) {
		super(String.format("Exporting data from query '%s'", data
				.getQueryFullyBound().getQuery().getDescription()));
		if (file == null) {
			throw new IllegalArgumentException("The file may not be null.");
		}
		f_data = data;
		f_file = file;
	}

	public SLStatus run(final SLProgressMonitor monitor) {
		monitor.begin();
		PrintWriter writer;
		try {
			writer = new PrintWriter(f_file);
			final AdornedTreeTableModel model = f_data.getModel();
			writeHeader(writer, model.getColumnLabels());
			for (final Cell[] cells : model.getRows()) {
				writeRow(writer, cells);
			}
			writeFooter(writer);
		} catch (final FileNotFoundException e) {
			return SLStatus.createErrorStatus(e);
		}
		writer.close();
		monitor.done();
		return SLStatus.OK_STATUS;
	}

	protected abstract void writeRow(final PrintWriter writer, final Cell[] row);

	protected abstract void writeHeader(PrintWriter writer, String[] headers);

	protected abstract void writeFooter(PrintWriter writer);

}
