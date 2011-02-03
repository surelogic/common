package com.surelogic.common.ui;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.jdesktop.swt.animation.timing.Animator;
import org.jdesktop.swt.animation.timing.TimingTargetAdapter;

import com.surelogic.common.logging.SLLogger;

/**
 * A composite specialized to provide a cascading list. It works like the Mac
 * Finder. This is <i>mostly</i> a general purpose control.
 * <p>
 * When a column is added to this cascading list and the cascading list is not
 * wide enough to display it it is animated into view.
 */
public class CascadingList extends ScrolledComposite {

	private final Composite f_contents;

	private final Listener f_resizeAction = new Listener() {
		public void handleEvent(Event event) {
			/*
			 * This action is the same regardless if the overall control size
			 * changed or just the size of one of the column contents.
			 */
			rememberColumnViewportOrigins();
			fixupSizeOfContents();
		}
	};

	public CascadingList(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL | SWT.NO_FOCUS);
		f_contents = new Composite(this, SWT.NO_FOCUS);
		setContent(f_contents);

		setExpandHorizontal(true);
		setAlwaysShowScrollBars(false);

		addListener(SWT.Resize, f_resizeAction);
		f_contents.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_DARK_GRAY));
	}

	public Color getContentsBackground() {
		return f_contents.getBackground();
	}

	/**
	 * The ordered list of columns currently displayed by this cascading list.
	 */
	private final List<Composite> f_columns = new LinkedList<Composite>();

	private final Set<Composite> f_noSpaceBeforeColumns = new HashSet<Composite>();

	/**
	 * Sets the application defined widget data associated with the column to be
	 * the argument. This can be used to associate model information with a
	 * column in this cascading list.
	 * 
	 * @param columnIndex
	 *            a column index in this cascading list.
	 * @param data
	 *            the column data.
	 */
	public void setColumnData(int columnIndex, Object data) {
		f_columns.get(columnIndex).setData(data);
	}

	/**
	 * Returns the application defined widget data associated with the column,
	 * or <code>null</code> if it has not been set.
	 * 
	 * @param columnIndex
	 *            a column index in this cascading list.
	 * @return the data associated with the column, or <code>null</code>.
	 */
	public Object getColumnData(int columnIndex) {
		return f_columns.get(columnIndex).getData();
	}

	/**
	 * Gets returns the column index that the passed control exists within or -1
	 * if the control is not found within this cascading list. The passed
	 * control simply has to be part of the widget tree managed by a column in
	 * this control, it doesn't have to be the root.
	 * 
	 * @param c
	 *            a control.
	 * @return the index of the column that <code>c</code> exists within, or
	 *         -1 if <code>c</code> is not within a column of this cascading
	 *         list.
	 */
	public int getColumnIndexOf(Control c) {
		if (c instanceof Composite) {
			Composite sc = (Composite) c;
			final int index = f_columns.indexOf(sc);
			/*
			 * Is this a scrolled composite that this cascading list created?
			 */
			if (index != -1)
				return index;
		} else {
			/*
			 * Have we gone all the way up and failed to find a column?
			 */
			if (c == null)
				return -1;
		}
		return getColumnIndexOf(c.getParent());
	}

	/**
	 * Implemented by objects that contribute a column to this cascading list.
	 * 
	 * @see CascadingList#addColumn(IColumn);
	 * @see CascadingList#addColumnAfter(IScrolledColumn, int)
	 */
	static public interface IScrolledColumn {
		void createContents(Composite panel);
	}

	static public interface IColumn {
		Composite createContents(Composite cascadingListContents);
	}

	/**
	 * Adds a column to this cascading list that self-scrolls. For example, a
	 * table or a tree.
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @param noSpaceBefore
	 *            <code>true</code> if no horizontal padding should be placed
	 *            between the previous column and this one, <code>false</code>
	 *            if padding is desired. This would be used to make two columns
	 *            look like a single column.
	 * 
	 * @return the index in this cascading list that the new column was added
	 *         at.
	 * @throws IllegalStateException
	 *             if something goes wrong while adding the column.
	 */
	public int addColumn(IColumn column, boolean noSpaceBefore) {
		try {
			rememberColumnViewportOrigins();

			final Composite newColumn = column.createContents(f_contents);
			newColumn.addListener(SWT.Resize, f_resizeAction);

			f_columns.add(newColumn);
			if (noSpaceBefore)
				f_noSpaceBeforeColumns.add(newColumn);
			fixupSizeOfContents();
			notifyObservers();
			animateStartOfLastColumn();
			return f_columns.indexOf(newColumn);
		} catch (Exception e) {
			/*
			 * Not much we can do, however, log the exception.
			 */
			final String msg = "General failure creating the contents of"
					+ " a column of a cascading list";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	/**
	 * Adds a column to this cascading list that self-scrolls (e.g., a table or
	 * a tree) after the specified column index. Any and all existing columns
	 * with an index after the specified column index are removed from this
	 * cascading list before the new column is added.
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @param columnIndex
	 *            the index after which the new column should be placed. A value
	 *            of -1 will clear out all columns.
	 * @param noSpaceBefore
	 *            <code>true</code> if no horizontal padding should be placed
	 *            between the previous column and this one, <code>false</code>
	 *            if padding is desired. This would be used to make two columns
	 *            look like a single column.
	 * 
	 * @return the index in this cascading list that the new column was added
	 *         at.
	 * @throws IllegalStateException
	 *             if something goes wrong while adding the column.
	 */
	public int addColumnAfter(IColumn column, int columnIndex,
			boolean noSpaceBefore) {
		emptyAfterHelper(columnIndex);
		return addColumn(column, noSpaceBefore);
	}

	/**
	 * Adds a column to this cascading list that is scrolled by the list (not by
	 * itself).
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @param noSpaceBefore
	 *            <code>true</code> if no horizontal padding should be placed
	 *            between the previous column and this one, <code>false</code>
	 *            if padding is desired. This would be used to make two columns
	 *            look like a single column.
	 * 
	 * @return the index in this cascading list that the new column was added
	 *         at.
	 * @throws IllegalStateException
	 *             if something goes wrong while adding the column.
	 */
	public int addColumn(IScrolledColumn column, boolean noSpaceBefore) {
		try {
			rememberColumnViewportOrigins();
			final ScrolledComposite columnViewport = new ScrolledComposite(
					f_contents, SWT.V_SCROLL);
			final Composite columnContents = new Composite(columnViewport,
					SWT.NONE);
			columnContents.setLayout(new FillLayout());

			column.createContents(columnContents);
			columnContents.addListener(SWT.Resize, f_resizeAction);

			columnViewport.setContent(columnContents);
			columnViewport.setAlwaysShowScrollBars(false);
			columnContents.pack();

			f_columns.add(columnViewport);
			if (noSpaceBefore)
				f_noSpaceBeforeColumns.add(columnViewport);
			fixupSizeOfContents();
			notifyObservers();
			animateStartOfLastColumn();
			return f_columns.indexOf(columnViewport);
		} catch (Exception e) {
			/*
			 * Not much we can do, however, log the exception.
			 */
			final String msg = "General failure creating the contents of"
					+ " a column of a cascading list";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	/**
	 * Adds a column to this cascading list that is scrolled by the list (not by
	 * itself) after the specified column index. Any and all existing columns
	 * with an index after the specified column index are removed from this
	 * cascading list before the new column is added.
	 * 
	 * @param column
	 *            the object that will be invoked to construct the column.
	 * @param columnIndex
	 *            the index after which the new column should be placed. A value
	 *            of -1 will clear out all columns.
	 * @param noSpaceBefore
	 *            <code>true</code> if no horizontal padding should be placed
	 *            between the previous column and this one, <code>false</code>
	 *            if padding is desired. This would be used to make two columns
	 *            look like a single column.
	 * 
	 * @return the index in this cascading list that the new column was added
	 *         at.
	 * @throws IllegalStateException
	 *             if something goes wrong while adding the column.
	 */
	public int addColumnAfter(IScrolledColumn column, int columnIndex,
			boolean noSpaceBefore) {
		emptyAfterHelper(columnIndex);
		return addColumn(column, noSpaceBefore);
	}

	/**
	 * Removes all existing columns from this cascading list.
	 * 
	 * @see #emptyAfter(int)
	 */
	public void empty() {
		emptyAfter(-1);
	}

	/**
	 * Removes all existing columns from this cascading list with an index after
	 * the specified column index.
	 * <p>
	 * Note: Clients should not invoke this method followed by
	 * {@link #addColumn(CascadingList.IScrolledColumn)} as this could cause a
	 * strange animation in several cases. Instead invoke
	 * {@link #addColumnAfter(com.surelogic.common.ui.CascadingList.IColumn, int, boolean)}
	 * which combines the two operations and produces the correct animation.
	 * 
	 * @param columnIndex
	 *            a column index that is a column of this cascading list. A
	 *            value of -1 will clear out all columns.
	 */
	public void emptyAfter(int columnIndex) {
		if (columnIndex >= f_columns.size())
			return;
		rememberColumnViewportOrigins();
		emptyAfterHelper(columnIndex);
		fixupSizeOfContents();
		notifyObservers();
	}

	public void emptyFrom(int columnIndex) {
		emptyAfter(columnIndex - 1);
	}

	public void show(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < f_columns.size())
			animateStartOfColumn(columnIndex);
	}

	public int getNumColumns() {
	  return f_columns.size();
	}
	
	public interface ICascadingListObserver {
		void notify(CascadingList cascadingList);
	}

	private final Set<ICascadingListObserver> f_observers = new CopyOnWriteArraySet<ICascadingListObserver>();

	public void addObserver(final ICascadingListObserver o) {
		if (o == null)
			return;
		f_observers.add(o);
	}

	public void removeObserver(final ICascadingListObserver o) {
		f_observers.remove(o);
	}

	private void notifyObservers() {
		for (ICascadingListObserver o : f_observers)
			o.notify(this);
	}

	/**
	 * A helper method to dump columns in the cascading list after the passed
	 * column index.
	 * 
	 * @param columnIndex
	 *            a column index that is a column of this cascading list.
	 * 
	 * @see #addColumnAfter(CascadingList.IScrolledColumn, int)
	 * @see #emptyAfter(int)
	 */
	private void emptyAfterHelper(int columnIndex) {
		int index = 0;
		for (Iterator<Composite> iterator = f_columns.iterator(); iterator
				.hasNext();) {
			Composite column = iterator.next();
			if (index > columnIndex) {
				iterator.remove();
				if (column instanceof ScrolledComposite) {
					final ScrolledComposite columnViewport = (ScrolledComposite) column;
					forgetColumnViewportOriginFor(columnViewport);
				}
				column.dispose();
			}
			index++;
		}
		f_noSpaceBeforeColumns.retainAll(f_columns);
	}

	/**
	 * This map remembers the vertical scroll bar positions for all of the
	 * columns in this viewer. It ensures that all the vertical scrollbars don't
	 * jump to the top each time the finer is manipulated.
	 */
	private final Map<ScrolledComposite, Point> f_columnViewportToOrigin = new HashMap<ScrolledComposite, Point>();

	private void rememberColumnViewportOrigins() {
		f_columnViewportToOrigin.clear();
		for (Composite column : f_columns) {
			if (column instanceof ScrolledComposite) {
				final ScrolledComposite columnViewport = (ScrolledComposite) column;
				final Point origin = columnViewport.getOrigin();
				f_columnViewportToOrigin.put(columnViewport, origin);
			}
		}
	}

	private void forgetColumnViewportOriginFor(ScrolledComposite columnViewport) {
		f_columnViewportToOrigin.remove(columnViewport);
	}

	/**
	 * The width in pixels around the cascading list contents.
	 */
	private static final int BORDER = 2;

	/**
	 * The vertical space between columns.
	 */
	private static final int PADDING = 2;

	/**
	 * A guess as to the width of a scroll bar if we can't determine what it
	 * actually is.
	 */
	private static final int SCROLL_BAR_WIDTH_DEFAULT = 15;

	/**
	 * We directly manage the layout of the contents of the cascading list. This
	 * method does the bulk of the work.
	 */
	private void fixupSizeOfContents() {
		Rectangle viewportSize = getClientArea();
		final int columnViewportHeight = viewportSize.height - (2 * BORDER);
		int xPos = BORDER;
		int xPosStartOfLast = xPos;
		for (Composite column : f_columns) {
			xPosStartOfLast = xPos;
			if (f_noSpaceBeforeColumns.contains(column) && xPos != BORDER)
				xPos -= PADDING;
			if (column instanceof ScrolledComposite) {
				/*
				 * We setup a scrolled composite for this column.
				 */
				final ScrolledComposite columnViewport = (ScrolledComposite) column;
				final Control columnContents = columnViewport.getContent();
				final Point pColumnContentsSize = columnContents.computeSize(
						SWT.DEFAULT, SWT.DEFAULT);
				int scrollBarWidth = 0;
				if (pColumnContentsSize.y > columnViewportHeight) {
					/*
					 * The scroll bar will be showing to the right of this
					 * column, so we need to make room for it.
					 */
					ScrollBar bar = columnViewport.getVerticalBar();
					if (bar != null) {
						// fix vertical scroll bar paging
						final Rectangle columnViewportClientArea = columnViewport
								.getClientArea();
						final int increment = columnViewportClientArea.height - 5;
						bar.setPageIncrement(increment);

						/*
						 * Try to get the scroll bar width on this OS.
						 */
						scrollBarWidth = bar.getSize().x;
					} else {
						SLLogger
								.getLogger()
								.log(Level.WARNING,
										"null vertical scroll bar for a column in the cascading list.");
					}
					/*
					 * If we have to, just guess the scroll bar width.
					 */
					if (scrollBarWidth <= 0)
						scrollBarWidth = SCROLL_BAR_WIDTH_DEFAULT;
				}
				final int columnViewportWidth = pColumnContentsSize.x
						+ scrollBarWidth;
				columnViewport.setBounds(xPos, BORDER, columnViewportWidth,
						columnViewportHeight);
				xPos += PADDING + columnViewportWidth;
				final Point origin = f_columnViewportToOrigin
						.get(columnViewport);
				if (origin != null) {
					columnViewport.setOrigin(origin);
				}
			} else {
				/*
				 * Self-managed
				 */
				final int columnWidth = column.computeSize(SWT.DEFAULT,
						columnViewportHeight).x;
				column.setBounds(xPos, BORDER, columnWidth,
						columnViewportHeight);
				xPos += PADDING + columnWidth;
			}
		}
		final int contentsWidth = xPos - PADDING + BORDER;
		f_contents.setSize(contentsWidth, viewportSize.height);
		setMinWidth(contentsWidth);
		f_xPosStartOfLast = xPosStartOfLast;

		// fix horizontal scroll bar paging
		ScrollBar bar = getHorizontalBar();
		if (bar != null) {
			final Rectangle viewportClientArea = getClientArea();
			final int increment = viewportClientArea.width - 5;
			bar.setPageIncrement(increment);
		}
	}

	private int f_xPosStartOfLast = 0;

	private Animator f_animator = null;

	private static final double DURATION_SCALE = 0.8;

	private class CLTimingTarget extends TimingTargetAdapter {

		final int f_sX;
		final int f_tX;
		final int f_delta;
		final boolean f_rightAnimation;

		CLTimingTarget(int startingXPos, int targetXPos) {
			f_sX = startingXPos;
			f_tX = targetXPos;
			f_delta = f_tX - f_sX;
			f_rightAnimation = f_delta > 0;
		}

		@Override
		public void end() {
			/*
			 * This is a HACK to solve bug 1056 where the scroll bars of new
			 * columns under Windows get "stuck" after an animation. The
			 * ScrolledComposite is busted per Eclipse bug 157058 but I don't
			 * know when it will be fixed.
			 */
			// if (!CascadingList.this.isDisposed()) {
			// CascadingList.this.fixupSizeOfContents();
			// }
		}

		@Override
		public void timingEvent(float fraction) {
			if (CascadingList.this.isDisposed()) {
				f_animator.stop();
				return;
			}
			/*
			 * Within the SWT thread.
			 */
			int x = f_sX + (int) ((float) f_delta * fraction);
			final Point origin = getOrigin();
			if ((f_rightAnimation && origin.x < x)
					|| (!f_rightAnimation && origin.x > x)) {
				setOrigin(x, origin.y);
			}
		}
	}

	private void animateStartOfLastColumn() {
		/*
		 * Stop any ongoing animation.
		 */
		if (f_animator != null) {
			if (f_animator.isRunning())
				f_animator.stop();
		}

		final int contentsWidth = f_contents.getSize().x;
		final int viewPortWidth = getSize().x;
		final int originX = getOrigin().x;
		final boolean endOfContentsVisible = originX + viewPortWidth >= contentsWidth;
		boolean needToAnimate = isVisible() && !endOfContentsVisible;
		if (needToAnimate) {
			int duration = (f_xPosStartOfLast - originX);
			if (duration <= 0)
				return;
			duration = (int) ((double) duration * DURATION_SCALE);
			f_animator = new Animator(duration, new CLTimingTarget(originX,
					f_xPosStartOfLast));
			f_animator.start();
		}
	}

	private void animateStartOfColumn(int columnIndex) {
		final Composite columnPanel = f_columns.get(columnIndex);
		if (columnPanel == null)
			return;
		final int xPosStartOfColumn = columnPanel.getBounds().x;
		/*
		 * Stop any ongoing animation.
		 */
		if (f_animator != null) {
			if (f_animator.isRunning())
				f_animator.stop();
		}

		final int originX = getOrigin().x;
		int duration = Math.abs(xPosStartOfColumn - originX);
		if (duration <= 0)
			return;
		duration = (int) ((double) duration * DURATION_SCALE);
		f_animator = new Animator(duration, new CLTimingTarget(originX,
				xPosStartOfColumn));
		f_animator.start();
	}
}
