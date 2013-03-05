package com.surelogic.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.surelogic.Utility;

@Utility
public final class EclipseColorUtility {

  private static Color f_subtleTextColor;

  public static Color getSubtleTextColor() {
    if (f_subtleTextColor == null) {
      f_subtleTextColor = new Color(EclipseUIUtility.getDisplay(), 149, 125, 71);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
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
        @Override
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
        @Override
        public void run() {
          f_diffHighlightColorObsolete.dispose();
        }
      });
    }
    return f_diffHighlightColorObsolete;
  }

  private static Color f_slightlyDarkerBackgroundColor;

  public static Color getSlightlyDarkerBackgroundColor() {
    if (f_slightlyDarkerBackgroundColor == null) {
      f_slightlyDarkerBackgroundColor = new Color(EclipseUIUtility.getDisplay(), 137, 157, 181);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_slightlyDarkerBackgroundColor.dispose();
        }
      });
    }
    return f_slightlyDarkerBackgroundColor;
  }

  private static Color f_QueryMenuSubtleColor;

  public static Color getQueryMenuSubtleColor() {
    if (f_QueryMenuSubtleColor == null) {
      f_QueryMenuSubtleColor = new Color(EclipseUIUtility.getDisplay(), 169, 145, 91);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_QueryMenuSubtleColor.dispose();
        }
      });
    }
    return f_QueryMenuSubtleColor;
  }

  private static Color f_QueryMenuGrayColor;

  public static Color getQueryMenuGrayColor() {
    if (f_QueryMenuGrayColor == null) {
      f_QueryMenuGrayColor = EclipseUIUtility.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
    }
    return f_QueryMenuGrayColor;
  }

  private EclipseColorUtility() {
    // no instances
  }
}
