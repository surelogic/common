package com.surelogic.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;

/**
 * The instance hash generator calculates the 30 alphanumeric characters before
 * and after the given line number
 * 
 * @author Tanmay.Sinha
 * 
 */
public class HashGenerator {
	public static final Long UNKNOWN = -1L;

	private static final Logger log = SLLogger.getLogger("sierra");

	private static final String FIRST = "FIRST";

	private static final String LAST = "LAST";

	private static final String TOO_FAR = "TOOFAR";

	// private int countFileAccess = 0;

	private String cachedFileName;

	private List<String> cachedFileLines;

	private long lastHashValue;

	private int lastHashLine = -1;

	private static class Singleton {
		static final HashGenerator hashGenerator = new HashGenerator();
	}

	HashGenerator() {
		// Nothing do to
	}

	public static HashGenerator getInstance() {
		return Singleton.hashGenerator;
	}

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(final String args[]) {

		final HashGenerator hashGenerator = HashGenerator.getInstance();
		if (args.length % 2 == 0) {
			for (int i = 0; i < args.length; i += 2) {
				System.out.println(""
						+ hashGenerator.getHash(args[i], Integer
								.valueOf(args[i + 1])));
			}
		}

	}

	public Long getHash(final String fileName, int lineNumber) {
		try {
			// adjust line number to be zero indexed
			if (lineNumber > 0) {
				lineNumber--;
			}
			final boolean updated = updateCache(fileName);
			if (!updated && lastHashLine == lineNumber) {
				return lastHashValue;
			}

			if (lineNumber >= cachedFileLines.size()) {
				log.severe("line# too big: " + lineNumber + " >= "
						+ cachedFileLines.size());
				cachedFileLines = buildCachedLines(fileName);
			}
			lastHashLine = lineNumber;

			final String valueUp = getChunkBefore(cachedFileLines, lineNumber,
					30);
			final String valueDown = getChunkAfter(cachedFileLines, lineNumber,
					30);

			final int hashUp = valueUp.hashCode();
			final int hashDown = valueDown.hashCode();

			lastHashValue = (((long) hashDown) << 32) + hashUp;
			return lastHashValue;
		} catch (final FileNotFoundException e) {
			log
					.log(Level.SEVERE, "The file " + fileName
							+ " was not found.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		} catch (final IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		}
	}

	/**
	 * @return true if updated
	 */
	private boolean updateCache(final String fileName) throws IOException {
		if (cachedFileLines == null) {
			cachedFileName = null;
		}
		if (cachedFileName == null || !cachedFileName.equals(fileName)) {
			cachedFileName = fileName;
			cachedFileLines = buildCachedLines(fileName);
			return false;
		}
		return true;
	}

	public void generateHash(final Map<String, Map<Integer, Long>> hashHolder) {
		for (final Map.Entry<String, Map<Integer, Long>> entry : hashHolder
				.entrySet()) {
			final String fileName = entry.getKey();
			final Map<Integer, Long> lineHashMap = entry.getValue();
			final Set<Integer> lineNumbers = lineHashMap.keySet();
			final Iterator<Integer> lineNumberIterator = lineNumbers.iterator();

			while (lineNumberIterator.hasNext()) {
				final int lineNumber = lineNumberIterator.next();
				final Long hashValue = getHash(fileName, lineNumber);
				lineHashMap.put(lineNumber, hashValue);
			}

			// countFileAccess++;

		}

		// System.out.println("File Access from the new way : " +
		// countFileAccess);

	}

	private List<String> buildCachedLines(final String fileName)
			throws IOException {
		if (fileName == null) {
			throw new IllegalArgumentException("Null filename");
		}
		final List<String> cachedLines = new ArrayList<String>();
		final BufferedReader in = new BufferedReader(new FileReader(fileName));
		try {
			final StringBuilder cachedLine = new StringBuilder();
			String inLine = in.readLine();
			while (inLine != null) {
				final String[] lineElements = inLine.split("\\s+");
				// TODO replace split with replaceAll?

				for (final String element : lineElements) {
					cachedLine.append(element);
				}
				cachedLines.add(cachedLine.toString());

				inLine = in.readLine();
				cachedLine.setLength(0);
			}
		} finally {
			in.close();
		}

		return cachedLines;
	}

	private String getChunkBefore(final List<String> cachedLines,
			final int lineNumber, final int maxChunkSize) {
		int chunkLine = lineNumber;
		if (chunkLine < 0) {
			return FIRST;
		}
		if (chunkLine >= cachedLines.size()) {
			log.severe("line# too big: " + chunkLine + " >= "
					+ cachedLines.size());
			return TOO_FAR;
		}
		final StringBuilder chunkBuf = new StringBuilder();
		while (chunkLine >= 0 && chunkBuf.length() < maxChunkSize) {
			chunkBuf.insert(0, cachedLines.get(chunkLine));
			chunkLine--;
		}

		if (chunkBuf.length() > maxChunkSize) {
			final int len = chunkBuf.length();
			return chunkBuf.substring(len - maxChunkSize, len);
		}
		return chunkBuf.toString();
	}

	private String getChunkAfter(final List<String> cachedLines,
			final int lineNumber, final int maxChunkSize) {
		int chunkLine = lineNumber + 1;
		if (chunkLine >= cachedLines.size()) {
			return LAST;
		}

		final StringBuilder chunkBuf = new StringBuilder();
		while (chunkLine < cachedLines.size()
				&& chunkBuf.length() < maxChunkSize) {
			chunkBuf.append(cachedLines.get(chunkLine));
			chunkLine++;
		}

		if (chunkBuf.length() > maxChunkSize) {
			return chunkBuf.substring(0, maxChunkSize);
		}
		return chunkBuf.toString();
	}

	private static final int BUF_SIZE = 16384;

	public int getLineForOffset(final String fileName, final int offset) {
		if (offset < 0 || offset == Integer.MAX_VALUE) {
			return -1;
		}
		if (lastOffset == offset && lastOffsetFile != null
				&& lastOffsetFile.equals(fileName)) {
			return lastOffsetLine;
		}
		try {
			// Read the file up to f_offset
			int numLeft = offset + 1;
			final FileReader fr = new FileReader(fileName);
			final StringBuffer sb = new StringBuffer(numLeft);
			final char[] cbuf = new char[BUF_SIZE];
			int num;
			while (numLeft > 0
					&& (num = fr.read(cbuf, 0, numLeft < BUF_SIZE ? numLeft
							: BUF_SIZE)) >= 0) {
				sb.append(cbuf, 0, num);
				numLeft -= num;
			}
			;
			fr.close();

			int line = 0;
			final BufferedReader in = new BufferedReader(new StringReader(sb
					.toString()));
			while (in.readLine() != null) {
				line++;
			}
			in.close();
			lastOffsetFile = fileName;
			lastOffset = offset;
			lastOffsetLine = line;
			return line;
		} catch (final IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Unable to read "+fileName, e);
		}
		return -1;
	}

	private String lastOffsetFile;
	private int lastOffset;
	private int lastOffsetLine;

	public Long getHashForOffset(final String fileName, final int offset) {
		final int line = getLineForOffset(fileName, offset);
		return getHash(fileName, line);
	}
}
