package com.surelogic.common.adhoc;

import com.surelogic.NonNull;
import com.surelogic.common.CommonImages;
import com.surelogic.common.SLUtility;
import com.surelogic.common.jdbc.DBConnection;

public final class AdHocQueryResultSqlUpdateCount extends AdHocQueryResult {

  private final int f_updateCount;

  public int getUpdateCount() {
    return f_updateCount;
  }

  public AdHocQueryResultSqlUpdateCount(final AdHocManager manager, final AdHocQueryResultSqlData parent,
      final AdHocQueryFullyBound query, final int updateCount, final DBConnection datasource) {
    super(manager, parent, query, datasource);
    f_updateCount = updateCount;
  }

  @Override
  public String getImageSymbolicName() {
    return CommonImages.IMG_DRUM_U;
  }

  @Override
  @NonNull
  public String getRowCountInformationAsHumanReadableString() {
    final int rows = getUpdateCount();
    if (rows == 0) {
      return "no rows updated";
    } else if (rows == 1) {
      return "one row updated";
    } else {
      return SLUtility.toStringHumanWithCommas(rows) + " rows updated";
    }
  }
}
