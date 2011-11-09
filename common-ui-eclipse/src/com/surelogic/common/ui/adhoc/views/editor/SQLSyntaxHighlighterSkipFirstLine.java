package com.surelogic.common.ui.adhoc.views.editor;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

/**
 * This class performs color syntax highlighting of SQL statements but skips the
 * first line. Typically the first line will be an error message or some prefix
 * line before an SQL statement that should not be highlighted.
 */
public final class SQLSyntaxHighlighterSkipFirstLine extends
		SQLSyntaxHighlighter {

	public SQLSyntaxHighlighterSkipFirstLine(final Display display) {
		super(display);
	}

	@Override
	public void lineGetStyle(LineStyleEvent event) {
		/*
		 * Skip the first line and set it to the default font.
		 */
		if (event.lineOffset == 0) {
			StyleRange sr = new StyleRange();
			sr.start = event.lineOffset;
			sr.length = event.lineText.length();
			sr.font = JFaceResources.getDefaultFont();
			event.styles = new StyleRange[] { sr };
			return;
		}
		super.lineGetStyle(event);
	}
}
