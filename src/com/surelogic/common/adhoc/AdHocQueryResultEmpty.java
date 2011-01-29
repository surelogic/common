package com.surelogic.common.adhoc;

import com.surelogic.common.CommonImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;

public final class AdHocQueryResultEmpty extends AdHocQueryResult {

	public AdHocQueryResultEmpty(final AdHocManager manager,
			final AdHocQueryResultSqlData parent,
			final AdHocQueryFullyBound query, final DBConnection datasource) {
		super(manager, parent, query, datasource);
	}

	@Override
	public String getImageSymbolicName() {
		return CommonImages.IMG_DRUM_GRAY;
	}

	/**
	 * Returns the empty message for this query. This is either a default
	 * message or one specified in the <tt>NO-ROWS-MSG</tt> structured comment
	 * for this query.
	 * <p>
	 * <i>Implementation Note:</i> This code uses <tt>"\n"</tt> to search for
	 * the end of line. This may in some cases be brittle.
	 * 
	 * @return the non-null empty message for this query.
	 */
	public String getEmptyMessage() {
		final String sql = getQueryFullyBound().getSql();
		final String annotation = "NO-ROWS-MSG=";

		int index = sql.indexOf(annotation);
		if (index == -1) {
			return I18N.msg("adhoc.query.results.empty.msg");
		} else {
			int eolIndex = sql.indexOf("\n", index);
			if (eolIndex == -1)
				eolIndex = sql.length();
			final String result = sql
					.substring(index + annotation.length(), eolIndex);
			return result;
		}
	}
}
