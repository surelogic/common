package com.surelogic.common.ui;

import java.util.Comparator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.Utility;
import com.surelogic.common.StringComparators;

/**
 * A utility to help with table sorting operations.
 * <p>
 * Try to use {@link ColumnViewerSorter} instead of this class if you can.
 */
@Utility
public final class TableUtility {

  private TableUtility() {
    // no instances
  }

  public static TableItem getFirstSelectedOrNull(Table table) {
    TableItem[] selected = table.getSelection();
    if (selected != null && selected.length > 0)
      return selected[0];
    else
      return null;
  }

  public static TableItem getFirstSelectedOrNull(TableViewer tableViewer) {
    return getFirstSelectedOrNull(tableViewer.getTable());
  }

  public static Object getDataOfFirstSelectedOrNull(Table table) {
    final TableItem selected = getFirstSelectedOrNull(table);
    if (selected == null)
      return null;
    else
      return selected.getData();
  }

  public static Object getDataOfFirstSelectedOrNull(TableViewer tableViewer) {
    return getDataOfFirstSelectedOrNull(tableViewer.getTable());
  }

  /**
   * Pack the columns of our table to the ideal width.
   * 
   * @param tableViewer
   *          a table viewer.
   */
  public static void packColumns(TableViewer tableViewer) {
    packColumns(tableViewer.getTable());
  }

  /**
   * Pack the columns of our table to the ideal width.
   * 
   * @param table
   *          a table.
   */
  public static void packColumns(Table table) {
    for (final TableColumn col : table.getColumns()) {
      col.pack();
    }
  }

  private static class SortListener implements Listener {

    private final Comparator<String> f_cmp;

    SortListener(final Comparator<String> cmp) {
      f_cmp = cmp;
    }

    @Override
    public void handleEvent(final Event e) {
      final TableColumn tc = (TableColumn) e.widget;
      final Table table = tc.getParent();
      table.setRedraw(false);
      final int columnCount = table.getColumnCount();
      TableItem[] items = table.getItems();
      final TableColumn column = (TableColumn) e.widget;
      final TableColumn[] columns = table.getColumns();
      int index = 0; // index of the column clicked on
      for (final TableColumn c : columns) {
        if (c == column) {
          break;
        }
        index++;
      }
      final TableColumn oldSortColumn = table.getSortColumn();
      if (column == oldSortColumn) {
        // Change the direction of the sort
        if (table.getSortDirection() == SWT.UP) {
          table.setSortDirection(SWT.DOWN);
        } else {
          table.setSortDirection(SWT.UP);
        }
      } else {
        // Remember the new sort column and sort UP
        table.setSortColumn(column);
        table.setSortDirection(SWT.UP);
      }
      // sort ascending and remember the sort column index
      for (int i = 1; i < items.length; i++) {
        final String value1 = items[i].getText(index);
        for (int j = 0; j < i; j++) {
          final String value2 = items[j].getText(index);
          final int cmp = f_cmp.compare(value1, value2);
          final boolean swap = table.getSortDirection() == SWT.UP ? cmp < 0 : cmp > 0;
          if (swap) {
            final String[] values = new String[columnCount];
            final Image[] images = new Image[columnCount];
            final Object data = items[i].getData();
            for (int ii = 0; ii < columnCount; ii++) {
              values[ii] = items[i].getText(ii);
              images[ii] = items[i].getImage(ii);
            }
            items[i].dispose();
            final TableItem item = new TableItem(table, SWT.NONE, j);
            item.setText(values);
            item.setImage(images);
            item.setData(data);
            items = table.getItems();
            break;
          }
        }
      }
      table.setRedraw(true);
    }
  }

  /**
   * A general purpose sorter for a table column that sorts the column
   * alphabetically without regard to case. It reflects upon the table column
   * and table it is passed to perform its function.
   * <p>
   * Typical usage is to add this listener to the selection event of a table
   * column.
   * 
   * <pre>
   * final TableColumn c = new TableColumn(table, SWT.NONE);
   * c.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
   * </pre>
   */
  public static final Listener SORT_COLUMN_ALPHABETICALLY = new SortListener(StringComparators.SORT_ALPHABETICALLY);

  /**
   * A general purpose sorter for a table column that sorts the column
   * numerically. It reflects upon the table column and table it is passed to
   * perform its function.
   * <p>
   * Typical usage is to add this listener to the selection event of a table
   * column.
   * 
   * <pre>
   * final TableColumn c = new TableColumn(table, SWT.NONE);
   * c.addListener(SWT.Selection, TableUtility.SORT_COLUMN_NUMERICALLY);
   * </pre>
   */
  public static final Listener SORT_COLUMN_NUMERICALLY = new SortListener(StringComparators.SORT_NUMERICALLY);
  /**
   * A general purpose sorter for a table column that will try to sort it column
   * cells as a number first, and as a string second. It reflects upon the table
   * column and table it is passed to perform its function.
   * <p>
   * Typical usage is to add this listener to the selection event of a table
   * column.
   * 
   * <pre>
   * final TableColumn c = new TableColumn(table, SWT.NONE);
   * c.addListener(SWT.Selection, TableUtility.SORT_COLUMN_NUMERICALLY);
   * </pre>
   */
  public static final Listener SORT_COLUMN_NUMERICALLY_THEN_LEXICALLY = new SortListener(
      StringComparators.SORT_NUMERICALLY_THEN_LEXICALLY);
}
