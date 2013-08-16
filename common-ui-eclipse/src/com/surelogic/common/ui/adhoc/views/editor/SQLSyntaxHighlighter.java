package com.surelogic.common.ui.adhoc.views.editor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.surelogic.common.adhoc.AdHocQuery;

/**
 * This class performs color syntax highlighting of SQL statements.
 */
public class SQLSyntaxHighlighter implements LineStyleListener {

  private static final int NOT_FOUND = -1;

  private final Color f_commentColor;

  private final Color f_doubleQuoteColor;

  private final Color f_metaQuoteColor;

  private final Color f_keyWordColor;

  private final Color f_problemColor;

  private final Color f_questionColor;

  private final Color f_singleQuoteColor;

  public SQLSyntaxHighlighter(final Display display) {
    f_commentColor = display.getSystemColor(SWT.COLOR_BLUE);
    f_doubleQuoteColor = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    f_metaQuoteColor = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    f_keyWordColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    f_problemColor = display.getSystemColor(SWT.COLOR_RED);
    f_questionColor = display.getSystemColor(SWT.COLOR_BLUE);
    f_singleQuoteColor = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
  }

  private LineStyleEvent f_event;

  private ArrayList<StyleRange> f_result;

  @Override
  public void lineGetStyle(LineStyleEvent event) {
    f_event = event;
    f_result = new ArrayList<StyleRange>();
    int upToIndex = highlightComment(0);
    if (upToIndex != 0) {
      for (String word : SQL_RESERVED_WORDS) {
        highlightWord(word, upToIndex);
      }
      highlightQuotedText(0, upToIndex);
    }
    event.styles = f_result.toArray(new StyleRange[f_result.size()]);
    f_event = null;
    f_result = null;
  }

  /**
   * Highlights SQL comments.
   * 
   * @param fromIndex
   *          where to start looking for a comment.
   * @return index of where the comment begins or -1 for no comment.
   */
  private int highlightComment(int fromIndex) {
    if (fromIndex >= f_event.lineText.length())
      return -1;
    int ci = f_event.lineText.indexOf("--", fromIndex);
    if (ci == NOT_FOUND)
      return -1;
    if (inSQLQuote(ci))
      return highlightComment(ci + 2);
    else {
      // we want to highlight the meta
      boolean keepLooking = true;
      while (keepLooking) {
        keepLooking = false;
        // BEGIN-META(name)
        int metaBeginIndex = f_event.lineText.indexOf(AdHocQuery.META_BEGIN, ci);
        // END-META
        int metaEndIndex = f_event.lineText.indexOf(AdHocQuery.META_END, ci);

        /*
         * Did we find both (in which case we must consider order)
         */
        if (metaBeginIndex != -1 && metaEndIndex != -1) {
          if (metaBeginIndex < metaEndIndex) {
            // BEGIN-META(name)
            final int closeMetaName = f_event.lineText.indexOf(AdHocQuery.META_BEGIN_CLOSE, metaBeginIndex);
            if (closeMetaName != -1) {
              set(f_commentColor, SWT.NORMAL, ci, metaBeginIndex - 1);
              set(f_metaQuoteColor, SWT.BOLD, metaBeginIndex, closeMetaName);
              ci = closeMetaName + 1;
              keepLooking = true;
            }
            // END-META
            set(f_commentColor, SWT.NORMAL, ci, metaEndIndex - 1);
            final int endOfEndIndex = metaEndIndex + AdHocQuery.META_END.length() - 1;
            set(f_metaQuoteColor, SWT.BOLD, metaEndIndex, endOfEndIndex);
            ci = endOfEndIndex + 1;
            keepLooking = true;
          } else {
            // END-META
            set(f_commentColor, SWT.NORMAL, ci, metaEndIndex - 1);
            final int endOfEndIndex = metaEndIndex + AdHocQuery.META_END.length() - 1;
            set(f_metaQuoteColor, SWT.BOLD, metaEndIndex, endOfEndIndex);
            ci = endOfEndIndex + 1;
            keepLooking = true;
            // BEGIN-META(name)
            final int closeMetaName = f_event.lineText.indexOf(AdHocQuery.META_BEGIN_CLOSE, metaBeginIndex);
            if (closeMetaName != -1) {
              set(f_commentColor, SWT.NORMAL, ci, metaBeginIndex - 1);
              set(f_metaQuoteColor, SWT.BOLD, metaBeginIndex, closeMetaName);
              ci = closeMetaName + 1;
              keepLooking = true;
            }
          }
        } else if (metaBeginIndex != -1) { // only found a begin
          // BEGIN-META(name)
          final int closeMetaName = f_event.lineText.indexOf(AdHocQuery.META_BEGIN_CLOSE, metaBeginIndex);
          if (closeMetaName != -1) {
            set(f_commentColor, SWT.NORMAL, ci, metaBeginIndex - 1);
            set(f_metaQuoteColor, SWT.BOLD, metaBeginIndex, closeMetaName);
            ci = closeMetaName + 1;
            keepLooking = true;
          }
        } else if (metaEndIndex != -1) { // only found an end
          // END-META
          set(f_commentColor, SWT.NORMAL, ci, metaEndIndex - 1);
          final int endOfEndIndex = metaEndIndex + AdHocQuery.META_END.length() - 1;
          set(f_metaQuoteColor, SWT.BOLD, metaEndIndex, endOfEndIndex);
          ci = endOfEndIndex + 1;
          keepLooking = true;
        }
      }
      setToEndOfLine(ci, f_commentColor);
      return ci;
    }
  }

  private boolean inSQLQuote(int index) {
    return inQuote(index, "\"") || inQuote(index, "'");
  }

  private boolean inAnyQuote(int index) {
    return inQuote(index, "\"") || inQuote(index, "'") || inQuote(index, "?");
  }

  /**
   * Checks if a comment is in a quote by examining the line up to where the
   * '--' occurs.
   * 
   * @param index
   *          where '--' occurs.
   * @param quote
   *          the quoting string
   * @return {@code true} if in a quote, {@code false} otherwise.
   */
  private boolean inQuote(int index, String quote) {
    final String beforeComment = f_event.lineText.substring(0, index);
    int count = 0;
    int pos = 0;
    while (true) {
      int dq = beforeComment.indexOf(quote, pos);
      if (dq == NOT_FOUND)
        break;
      // check for two quotes in a row
      int dqNext = beforeComment.indexOf(quote, dq + 1);
      if (dqNext == dq + 1) {
        pos = dqNext + 1;
      } else {
        count++;
        pos = dq + 1;
      }
    }
    return !(count % 2 == 0);
  }

  /**
   * Quotes strings in double quotes, single quotes, and question marks. The
   * question marks indicate a parameter to the query. Quoted question marks may
   * be nested within single or double quotes.
   * <p>
   * This method is called recursively.
   * 
   * @param fromIndex
   *          the index to begin quote from.
   * @param upToIndex
   *          the index to stop quoting (a comment starts there).
   */
  private void highlightQuotedText(int fromIndex, int upToIndex) {
    if (upToIndex != -1 && fromIndex >= upToIndex)
      return;

    int dq = f_event.lineText.indexOf("\"", fromIndex);
    int sq = f_event.lineText.indexOf("'", fromIndex);
    int q = f_event.lineText.indexOf("?", fromIndex);
    if (dq == NOT_FOUND && sq == NOT_FOUND && q == NOT_FOUND)
      return;
    if (dq == NOT_FOUND)
      dq = Integer.MAX_VALUE;
    if (sq == NOT_FOUND)
      sq = Integer.MAX_VALUE;
    if (q == NOT_FOUND)
      q = Integer.MAX_VALUE;
    if (dq < sq && dq < q) {
      /*
       * Highlight the double quote
       */
      if (upToIndex != -1 && dq >= upToIndex)
        return;
      set(f_doubleQuoteColor, SWT.NORMAL, dq, dq);
      int afterQuote = finishQuote(dq + 1, f_doubleQuoteColor, "\"");
      if (afterQuote == NOT_FOUND)
        return;
      highlightQuotedText(afterQuote, upToIndex);
    } else if (sq < dq && sq < q) {
      /*
       * Highlight the single quote.
       */
      if (upToIndex != -1 && sq >= upToIndex)
        return;
      set(f_singleQuoteColor, SWT.NORMAL, sq, sq);
      int afterQuote = finishQuote(sq + 1, f_singleQuoteColor, "'");
      if (afterQuote == NOT_FOUND)
        return;
      highlightQuotedText(afterQuote, upToIndex);
    } else {
      /*
       * Highlight the question mark (query parameter).
       */
      if (upToIndex != -1 && q >= upToIndex)
        return;
      int afterQuestionMark = highlightQuestionMark(q);
      if (afterQuestionMark == NOT_FOUND)
        return;
      highlightQuotedText(afterQuestionMark, upToIndex);
    }
  }

  private int finishQuote(int fromIndex, final Color color, String endQuote) {
    int dq = f_event.lineText.indexOf(endQuote, fromIndex);
    int q = f_event.lineText.indexOf("?", fromIndex);
    if (dq == NOT_FOUND) {
      setToEndOfLine(fromIndex, f_problemColor);
      return NOT_FOUND;
    } else {
      if (q != NOT_FOUND && q < dq) {
        if (fromIndex <= q - 1)
          set(color, SWT.NORMAL, fromIndex, q - 1);
        int afterQuestionMark = highlightQuestionMark(q);
        if (afterQuestionMark == NOT_FOUND)
          return NOT_FOUND;
        return finishQuote(afterQuestionMark, color, endQuote);
      } else {
        set(color, SWT.NORMAL, fromIndex, dq);
        return dq + 1;
      }
    }
  }

  private int highlightQuestionMark(int fromIndex) {
    int q = f_event.lineText.indexOf("?", fromIndex + 1);
    if (q == NOT_FOUND) {
      setToEndOfLine(fromIndex, f_problemColor);
      return NOT_FOUND;
    } else {
      set(f_questionColor, SWT.BOLD, fromIndex, q);
      return q + 1;
    }
  }

  /**
   * Highlights the passed word in the line. The match is not case sensitive.
   * 
   * @param word
   *          a word.
   * @param upToIndex
   *          the index to stop quoting (a comment starts there).
   */
  private void highlightWord(final String word, int upToIndex) {
    final String lineText = f_event.lineText.toUpperCase();
    final int wordLength = word.length();
    int index = 0;
    while (true) {
      index = lineText.indexOf(word, index);
      if (index == NOT_FOUND)
        break;
      if (isWord(index, wordLength, lineText)) {
        final int beginIndex = index;
        final int endIndex = beginIndex + wordLength - 1;
        if (upToIndex != -1 && beginIndex >= upToIndex)
          return;
        if (!inAnyQuote(beginIndex)) {
          set(f_keyWordColor, SWT.BOLD, beginIndex, endIndex);
        }
      }
      index = index + wordLength;
    }
  }

  private boolean isWord(final int wordStartIndex, final int wordLength, final String lineText) {
    boolean start = wordStartIndex == 0;
    if (!start) {
      start = isWhiteSpaceOrPunc(lineText.charAt(wordStartIndex - 1));
    }
    if (!start)
      return false;
    int onePastEnd = wordStartIndex + wordLength;
    boolean end = onePastEnd >= lineText.length();
    if (!end) {
      end = isWhiteSpaceOrPunc(lineText.charAt(onePastEnd));
    }
    return end;
  }

  private boolean isWhiteSpaceOrPunc(final char c) {
    return c == ' ' || c == '\t' || c == ',' || c == '.' || c == '(' || c == ')';
  }

  private void set(Color color, int style, int beginIndex, int endIndex) {
    StyleRange sr = new StyleRange();
    sr.start = f_event.lineOffset + beginIndex;
    sr.length = (endIndex - beginIndex) + 1;
    sr.foreground = color;
    sr.fontStyle = style;
    if (!isNested(sr))
      f_result.add(sr);
  }

  private void setToEndOfLine(int beginIndex, Color color) {
    if (beginIndex >= f_event.lineText.length())
      return;
    StyleRange sr = new StyleRange();
    sr.start = f_event.lineOffset + beginIndex;
    sr.length = f_event.lineText.length() - beginIndex;
    sr.foreground = color;
    sr.fontStyle = SWT.NORMAL;
    if (!isNested(sr))
      f_result.add(sr);
  }

  private boolean isNested(StyleRange sr) {
    int srB = sr.start;
    int srE = sr.start + sr.length - 1;
    for (StyleRange esr : f_result) {
      int esrB = esr.start;
      int esrE = esr.start + esr.length - 1;
      boolean overlaps = (srB <= esrB && srE >= esrB) || (srB >= esrB && srE <= esrE) || (srB <= esrB && srE >= esrE)
          || (srB <= esrE && srE >= esrE);
      if (overlaps)
        return true;
    }
    return false;
  }

  static private final String[] SQL_RESERVED_WORDS = { "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC",
      "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BIT", "BOOLEAN", "BOTH", "BY", "CALL", "CASCADE",
      "CASCADED", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT",
      "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CURRENT",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
      "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP",
      "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "FALSE",
      "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GET", "GETCURRENTCONNECTION", "GLOBAL",
      "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INOUT",
      "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LAST", "LEFT",
      "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN", "MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NVARCHAR", "NEXT", "NO", "NOT",
      "NULL", "NULLIF", "NUMERIC", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD",
      "PARTIAL", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES",
      "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SELECT",
      "SESSION_USER", "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM",
      "SYSTEM_USER", "TABLE", "TEMPORARY", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRANSACTION", "TRANSLATE",
      "TRANSLATION", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USER", "USING", "VALUES", "VARCHAR", "VARYING",
      "VIEW", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "XML", "XMLEXISTS", "XMLPARSE", "XMLQUERY", "XMLSERIALIZE", "YEAR" };
}
