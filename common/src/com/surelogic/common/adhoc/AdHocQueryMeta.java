package com.surelogic.common.adhoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.surelogic.NonNull;

public final class AdHocQueryMeta {

  private String f_name;

  /**
   * Actually has to have a variable ?name? is not enough. If there is nothing
   * to resolve we just say we don't need variables.
   */
  private boolean f_usesVariables;

  private String f_text;

  /**
   * Returns the set of variables found in this meta's text. The returned set is
   * a copy and may be freely mutated by the caller.
   * <p>
   * This method does not consider escaped question marks as indicating a
   * variable start/end, e.g., <b>\?</b>. Therefore, <b>?foo?</b> indicates the
   * variable <b>foo</b>, while <b>\?foo\?</b> is not a variable and will print
   * <tt>"?foo?"</tt> in the fully-qualified meta text.
   * 
   * @return the set of variables found in this meta's text or the empty set if
   *         no variable are used.
   */
  public Set<String> getVariables() {
    final String text = f_text;
    final Set<String> variableSet = new HashSet<String>();
    final BufferedReader sr = new BufferedReader(new StringReader(text));
    try {
      String line;
      while ((line = sr.readLine()) != null) {
        int q1 = line.indexOf('?');
        while (q1 != -1) {
          int q2 = line.indexOf('?', q1 + 1);
          if (q2 != -1) {
            final String key = line.substring(q1 + 1, q2);
            if (key != null && key.length() > 0) {
              variableSet.add(key);
            }
            q1 = line.indexOf('?', q2 + 1);
          } else {
            break;
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return variableSet;
  }

  public String getText() {
    return f_text;
  }

  public String getText(final Map<String, String> variableValues) {
    return f_text;
  }

  public boolean isCompletelySubstitutedBy(final Map<String, String> variableValues) {
    final Set<String> work = getVariables();
    if (variableValues == null) {
      return work.isEmpty();
    }
    work.removeAll(variableValues.keySet());
    return work.isEmpty();
  }

  public static void main(String[] args) {
    AdHocQueryMeta meta = new AdHocQueryMeta();
    meta.f_text = "this ?foo\\??bar?";
    System.out.println(meta.getVariables());
    int i = 0;
    i = indexOfNonEscapedQuestionMark(meta.f_text, i);
    System.out.println("one: " + i);
    i = indexOfNonEscapedQuestionMark(meta.f_text, i + 1);
    System.out.println("two: " + i);
    i = indexOfNonEscapedQuestionMark(meta.f_text, i + 1);
    System.out.println("three: " + i);
    i = indexOfNonEscapedQuestionMark(meta.f_text, i + 1);
    System.out.println("four: " + i);
    i = indexOfNonEscapedQuestionMark(meta.f_text, i + 1);
    System.out.println("five: " + i);
    System.out.println(escapeQuestionMarks("?"));
    System.out.println(escapeQuestionMarks("??"));
    System.out.println(escapeQuestionMarks("\\?foo\\?"));
    System.out.println(escapeQuestionMarks("\\\\?"));
  }

  /**
   * Replaces all instances of <b>\?</b> in a string with <b>?</b>.
   * 
   * @param text
   *          a string.
   * @return the passed string with all instances of <b>\?</b> in a string with
   *         <b>?</b>.
   */
  @NonNull
  public static String escapeQuestionMarks(@NonNull final String text) {
    return text.replaceAll("\\\\\\?", "?");
  }

  public static int indexOfNonEscapedQuestionMark(final String text, final int fromIndex) {
    int from = fromIndex;
    while (true) {
      int qm = text.indexOf('?', from);
      if (qm == -1)
        break;
      else if (qm == 0)
        return qm;
      else {
        /*
         * ensure that '\' is not immediately prior, i.e., the question mark is
         * escaped.
         */
        if (text.charAt(qm - 1) == '\\') {
          from = qm + 1;
          continue; // look for the next one
        } else
          return qm;
      }
    }
    return -1;
  }
}
