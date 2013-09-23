package com.surelogic.common.adhoc;

import com.surelogic.NonNull;
import com.surelogic.common.CommonImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;

public final class AdHocQueryResultEmpty extends AdHocQueryResult {

  public AdHocQueryResultEmpty(final AdHocManager manager, final AdHocQueryResultSqlData parent, final AdHocQueryFullyBound query,
      final DBConnection datasource) {
    super(manager, parent, query, datasource);
  }

  @Override
  public String getImageSymbolicName() {
    return CommonImages.IMG_DRUM_GRAY;
  }

  /**
   * Returns the empty message for this query. This is either a default message
   * or the custom one specified in the <tt>no-rows</tt> meta comment for this
   * query. This message is intended to be used when the query returns no rows
   * from the database.
   * 
   * @return the empty message for this query.
   */
  @NonNull
  public String getEmptyMessage() {
    final AdHocQueryMeta meta = getQueryFullyBound().getQuery().getMetaWithName(AdHocQuery.META_NO_ROWS_NAME);
    if (meta != null) {
      final String msg = meta.getText().trim();
      if (!"".equals(msg))
        return msg;
    }
    return I18N.msg("adhoc.query.results.empty.msg");
  }

  @Override
  @NonNull
  public String getRowCountInformationAsHumanReadableString() {
    return "no rows";
  }
}
