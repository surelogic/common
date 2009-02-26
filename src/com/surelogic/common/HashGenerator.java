package com.surelogic.common;

import java.io.*;
import java.util.*;
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

	//private int countFileAccess = 0;

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
	public static void main(String args[]) {

		HashGenerator hashGenerator = HashGenerator.getInstance();
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 1));
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 5));
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 5));
		System.out.println("\t"
				+ hashGenerator.getHash("C:/fibonacci.java", 18));

	}

	public Long getHash(String fileName, int lineNumber) {
		try {
			// adjust line number to be zero indexed
			if (lineNumber > 0) {
				lineNumber--;
			}
			boolean updated = updateCache(fileName);
			if (!updated && lastHashLine == lineNumber) {
				return lastHashValue;
			}
	     
			if (lineNumber >= cachedFileLines.size()) {
			  log.severe("line# too big: "+lineNumber+" >= "+cachedFileLines.size());
			  cachedFileLines = buildCachedLines(fileName);
			}			
			lastHashLine = lineNumber;

			String valueUp = getChunkBefore(cachedFileLines, lineNumber, 30);
			String valueDown = getChunkAfter(cachedFileLines, lineNumber, 30);

			int hashUp = valueUp.hashCode();
			int hashDown = valueDown.hashCode();

			lastHashValue = (((long) hashDown) << 32) + hashUp;
			return lastHashValue;
		} catch (FileNotFoundException e) {
			log
					.log(Level.SEVERE, "The file " + fileName
							+ " was not found.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		} catch (IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		}
	}

	/**
	 * @return true if updated
	 */
	private boolean updateCache(String fileName) throws IOException {
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
	
	public void generateHash(Map<String, Map<Integer, Long>> hashHolder) {
		for(Map.Entry<String, Map<Integer, Long>> entry : hashHolder.entrySet()) {
			String fileName = entry.getKey();
			Map<Integer, Long> lineHashMap = entry.getValue();
			Set<Integer> lineNumbers = lineHashMap.keySet();
			Iterator<Integer> lineNumberIterator = lineNumbers.iterator();

			while (lineNumberIterator.hasNext()) {
				int lineNumber = lineNumberIterator.next();
				Long hashValue = getHash(fileName, lineNumber);
				lineHashMap.put(lineNumber, hashValue);
			}

			//countFileAccess++;

		}

		// System.out.println("File Access from the new way : " +
		// countFileAccess);

	}

	private List<String> buildCachedLines(String fileName) throws IOException {
	  if (fileName == null) {
	    throw new IllegalArgumentException("Null filename");
	  }
		List<String> cachedLines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		try {
		  final StringBuilder cachedLine = new StringBuilder();
			String inLine = in.readLine();
			while (inLine != null) {
				String[] lineElements = inLine.split("\\s+");
				// TODO replace split with replaceAll?
				
				for (String element : lineElements) {
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

	private String getChunkBefore(List<String> cachedLines, int lineNumber,
			int maxChunkSize) {
		int chunkLine = lineNumber;
		if (chunkLine < 0) {
			return FIRST;
		}
    if (chunkLine >= cachedLines.size()) {
      log.severe("line# too big: "+chunkLine+" >= "+cachedLines.size());
      return TOO_FAR;
    }
		StringBuilder chunkBuf = new StringBuilder();
		while (chunkLine >= 0 && chunkBuf.length() < maxChunkSize) {
			chunkBuf.insert(0, cachedLines.get(chunkLine));
			chunkLine--;
		}

		if (chunkBuf.length() > maxChunkSize) {
			int len = chunkBuf.length();
			return chunkBuf.substring(len - maxChunkSize, len);
		}
		return chunkBuf.toString();
	}

	private String getChunkAfter(List<String> cachedLines, int lineNumber,
			int maxChunkSize) {
		int chunkLine = lineNumber + 1;
		if (chunkLine >= cachedLines.size()) {
			return LAST;
		}

		StringBuilder chunkBuf = new StringBuilder();
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
	
	public int getLineForOffset(String fileName, int offset) {
		if (offset < 0 || offset == Integer.MAX_VALUE) {
			return -1;
		}
		if (lastOffset == offset && lastOffsetFile != null &&
			lastOffsetFile.equals(fileName)) {
			return lastOffsetLine;
		}
		try {
			// Read the file up to f_offset
			int numLeft     = offset+1;
			FileReader fr   = new FileReader(fileName);
			StringBuffer sb = new StringBuffer(numLeft);
			char[] cbuf     = new char[BUF_SIZE];			
			int num;			
			while (numLeft > 0  && (num = fr.read(cbuf, 0, numLeft < BUF_SIZE ? numLeft : BUF_SIZE)) >= 0) {
				sb.append(cbuf, 0, num);				
				numLeft -= num;
			};			
			
			int line = 0;
			BufferedReader in = new BufferedReader(new StringReader(sb.toString()));			
			while (in.readLine() != null) {
				line++;
			}			
			in.close();
			lastOffsetFile = fileName;
			lastOffset     = offset;
			lastOffsetLine = line;
			return line;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private String lastOffsetFile;
	private int lastOffset;
	private int lastOffsetLine;
	
	public Long getHashForOffset(String fileName, int offset) {
		int line = getLineForOffset(fileName, offset);
		return getHash(fileName, line);
	}
}
