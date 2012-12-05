package com.surelogic.common.ui;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.TreeColumn;

import com.surelogic.common.core.EclipseUtility;

/**
 * Utility class used to persist column widths based upon the use's preference.
 */
public class ColumnResizeListener extends ControlAdapter {

  final String f_prefKey;

  /**
   * Constructs an instance.
   * 
   * @param prefKey
   *          the preference key to use when calling
   *          {@link EclipseUtility#setIntPreference(String, int)} to persist
   *          the width of this column.
   */
  public ColumnResizeListener(String prefKey) {
    f_prefKey = prefKey;
  }

  @Override
  public void controlResized(ControlEvent e) {
    if (e.widget instanceof TreeColumn) {
      int width = ((TreeColumn) e.widget).getWidth();
      EclipseUtility.setIntPreference(f_prefKey, width);
    }
  }
}
