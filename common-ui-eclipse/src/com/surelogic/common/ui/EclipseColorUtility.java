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

  private static Color f_compoundScheme1Color0;

  public static Color getCompoundScheme1Color0() {
    if (f_compoundScheme1Color0 == null) {
      f_compoundScheme1Color0 = new Color(EclipseUIUtility.getDisplay(), 106, 56, 114);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_compoundScheme1Color0.dispose();
        }
      });
    }
    return f_compoundScheme1Color0;
  }

  private static Color f_compoundScheme1Color1;

  public static Color getCompoundScheme1Color1() {
    if (f_compoundScheme1Color1 == null) {
      f_compoundScheme1Color1 = new Color(EclipseUIUtility.getDisplay(), 153, 155, 59);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_compoundScheme1Color1.dispose();
        }
      });
    }
    return f_compoundScheme1Color1;
  }

  private static Color f_compoundScheme1Color2;

  public static Color getCompoundScheme1Color2() {
    if (f_compoundScheme1Color2 == null) {
      f_compoundScheme1Color2 = new Color(EclipseUIUtility.getDisplay(), 255, 151, 60);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_compoundScheme1Color2.dispose();
        }
      });
    }
    return f_compoundScheme1Color2;
  }

  private static Color f_compoundScheme1Color3;

  public static Color getCompoundScheme1Color3() {
    if (f_compoundScheme1Color3 == null) {
      f_compoundScheme1Color3 = new Color(EclipseUIUtility.getDisplay(), 102, 144, 196);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_compoundScheme1Color3.dispose();
        }
      });
    }
    return f_compoundScheme1Color3;
  }

  private static Color f_compoundScheme1Color4;

  public static Color getCompoundScheme1Color4() {
    if (f_compoundScheme1Color4 == null) {
      f_compoundScheme1Color4 = new Color(EclipseUIUtility.getDisplay(), 28, 204, 137);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_compoundScheme1Color4.dispose();
        }
      });
    }
    return f_compoundScheme1Color4;
  }

  private static Color f_analogousScheme1Color0;

  public static Color getAnalogousScheme1Color0() {
    if (f_analogousScheme1Color0 == null) {
      f_analogousScheme1Color0 = new Color(EclipseUIUtility.getDisplay(), 194, 172, 255);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_analogousScheme1Color0.dispose();
        }
      });
    }
    return f_analogousScheme1Color0;
  }

  private static Color f_analogousScheme1Color1;

  public static Color getAnalogousScheme1Color1() {
    if (f_analogousScheme1Color1 == null) {
      f_analogousScheme1Color1 = new Color(EclipseUIUtility.getDisplay(), 218, 255, 200);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_analogousScheme1Color1.dispose();
        }
      });
    }
    return f_analogousScheme1Color1;
  }

  private static Color f_analogousScheme1Color2;

  public static Color getAnalogousScheme1Color2() {
    if (f_analogousScheme1Color2 == null) {
      f_analogousScheme1Color2 = new Color(EclipseUIUtility.getDisplay(), 232, 214, 128);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_analogousScheme1Color2.dispose();
        }
      });
    }
    return f_analogousScheme1Color2;
  }

  private static Color f_analogousScheme1Color3;

  public static Color getAnalogousScheme1Color3() {
    if (f_analogousScheme1Color3 == null) {
      f_analogousScheme1Color3 = new Color(EclipseUIUtility.getDisplay(), 255, 169, 113);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_analogousScheme1Color3.dispose();
        }
      });
    }
    return f_analogousScheme1Color3;
  }

  private static Color f_analogousScheme1Color4;

  public static Color getAnalogousScheme1Color4() {
    if (f_analogousScheme1Color4 == null) {
      f_analogousScheme1Color4 = new Color(EclipseUIUtility.getDisplay(), 232, 152, 192);
      EclipseUIUtility.disposeExec(new Runnable() {
        @Override
        public void run() {
          f_analogousScheme1Color4.dispose();
        }
      });
    }
    return f_analogousScheme1Color4;
  }

  private EclipseColorUtility() {
    // no instances
  }
}
