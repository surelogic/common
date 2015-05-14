package com.surelogic.common.adhoc.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.model.Cell;

public class ExportResultDataInCSVFormatJob extends ExportResultDataJob {

	final StringBuilder b = new StringBuilder();

	public ExportResultDataInCSVFormatJob(final AdHocQueryResultSqlData data,
			final File file) {
		super(data, file);
	}

	@Override
	protected void writeHeader(final PrintWriter writer, final String[] headers) {
		writer.println(delimit(Arrays.asList(headers)));
	}

	private CharSequence delimit(final List<String> asList) {
		b.setLength(0);
		if (!asList.isEmpty()) {
			for (final String str : asList) {
				b.append('"');
				b.append(str.replace("\"", "\"\""));
				b.append('"');
				b.append(',');
			}
			b.setLength(b.length() - 1);
		}
		return b;
	}

	@Override
	protected void writeRow(final PrintWriter writer, final Cell[] row) {
		final List<String> list = new ArrayList<>(row.length);
		for (final Cell c : row) {
			list.add(c.getText());
		}
		writer.println(delimit(list));
	}

	@Override
	protected void writeFooter(final PrintWriter writer) {
		// Do nothing
	}

}
