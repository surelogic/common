package com.surelogic.common.adhoc;

import com.surelogic.common.CommonImages;
import com.surelogic.common.jdbc.DBConnection;

public final class AdHocQueryResultSqlUpdateCount extends AdHocQueryResult {

	private final int f_updateCount;

	public int getUpdateCount() {
		return f_updateCount;
	}

	public AdHocQueryResultSqlUpdateCount(final AdHocManager manager,
			final AdHocQueryResultSqlData parent,
			final AdHocQueryFullyBound query, final int updateCount,
			final DBConnection datasource) {
		super(manager, parent, query, datasource);
		f_updateCount = updateCount;
	}

	@Override
	public String getImageSymbolicName() {
		return CommonImages.IMG_DRUM_U;
	}
}
