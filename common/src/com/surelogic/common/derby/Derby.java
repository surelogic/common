package com.surelogic.common.derby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Derby {

	private Derby() {
		// a utility, no instances
	}

	/**
	 * Defines the fully-qualified class name for the Derby embedded JDBC
	 * driver.
	 */
	private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

	/**
	 * Boots the embedded Derby database Driver.
	 */
	public static void bootEmbedded() {
		try {
			/*
			 * Load the Derby driver. Since the embedded driver is used this
			 * action starts up the Derby engine.
			 */
			Thread.currentThread().getContextClassLoader().loadClass(DRIVER)
					.newInstance();
		} catch (final java.lang.ClassNotFoundException e) {
			throw new IllegalStateException(
					"Unable to boot embedded Derby using: " + DRIVER, e);
		} catch (final InstantiationException e) {
			throw new IllegalStateException(
					"Unable to boot embedded Derby using: " + DRIVER, e);
		} catch (final IllegalAccessException e) {
			throw new IllegalStateException(
					"Unable to boot embedded Derby using: " + DRIVER, e);
		}
	}

	/**
	 * Opens and reads a series of SQL statements, separated by semicolons, from
	 * the specified SQL file. The statements are returned, in the order that
	 * they were read, via a list.
	 * <p>
	 * This method helps a program using JDBC to execute script containing lots
	 * of SQL statements.
	 * 
	 * @param sqlFile
	 *            the file to be read that contains the SQL statements.
	 * @return a list of SQL statements (with the semicolons removed) suitable
	 *         for executing via JDBC.
	 */
	public static List<StringBuilder> getSQLStatements(final URL sqlFile) {
		if (sqlFile == null) {
			throw new IllegalArgumentException("sqlFile must be non-null");
		}

		final List<StringBuilder> result = new ArrayList<>();

		try {
			final InputStream is = sqlFile.openStream();
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);

			try {
				StringBuilder b = new StringBuilder();
				String buffer;
				while ((buffer = br.readLine()) != null) {
					buffer = buffer.trim();
					if (buffer.startsWith("--") || "".equals(buffer)) {
						// comment or blank line -- ignore this line
					} else if (buffer.endsWith(";")) {
						// end of an SQL statement -- add to our resulting list
						if (b.length() > 0) {
							b.append("\n");
						}
						b.append(buffer);
						b.deleteCharAt(b.length() - 1); // remove the ";"
						result.add(b);
						b = new StringBuilder();
					} else {
						// add this line (with a newline after the first line)
						if (b.length() > 0) {
							b.append("\n");
						}
						b.append(buffer);
					}
				}
				br.readLine();
			} finally {
				br.close();
			}
		} catch (final IOException e) {
			throw new IllegalStateException(
					"Unable to open/read the SQL file: " + sqlFile, e);
		}
		return result;
	}
}
