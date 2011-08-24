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
	 * @param title
	 *            the title of the document, {@code null} if you don't want a
	 *            title to be printed.
	 * @param text
	 *            the document text, cannot be {@code null}.
	 * @param printPageNumbers
	 *            {@code true} if you want page numbers printed, {@code false}
	 *            otherwise.
	 */
	static public void printText(final String title, final String text,
			final boolean printPageNumbers) {
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

				final Printer printer = getPrinter(printerData);

				final Job printJob = new Job("Printing SureLogic Report") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Printing SureLogic Report",
								IProgressMonitor.UNKNOWN);
						try {
							print(printer, title, text, printPageNumbers);
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
		int tabWidth = 0;
		int lineHeight = 0;
		StringBuilder wordBuffer;
		int x, y;
		int index, end;
		String title;
		String textToPrint;
		Rectangle clientArea;
		boolean printPageNumbers;
		int pageNumber = 1;
		int padX, padY;
	}

	/**
	 * We want a landscape printer, so try to set that and return a printer. If
	 * that doesn't work use what we were passed.
	 * 
	 * @param data
	 *            the printer data.
	 * @return a printer.
	 * 
	 * @throws IllegalArgumentException
	 *             if the specified printer data does not represent a valid
	 *             printer.
	 */
	private static Printer getPrinter(final PrinterData data) {
		/**
		 * Use landscape mode, if possible
		 */
		if (data.orientation != PrinterData.LANDSCAPE) {
			final int oldOrientation = data.orientation;
			data.orientation = PrinterData.LANDSCAPE;
			try {
				Printer result = new Printer(data);
				return result;
			} catch (Exception ignore) {
				/*
				 * We'll try again without landscape orientation
				 */
			}
			data.orientation = oldOrientation;
		}

		return new Printer(data);
	}

	private static void print(Printer printer, String title, String text,
			boolean printPageNumbers) {
		Config c = new Config();
		c.printer = printer;
		c.title = title;
		c.textToPrint = text + " ";
		c.printPageNumbers = printPageNumbers;

		if (printer.startJob("SureLogic Report")) {
			/*
			 * the string is the job name - shows up in the printer's job list
			 */
			c.clientArea = printer.getClientArea();
			Point dpi = printer.getDPI();
			c.padX = dpi.x / 10;
			c.padY = dpi.y / 10;

			c.leftMargin = c.clientArea.x + c.padX;
			c.rightMargin = c.clientArea.x + c.clientArea.width - c.padX;

			c.topMargin = c.clientArea.y + c.padY + (dpi.y / 4);
			c.bottomMargin = c.clientArea.y + c.clientArea.height - c.padY;

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

			c.gc.setForeground(printer.getSystemColor(SWT.COLOR_BLACK));

			/*
			 * Print text to current gc using word wrap.
			 */
			printText(c);
			printer.endJob();

			/*
			 * Cleanup graphics resources used in printing.
			 */
			c.printerFont.dispose();
			c.gc.dispose();
		}
	}

	/**
	 * We need a fixed width font so this method tries to find one that works on
	 * Windows, OS X, and Linux.
	 * 
	 * @param printer
	 *            a printer.
	 * @return a fixed width font at 9 points.
	 */
	private static Font getPrinterFont(Printer printer) {
		String[] names = { "Consolas", "Terminal", "Monaco", "Mono",
				"Anonymous Pro", "Courier New", "Courier" };
		/*
		 * Try several monospaced fonts
		 */
		for (String name : names) {
			try {
				Font f = new Font(printer, name, 9, SWT.NONE);
				return f;
			} catch (Exception ignore) {
				// didn't work, we'll try the next
			}
		}
		/*
		 * Well, go with the (awful) default.
		 */
		return printer.getSystemFont();
	}

	private static void printText(Config c) {
		c.printer.startPage();
		decoratePage(c);
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
				decoratePage(c);
			}
		}
	}

	private static void decoratePage(Config c) {
		Color oldColor = c.gc.getForeground();
		c.gc.setForeground(c.printer.getSystemColor(SWT.COLOR_DARK_RED));

		/*
		 * Page number (optional)
		 */
		if (c.printPageNumbers) {
			final String pn = "page " + c.pageNumber++;
			final Point pnEx = c.gc.stringExtent(pn);

			int pnX = c.clientArea.x + c.clientArea.width - pnEx.x - c.padX;
			int pnY = c.clientArea.y + c.padY;
			c.gc.drawString(pn, pnX, pnY);
		}

		/*
		 * Title (optional)
		 */
		if (c.title != null) {
			int titleX = c.clientArea.x + c.padX;
			int titleY = c.clientArea.y + c.padY;
			c.gc.drawString(c.title, titleX, titleY);
		}

		/*
		 * Box (last so it doesn't cropped by the page number or title)
		 */
		final int padBox = 20;
		c.gc.setForeground(c.printer.getSystemColor(SWT.COLOR_BLUE));
		c.gc.setLineWidth(5);
		c.gc.drawRectangle(c.clientArea.x + padBox, c.clientArea.y + padBox,
				c.clientArea.width - padBox * 2, c.clientArea.height - padBox
						* 2);

		c.gc.setForeground(oldColor);
	}
}
