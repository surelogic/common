package com.surelogic.common.adhoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;

/**
 * Represents a piece of meta-information within an ad hoc query&mdash;some
 * piece of information about the query.
 * <p>
 * <b>Simple:</b> A simple meta section is placed within a comment and is named.
 * A meta is simple if it does not use variable substitution (discussed below).
 * For example, consider the query snippet below.
 * 
 * <pre>
 * -- META-BEGIN(doc)
 * -- This is information about
 * -- this <b>great</b> query that
 * -- should be displayed to the <i>user</i>.
 * -- META-END
 * </pre>
 * 
 * This example creates a meta that is named <b>doc</b> that contains the text
 * 
 * <pre>
 * This is information about
 * this <b>great</b> query that
 * should be displayed to the <i>user</i>.
 * </pre>
 * 
 * <p>
 * <b>Variables:</b> Any meta section can request variable substitution by
 * surrounding its name with question marks. For example, consider the query
 * snippet below.
 * 
 * <pre>
 * -- META-BEGIN(?gremlin?)
 * --   parentClass.matches(?package?.?class?)
 * -- META-END
 * </pre>
 * 
 * This example creates a meta that is named <b>gremlin</b> where variable
 * substitution is requested. The text of the meta contains two variables
 * <b>package</b> and <b>class</b>. If these are defined to be
 * <tt>"com.surelogic"</tt> and <tt>"Object"</tt>, respectively, then text of
 * the meta is transformed into
 * 
 * <pre>
 * parentClass.matches(com.surelogic.Object)
 * </pre>
 * 
 * Note that the question marks are <b>never</b> considered part of the meta's
 * name. They just indicate a request for variable substitution. In the example
 * above the meta's name is <b>gremlin</b> (without the question marks). This
 * allows a single named meta to be used with or without variable substitution.
 * <p>
 * In addition to variable substitution, a meta with variable substitution
 * allows escaping of question marks in the text. This is not supported in a
 * simple meta. Escaping changes all <b>\?</b> to <b>?</b> in the meta text.
 * This capability allows question marks to exist in the final text.
 */
public final class AdHocQueryMeta {

  /**
   * Constructs a meta with the passed name and text.
   * <p>
   * Constructing this object with a name surrounded by <b>?</b>, such as
   * <b>?foo?</b> indicates that this meta has variables that need to be
   * resolved for the text to be fully-qualified. Note that even if this is done
   * the object must contain actual variables in its text or the <b>?</b> is
   * ignored in the name (however, '\?' is resolved to '?' in this case).
   * <p>
   * If the meta name is surrounded by <b>?</b>, such as <b>?foo?</b>, then
   * escaped <b>?</b> are resolved (any '\?' in the text is resolved to '?').
   * And escaped <b>?</b> are not considered a delimiter for a variable.
   * 
   * @param name
   *          the name of the meta, such as <b>foo</b> or <b>?foo?</b>.
   *          Surrounding with <b>?</b> indicates that variable substitution is
   *          desired in the text.
   * @param text
   *          the text of the meta.
   * @throws IllegalArgumentException
   *           if any parameter is {@code null}.
   */
  AdHocQueryMeta(final String name, final String text) {
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    if (text == null)
      throw new IllegalArgumentException(I18N.err(44, "text"));
    if (name.startsWith("?") && name.endsWith("?")) {
      f_name = name.substring(1, name.length() - 1);
      f_usesVariables = !getVariablesHelper(text).isEmpty();
      if (!f_usesVariables) {
        /*
         * No variables in the text, but due to the name we resolve '\?' to '?'.
         */
        f_text = escapeQuestionMarks(text);
      } else {
        /*
         * In this case leave text with variables and '\?' within it.
         */
        f_text = text;
      }
    } else {
      f_name = name;
      f_usesVariables = false;
      f_text = text;
    }
  }

  /**
   * The name of this meta. Any surrounding <b>?</b> are always removed.
   */
  private final String f_name;

  /**
   * Gets the name of this meta. Any surrounding <b>?</b> are always removed.
   * 
   * @return the name of this meta.
   */
  public String getName() {
    return f_name;
  }

  /**
   * Actually has to have a variable ?name? is not enough. If there is nothing
   * to resolve we just say we don't need variables.
   */
  private boolean f_usesVariables;

  /**
   * Indicates if this meta has variables that need to be resolved for the text
   * to be fully-qualified. This is flagged by constructing this object with a
   * name surrounded by <b>?</b>, such as <b>?foo?</b>. Note that even if this
   * is done the object must contain actual variables in its text or the
   * <b>?</b> is ignored in the name.
   * 
   * @return {@code true} if this meta has variables that need to be resolved
   *         for the text to be fully-qualified, {@code false} otherwise.
   */
  public boolean usesVariables() {
    return f_usesVariables;
  }

  @NonNull
  private final String f_text;

  /**
   * Returns the set of variables found in this meta's text. The returned set is
   * fresh and may be freely mutated by the caller.
   * <p>
   * This method does not consider escaped question marks as indicating a
   * variable start/end, e.g., <b>\?</b>. Therefore, <b>?foo?</b> indicates the
   * variable <b>foo</b>, while <b>\?foo\?</b> is not a variable and will print
   * <tt>"?foo?"</tt> in the fully-qualified meta text.
   * 
   * @return the set of variables found in this meta's text or the empty set if
   *         no variables are used.
   */
  public Set<String> getVariables() {
    return getVariablesHelper(f_text);
  }

  /**
   * Gets the text of this meta. If {@link #usesVariables()} is {@code true}
   * then the returned text contains unresolved variable references, such as
   * <b>?myVar?</b> and may contain escaped question marks, such as <b>\?</b>.
   * 
   * @return the text of this meta, in raw form if the meta uses variable
   *         references.
   */
  @NonNull
  public String getText() {
    return f_text;
  }

  /**
   * Replaces all variables in the text of this meta with the passed values and
   * returns the resulting text. After substitution takes place all escaped
   * question marks are resolved, i.e., all <b>\?</b> in the text are changed to
   * <b>?</b>.
   * <p>
   * If this meta does not use variables, i.e., {@link #usesVariables()} is
   * {@code false}, then this method always returns the same value as
   * {@link #getText()}.
   * 
   * @param variableValues
   *          the defined values for variables.
   * @return the text of this meta with as many substitutions made as possible
   *         and escaped question marks resolved. If this meta does not use
   *         variables then the result of {@link #getText()} is returned.
   */
  @NonNull
  public String getText(@NonNull final Map<String, String> variableValues) {
    if (!f_usesVariables)
      return getText();

    if (variableValues == null) {
      throw new IllegalArgumentException(I18N.err(44, "variableValues"));
    }
    final StringBuilder b = new StringBuilder();
    final BufferedReader r = new BufferedReader(new StringReader(f_text));

    String line;
    try {
      while ((line = r.readLine()) != null) {
        int q1 = indexOfNonEscapedQuestionMark(line);
        int q2 = indexOfNonEscapedQuestionMark(line, q1 + 1);
        while (q1 != -1 && q2 != -1) {
          // there could be escaped question marks in the variable name (yuck)
          final String var = line.substring(q1 + 1, q2);
          final String varEscaped = escapeQuestionMarks(var);
          // lookup with escaped variable name
          final String value = variableValues.get(varEscaped);
          // replace raw (unescaped) variable name with a value
          line = line.replace('?' + var + '?', value == null ? "" : value);
          q1 = indexOfNonEscapedQuestionMark(line);
          q2 = indexOfNonEscapedQuestionMark(line, q1 + 1);
        }
        b.append(line);
        b.append('\n');
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return escapeQuestionMarks(b.toString());
  }

  /**
   * Checks if the passed set of variable values would allow a complete
   * substitution of the variables in this meta.
   * 
   * @param variableValues
   *          the defined values for variables, or {@code null} for no defined
   *          values.
   * @return {@code true} if the passed set of variable values would allow a
   *         complete substitution of the variables in this query, {@code false}
   *         otherwise.
   */
  public boolean isCompletelySubstitutedBy(@Nullable final Map<String, String> variableValues) {
    final Set<String> work = getVariables();
    if (variableValues == null) {
      return work.isEmpty();
    }
    work.removeAll(variableValues.keySet());
    return work.isEmpty();
  }

  @Override
  public String toString() {
    return super.toString() + "[name=\"" + f_name + "\" (" + (f_usesVariables ? "uses variables" : "no variables") + ") text=\""
        + f_text + "\"]";
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

  /**
   * Returns the index within the passed text string of the first occurrence of
   * <b>?</b> that is not escaped (preceded by <b>\</b>), starting the search at
   * the specified index.
   * 
   * @param text
   *          the string to search.
   * @param fromIndex
   *          the index to start the search from.
   * 
   * @return the index of the first occurrence of <b>?</b> that is not escaped
   *         (preceded by <b>\</b>) in the character sequence represented by
   *         text that is greater than or equal to fromIndex, or -1 if the
   *         character does not occur.
   */
  public static int indexOfNonEscapedQuestionMark(final String text, final int fromIndex) {
    int from = fromIndex;
    while (true) {
      int qm = text.indexOf('?', from);
      if (qm == -1)
        break;
      else if (qm == 0)
        return qm; // ? at 0 cannot be escaped
      else {
        /*
         * Ensure that '\' is not immediately prior, i.e., the question mark is
         * escaped. If it is escaped we skip it and look for the next one.
         */
        if (text.charAt(qm - 1) == '\\') {
          // it is escaped, bump from index, and look for next one
          from = qm + 1;
          continue;
        } else
          return qm;
      }
    }
    return -1;
  }

  /**
   * Returns the index within the passed text string of the first occurrence of
   * <b>?</b> that is not escaped (preceded by <b>\</b>), starting the search at
   * the beginning of the text.
   * 
   * @param text
   *          the string to search.
   * 
   * @return the index of the first occurrence of <b>?</b> that is not escaped
   *         (preceded by <b>\</b>) in the character sequence represented by
   *         text that is greater than or equal to 0, or -1 if the character
   *         does not occur.
   */
  public static int indexOfNonEscapedQuestionMark(final String text) {
    return indexOfNonEscapedQuestionMark(text, 0);
  }

  /**
   * Returns the set of variables found in the passed text. The returned set is
   * fresh and may be freely mutated by the caller.
   * <p>
   * This method does not consider escaped question marks as indicating a
   * variable start/end, e.g., <b>\?</b>. Therefore, <b>?foo?</b> indicates the
   * variable <b>foo</b>, while <b>\?foo\?</b> is not a variable and will print
   * <tt>"?foo?"</tt> in the fully-qualified meta text.
   * 
   * @return the set of variables found in the passed text or the empty set if
   *         no variables are used.
   */
  private static Set<String> getVariablesHelper(@NonNull final String text) {
    final Set<String> variableSet = new HashSet<String>();
    final BufferedReader sr = new BufferedReader(new StringReader(text));
    try {
      String line;
      while ((line = sr.readLine()) != null) {
        int q1 = indexOfNonEscapedQuestionMark(line);
        while (q1 != -1) {
          int q2 = indexOfNonEscapedQuestionMark(line, q1 + 1);
          if (q2 != -1) {
            // there could be escaped question marks in the variable name (yuck)
            final String key = escapeQuestionMarks(line.substring(q1 + 1, q2));
            if (key != null && key.length() > 0) {
              variableSet.add(key);
            }
            q1 = indexOfNonEscapedQuestionMark(line, q2 + 1);
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
}
