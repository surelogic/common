package com.surelogic.common.adhoc;

import java.sql.SQLException;

import com.surelogic.NonNull;
import com.surelogic.common.CommonImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;

public final class AdHocQueryResultSqlException extends AdHocQueryResult {

	private final SQLException f_exception;

	public SQLException getSqlException() {
		return f_exception;
	}

	public AdHocQueryResultSqlException(final AdHocManager manager,
			final AdHocQueryResultSqlData parent,
			final AdHocQueryFullyBound query, final SQLException exception,
			final DBConnection datasource) {
		super(manager, parent, query, datasource);
		if (exception == null) {
			throw new IllegalArgumentException(I18N.err(44, "exception"));
		}
		f_exception = exception;
	}

	@Override
	public String getImageSymbolicName() {
		return CommonImages.IMG_ERROR;
	}

  @Override
  @NonNull
  public String getRowCountInformationAsHumanReadableString() {
    return "query failed";
  }
}
