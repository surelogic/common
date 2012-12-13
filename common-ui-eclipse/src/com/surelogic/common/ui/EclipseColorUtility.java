package com.surelogic.common.ui;

import org.eclipse.swt.graphics.Color;

import com.surelogic.Utility;

@Utility
public final class EclipseColorUtility {

  private static Color f_subtleTextColor;

  public static Color getSubtleTextColor() {
    if (f_subtleTextColor == null) {
      f_subtleTextColor = new Color(EclipseUIUtility.getDisplay(), 149, 125, 71);
      EclipseUIUtility.disposeExec(new Runnable() {
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
      f_diffHighlightColorNewChanged = new Color(EclipseUIUtility.getDisplay(), 255, 255, 190);
      EclipseUIUtility.disposeExec(new Runnable() {
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
      f_diffHighlightColorObsolete = new Color(EclipseUIUtility.getDisplay(), 255, 190, 190);
      EclipseUIUtility.disposeExec(new Runnable() {
        public void run() {
          f_diffHighlightColorObsolete.dispose();
        }
      });
    }
    return f_diffHighlightColorObsolete;
  }

  private static Color f_runControlBackgroundColor;

  public static Color getRunControlBackgroundColor() {
    if (f_runControlBackgroundColor == null) {
      f_runControlBackgroundColor = new Color(EclipseUIUtility.getDisplay(), 137, 157, 181);
      EclipseUIUtility.disposeExec(new Runnable() {
        public void run() {
          f_runControlBackgroundColor.dispose();
        }
      });
    }
    return f_runControlBackgroundColor;
  }

  private EclipseColorUtility() {
    // no instances
  }
}
