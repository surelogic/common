package com.surelogic.common.i18n;

import com.surelogic.NonNull;

/**
 * Represents an analysis result message. The message can be output in a human
 * readable form as well as in a canonical form.
 * <p>
 * The {@code getInstance} methods are used to construct methods and the result
 * number matches those in the <tt>SureLogicResults.properties</tt> file defined
 * in this package.
 * 
 * @see I18N
 * @see JavaSourceReference
 */
public final class AnalysisResultMessage {
  public static final Object[] noArgs = new Object[0];

  /**
   * This number must exist in the <tt>SureLogicResults.properties</tt> file
   * defined in this package.
   */
  private final int f_number;

  @NonNull
  private final Object[] f_args;

  private AnalysisResultMessage(int number, Object... args) {
    f_number = number;
    f_args = (args.length > 0) ? args : noArgs;
  }

  public static AnalysisResultMessage getInstance(int number, Object... args) {
    I18N.res(number, args); // toss result, but ensure the call works
    return new AnalysisResultMessage(number, args);
  }

  @NonNull
  public String getResultString() {
    return f_args.length == 0 ? I18N.res(f_number) : I18N.res(f_number, f_args);
  }

  @NonNull
  public String getResultStringCanonical() {
    return f_args.length == 0 ? I18N.resc(f_number) : I18N.resc(f_number, f_args);
  }

  @Override
  @NonNull
  public String toString() {
    return getResultString();
  }

  public boolean sameAs(int num, Object[] args) {
    if (num == f_number) {
      if (args == null) {
        return f_args == null;
      }
      if (args.length != f_args.length) {
        return false;
      }
      for (int i = 0; i < args.length; i++) {
        if (args[i] != null) {
          if (!args[i].equals(f_args[i])) {
            return false;
          }
        } else if (f_args[i] != null) {
          return false; // args[i] is null, so different
        }
      }
      return true;
    }
    return false;
  }
}
