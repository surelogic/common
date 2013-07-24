package com.surelogic.common.adhoc.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.surelogic.Nullable;
import com.surelogic.common.Justification;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This class represents the data gathered from parsing a column annotation in
 * an SQL query.
 */
public final class ColumnAnnotation {

  private boolean f_isValid = true;

  public boolean isValid() {
    return f_isValid;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setIsValid(final boolean value) {
    f_isValid = value;
  }

  /**
   * A value of {@code null} indicates no icon.
   */
  private String f_iconName = null;

  /**
   * Gets the icon name or {@code null} if none.
   * 
   * @return the icon name or {@code null} if none.
   */
  public String getIconName() {
    return f_iconName;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   * 
   * @param value
   *          the icon named quoted with single quotes. The quotes are removed
   *          from the value.
   */
  public void setIconName(String value) {
    if (value != null) {
      value = stripSingleQuotes(value);
      if ("".equals(value)) {
        value = null;
      }
    }
    f_iconName = value;
  }

  /**
   * Indicates if the icon for this column should be displayed when the text in
   * a row is blank. {@code true} indicates that it should, {@code false} that
   * it should not.
   */
  private boolean f_showIconWhenEmpty = true;

  /**
   * Gets if the icon for this column should be displayed when the text in a row
   * is blank.
   * 
   * @return {@code true} indicates that the icon should be displayed when the
   *         text in a row is blank, {@code false} that that the icon should not
   *         be displayed when the text in a row is blank.
   */
  public boolean getShowIconWhenEmpty() {
    return f_showIconWhenEmpty;
  }

  /**
   * Sets if the icon for this column should be displayed when the text in a row
   * is blank.
   * 
   * @param value
   *          {@code true} indicates that the icon should be displayed when the
   *          text in a row is blank, {@code false} that that the icon should
   *          not be displayed when the text in a row is blank.
   */
  public void setShowIconWhenEmpty(final boolean value) {
    f_showIconWhenEmpty = value;
  }

  private Justification f_justification = Justification.LEFT;

  public Justification getJustification() {
    return f_justification;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setJusification(final Justification value) {
    if (value == null) {
      throw new IllegalArgumentException(I18N.err(44, "value"));
    }
    f_justification = value;
  }

  private boolean f_isLastTreeColumn = false;

  public boolean isLastTreeColumn() {
    return f_isLastTreeColumn;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setIsLastTreeColumn(final boolean value) {
    f_isLastTreeColumn = value;
  }

  private boolean f_isLastInitiallyVisible = false;

  public boolean isLastInitiallyVisible() {
    return f_isLastInitiallyVisible;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setLastInitiallyVisible(final boolean value) {
    f_isLastInitiallyVisible = value;
  }

  private boolean f_isHidden = false;

  public boolean isHidden() {
    return f_isHidden;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setIsHidden(final boolean value) {
    f_isHidden = value;
  }

  private boolean f_definesAnIconForAnotherColumn = false;

  public boolean definesAnIconForAnotherColumn() {
    return f_definesAnIconForAnotherColumn;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setDefinesAnIconForAnotherColumn(final boolean value) {
    f_definesAnIconForAnotherColumn = value;
  }

  private String f_blankIf = null;

  @Nullable
  public String getBlankIf() {
    return f_blankIf;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setBlankIf(String value) {
    if (value != null) {
      value = stripSingleQuotes(value);
      if ("".equals(value)) {
        value = null;
      }
    }
    f_blankIf = value;
  }

  private boolean f_sumPartialRows = false;

  public boolean sumPartialRows() {
    return f_sumPartialRows;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setSumPartialRows(final boolean value) {
    f_sumPartialRows = value;
  }

  private boolean f_countPartialRows = false;

  public boolean countPartialRows() {
    return f_countPartialRows;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setCountPartialRows(final boolean value) {
    f_countPartialRows = value;
  }

  private String f_countReplaceValueWith = null;

  public String getCountReplaceValueWith() {
    return f_countReplaceValueWith;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setCountReplaceValueWith(final String value) {
    f_countReplaceValueWith = stripSingleQuotes(value);
  }

  private boolean f_maxPartialRows = false;

  public boolean maxPartialRows() {
    return f_maxPartialRows;
  }

  public void setMaxPartialRows(final boolean value) {
    f_maxPartialRows = value;
  }

  private boolean f_countDistinct = false;

  public boolean countDistinct() {
    return f_countDistinct;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setCountDistinct(final boolean value) {
    f_countDistinct = value;
  }

  private final Set<Integer> f_onSet = new HashSet<Integer>();

  public Set<Integer> getOnSet() {
    return f_onSet;
  }

  /**
   * Checks if summary information should be displayed on a partial row with the
   * passed number of columns. This is checking the contents of the <tt>on</tt>
   * set.
   * <p>
   * If the <tt>on</tt> set is empty then this method always returns
   * {@code true} as no <tt>on</tt> clause means to display summary information
   * on all partial rows.
   * 
   * @param value
   *          the column count to see if summary information should be
   *          displayed.
   * @return {@code true} if summary information should be displayed,
   *         {@code false} if it should not.
   */
  public boolean onSetContains(final int value) {
    if (!f_onSet.isEmpty()) {
      return f_onSet.contains(value);
    } else {
      return true;
    }
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void addToOnSet(final String value) {
    try {
      final int intValue = Integer.parseInt(value);
      f_onSet.add(intValue);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException(I18N.err(131, value), e);
    }
  }

  private String f_prefix = "";

  public String getPrefix() {
    return f_prefix;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setPrefix(final String value) {
    f_prefix = stripSingleQuotes(value);
  }

  private boolean f_prefixOnlyIfNonempty = false;

  public boolean getPrefixOnlyIfNonempty() {
    return f_prefixOnlyIfNonempty;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setPrefixOnlyIfNonempty(final boolean value) {
    f_prefixOnlyIfNonempty = value;
  }

  @Nullable
  private String f_aggregatePrefix = null;

  /**
   * Gets the prefix for an aggregate if any was set, otherwise returns the
   * normal prefix from {@link #getPrefix()}.
   * 
   * @return a prefix.
   */
  public String getAggregatePrefix() {
    if (f_aggregatePrefix != null)
      return f_aggregatePrefix;
    else
      return getPrefix();
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setAggregatePrefix(final String value) {
    f_aggregatePrefix = stripSingleQuotes(value);
  }

  private String f_suffix = "";

  public String getSuffix() {
    return f_suffix;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setSuffix(final String value) {
    f_suffix = stripSingleQuotes(value);
  }

  private boolean f_suffixOnlyIfNonempty = false;

  public boolean getSuffixOnlyIfNonempty() {
    return f_suffixOnlyIfNonempty;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setSuffixOnlyIfNonempty(final boolean value) {
    f_suffixOnlyIfNonempty = value;
  }

  @Nullable
  private String f_aggregateSuffix = null;

  /**
   * Gets the suffix for an aggregate if any was set, otherwise returns the
   * normal suffix from {@link #getSuffix()}.
   * 
   * @return a suffix.
   */
  public String getAggregateSuffix() {
    if (f_aggregateSuffix != null)
      return f_aggregateSuffix;
    else
      return getSuffix();
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setAggregateSuffix(final String value) {
    f_aggregateSuffix = stripSingleQuotes(value);
  }

  private boolean f_addCommas = false;

  public boolean getAddCommas() {
    return f_addCommas;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setAddCommas(final boolean value) {
    f_addCommas = value;
  }

  private boolean f_humanReadableDuration = false;
  private TimeUnit f_humanReadableDurationUnit = TimeUnit.NANOSECONDS;

  public boolean getHumanReadableDuration() {
    return f_humanReadableDuration;
  }

  public TimeUnit getHumanReadableDurationUnit() {
    return f_humanReadableDurationUnit;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setHumanReadableDuration(final boolean value) {
    f_humanReadableDuration = value;
  }

  /**
   * Do not call this method. It should only be called by the parser.
   */
  public void setHumanReadableDurationUnit(String value) {
    if (value == null)
      return;
    value = stripSingleQuotes(value);
    try {
      f_humanReadableDurationUnit = TimeUnit.valueOf(value);
    } catch (Exception e) {
      SLLogger.getLogger().log(Level.WARNING,
          "Human readable duration unit '" + value + "' not recornised as a java.util.concurrent.TimeUnit enumeration value", e);
    }
  }

  private String stripSingleQuotes(String value) {
    if (value == null) {
      throw new IllegalArgumentException(I18N.err(44, "value"));
    }
    if (value.length() < 2 || !value.startsWith("'") || !value.endsWith("'")) {
      throw new IllegalArgumentException(I18N.err(130, value));
    }
    value = value.substring(1, value.length() - 1);
    return value;
  }

  @Override
  public String toString() {
    return "[ColumnAnnotation: isValid=" + f_isValid + " icon=" + f_iconName + " justification=" + f_justification
        + " isLastTreeColumn=" + f_isLastTreeColumn + " isLastInitiallyVisible=" + f_isLastInitiallyVisible + " isHidden="
        + f_isHidden + " definesAnIconForAnotherColumn=" + f_definesAnIconForAnotherColumn + " sumPartialRows=" + f_sumPartialRows
        + " countPartialRows=" + f_countPartialRows + " countDistinct=" + f_countDistinct + " onSet=" + f_onSet + " suffix="
        + f_suffix + "]";
  }
}
