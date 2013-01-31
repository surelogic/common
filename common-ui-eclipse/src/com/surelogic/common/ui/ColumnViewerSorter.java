package com.surelogic.common.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Helper class to sort a JFace viewer. Typical use would be as follows:
 * 
 * <pre>
 * column = new TableViewerColumn(viewer, SWT.NONE);
 * ...
 * new ColumnViewerSorter&lt;Person&gt;(viewer, column.getColumn()) {
 * 	protected int doCompare(Viewer viewer, Person e1, Person e2) {
 * 		return e1.surname.compareToIgnoreCase(e2.surname);
 * 	}
 * };
 * </pre>
 */
public abstract class ColumnViewerSorter<T> extends ViewerComparator {

	public static final int ASC = 1;

	public static final int NONE = 0;

	public static final int DESC = -1;

	private int f_direction = 0;

	private TableColumn f_column;

	private ColumnViewer f_viewer;

	/**
	 * Constructs an object to help sort a JFace viewer. Typical use would be as
	 * follows:
	 * 
	 * <pre>
	 * column = new TableViewerColumn(viewer, SWT.NONE);
	 * ...
	 * new ColumnViewerSorter&lt;Person&gt;(viewer, column.getColumn()) {
	 * 	protected int doCompare(Viewer viewer, Person e1, Person e2) {
	 * 		return e1.surname.compareToIgnoreCase(e2.surname);
	 * 	}
	 * };
	 * </pre>
	 * 
	 * @param viewer
	 *            the view to add this sorter to.
	 * @param column
	 *            the table column from the viewer to sort.
	 */
	public ColumnViewerSorter(ColumnViewer viewer, TableColumn column) {
		this.f_column = column;
		this.f_viewer = viewer;
		this.f_column.addSelectionListener(new SelectionAdapter() {

			@Override
      public void widgetSelected(SelectionEvent e) {
				if (ColumnViewerSorter.this.f_viewer.getComparator() != null) {
					if (ColumnViewerSorter.this.f_viewer.getComparator() == ColumnViewerSorter.this) {
						int tdirection = ColumnViewerSorter.this.f_direction;

						if (tdirection == ASC) {
							setSorter(ColumnViewerSorter.this, DESC);
						} else if (tdirection == DESC) {
							setSorter(ColumnViewerSorter.this, NONE);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				} else {
					setSorter(ColumnViewerSorter.this, ASC);
				}
			}
		});
	}

	public void setSorter(ColumnViewerSorter<T> sorter, int direction) {
		if (direction == NONE) {
			f_column.getParent().setSortColumn(null);
			f_column.getParent().setSortDirection(SWT.NONE);
			f_viewer.setComparator(null);
		} else {
			f_column.getParent().setSortColumn(f_column);
			sorter.f_direction = direction;

			if (direction == ASC) {
				f_column.getParent().setSortDirection(SWT.DOWN);
			} else {
				f_column.getParent().setSortDirection(SWT.UP);
			}

			if (f_viewer.getComparator() == sorter) {
				f_viewer.refresh();
			} else {
				f_viewer.setComparator(sorter);
			}
		}
	}

	@Override
  public int compare(Viewer viewer, Object e1, Object e2) {
		@SuppressWarnings("unchecked")
		final T o1 = (T) e1;
		@SuppressWarnings("unchecked")
		final T o2 = (T) e2;
		return f_direction * doCompare(viewer, o1, o2);
	}

	protected abstract int doCompare(Viewer viewer, T e1, T e2);
}
