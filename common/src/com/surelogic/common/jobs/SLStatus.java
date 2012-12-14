package com.surelogic.common.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.surelogic.NotThreadSafe;
import com.surelogic.ThreadSafe;
import com.surelogic.Vouch;
import com.surelogic.common.i18n.I18N;

/**
 * An IDE independent status object.
 */
@ThreadSafe
public final class SLStatus {

  /**
   * Used for plug-in or module-specific status codes.
   */
  public static final int OK = 0;

  /**
   * The severity of this status.
   */
  private final SLSeverity f_severity;

  /**
   * The severity of this status.
   * 
   * @return The severity of this status.
   */
  public SLSeverity getSeverity() {
    return f_severity;
  }

  /**
   * The plug-in or module-specific status code, or {@link #OK}.
   */
  private final int f_code;

  /**
   * The plug-in or module-specific status code, or {@link #OK}.
   * 
   * @return The plug-in or module-specific status code, or {@link #OK}.
   */
  public int getCode() {
    return f_code;
  }

  /**
   * A human-readable message, localized to the current locale.
   */
  private final String f_message;

  /**
   * A human-readable message, localized to the current locale.
   * 
   * @return A human-readable message, localized to the current locale.
   */
  public String getMessage() {
    return f_message;
  }

  /**
   * A low-level exception, or <code>null</code> if not applicable.
   */
  @Vouch("ThreadSafe")
  private final Throwable f_exception;

  /**
   * A low-level exception, or <code>null</code> if not applicable.
   * 
   * @return A low-level exception, or <code>null</code> if not applicable.
   */
  public Throwable getException() {
    return f_exception;
  }

  /**
   * Child status objects.
   */
  private final CopyOnWriteArrayList<SLStatus> f_children = new CopyOnWriteArrayList<SLStatus>();

  /**
   * Adds a child status object to this status. Children of this status are
   * placed in the order in which they are added.
   * 
   * @param status
   *          the child status object to attach to this status.
   */
  public void addChild(final SLStatus status) {
    f_children.add(status);
  }

  /**
   * Gets the list of child status objects attached to this.
   * 
   * @return the list of child status objects attached to this. This list is a
   *         copy and can be mutated by the caller.
   */
  public List<SLStatus> getChildren() {
    return new ArrayList<SLStatus>(f_children);
  }

  /**
   * Constructs a status object.
   * 
   * @param severity
   *          the non-null severity of this status.
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   * @param message
   *          a non-null human-readable message, localized to the current
   *          locale.
   * @param exception
   *          a low-level exception, or <code>null</code> if not applicable.
   * 
   * @throws IllegalArgumentException
   *           if the severity or the message are {@code null}.
   */
  private SLStatus(final SLSeverity severity, final int code, final String message, final Throwable exception) {
    if (severity == null)
      throw new IllegalArgumentException(I18N.err(44, "severity"));
    if (message == null)
      throw new IllegalArgumentException(I18N.err(44, "message"));

    f_severity = severity;
    f_code = code;
    f_message = message;
    f_exception = exception;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    toStringHelper(b, this);
    return b.toString();
  }

  public void logTo(Logger log) {
    final StringBuilder b = new StringBuilder();
    toStringHelperMessage(b, this, false);
    toStringHelperChildren(b, this);
    b.append("\n");
    log.log(f_severity.toLevel(), b.toString(), f_exception);
  }

  private static void toStringHelper(StringBuilder b, SLStatus status) {
    toStringHelperMessage(b, status, true);
    Throwable t = status.getException();
    if (t != null) {
      b.append("\nException: ");
      final Writer writer = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(writer);
      t.printStackTrace(printWriter);
      b.append(writer.toString());
    }
    toStringHelperChildren(b, status);
  }

  private static void toStringHelperMessage(StringBuilder b, SLStatus status, boolean showSeverity) {
    if (showSeverity)
      b.append(status.getSeverity().toString()).append(" ");
    final int code = status.getCode();
    if (code != OK)
      b.append("(SureLogic #").append(code).append(") ");
    b.append(status.getMessage());
  }

  private static void toStringHelperChildren(StringBuilder b, SLStatus status) {
    for (SLStatus child : status.getChildren()) {
      b.append("\n[Contributing] ");
      toStringHelper(b, child);
    }
  }

  /*
   * Static factory methods
   */

  /**
   * Creates a new <code>INFO</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   */
  public static SLStatus createInfoStatus(final String message) {
    return new SLStatus(SLSeverity.INFO, OK, message, null);
  }

  /**
   * Creates a new <code>INFO</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   * @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createInfoStatus(final String message, final Throwable exception) {
    return new SLStatus(SLSeverity.INFO, OK, message, exception);
  }

  /**
   * Creates a new <code>WARNING</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   */
  public static SLStatus createWarningStatus(final String message) {
    return new SLStatus(SLSeverity.WARNING, OK, message, null);
  }

  /**
   * Creates a new <code>WARNING</code> status object.
   * 
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   * @param message
   *          a human-readable message, localized to the current locale.
   */
  public static SLStatus createWarningStatus(final int code, final String message) {
    return new SLStatus(SLSeverity.WARNING, code, message, null);
  }

  /**
   * Creates a new <code>WARNING</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   * @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createWarningStatus(final String message, final Throwable exception) {
    return new SLStatus(SLSeverity.WARNING, OK, message, exception);
  }

  /**
   * Creates a new <code>WARNING</code> status object.
   * 
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   * @param message
   *          a human-readable message, localized to the current locale.
   * @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createWarningStatus(final int code, final String message, final Throwable exception) {
    return new SLStatus(SLSeverity.WARNING, code, message, exception);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   */
  public static SLStatus createErrorStatus(final String message) {
    return new SLStatus(SLSeverity.ERROR, OK, message, null);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   * @param message
   *          a human-readable message, localized to the current locale.
   */
  public static SLStatus createErrorStatus(final int code, final String message) {
    return new SLStatus(SLSeverity.ERROR, code, message, null);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   ** @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createErrorStatus(final Throwable exception) {
    return new SLStatus(SLSeverity.ERROR, OK, "unexpected error", exception);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   ** @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createErrorStatus(final int code, final Throwable exception) {
    return new SLStatus(SLSeverity.ERROR, code, "unexpected error", exception);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   * @param message
   *          a human-readable message, localized to the current locale.
   * @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createErrorStatus(final String message, final Throwable exception) {
    return new SLStatus(SLSeverity.ERROR, OK, message, exception);
  }

  /**
   * Creates a new <code>ERROR</code> status object.
   * 
   * @param code
   *          the plug-in or module-specific status code, or {@link #OK}.
   * @param message
   *          a human-readable message, localized to the current locale.
   * @param exception
   *          a low-level exception, or {@code null} if not applicable.
   */
  public static SLStatus createErrorStatus(final int code, final String message, final Throwable exception) {
    return new SLStatus(SLSeverity.ERROR, code, message, exception);
  }

  /**
   * Used to indicate that something was canceled.
   */
  public static final SLStatus CANCEL_STATUS = new SLStatus(SLSeverity.CANCEL, OK, "Canceled", null);

  /**
   * Used to indicate that something completed successfully.
   */
  public static final SLStatus OK_STATUS = createOkStatus();

  public static SLStatus createOkStatus() {
    return new SLStatus(SLSeverity.OK, OK, "OK", null);
  }

  /**
   * A builder class to help construct multi-status objects.
   */
  @NotThreadSafe
  public static class Builder {
    private final List<SLStatus> f_children = new ArrayList<SLStatus>();

    /**
     * Adds a status object to this builder. It will become a child of the
     * status object when {@link #build()} is called.
     * 
     * @param status
     *          a status object.
     */
    public void addChild(SLStatus status) {
      if (status == null)
        throw new IllegalArgumentException(I18N.err(44, "status"));
      f_children.add(status);
    }

    /**
     * Constructs a status object with a severity that matches the highest
     * severity of one of its child status objects.
     * <p>
     * If no children have been added to this builder then
     * {@link SLStatus#OK_STATUS} is returned.
     * <p>
     * If only one status object has been added to this builder than that object
     * is returned (i.e., no multi-status object is constructed).
     * 
     * @return a status object.
     */
    public SLStatus build() {
      int num = f_children.size();
      if (num == 0) {
        return OK_STATUS;
      } else if (num == 1) {
        return f_children.get(0);
      }
      SLSeverity sev = SLSeverity.OK;
      int code = OK;
      Throwable t = null;
      for (SLStatus c : f_children) {
        if (c.getSeverity().ordinal() > sev.ordinal()) {
          sev = c.getSeverity();
          code = c.getCode();
          t = c.getException();
        }
      }
      SLStatus s = new SLStatus(sev, code, "Top-level status", t);
      for (SLStatus c : f_children) {
        s.addChild(c);
      }
      f_children.clear();
      return s;
    }
  }
}
