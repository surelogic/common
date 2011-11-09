/*
 * Created on Jun 13, 2003
 *  
 */
package com.surelogic.common.ui.views;

import java.util.logging.Logger;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;

import com.surelogic.common.logging.SLLogger;

/**
 * A content provider for Tables and TableTrees, extended with methods to
 * relate the layout of the table.
 * 
 * @author chance
 */
public interface ITableContentProvider
  extends IStructuredContentProvider, ITableLabelProvider {
  static final Logger LOG = SLLogger.getLogger("ECLIPSE.ui.contentProvider");

  /**
	 * @return The total number of columns in the table (including any tree as
	 *         the first column)
	 */
  int numColumns();

  /**
	 * @param column
	 * @return A String representing the title at the top of the column
	 */
  String getColumnTitle(int column);

  /**
	 * @param column
	 * @return The relative size of the column
	 */
  int getColumnWeight(int column);

  // TODO send event if layout changes
}
