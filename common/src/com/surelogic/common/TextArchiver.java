package com.surelogic.common;

import java.io.*;

import com.surelogic.common.FileUtility.FileRunner;

/**
 * A human-readable archive of mostly text/XML files
 * 
 * @author Edwin
 */
public class TextArchiver extends FileRunner {
	public static final String SEPARATOR = "==================================================================================================";
	private static final String WARNING = "== WARNING == ";
	private static final String DOES_NOT_EXIST = "File does not exist: ";
	
	private final String targetLabel;
	private final PrintStream targetOut; 
	
	public TextArchiver(File target) throws IOException {
		targetLabel = target.getAbsolutePath();
		targetOut = new PrintStream(target);
	}
	
	public void archive(String relativePath, File f) {
		copyContentsToStream(relativePath, f, targetOut, targetLabel);
	}
	
	@Override
	protected void iterate(String relativePath, File f) {
		System.out.println("Copying "+relativePath);
		copyContentsToStream(relativePath, f, targetOut, targetLabel);
	}
	
	public void outputWarning(String msg) {
		outputMessage(targetOut, WARNING + msg);
	}
	
	public void close() {
		targetOut.close();
	}
	
	private static void outputMessage(PrintStream out, String msg) {
		out.println(SEPARATOR);
		out.println(msg);
	}
	
	private static void copyContentsToStream(final String source, File file, PrintStream out,
			String target) {
		//final String source = file.getAbsolutePath();

		if (file.isFile()) {
			outputMessage(out, source);
			out.println(SEPARATOR);
			try {
				FileUtility.copyToStream(false, source, new FileInputStream(
						file), target, out, false);
			} catch (IOException e) {
				e.printStackTrace(out);
			}
		} else {
			outputMessage(out, WARNING + DOES_NOT_EXIST + source);
		}
	}
	
	private static String findSeparator(BufferedReader br) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			if (SEPARATOR.equals(line)) {
				return line;
			}
		}			
		return line;
	}
	
	public static void unarchive(File srcArchive, File destDir) throws IOException {
		FileReader fr = new FileReader(srcArchive);
		BufferedReader br = new BufferedReader(fr);

		// Find separator for the first file
		String line = findSeparator(br);
		
		// Should be either SEPARATOR or null
		while (line != null) {			
			final String path = br.readLine();
			if (path == null) {
				// Nothing more to look at
				break;
			}
			if (path.startsWith(DOES_NOT_EXIST)) {
				// Doesn't exist, so start over
				line = findSeparator(br);
				continue;
			}
			System.out.println("Path: "+path);
			
			final String sep  = br.readLine();
			if (!SEPARATOR.equals(sep)) {
				// Not what is expected, so start over
				line = findSeparator(br);
				continue;
			}
			// Handle the rest of the file
			PrintWriter out = null;
			while ((line = br.readLine()) != null) {
				if (SEPARATOR.equals(line)) {
					// Now done with this particular file
					break;
				}
				if (out == null) {
					File dest = new File(destDir, path);
					dest.getParentFile().mkdirs();
					FileWriter fw = new FileWriter(dest);
					out = new PrintWriter(fw);
				}
				out.println(line);
				System.out.println("\t"+line);
			}			
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static void main(String... args) throws IOException {
		File temp = File.createTempFile("TextArchiver", ".test.txt");	
		
		TextArchiver t = new TextArchiver(temp);		
		File dir = new File(".");
		System.out.println("Iterating over "+dir.getAbsolutePath());
		FileUtility.recursiveIterate(t, dir);
		
		File tempDir = new File(temp.getParentFile(), temp.getName()+".dir");
		tempDir.mkdirs();
		TextArchiver.unarchive(temp, tempDir);
	}
}
