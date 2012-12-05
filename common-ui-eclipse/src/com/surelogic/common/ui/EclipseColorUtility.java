package com.surelogic.common.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.surelogic.Utility;

@Utility
public final class EclipseColorUtility {

  private static Color f_subtleTextColor;

  public static Color getSubtleTextColor() {
    if (f_subtleTextColor == null) {
      f_subtleTextColor = new Color(Display.getCurrent(), 149, 125, 71);
      Display.getCurrent().disposeExec(new Runnable() {
        public void run() {
          f_subtleTextColor.dispose();
        }
      });
    }
    return f_subtleTextColor;
  }

  private static Color f_diffHighlightColorNewChanged;

  public static Color getDiffHighlightColorNewChanged() {
    if (f_diffHighlightColorNewChanged == null) {
      f_diffHighlightColorNewChanged = new Color(Display.getCurrent(), 255, 255, 190);
      Display.getCurrent().disposeExec(new Runnable() {
        public void run() {
          f_diffHighlightColorNewChanged.dispose();
        }
      });
    }
    return f_diffHighlightColorNewChanged;
  }

  private static Color f_diffHighlightColorObsolete;

  public static Color getDiffHighlightColorObsolete() {
    if (f_diffHighlightColorObsolete == null) {
      f_diffHighlightColorObsolete = new Color(Display.getCurrent(), 255, 190, 190);
      Display.getCurrent().disposeExec(new Runnable() {
        public void run() {
          f_diffHighlightColorObsolete.dispose();
        }
      });
    }
    return f_diffHighlightColorObsolete;
  }

  private EclipseColorUtility() {
    // no instances
  }
}
