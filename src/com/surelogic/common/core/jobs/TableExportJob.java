package com.surelogic.common.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.export.ITableExporter;
import com.surelogic.common.i18n.I18N;

public final class TableExportJob extends DatabaseJob {

	private final ITableExporter f_exporter;

	public TableExportJob(ITableExporter exporter, String accessKey) {
		super("Export a table to a file", accessKey);
		assert exporter != null;
		f_exporter = exporter;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Exporting data", IProgressMonitor.UNKNOWN);
		try {
			f_exporter.export();
		} catch (Exception e) {
			final int errNo = 45;
			return SLEclipseStatusUtility.createErrorStatus(errNo, I18N
					.err(errNo), e);
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}
