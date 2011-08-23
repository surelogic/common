package com.surelogic.common.ui.printing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.Utility;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.jobs.SLUIJob;

@Utility
public final class SLPrintingUtility {

	static public boolean isPrintingAvailable() {
		return Printer.getPrinterList().length > 0;
	}

	/**
	 * Submits a UI job to print the passed text. This job will prompt the use
	 * for the correct printer to print with and gives the user a chance to
	 * cancel the printing.
	 * 
	 * @param text
	 */
	static public void printText(final String text) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!isPrintingAvailable()) {
					// Can't print or no printers are installed
					MessageDialog.openInformation(EclipseUIUtility.getShell(),
							"Cannot Print",
							"Printing from Eclipse is unavailable.");
					return Status.OK_STATUS;
				}
				final PrintDialog printDialog = new PrintDialog(
						EclipseUIUtility.getShell());
				PrinterData printerData = printDialog.open();
				if (printerData == null)
					return Status.CANCEL_STATUS;

				final Printer printer = new Printer(printerData);
				final Job printJob = new Job("Printing SureLogic Report") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Printing SureLogic Report",
								IProgressMonitor.UNKNOWN);
						try {
							print(printer, text);
						} finally {
							printer.dispose();
							monitor.done();
						}
						return Status.OK_STATUS;
					}
				};
				printJob.schedule();

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static class Config {
		Printer printer;
		int leftMargin, rightMargin, topMargin, bottomMargin;
		String tabs;
		GC gc;
		Font printerFont;
		Color printerForegroundColor;
		int tabWidth = 0;
		int lineHeight = 0;
		StringBuilder wordBuffer;
		int x, y;
		int index, end;
		String textToPrint;
	}

	private static void print(Printer printer, String text) {
		Config c = new Config();
		c.printer = printer;
		c.textToPrint = text;

		if (printer.startJob("SureLogic Report")) {
			/*
			 * the string is the job name - shows up in the printer's job list
			 */
			Rectangle clientArea = printer.getClientArea();
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			/*
			 * one inch from left side of paper
			 */
			c.leftMargin = dpi.x + trim.x;
			/*
			 * one inch from right side of paper
			 */
			c.rightMargin = clientArea.width - dpi.x + trim.x + trim.width;
			/*
			 * one inch from top edge of paper
			 */
			c.topMargin = dpi.y + trim.y;
			/*
			 * one inch from bottom edge of paper
			 */
			c.bottomMargin = clientArea.height - dpi.y + trim.y + trim.height;

			/*
			 * Create a buffer for computing tab width.
			 */
			int tabSize = 4; // is tab width a user setting in your UI?
			StringBuffer tabBuffer = new StringBuffer(tabSize);
			for (int i = 0; i < tabSize; i++)
				tabBuffer.append(' ');
			c.tabs = tabBuffer.toString();

			/*
			 * Create printer GC, and create and set the printer font &
			 * foreground color.
			 */
			c.gc = new GC(printer);

			c.printerFont = getPrinterFont(printer);
			c.gc.setFont(c.printerFont);
			c.tabWidth = c.gc.stringExtent(c.tabs).x;
			c.lineHeight = c.gc.getFontMetrics().getHeight();

			c.printerForegroundColor = printer.getSystemColor(SWT.COLOR_BLACK);
			c.gc.setForeground(c.printerForegroundColor);

			/*
			 * Print text to current gc using word wrap.
			 */
			printText(c);
			printer.endJob();

			/*
			 * Cleanup graphics resources used in printing.
			 */
			c.printerFont.dispose();
			c.printerForegroundColor.dispose();
			c.gc.dispose();
		}
	}

	private static Font getPrinterFont(Printer printer) {
		String[] names = { "Consolas", "Terminal", "Monaco", "Mono",
				"Anonymous Pro", "Courier New", "Courier" };
		/*
		 * Try several monospaced fonts
		 */
		for (String name : names) {
			try {
				Font f = new Font(printer, name, 9, SWT.NONE);
				System.out.println("Got FONT " + name);
				return f;
			} catch (Exception e) {
				// didn't work
			}
		}
		/*
		 * Well, go with the crappy default.
		 */
		return printer.getSystemFont();
	}

	private static void printText(Config c) {
		c.printer.startPage();
		c.wordBuffer = new StringBuilder();
		c.x = c.leftMargin;
		c.y = c.topMargin;
		c.index = 0;
		c.end = c.textToPrint.length();
		while (c.index < c.end) {
			char ch = c.textToPrint.charAt(c.index);
			c.index++;
			if (ch != 0) {
				if (ch == 0x0a || ch == 0x0d) {
					if (ch == 0x0d && c.index < c.end
							&& c.textToPrint.charAt(c.index) == 0x0a) {
						/*
						 * if this is cr-lf, skip the lf
						 */
						c.index++;
					}
					printWordBuffer(c);
					newline(c);
				} else {
					if (ch != '\t') {
						c.wordBuffer.append(ch);
					}
					if (Character.isWhitespace(ch)) {
						printWordBuffer(c);
						if (ch == '\t') {
							c.x += c.tabWidth;
						}
					}
				}
			}
		}
		if (c.y + c.lineHeight <= c.bottomMargin) {
			c.printer.endPage();
		}
	}

	private static void printWordBuffer(Config c) {
		if (c.wordBuffer.length() > 0) {
			String word = c.wordBuffer.toString();
			int wordWidth = c.gc.stringExtent(word).x;
			if (c.x + wordWidth > c.rightMargin) {
				/* word doesn't fit on current line, so wrap */
				newline(c);
			}
			c.gc.drawString(word, c.x, c.y, false);
			c.x += wordWidth;
			c.wordBuffer = new StringBuilder();
		}
	}

	private static void newline(Config c) {
		c.x = c.leftMargin;
		c.y += c.lineHeight;
		if (c.y + c.lineHeight > c.bottomMargin) {
			c.printer.endPage();
			if (c.index + 1 < c.end) {
				c.y = c.topMargin;
				c.printer.startPage();
			}
		}
	}
}
