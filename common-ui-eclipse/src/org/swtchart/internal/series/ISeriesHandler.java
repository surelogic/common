package org.swtchart.internal.series;

import org.eclipse.swt.graphics.Point;
import org.swtchart.ISeries;

public interface ISeriesHandler {
	void handleDataPoint(int x, ISeries s, Point point);
}
