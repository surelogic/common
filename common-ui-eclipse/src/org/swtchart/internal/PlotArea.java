/*******************************************************************************
 * Copyright (c) 2008-2011 SWTChart project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.swtchart.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ICustomPaintListener;
import org.swtchart.ILineSeries;
import org.swtchart.IPlotArea;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.internal.series.ISeriesHandler;
import org.swtchart.internal.series.Series;
import org.swtchart.internal.series.SeriesSet;

import com.surelogic.common.ui.tooltip.*;

/**
 * Plot area to draw series and grids.
 */
public class PlotArea extends Composite implements PaintListener, IPlotArea, MouseTrackListener, MouseMoveListener {
	private static final int TIP_HEIGHT = 20;
	private static final int MIN_TIP_TIME = 200;
	
    /** the chart */
    protected Chart chart;

    /** the set of plots */
    protected SeriesSet seriesSet;

    /** the image cache */
    private Image imageCache;

    /** the state indicating if image cache has to be updated */
    private boolean updateImageCache;

    /** the custom paint listeners */
    List<ICustomPaintListener> paintListeners;

    /** the default background color */
    private static final int DEFAULT_BACKGROUND = SWT.COLOR_WHITE;

    private final List<SeriesPoint> points = new ArrayList<SeriesPoint>();
    
    private CompactToolTipInformationControl tip;
    
    private long tipOnTime = Long.MIN_VALUE;
    
    /**
     * Constructor.
     *
     * @param chart
     *            the chart
     * @param style
     *            the style
     */
    public PlotArea(Chart chart, int style) {
        super(chart, style | SWT.NO_BACKGROUND);

        this.chart = chart;

        seriesSet = new SeriesSet(chart);
        updateImageCache = true;
        paintListeners = new ArrayList<ICustomPaintListener>();

        setBackground(Display.getDefault().getSystemColor(DEFAULT_BACKGROUND));
        addPaintListener(this);
        addMouseTrackListener(this);
        addMouseMoveListener(this);
        /*
        ToolTip t = new ToolTip(chart.getShell());
        t.activateToolTip(chart);
        */
        // NO_TRIM implies not resizable by the user
        tip = new CompactToolTipInformationControl(chart.getShell(), SWT.NO_TRIM);                
        tip.setVisible(false);
    }

    /**
     * Gets the set of series.
     *
     * @return the set of series
     */
    public ISeriesSet getSeriesSet() {
        return seriesSet;
    }

    /*
     * @see Control#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        ((SeriesSet) getSeriesSet()).compressAllSeries();
    }

    /*
     * @see Control#setBackground(Color)
     */
    @Override
    public void setBackground(Color color) {
        if (color == null) {
            super.setBackground(Display.getDefault().getSystemColor(
                    DEFAULT_BACKGROUND));
        } else {
            super.setBackground(color);
        }
    }

    /*
     * @see IPlotArea#addCustomPaintListener(ICustomPaintListener)
     */
    public void addCustomPaintListener(ICustomPaintListener listener) {
        paintListeners.add(listener);
    }

    /*
     * @see IPlotArea#removeCustomPaintListener(ICustomPaintListener)
     */
    public void removeCustomPaintListener(ICustomPaintListener listener) {
        paintListeners.remove(listener);
    }

    /*
     * @see PaintListener#paintControl(PaintEvent)
     */
    public void paintControl(PaintEvent e) {
        if (updateImageCache) {
            Point p = getSize();
            if (imageCache != null && !imageCache.isDisposed()) {
                imageCache.dispose();
            }
            imageCache = new Image(Display.getCurrent(), p.x, p.y);
            GC gc = new GC(imageCache);

            // draw the plot area background
            gc.setBackground(getBackground());
            gc.fillRectangle(0, 0, p.x, p.y);

            // draw grid
            for (IAxis axis : chart.getAxisSet().getAxes()) {
                ((Grid) axis.getGrid()).draw(gc, p.x, p.y);
            }

            // draw behind series
            GC prevGC = e.gc;
            e.gc = gc;
            for (ICustomPaintListener listener : paintListeners) {
                if (listener.drawBehindSeries()) {
                    listener.paintControl(e);
                }
            }
            
            points.clear();
            final ISeriesHandler h = new SeriesHandler(points);            
            
            // draw series. The line series should be drawn on bar series.
            for (ISeries series : chart.getSeriesSet().getSeries()) {
                if (series instanceof IBarSeries) {
                    ((Series) series).draw(gc, p.x, p.y, h);
                }
            }
            for (ISeries series : chart.getSeriesSet().getSeries()) {
                if (series instanceof ILineSeries) {
                    ((Series) series).draw(gc, p.x, p.y, h);
                }
            }

            // draw over series
            for (ICustomPaintListener listener : paintListeners) {
                if (!listener.drawBehindSeries()) {
                    listener.paintControl(e);
                }
            }
            e.gc = prevGC;

            gc.dispose();
            updateImageCache = false;
        }
        e.gc.drawImage(imageCache, 0, 0);
    }

    /*
     * @see Control#update()
     */
    @Override
    public void update() {
        super.update();
        updateImageCache = true;
    }

    /*
     * @see Control#redraw()
     */
    @Override
    public void redraw() {
        super.redraw();
        updateImageCache = true;
    }

    /*
     * @see Widget#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        seriesSet.dispose();
        if (imageCache != null && !imageCache.isDisposed()) {
            imageCache.dispose();
        }
    }

    @Override
    public void mouseMove(MouseEvent e) {
    	//System.out.println("Mouse at "+e.x+", "+e.y);
    	if (tip.isVisible()) {
    		final long now = System.currentTimeMillis();
    		if (now - tipOnTime > MIN_TIP_TIME) {
    			//System.out.println("Turning off tip -- due to move");
    			tip.setVisible(false);
    		}
    	}
    }
    
	@Override
	public void mouseEnter(MouseEvent e) {
		/*
		System.out.println("Turning off tip -- due to enter");
		tip.setVisible(false);
		*/
	}

	@Override
	public void mouseExit(MouseEvent e) {
		/*
		System.out.println("Turning off tip -- due to exit");
		tip.setVisible(false);
		*/
	}	
	
	@Override
	public void mouseHover(MouseEvent e) {
		SeriesPoint p = findClosestPoint(e.x, e.y);
		if (p != null) {
			final String point = p.series.getId()+"["+p.index+"]";
			final String msg = point+"="+((int) p.series.getYSeries()[p.index]);
			//System.out.println("Close to "+point);
			Point loc = chart.toDisplay(p.point);
			tip.setHoverLocation(new Point(loc.x, loc.y - TIP_HEIGHT));
			tip.setTip(msg);
			tip.setSize(6*msg.length(), TIP_HEIGHT);
			tip.setVisible(true);
			tipOnTime = System.currentTimeMillis();
		}
	}
	
	private SeriesPoint findClosestPoint(final int x, final int y) {		
		SeriesPoint closest = null;
		int distance = 4000; //Integer.MAX_VALUE;
		for(SeriesPoint p : points) {
			final int d = computeDistance(x, y, p.point);
			//System.out.println(p.series.getId()+":"+p.index+" = "+d);	
			if (d < distance) {						
				distance = d;
				closest = p;
			}
		}
		return closest;
	}

	private static int computeDistance(int x, int y, Point p) {
		final int dx = x - p.x;
		final int dy = y - p.y;
		return dx*dx + dy*dy;
	}
	
	static class SeriesPoint {
		final Point point;
		final ISeries series;
		final int index;
		
		SeriesPoint(int i, ISeries s, Point p) {
			point = p;
			series = s;
			index = i;
		}		
	}
	
	static class SeriesHandler implements ISeriesHandler {
		final List<SeriesPoint> points;
		
		public SeriesHandler(List<SeriesPoint> p) {
			points = p;
		}

		@Override
		public void handleDataPoint(int x, ISeries s, Point p) {
			points.add(new SeriesPoint(x, s, p));
		}
	}
}