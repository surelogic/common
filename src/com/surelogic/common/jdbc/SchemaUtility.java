package com.surelogic.common.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class SchemaUtility {

	private SchemaUtility() {
		// no instances
	}

	private static final String Q_CREATE = "create table VERSION (N INT NOT NULL)";
	private static final String Q_DELETE = "drop table VERSION";
	private static final String Q_SELECT = "select N from VERSION";
	private static final String Q_INSERT = "insert into VERSION values (0)";
	private static final String Q_UPDATE = "update VERSION set N=";

	/**
	 * Checks the passed database connection is at the correct version. Versions
	 * start at 0 and go up as sequential integers. The current version of the
	 * database is held within a table named <code>VERSION</code> as its only
	 * row. The appropriate SQL script to go from version <i>n</i> - 1 to <i>n</i>
	 * is referenced by <code>sqlScripts[n]</code>. These scripts are always
	 * run <b>in sequence</b> from lowest to highest. Optionally, code may be
	 * passed for each version that is executed <b>after</b> the SQL script for
	 * that version.
	 * <p>
	 * The <code>VERSION</code> is defined as follows:
	 * 
	 * <pre>
	 * create table Version (
	 * 	 N INT NOT NULL
	 * );
	 * </pre>
	 * 
	 * The lack of a version table implies that the database is not even at
	 * version 0 yet and all SQL scripts need to be run. In this case this
	 * method adds the <code>VERSION</code> table to the database, i.e., it is
	 * not the program's responsibility to create this table. However, the
	 * program must not define another table named <code>VERSION</code>.
	 * 
	 * @param c
	 *            the database connection to examine.
	 * @param sqlScripts
	 *            the SQL scripts. This array must at least define one script.
	 *            No element of this array can be <code>null</code>.
	 * @param actions
	 *            actions to run after the SQL scripts. This parameter may be
	 *            <code>null</code> if no actions are required. If it is
	 *            non-null then the invariant
	 *            <code>sqlScripts.length == actions.length</code> must hold.
	 * @throws SQLException
	 *             if an unexpected problem interacting with the database
	 *             occurs.
	 * @throws IOException
	 *             if a problem occurs reading an SQL script.
	 */
	public static void checkAndUpdate(Connection c, URL[] sqlScripts, SchemaAction[] actions)
			throws SQLException, IOException {
		/*
		 * Check preconditions
		 */
		if (sqlScripts == null)
			throw new IllegalArgumentException(
					"SQL script array cannot be null");
		if (sqlScripts.length < 1)
			throw new IllegalArgumentException(
					"SQL script array cannot be empty");
		for (URL f : sqlScripts)
			if (f == null)
				throw new IllegalArgumentException(
						"elements of SQL script array cannot be null: "
								+ sqlScripts);
		if (actions != null && sqlScripts.length != actions.length)
			throw new IllegalArgumentException(
					"sqlScripts.length == actions.length when actions != null");

		final int programSchemaVersion = sqlScripts.length - 1;

		final Statement st = c.createStatement();
		try {

			final int dbSchemaVersion = getVersion(st);

			if (dbSchemaVersion < programSchemaVersion) {

				/*
				 * If the database is empty create the VERSION table.
				 */
				if (dbSchemaVersion == -1)
					createVersionTable(st);

				for (int i = 0; i < sqlScripts.length; i++) {
					if (i > dbSchemaVersion) {
						/*
						 * Update the schema in the database.
						 */
						runScript(sqlScripts[i], st);
						/*
						 * Run the action on the database (optional).
						 */
						if (actions != null)
							if (actions[i] != null)
								actions[i].run(c);
					}
				}

				/*
				 * Update the schema version number in the database. (We avoid
				 * the database call if the version is 0 since the
				 * createVersionTable(st) call sets the version to 0.)
				 */
				if (programSchemaVersion != 0)
					setVersion(programSchemaVersion, st);
			}
		} finally {
			st.close();
		}
	}

	/**
	 * Creates the table to hold the schema version number in the database and
	 * sets the schema to version 0.
	 * 
	 * @param st
	 *            an open statement.
	 * @throws SQLException
	 *             if an unexpected problem interacting with the database
	 *             occurs.
	 */
	private static void createVersionTable(final Statement st)
			throws SQLException {
		assert st != null;

		try {
			st.execute(Q_DELETE);
		} catch (SQLException e) {
			// will fail if the database is completely empty
		}
		st.execute(Q_CREATE);
		st.execute(Q_INSERT);
	}

	/**
	 * Gets the schema version number in the database.
	 * 
	 * @param st
	 *            an open statement.
	 * @return the schema version number from the database, or -1 if the VERSION
	 *         table does not exist in the database (or is corrupted.)
	 */
	private static int getVersion(final Statement st) {
		assert st != null;

		int result = -1;
		try {
			final ResultSet ver = st.executeQuery(Q_SELECT);
			while (ver.next()) {
				result = ver.getInt(1);
			}
		} catch (SQLException e) {
			/*
			 * Ignore, this exception occurred because the schema was not found
			 * within the database.
			 */
		}
		return result;
	}

	/**
	 * Sets the schema version in the database.
	 * 
	 * @param version
	 *            the version number to set.
	 * @param st
	 *            an open statement.
	 * @throws SQLException
	 *             if an unexpected problem interacting with the database
	 *             occurs.
	 */
	private static void setVersion(final int version, final Statement st)
			throws SQLException {
		st.execute(Q_UPDATE + version);
	}

	private static void runScript(final URL script, final Statement st)
			throws SQLException, IOException {
		assert script != null;
		assert st != null;

		final List<StringBuilder> stmts = getSQLStatements(script);
		for (StringBuilder b : stmts) {
			st.execute(b.toString());
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
	 * @param sqlScript
	 *            the SQL script.
	 * @return a list of SQL statements (with the semicolons removed) suitable
	 *         for executing via JDBC.
	 * @throws IOException
	 *             if a problem occurs while reading the SQL script.
	 */
	public static List<StringBuilder> getSQLStatements(final URL sqlScript)
			throws IOException {
		if (sqlScript == null)
			throw new IllegalArgumentException("sqlFile must be non-null");

		List<StringBuilder> result = new ArrayList<StringBuilder>();

		final InputStream is = sqlScript.openStream();
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		try {
			StringBuilder b = new StringBuilder();
			String buffer;
			while ((buffer = br.readLine()) != null) {
				buffer = buffer.trim();
				if (buffer.startsWith("--") || buffer.equals("")) {
					// comment or blank line -- ignore this line
				} else if (buffer.endsWith(";")) {
					// end of an SQL statement -- add to our resulting list
					if (b.length() > 0)
						b.append("\n");
					b.append(buffer);
					b.deleteCharAt(b.length() - 1); // remove the ";"
					result.add(b);
					b = new StringBuilder();
				} else {
					// add this line (with a newline after the first line)
					if (b.length() > 0)
						b.append("\n");
					b.append(buffer);
				}
			}
			br.readLine();
		} finally {
			br.close();
		}
		return result;
	}
}
