package com.surelogic.common.adhoc;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.common.CommonImages;
import com.surelogic.common.SLUtility;
import com.surelogic.common.adhoc.model.AdornedTreeTableModel;
import com.surelogic.common.adhoc.model.NonLeafTreeCell;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.ResultSetUtility;

public final class AdHocQueryResultSqlData extends AdHocQueryResult {

  private final AdornedTreeTableModel f_model;

  public AdornedTreeTableModel getModel() {
    return f_model;
  }

  /*
   * Only one of the below two can be selected at a time so the other should be
   * -1 or null.
   * 
   * This is a bit of a hack, but partial row selection is so different from
   * full row selection that it is a must for now.
   */
  private int f_selectedRowIndex = -1;
  private NonLeafTreeCell f_selectedCell = null;
  private final boolean f_rowLimited;
  private final String[] f_accessKeys;

  public boolean isRowLimited() {
    return f_rowLimited;
  }

  /**
   * Clears any selected row or partial row in this result.
   */
  public void clearSelection() {
    if (f_selectedRowIndex != -1 || f_selectedCell != null) {
      f_selectedRowIndex = -1;
      f_selectedCell = null;
      getManager().notifyResultVariableValueChange(this);
    }
  }

  /**
   * Gets the row index selected in this result or -1 if nothing is selected or
   * if a partial row is selected.
   * 
   * @return the zero-based row index selected in this result or -1 if no row is
   *         selected or if a partial row is selected.
   * @see #getSelectedCell()
   */
  public int getSelectedRowIndex() {
    return f_selectedRowIndex;
  }

  /**
   * Sets the selected result row to the passed row index.
   * 
   * @param rowIndex
   *          a zero-based row index within the result model.
   */
  public void setSelectedRowIndex(final int rowIndex) {
    if (rowIndex < -1 || rowIndex >= f_model.getRowCount()) {
      throw new IllegalArgumentException(I18N.err(127, rowIndex, f_model.getRowCount()));
    }

    f_selectedRowIndex = rowIndex;
    f_selectedCell = null;
    getManager().notifyResultVariableValueChange(this);
  }

  /**
   * Gets the selected partial result row or {@code null} if nothing is selected
   * or if a whole row is selected.
   * 
   * @return the selected partial result row or {@code null} if nothing is
   *         selected or if a whole row is selected.
   * @see #getSelectedRowIndex()
   */
  public NonLeafTreeCell getSelectedCell() {
    return f_selectedCell;
  }

  /**
   * Sets the selected partial result row.
   * 
   * @param cell
   *          a non-leaf tree cell within the result model.
   * @throws IllegalStateException
   *           if this method is called on a pure table.
   * @see AdornedTreeTableModel#isPureTable()
   */
  public void setSelectedCell(final NonLeafTreeCell cell) {
    if (getModel().isPureTable()) {
      throw new IllegalStateException(I18N.err(133));
    }
    if (cell != f_selectedCell) {
      f_selectedCell = cell;
      if (cell != null) {
        f_selectedRowIndex = -1;
      }
      getManager().notifyResultVariableValueChange(this);
    }
  }

  /**
   * Gets the variable values that were defined when the query was run that
   * created result as well as any variable values defined by selection of a
   * row.
   * 
   * @return the variable values that were defined when the query was run that
   *         created result as well as any variable values defined by selection
   *         of a row.
   */
  public Map<String, String> getVariableValues() {
    /*
     * Get a copy of the variable values used by the query.
     */
    final Map<String, String> result = getQueryFullyBound().getVariableValues();
    // remove meta variables from the previous query.
    SLUtility.removeAdHocQueryMetaVariablesFrom(result);
    // add in the selection
    result.putAll(getTopVariableValues());
    return result;
  }

  /**
   * Gets variable values defined by selection of a row or an empty map if no
   * row is selected.
   * <p>
   * This method filters out all variables in the selected row that are
   * {@code null}, the empty string, or whitespace.
   * 
   * @return variable values defined by selection of a row. May be empty.
   */
  public Map<String, String> getTopVariableValues() {
    final Map<String, String> result = new HashMap<String, String>();
    if (f_selectedRowIndex != -1) {
      /*
       * Add in all the variable values from the selected row index.
       */
      result.putAll(f_model.getVariablesFor(f_selectedRowIndex));
    } else if (f_selectedCell != null) {
      /*
       * Add in all the variable values from the selected partial result row.
       */
      result.putAll(f_model.getVariablesFor(f_selectedCell));
    }
    return result;
  }

  public AdHocQueryResultSqlData(final AdHocManager manager, final AdHocQueryResultSqlData parent,
      final AdHocQueryFullyBound query, final ResultSetUtility.Result results, final DBConnection datasource) throws Exception {
    super(manager, parent, query, datasource);

    final AdornedTreeTableModel model = AdornedTreeTableModel.getInstance(results.columnLabels, results.rows);
    if (model == null) {
      throw new IllegalArgumentException(I18N.err(44, "model"));
    }
    if (parent == null) {
      throw new IllegalArgumentException(I18N.err(44, "parent"));
    }
    f_model = model;
    f_rowLimited = results.limited;
    f_accessKeys = parent.getAccessKeys();
  }

  public AdHocQueryResultSqlData(final AdHocManager manager, final AdHocQueryFullyBound query,
      final ResultSetUtility.Result results, final DBConnection datasource, final String... accessKeys) throws Exception {
    super(manager, null, query, datasource);

    final AdornedTreeTableModel model = AdornedTreeTableModel.getInstance(results.columnLabels, results.rows);
    if (model == null) {
      throw new IllegalArgumentException(I18N.err(44, "model"));
    }
    f_model = model;
    f_rowLimited = results.limited;
    f_accessKeys = accessKeys;
  }

  @Override
  public String getImageSymbolicName() {
    return CommonImages.IMG_DRUM;
  }

  public String[] getAccessKeys() {
    return f_accessKeys;
  }

  @Override
  public String toString() {
    String result = super.toString();
    int rows = getModel().getRowCount();
    final String rowString;
    if (rows < 1) {
      rowString = " - no rows";
    } else if (rows == 1) {
      rowString = " - one row";
    } else {
      rowString = " - " + rows + " rows";
    }
    return result + rowString;
  }
}
