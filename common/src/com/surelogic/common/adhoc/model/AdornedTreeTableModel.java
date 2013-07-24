package com.surelogic.common.adhoc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.surelogic.common.Justification;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * An IDE independent model of the result of a database query. The model can
 * represent a table, a tree, or a tree-table.
 * <p>
 * For a description of how the query is constructed to work with this class see
 * {@link #getInstance(String[], String[][])}.
 */
public final class AdornedTreeTableModel {

  /**
   * Constructs an adorned tree-table model that can be used to display the
   * passed tabular data.
   * <p>
   * It is a precondition of this method that for all <i>i</i> such that 0 &lt;=
   * <i>i</i> &lt; <code>rows.length</code>,
   * <code>rows[i].length == columnLabels.length</code> (i.e., that all rows
   * have the same number of columns).
   * 
   * @param columnLabels
   *          the column labels (with optional adornment information).
   * @param rows
   *          the text contents of the table. Some of the references in second
   *          dimension of the array may be <code>null</code>.
   * @return the constructed adorned tree-table model.
   * @throws IllegalArgumentException
   *           if any of the parameters is {@code null}.
   */
  public static AdornedTreeTableModel getInstance(final String[] columnLabels, final String[][] rows) throws Exception {
    if (columnLabels == null) {
      throw new IllegalArgumentException(I18N.err(44, "columnLabels"));
    }
    if (rows == null) {
      throw new IllegalArgumentException(I18N.err(44, "rows"));
    }

    boolean asTreeTable = false;
    int lastTreeIndex = NOT_FOUND;
    int lastTreeIndexInitiallyVisible = NOT_FOUND;

    /*
     * Create the adorned column labels by understanding the encoding in the
     * passed column labels. The resulting array should be less than or equal to
     * the length of the passed column label array.
     */
    final ColumnAnnotation[] columnAnnotationInfo = new ColumnAnnotation[columnLabels.length];
    final String[] fixedColumnImages = new String[columnLabels.length];
    final int[] definesImageFor = new int[columnLabels.length];
    Arrays.fill(definesImageFor, NOT_FOUND);
    final Justification[] columnJustification = new Justification[columnLabels.length];
    Arrays.fill(columnJustification, Justification.LEFT);
    final boolean[] isColumnVisible = new boolean[columnLabels.length];
    Arrays.fill(isColumnVisible, true);
    final List<String> cl = new ArrayList<String>();
    for (int i = 0; i < columnLabels.length; i++) {
      String columnLabel = columnLabels[i];
      boolean dividerFound = false;
      final boolean visiblityDividerFound = false;
      /*
       * If there is no "__" we don't need to examine this column.
       */
      final int breakIndex = columnLabel.indexOf(BREAK);
      if (breakIndex != NOT_FOUND) {
        final String annotation = columnLabel.substring(breakIndex + BREAK.length(), columnLabel.length());
        columnLabel = columnLabel.substring(0, breakIndex);
        final ColumnAnnotation info = ColumnAnnotationParserUtility.parse(annotation);
        columnAnnotationInfo[i] = info;

        /*
         * Check if the annotation portion is valid. If not just ignore it.
         */
        if (info.isValid()) {
          /*
           * Tree-table divider: "|"
           */
          if (info.isLastTreeColumn()) {
            dividerFound = true;
            asTreeTable = true;
            lastTreeIndex = cl.size();
          }
          /*
           * Tree-table column hint to open tree portion: "]"
           */
          if (info.isLastInitiallyVisible()) {
            lastTreeIndexInitiallyVisible = cl.size();
          }

          /*
           * Justification of this column.
           */
          columnJustification[i] = info.getJustification();

          if (info.definesAnIconForAnotherColumn()) {
            /*
             * We need to discover what column the contents this column are
             * defining icons for.
             */
            for (int j = 0; j < columnLabels.length; j++) {
              // skip this column
              if (j == i) {
                continue;
              }
              String targetColumnLabel = columnLabels[j];
              // Strip off any annotation for this target column
              final int targetBreakIndex = targetColumnLabel.indexOf(BREAK);
              if (targetBreakIndex != NOT_FOUND) {
                targetColumnLabel = targetColumnLabel.substring(0, targetBreakIndex);
              }
              if (targetColumnLabel.equals(columnLabel)) {
                definesImageFor[i] = j;
                break;
              }
            }
          } else if (info.isHidden()) {
            isColumnVisible[i] = false;
          } else {
            fixedColumnImages[i] = info.getIconName();
          }
        }
      }

      final boolean notAnImageDefinitionColumn = definesImageFor[i] == NOT_FOUND;
      if (notAnImageDefinitionColumn) {
        cl.add(columnLabel);
      } else {
        /*
         * If the "|" annotation to divide a tree-table was on a __ICON label,
         * i.e., "FOO__ICON|", then we need to move the last tree index back.
         */
        if (dividerFound) {
          lastTreeIndex--;
        }
        /*
         * And, if this action to the query put this as at the first label just
         * shut off the tree-table.
         */
        if (visiblityDividerFound) {
          lastTreeIndexInitiallyVisible--;
        }
      }
    }
    /*
     * The the divider and visibility divider to ensure that they make sense. If
     * the lastTreeIndex=0 this is just a table so we reset it. Thus, saying
     * 'select C "FOO|", ...' is a table not a tree-table.
     * 
     * We also need to check that the visibility divider column isn't more than
     * the last tree index column also if the visibility divider was not
     * specified then it should be the same as the divider column.
     */
    if (lastTreeIndex <= 0) {
      lastTreeIndex = NOT_FOUND;
      asTreeTable = false;
    }
    if (lastTreeIndexInitiallyVisible == NOT_FOUND || lastTreeIndexInitiallyVisible > lastTreeIndex) {
      lastTreeIndexInitiallyVisible = lastTreeIndex;
    }

    final String[] adornedColumnLabels = cl.toArray(new String[cl.size()]);
    final int adornedColumnCount = adornedColumnLabels.length;

    /*
     * Flag which adorned columns should be hidden and filter column
     * justification arrays.
     */
    final boolean[] adornedIsColumnVisible = new boolean[adornedColumnCount];
    final Justification[] adornedColumnJustification = new Justification[adornedColumnCount];
    final ColumnAnnotation[] adornedColumnAnnotationInfo = new ColumnAnnotation[adornedColumnCount];
    int adornedColI = 0;
    for (int colI = 0; colI < isColumnVisible.length; colI++) {
      final boolean notAnImageDefinitionColumn = definesImageFor[colI] == NOT_FOUND;
      if (notAnImageDefinitionColumn) {
        adornedColumnJustification[adornedColI] = columnJustification[colI];
        adornedIsColumnVisible[adornedColI] = isColumnVisible[colI];
        adornedColumnAnnotationInfo[adornedColI] = columnAnnotationInfo[colI];
        adornedColI++;
      }
    }
    /*
     * The tree portion of a tree-table cannot have a hidden column.
     */
    Arrays.fill(adornedIsColumnVisible, 0, lastTreeIndex + 1, true);

    /*
     * Move hidden fields to the far right of the result.
     */
    for (int colI = 0; colI < adornedIsColumnVisible.length; colI++) {
      if (!adornedIsColumnVisible[colI]) {
        for (int colJ = colI; colJ < adornedIsColumnVisible.length; colJ++) {
          if (adornedIsColumnVisible[colJ]) {
            SLLogger.getLogger().log(Level.WARNING, I18N.err(138));
            break;
          }
        }
        break;
      }
    }

    /*
     * Create the adorned rows filled with cells.
     */
    final Cell[][] adornedRows = new Cell[rows.length][];
    for (int rowI = 0; rowI < rows.length; rowI++) {
      final String[] row = rows[rowI];
      // allocate this row in the adorned result
      adornedRows[rowI] = new Cell[adornedColumnCount];
      adornedColI = 0;
      for (int colI = 0; colI < columnLabels.length; colI++) {
        final boolean notAnImageDefinitionColumn = definesImageFor[colI] == NOT_FOUND;
        String cellText;
        String cellImageSymbolicName;
        if (notAnImageDefinitionColumn) {
          cellText = row[colI];
          cellImageSymbolicName = fixedColumnImages[colI];
          if (cellImageSymbolicName == null) {
            int imageDefColI = NOT_FOUND;
            for (int imageColI = 0; imageColI < definesImageFor.length; imageColI++) {
              final int column = definesImageFor[imageColI];
              if (column == colI) {
                imageDefColI = imageColI;
                break;
              }
            }
            if (imageDefColI != NOT_FOUND) {
              cellImageSymbolicName = row[imageDefColI];
            }
          } else {
            // Do we need to null out the image name because the
            // text is blank?
            if (!adornedColumnAnnotationInfo[adornedColI].getShowIconWhenEmpty()) {
              if (cellText == null || "".equals(cellText)) {
                cellImageSymbolicName = null;
              }
            }
          }

          /*
           * Handle blanking, commas, prefix, and suffix if necessary
           */
          boolean blankText = false; // default
          final ColumnAnnotation colInfo = adornedColumnAnnotationInfo[adornedColI];
          if (colInfo != null) {
            if (cellText != null) {
              // Check if we should blank the text
              if (cellText.equals(colInfo.getBlankIf()))
                blankText = true;
              // Human readable duration (hrd)
              if (colInfo.getHumanReadableDuration()) {
                Pair<Long, String> breakout = safeParseLongAtStart(cellText);
                if (breakout != null) {
                  // the string begins with an integer
                  cellText = SLUtility.toStringDurationNS(breakout.first(), colInfo.getHumanReadableDurationUnit())
                      + breakout.second();
                }
              } else if (colInfo.getAddCommas()) { // incompatible with hrd
                // Comma-ize starting number (1000 -> 1,000 / 4544j -> 4,544j)
                Pair<Long, String> breakout = safeParseLongAtStart(cellText);
                if (breakout != null) {
                  // the string begins with an integer
                  cellText = SLUtility.toStringHumanWithCommas(breakout.first()) + breakout.second();
                }
              }
            }
            // Add prefix if necessary
            final String prefix = colInfo.getPrefix();
            if (!"".equals(prefix)) {
              cellText = (cellText == null ? prefix : prefix + cellText);
            }
            // add suffix if necessary
            final String suffix = colInfo.getSuffix();
            if (!"".equals(suffix)) {
              cellText = (cellText == null ? suffix : cellText + suffix);
            }
          }

          adornedRows[rowI][adornedColI] = new Cell(cellText, blankText, cellImageSymbolicName);
          adornedColI++;
        }
      }
    }

    /*
     * Put together the final adorned tree-table model.
     */
    final TreeCell[] modelTreePart;
    final String modelTreePartColumnLabel;
    if (asTreeTable) {
      final LinkedList<IndexedRowOfCells> rowsLeft = IndexedRowOfCells.toList(adornedRows);
      final List<TreeCell> root = new LinkedList<TreeCell>();
      recursiveTreeTableBuilder(root, null, 0, lastTreeIndex, rowsLeft);
      addNonLeafColumnSummaries(getAllNonLeaf(root), adornedColumnAnnotationInfo, lastTreeIndex, adornedRows);
      modelTreePart = root.toArray(new TreeCell[root.size()]);
      final boolean isPureTree = lastTreeIndex == adornedColumnCount - 1;
      if (isPureTree) {
        modelTreePartColumnLabel = makeSlashedTreeLabel(adornedColumnLabels);
      } else {
        final int treeColumnLabelSize = lastTreeIndex + 1;
        final String[] treeColumnLabels = Arrays.copyOfRange(adornedColumnLabels, 0, treeColumnLabelSize);
        modelTreePartColumnLabel = makeSlashedTreeLabel(treeColumnLabels);
      }
    } else {
      modelTreePart = null;
      modelTreePartColumnLabel = null;
    }
    final AdornedTreeTableModel model = new AdornedTreeTableModel(adornedRows, adornedColumnLabels, adornedColumnJustification,
        adornedIsColumnVisible, lastTreeIndex, lastTreeIndexInitiallyVisible, modelTreePart, modelTreePartColumnLabel);
    return model;
  }

  private static void recursiveTreeTableBuilder(final List<TreeCell> root, final NonLeafTreeCell parent, final int columnIndex,
      final int lastTreeIndex, final LinkedList<IndexedRowOfCells> rowsLeft) {
    if (columnIndex == lastTreeIndex) {
      for (final IndexedRowOfCells row : rowsLeft) {
        addLeaf(root, parent, row.getRow()[columnIndex], row.getRowIndex());
      }
    } else {
      while (!rowsLeft.isEmpty()) {
        final IndexedRowOfCells row = rowsLeft.remove();
        final LinkedList<IndexedRowOfCells> childrenOfRow = new LinkedList<IndexedRowOfCells>();
        childrenOfRow.add(row);
        final NonLeafTreeCell rowTreeCell = addNonLeaf(root, parent, row.getRow()[columnIndex]);
        for (final Iterator<IndexedRowOfCells> i = rowsLeft.iterator(); i.hasNext();) {
          final IndexedRowOfCells possibleChild = i.next();
          final String rowText = rowTreeCell.getText();
          final String possibleChildText = possibleChild.getRow()[columnIndex].getText();
          if (rowText != null) {
            if (rowText.equals(possibleChildText)) {
              i.remove();
              childrenOfRow.add(possibleChild);
            }
          } else if (possibleChildText == null) {
            i.remove();
            childrenOfRow.add(possibleChild);
          }
        }
        recursiveTreeTableBuilder(root, rowTreeCell, columnIndex + 1, lastTreeIndex, childrenOfRow);
      }
    }
  }

  private static NonLeafTreeCell addNonLeaf(final List<TreeCell> root, final NonLeafTreeCell parent, final Cell cell) {
    final NonLeafTreeCell result = new NonLeafTreeCell(cell);
    connectAddedTreeCell(root, parent, result);
    return result;
  }

  private static LeafTreeCell addLeaf(final List<TreeCell> root, final NonLeafTreeCell parent, final Cell cell, final int rowIndex) {
    final LeafTreeCell result = new LeafTreeCell(cell, rowIndex);
    connectAddedTreeCell(root, parent, result);
    return result;
  }

  private static void connectAddedTreeCell(final List<TreeCell> root, final NonLeafTreeCell parent, final TreeCell newCell) {
    if (parent == null) {
      // add the new cell to the root of the tree
      root.add(newCell);
    } else {
      // add the new cell as a child of the parent tree cell
      parent.addChild(newCell);
    }
  }

  private static String makeSlashedTreeLabel(final String[] columnLabels) {
    final StringBuilder b = new StringBuilder();
    boolean first = true;
    for (final String columnLabel : columnLabels) {
      if (first) {
        first = false;
      } else {
        b.append("/");
      }
      b.append(columnLabel);
    }
    return b.toString();
  }

  private static void addNonLeafColumnSummaries(final List<NonLeafTreeCell> nonLeafCells,
      final ColumnAnnotation[] adornedColumnAnnotationInfo, final int lastTreeIndex, final Cell[][] adornedRows) {
    final Set<Pair<Cell, String>> cellsToReplaceText = new HashSet<Pair<Cell, String>>();
    for (final NonLeafTreeCell cell : nonLeafCells) {
      for (int colI = lastTreeIndex; colI < adornedColumnAnnotationInfo.length; colI++) {
        final ColumnAnnotation info = adornedColumnAnnotationInfo[colI];
        if (info != null) {
          if (info.sumPartialRows()) {
            /*
             * SUM
             */
            if (info.onSetContains(cell.filledColumnCount())) {
              long summaryTotal = 0;
              for (final LeafTreeCell leaf : cell.getLeaves()) {
                final String contents = adornedRows[leaf.getRowIndex()][colI].getText();
                final long value = safeParseLong(contents);
                summaryTotal += value;
              }
              String cellText = Long.toString(summaryTotal);
              final boolean blankText = cellText.equals(info.getBlankIf());
              if (info.getHumanReadableDuration()) {
                // the string begins with an integer
                cellText = SLUtility.toStringDurationNS(summaryTotal, info.getHumanReadableDurationUnit());
              } else if (info.getAddCommas()) {
                cellText = SLUtility.toStringHumanWithCommas(summaryTotal);
              }
              final String text = info.getAggregatePrefix() + cellText + info.getAggregateSuffix();
              final NonLeafColumnSummaryCell columnSummary = new NonLeafColumnSummaryCell(text, blankText, colI);
              cell.addColumnSummary(columnSummary);
            }
          } else if (info.maxPartialRows()) {
            /*
             * MAX
             */
            if (info.onSetContains(cell.filledColumnCount())) {
              long runningMax = 0;
              for (final LeafTreeCell leaf : cell.getLeaves()) {
                final String contents = adornedRows[leaf.getRowIndex()][colI].getText();
                final long value = safeParseLong(contents);
                runningMax = Math.max(runningMax, value);
              }
              String cellText = Long.toString(runningMax);
              final boolean blankText = cellText.equals(info.getBlankIf());
              if (info.getHumanReadableDuration()) {
                // the string begins with an integer
                cellText = SLUtility.toStringDurationNS(runningMax, info.getHumanReadableDurationUnit());
              } else if (info.getAddCommas()) {
                cellText = SLUtility.toStringHumanWithCommas(runningMax);
              }
              final String text = info.getAggregatePrefix() + cellText + info.getAggregateSuffix();
              final NonLeafColumnSummaryCell columnSummary = new NonLeafColumnSummaryCell(text, blankText, colI);
              cell.addColumnSummary(columnSummary);
            }

          } else if (info.countPartialRows()) {
            /*
             * COUNT (DISTINCT)
             */
            if (info.onSetContains(cell.filledColumnCount())) {
              final boolean distinct = info.countDistinct();
              final String replaceValueWith = info.getCountReplaceValueWith();
              final Set<String> distinctFound = new HashSet<String>();
              int countTotal = 0;
              for (final LeafTreeCell leaf : cell.getLeaves()) {
                final Cell adornedCell = adornedRows[leaf.getRowIndex()][colI];
                if (distinct) {
                  final String contents = adornedCell.getText();
                  if (distinctFound.add(contents)) {
                    countTotal++;
                  }
                } else {
                  countTotal++;
                }
                if (replaceValueWith != null) {
                  cellsToReplaceText.add(new Pair<Cell, String>(adornedCell, replaceValueWith));
                }
              }
              final String simpleText = Long.toString(countTotal);
              final boolean blankText = simpleText.equals(info.getBlankIf());
              final String text = info.getAggregatePrefix()
                  + (info.getAddCommas() ? SLUtility.toStringHumanWithCommas(countTotal) : simpleText) + info.getAggregateSuffix();
              final NonLeafColumnSummaryCell columnSummary = new NonLeafColumnSummaryCell(text, blankText, colI);
              cell.addColumnSummary(columnSummary);
            }
          }
        }
      }
    }
    for (final Pair<Cell, String> pair : cellsToReplaceText)
      pair.first().setText(pair.second());
  }

  /**
   * This method converts a string to a long but it ignores non-numeric
   * suffices. For example, invoking {@code safeParseLong("40 ns")} would result
   * in 40 (i.e., not an error). Also commas are skipped so
   * {@code safeParseLong("47,340 ns")} would result in 47340 (i.e., not an
   * error).
   * 
   * @param value
   *          the string to convert.
   * @return the resulting long value. If the value is entirely non-numeric the
   *         result will be 0.
   */
  private static long safeParseLong(String value) {
    value = value.trim();
    long result = 0;
    if (value != null) {
      for (int i = 0; i < value.length(); i++) {
        final char ch = value.charAt(i);
        if (ch != ',') { // skip commas
          final long digit = ch - '0';
          final boolean isNumeric = 0 <= digit && digit <= 9;
          if (isNumeric) {
            result = (result * 10) + digit;
          } else {
            return result;
          }
        }
      }
    }
    return result;
  }

  /**
   * Breaks a string into a long for the starting integer and the rest of the
   * string.
   * 
   * @param value
   *          a string value
   * @return a pair containing the starting number followed by the remaining
   *         string or {@code null} if the passed value does not being with a
   *         number.
   */
  private static Pair<Long, String> safeParseLongAtStart(String value) {
    if (value == null)
      return null;

    boolean startsWithANumber = false;
    long result = 0;
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      final long digit = ch - '0';
      final boolean isNumeric = 0 <= digit && digit <= 9;
      if (isNumeric) {
        result = (result * 10) + digit;
        startsWithANumber = true;
      } else {
        if (startsWithANumber)
          return new Pair<Long, String>(result, value.substring(i));
        else
          return null;
      }
    }
    if (startsWithANumber)
      return new Pair<Long, String>(result, "");
    else
      return null;
  }

  private static List<NonLeafTreeCell> getAllNonLeaf(final List<TreeCell> root) {
    final List<NonLeafTreeCell> result = new ArrayList<NonLeafTreeCell>();
    for (final TreeCell cell : root) {
      if (cell instanceof NonLeafTreeCell) {
        getAllNonLeafHelper((NonLeafTreeCell) cell, result);
      }
    }
    return result;
  }

  private static void getAllNonLeafHelper(final NonLeafTreeCell cell, final List<NonLeafTreeCell> mutableResult) {
    mutableResult.add(cell);
    for (final TreeCell child : cell.getChildren()) {
      if (child instanceof NonLeafTreeCell) {
        getAllNonLeafHelper((NonLeafTreeCell) child, mutableResult);
      }
    }
  }

  private static final int NOT_FOUND = -1;
  private static final String BREAK = "__";
  private static final String IMG_VARIABLE_SUFFIX = " (Image)";

  private AdornedTreeTableModel(final Cell[][] rows, final String[] columnLabels, final Justification[] columnJustification,
      final boolean[] isColumnVisible, final int lastTreeIndex, final int lastTreeIndexInitiallyVisible, final TreeCell[] treePart,
      final String treePartColumnLabel) {
    if (rows == null) {
      throw new IllegalArgumentException(I18N.err(44, "rows"));
    }
    f_rows = rows;
    if (columnLabels == null) {
      throw new IllegalArgumentException(I18N.err(44, "columnLabels"));
    }
    f_columnLabels = columnLabels;
    if (columnJustification == null) {
      throw new IllegalArgumentException(I18N.err(44, "columnJustification"));
    }
    f_columnJustification = columnJustification;
    if (isColumnVisible == null) {
      throw new IllegalArgumentException(I18N.err(44, "isColumnVisible"));
    }
    f_isColumnVisible = isColumnVisible;
    f_lastTreeIndex = lastTreeIndex;
    f_lastTreeIndexInitiallyVisible = lastTreeIndexInitiallyVisible;
    f_treePart = treePart;
    f_treePartColumnLabel = treePartColumnLabel;
    checkModel();
  }

  /**
   * Runs lots of structural checks on this model to ensure that it is at least
   * well-formed.
   */
  private void checkModel() {
    /*
     * Check the model integrity. This should save us a lot of grief by being
     * fail fast if the code in getInstance(...) bugs.
     */
    if (f_columnLabels.length != f_isColumnVisible.length) {
      throw new IllegalArgumentException("f_columnLabels.length=" + f_columnLabels.length + " must equal f_isColumnVisible.length="
          + f_isColumnVisible.length + ".");
    }
    if (f_columnLabels.length != f_columnJustification.length) {
      throw new IllegalArgumentException("f_columnLabels.length=" + f_columnLabels.length
          + " must equal f_columnJustification.length=" + f_columnJustification.length + ".");
    }
    for (int rowI = 0; rowI < f_rows.length; rowI++) {
      if (f_rows[rowI].length != f_isColumnVisible.length) {
        throw new IllegalArgumentException("f_isColumnVisible.length=" + f_isColumnVisible.length
            + " must be the same as f_rows[x=" + rowI + "].length=" + f_rows[rowI].length + " for all rows x.");
      }
    }
    if (f_treePart != null) {
      if (f_lastTreeIndex < 0) {
        throw new IllegalArgumentException("f_lastTreeIndex=" + f_lastTreeIndex
            + " must be nonnegative when f_treePart is non-null.");
      }
      if (f_treePartColumnLabel == null) {
        throw new IllegalArgumentException("f_treePartColumnLabel cannot be null when f_treePart is non-null.");
      }

    }
    if (f_treePart == null) {
      if (f_lastTreeIndex != -1) {
        throw new IllegalArgumentException("f_lastTreeIndex=" + f_lastTreeIndex + " must be -1 when f_treePart is null.");
      }
    }
  }

  private final Cell[][] f_rows;
  private final String[] f_columnLabels;
  private final Justification[] f_columnJustification;
  private final boolean[] f_isColumnVisible;
  private final int f_lastTreeIndex;
  private final int f_lastTreeIndexInitiallyVisible;
  private final TreeCell[] f_treePart;
  private final String f_treePartColumnLabel;

  /**
   * Indicates if this model represents a table with no tree part.
   * 
   * @return {@code true} if the model represents a table, {@code false}
   *         otherwise.
   */
  public boolean isPureTable() {
    return f_lastTreeIndex == -1;
  }

  /**
   * Indicates if this model represents a tree with no table part.
   * 
   * @return {@code true} if the model represents a tree, {@code false}
   *         otherwise.
   */
  public boolean isPureTree() {
    return f_lastTreeIndex == getColumnCount() - 1;
  }

  /**
   * Indicates if this model represents a tree-table. The result of this method
   * on a model, {@code m}, is the same as invoking
   * 
   * <pre>
   * !m.isPureTable() &amp;&amp; !m.isPureTree()
   * </pre>
   * 
   * @return {@code true} if the model represents a tree-table, {@code false}
   *         otherwise.
   */
  public boolean isTreeTable() {
    return !isPureTable() && !isPureTree();
  }

  /**
   * Gets the number of rows in this model.
   * 
   * @return the number of rows in this model.
   */
  public int getRowCount() {
    return f_rows.length;
  }

  /**
   * Gets a table representation of this model. The table representation
   * includes the tree part represented as a table. To determine the last column
   * used in the tree part invoke the {@link #getLastTreeIndex()} method.
   * 
   * @return a table representation of this model.
   */
  public Cell[][] getRows() {
    return f_rows;
  }

  /**
   * Gets the number of columns in the table representation of this model.
   * 
   * @return the number of columns in the table representation of this model.
   */
  public int getColumnCount() {
    return f_columnLabels.length;
  }

  /**
   * Gets the column labels for table representation of this model.
   * 
   * @return the column labels for table representation of this model.
   */
  public String[] getColumnLabels() {
    return f_columnLabels;
  }

  /**
   * Checks if the column at the passed index into the table representation of
   * this model should be visible in the user interface. Columns that are not
   * visible are used to simply define a variable for sub-queries.
   * <p>
   * No column used for the tree part of this model can be marked not visible.
   * 
   * @param columnIndex
   *          a column index within the table representation of this model.
   * @return {@code true} if the column should be visible in the user interface,
   *         {@code false} otherwise.
   */
  public boolean isColumnVisible(final int columnIndex) {
    return f_isColumnVisible[columnIndex];
  }

  /**
   * Returns an array of flags indicating what columns in the table
   * representation of this model should be visible in the user interface. The
   * returned array should not be mutated. Columns that are not visible are used
   * to simply define a variable for sub-queries.
   * <p>
   * No column used for the tree part of this model can be marked not visible.
   * 
   * @return an array of flags indicating what columns in the table
   *         representation of this model should be visible in the user
   *         interface. The returned array should not be mutated.
   */
  public boolean[] getIsColumnVisible() {
    return f_isColumnVisible;
  }

  /**
   * Gets the justification of the specified column index.
   * 
   * @param columnIndex
   *          a column index.
   * @return the justification of the specified column index.
   */
  public Justification getColumnJustification(final int columnIndex) {
    return f_columnJustification[columnIndex];
  }

  /**
   * Gets the index of last column in the table representation of this model
   * that is used to define the tree part of this model.
   * 
   * @return the index of the last column in the table representation of this
   *         model that is used to define the tree part of this model. A value
   *         of <tt>-1</tt> indicates that this model has no tree part.
   */
  public int getLastTreeIndex() {
    return f_lastTreeIndex;
  }

  /**
   * Gets the index of the last column in the table representation of this model
   * that should be seen when the tree-table is shown in the user interface.
   * <p>
   * This is a hint from the query about how the tree should initially be
   * expanded when it is first displayed.
   * 
   * @return the index of the last column in the table representation of this
   *         model that should be seen when the tree-table is shown in the user
   *         interface. A value of <tt>-1</tt> indicates that this model has no
   *         tree part.
   */
  public int getLastTreeIndexInitiallyVisible() {
    return f_lastTreeIndexInitiallyVisible;
  }

  /**
   * Gets the tree portion of this model.
   * 
   * @return the tree portion of this model or {@code null} if the model is a
   *         pure table.
   */
  public TreeCell[] getTreePart() {
    return f_treePart;
  }

  /**
   * Gets the column label for the tree portion of this model.
   * <p>
   * Labels for the tree parts are made up by taking the column labels and
   * adding a "/" between them. For example if the tree part contained the
   * columns <tt>Project</tt>, <tt>Package</tt>, and <tt>Class</tt> the label
   * for the tree part (or the whole tree if no table part existed) would be
   * <tt>"Project/Package/Class"</tt>.
   * 
   * @return the column label for the tree portion of this model.
   */
  public String getTreePartColumnLabel() {
    return f_treePartColumnLabel;
  }

  /**
   * Gets the set of variables at a given row in this model.
   * <p>
   * This method filters out all variables in the selected row that are
   * {@code null}, the empty string, or whitespace.
   * 
   * @param rowIndex
   *          a row in this model.
   * @return the set of variables.
   */
  public Map<String, String> getVariablesFor(final int rowIndex) {
    final Map<String, String> result = new HashMap<String, String>();
    final Cell[] row = f_rows[rowIndex];
    for (int colI = 0; colI < row.length; colI++) {
      final String key = f_columnLabels[colI];
      final Cell cell = row[colI];
      final String value = cell.getText();
      final String imgValue = cell.getImageSymbolicName();
      if (key != null) {
        result.put(key, value);
        if (imgValue != null) {
          result.put(key + IMG_VARIABLE_SUFFIX, imgValue);
        }
      }
    }
    SLUtility.removeEmptyStringValuesFromMap(result, true);
    return result;
  }

  /**
   * Gets the set of variables for the partial row defined by the passed
   * non-leaf tree cell. This is done by examining all the leaf cells below the
   * passed cell and defining variables for all columns where the value is
   * invariant.
   * <p>
   * This method filters out all variables in the selected row that are
   * {@code null}, the empty string, or whitespace.
   * 
   * @param cell
   *          a non-leaf tree cell.
   * @return the set of variables for the partial row defined by the passed
   *         non-leaf tree cell.
   */
  public Map<String, String> getVariablesFor(final NonLeafTreeCell cell) {
    if (cell == null) {
      throw new IllegalArgumentException(I18N.err(44, "cell"));
    }

    final Map<String, String> result = new HashMap<String, String>();
    final Set<LeafTreeCell> leaves = cell.getLeaves();
    if (leaves.isEmpty()) {
      return result; // empty
    }

    final int columnCount = getColumnCount();

    final String[] values = new String[columnCount];
    final String[] imgValues = new String[columnCount];
    Arrays.fill(values, null);
    boolean first = true;

    /*
     * Keep values that are invariant; null out those that change.
     */
    for (final LeafTreeCell leaf : leaves) {
      if (first) {
        first = false;
        for (int colI = 0; colI < columnCount; colI++) {
          final Cell c = f_rows[leaf.getRowIndex()][colI];
          values[colI] = c.getText();
          imgValues[colI] = c.getImageSymbolicName();
        }
      } else {
        for (int colI = 0; colI < columnCount; colI++) {
          if (values[colI] != null) {
            final String rowValue = f_rows[leaf.getRowIndex()][colI].getText();
            if (!values[colI].equals(rowValue)) {
              values[colI] = null;
            }
          }
          if (imgValues[colI] != null) {
            final String imgValue = f_rows[leaf.getRowIndex()][colI].getImageSymbolicName();
            if (!imgValues[colI].equals(imgValue)) {
              imgValues[colI] = null;
            }
          }
        }
      }
    }

    for (int colI = 0; colI < columnCount; colI++) {
      final String key = f_columnLabels[colI];
      final String value = values[colI];
      final String imgValue = imgValues[colI];
      if (key != null && value != null) {
        result.put(key, value);
      }
      if (key != null && imgValue != null) {
        result.put(key + IMG_VARIABLE_SUFFIX, imgValue);
      }
    }

    SLUtility.removeEmptyStringValuesFromMap(result, true);
    return result;
  }
}
