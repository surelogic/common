package com.surelogic.common.xml;

import java.util.ArrayList;

import com.surelogic.InRegion;
import com.surelogic.Nullable;
import com.surelogic.Region;
import com.surelogic.RegionLock;
import com.surelogic.RequiresLock;
import com.surelogic.ThreadSafe;
import com.surelogic.UniqueInRegion;
import com.surelogic.common.CharBuffer;

/**
 * Manages a table of entities for XML. This class can be used to help escape
 * strings that are output in XML format.
 */
@ThreadSafe
@Region("EntitiesState")
@RegionLock("EntitiesLock is this protects EntitiesState")
public final class Entities {

  public static final class Holder {
    /*
     * Holds instances for use by callers
     */
    public static final Entities DEFAULT = new Entities();
    public static final Entities DEFAULT_PLUS_WHITESPACE = new Entities().setEscapeWhitespace(true);
  }

  public static void start(final String name, final StringBuilder b) {
    start(name, b, 0);
  }

  public static void start(final String name, final StringBuilder b, int indent) {
    indent(b, indent);
    b.append('<').append(name);
  }

  public static void indent(final StringBuilder b, int indent) {
    for (int i = 0; i < indent; i++) {
      b.append(XMLConstants.INDENT);
    }
  }

  public static void newLine(final StringBuilder b, int indent) {
    b.append('\n');
    indent(b, indent);
  }

  public static void closeStart(StringBuilder b, boolean end) {
    closeStart(b, end, true);
  }

  public static void closeStart(StringBuilder b, boolean end, boolean newline) {
    b.append(end ? "/>" : ">");
    if (newline) {
      b.append('\n');
    }
  }

  /**
   * Helper to create <name\>enclosedText</name>
   */
  public static void createTag(String name, String enclosedText, StringBuilder b) {
    b.append('<').append(name).append('>');
    addEscaped(enclosedText, b);
    end(name, b, 0);
  }

  public static void end(String name, StringBuilder b, int indent) {
    if (indent > 0) {
      indent(b, indent);
    }
    b.append("</").append(name).append(">\n");
  }

  /**
   * Helper to avoid having to escape non-string values.
   */
  private static void add(final String name, final String value, final StringBuilder b) {
    b.append(' ').append(name).append("=\"");
    b.append(value);
    b.append('\"');
  }

  public static void addAttribute(final String name, final String value, final StringBuilder b) {
    addAttribute(name, value, null, b);
  }

  public static void addAttribute(final String name, final String value, @Nullable Entities useToEscape, final StringBuilder b) {
    if (value == null)
      return;
    if (useToEscape == null)
      useToEscape = Holder.DEFAULT;
    add(name, useToEscape.escape(value), b);
  }

  public static void addAttribute(final String name, final boolean value, final StringBuilder b) {
    add(name, Boolean.toString(value), b);
  }

  public static void addAttribute(final String name, final int value, final StringBuilder b) {
    add(name, Integer.toString(value), b);
  }

  public static void addAttribute(final String name, final long value, final StringBuilder b) {
    add(name, Long.toString(value), b);
  }

  public static void addEscaped(final String value, final StringBuilder b) {
    b.append(Holder.DEFAULT.escape(value));
  }

  public static CharBuffer addEscaped(final String value, final CharBuffer b) {
    b.append(Holder.DEFAULT.escape(value));
    return b;
  }

  public static String trimInternal(final String value) {
    return value.replaceAll("\\s+", " ");
  }

  private static final class EscapePair {
    /**
     * The character to be escaped.
     */
    char value;
    /**
     * Call {@link #getEscapeValue()} don't use this field directly because it
     * may be {@code null} if we are to simply generate a Unicode escape value.
     */
    String escapeValueOrNullForUnicode;

    EscapePair(char value, String escapeValueOrNullForUnicode) {
      this.value = value;
      this.escapeValueOrNullForUnicode = escapeValueOrNullForUnicode;
    }

    String getEscapeValue() {
      if (escapeValueOrNullForUnicode != null)
        return escapeValueOrNullForUnicode;
      else
        return getUnicodeEscapeFor(value);
    }
  }

  /**
   * Escapes defined by this.
   */
  @UniqueInRegion("EntitiesState")
  private final ArrayList<EscapePair> f_escapes = new ArrayList<EscapePair>();
  /**
   * {@code true} indicates that whitespace should be escaped.
   */
  @InRegion("EntitiesState")
  private boolean f_escapeWhitespace = false;

  public Entities() {
    define('&', "&amp;");
    define('\'', "&apos;");
    define('>', "&gt;");
    define('<', "&lt;");
    define('\"', "&quot;");
  }

  public String escape(final String value) {
    StringBuilder b = new StringBuilder(value);
    synchronized (this) {
      int charIndex = 0;
      while (charIndex < b.length()) {
        char c = b.charAt(charIndex);
        final String escapeValue = getEscapeValueOrNullFor(c);
        if (escapeValue != null) {
          b.replace(charIndex, charIndex + 1, escapeValue);
          charIndex = charIndex + escapeValue.length(); // skip what we added
        } else {
          charIndex++; // next char
        }
      }
    }
    return b.toString();
  }

  @RequiresLock("EntitiesLock")
  private String getEscapeValueOrNullFor(char value) {
    for (EscapePair p : f_escapes) {
      if (p.value == value) {
        return p.getEscapeValue();
      }
    }
    if (f_escapeWhitespace && Character.isWhitespace(value)) {
      return getUnicodeEscapeFor(value);
    }
    /*
     * Always escape invalid XML characters
     */
    if (XMLChar.isInvalid(value)) {
      return getUnicodeEscapeFor(value);
    }
    return null;
  }

  private static String getUnicodeEscapeFor(char value) {
    return "&#x" + Integer.toHexString(value) + ";";
  }

  /**
   * Defines a new character entity. For example, the default quotation is
   * defined as:
   * 
   * <pre>
   * Entities e = ...
   * e.define('\&quot;', &quot;quot&quot;);
   * </pre>
   * 
   * @param value
   *          a character.
   * @param escapeValueOrNullForUnicode
   *          the name for the character entity.
   * @return this set of entities.
   */
  public Entities define(char value, String escapeValueOrNullForUnicode) {
    // check if this value has an escape, if so just update it
    synchronized (this) {
      for (EscapePair p : f_escapes) {
        if (p.value == value) {
          p.escapeValueOrNullForUnicode = escapeValueOrNullForUnicode;
          return this;
        }
      }
      final EscapePair p = new EscapePair(value, escapeValueOrNullForUnicode);
      f_escapes.add(p);
    }
    return this;
  }

  /**
   * Sets if this set of entities should escape whitespace characters into a
   * Unicode escape.
   * <p>
   * For example a space becomes <tt>&amp;#x20;</tt> and a newline becomes
   * <tt>&amp;#xa;</tt>.
   * <p>
   * This is intended to allow encoding a string into an attribute.
   * 
   * @param newValue
   *          {code true} if whitespace characters should be escaped.
   * @return this set of entities.
   */
  public Entities setEscapeWhitespace(boolean newValue) {
    synchronized (this) {
      f_escapeWhitespace = newValue;
    }
    return this;
  }
}
