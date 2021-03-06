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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class SchemaUtility {
	private static final Logger LOG = SLLogger.getLogger();

	private SchemaUtility() {
		// no instances
	}

	/**
	 * Checks the passed database connection is at the correct version. Versions
	 * start at 0 and go up as sequential integers. The current version of the
	 * database is held within a table named <code>VERSION</code> as its only
	 * row. The appropriate SQL script to go from version <i>n</i> - 1 to
	 * <i>n</i> is referenced by <code>sqlScripts[n]</code>. These scripts are
	 * always run <b>in sequence</b> from lowest to highest. Optionally, code
	 * may be passed for each version that is executed <b>after</b> the SQL
	 * script for that version.
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
	 * @throws FutureDatabaseException
	 *             if the database version is higher (newer) than what this code
	 *             expects.
	 */
	public static void checkAndUpdate(final Connection c,
			final URL[] sqlScripts, final SchemaAction[] actions)
			throws SQLException, IOException, FutureDatabaseException {
		/*
		 * Check preconditions
		 */
		if (sqlScripts == null) {
			throw new IllegalArgumentException(
					"SQL script array cannot be null");
		}
		if (sqlScripts.length < 1) {
			throw new IllegalArgumentException(
					"SQL script array cannot be empty");
		}

		if (actions != null) {
			if (sqlScripts.length != actions.length) {
				throw new IllegalArgumentException(
						"sqlScripts.length == actions.length when actions != null");
			}
			// Check if there is either a script or an action for every version
			for (int i = 0; i < sqlScripts.length; i++) {
				if ((sqlScripts[i] == null) && (actions[i] == null)) {
					throw new IllegalArgumentException(
							"There must be a script or action for version " + i);
				}
			}
		} else { // no actions, so there must be a script for every version
			for (final URL f : sqlScripts) {
				if (f == null) {
					throw new IllegalArgumentException(
							"elements of SQL script array cannot be null: "
									+ Arrays.toString(sqlScripts));
				}
			}
		}

		final int programSchemaVersion = sqlScripts.length - 1;

		final Statement st = c.createStatement();
		try {

			final int dbSchemaVersion = getVersion(st);

			/*
			 * Check if the database is too new for this code
			 */
			if (programSchemaVersion < dbSchemaVersion) {
				throw new FutureDatabaseException(programSchemaVersion,
						dbSchemaVersion);
			}

			if (dbSchemaVersion < programSchemaVersion) {

				/*
				 * If the database is empty create the VERSION table.
				 */
				if (dbSchemaVersion == -1) {
					createVersionTable(st);
				}

				for (int i = 0; i < sqlScripts.length; i++) {
					if (i > dbSchemaVersion) {
						/*
						 * Update the schema in the database.
						 */
						if (sqlScripts[i] != null) {
							runScript(sqlScripts[i], st);
						}
						/*
						 * Run the action on the database (optional).
						 */
						if (actions != null) {
							if (actions[i] != null) {
								runAction(actions[i], c);
							}
						}
					}
				}

				/*
				 * Update the schema version number in the database. (We avoid
				 * the database call if the version is 0 since the
				 * createVersionTable(st) call sets the version to 0.)
				 */
				if (programSchemaVersion != 0) {
					setVersion(programSchemaVersion, st);
				}

				LOG
						.info(I18N.msg("db.updatedVersion",
								programSchemaVersion, c));
			} else {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine(I18N.msg("db.atVersion", programSchemaVersion, c));
				}
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
			st.execute(QB.get(8));
		} catch (final SQLException e) {
			// will fail if the database is completely empty
		}
		st.execute(QB.get(7));
		st.execute(QB.get(10));
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
			final ResultSet ver = st.executeQuery(QB.get(9));
			try {
				while (ver.next()) {
					result = ver.getInt(1);
				}
			} finally {
				ver.close();
			}
		} catch (final SQLException e) {
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
		st.execute(QB.get(11, version));
	}

	/**
	 * Runs the given SQL script by reading each SQL statement from the file and
	 * then using the passed JDBC statement to execute the statement on the
	 * database.
	 * 
	 * @param script
	 *            the script to load and run on the database.
	 * @param st
	 *            an open JDBC statement to use.
	 * @throws SQLException
	 *             if an unexpected problem interacting with the database
	 *             occurs.
	 * @throws IOException
	 *             if the script cannot be found or read.
	 */
	public static void runScript(final URL script, final Statement st)
			throws SQLException, IOException {
		assert script != null;
		assert st != null;

		final List<StringBuilder> stmts = getSQLStatements(script);
		for (final StringBuilder b : stmts) {
			try {
				st.execute(b.toString());
			} catch (final SQLException e) {
				throw new IllegalStateException(I18N.err(12, b.toString(),
						script), e);
			}
		}

		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().fine(
					I18N.msg("db.ranSQLScript", script.getPath(), st
							.getConnection()));
		}
	}

	/**
	 * Runs the given schema action on the database.
	 * 
	 * @param action
	 *            the schema action to run on the database, may not be
	 *            <code>null</code>.
	 * @param c
	 *            the database connection to use.
	 * @throws SQLException
	 *             if an unexpected problem interacting with the database
	 *             occurs.
	 * @throws NullPointerException
	 *             if action is <code>null</code>.
	 */
	public static void runAction(final SchemaAction action, final Connection c)
			throws SQLException {
		action.run(c);

		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger()
					.fine(
							I18N.msg("db.ranSQLAction", action.getClass()
									.getName(), c));
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
		if (sqlScript == null) {
			throw new IllegalArgumentException("sqlFile must be non-null");
		}

		final List<StringBuilder> result = new ArrayList<>();

		final InputStream is = sqlScript.openStream();
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		try {
			StringBuilder b = new StringBuilder();
			String buffer;
			while ((buffer = br.readLine()) != null) {
				buffer = buffer.trim();
				if (buffer.startsWith("--") || "".equals(buffer)) {
					// comment or blank line -- ignore this line
				} else if (buffer.endsWith("<<>>")) {
					// end of an SQL statement -- add to our resulting list
					if (b.length() > 0) {
						b.append("\n");
					}
					b.append(buffer);
					b.delete(b.length() - 4, b.length());
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
		return result;
	}

	/**
	 * Pads the given positive integer with 0s and returns a string of at least
	 * 4 characters. For example: <code>getZeroPadded(0)</code> results in the
	 * string <code>"0000"</code>; <code>getZeroPadded(436)</code> results in
	 * the string <code>"0456"</code>; <code>getZeroPadded(56900)</code> results
	 * in the string <code>"56900"</code>.
	 * 
	 * @param n
	 *            a non-negative integer (i.e., n >=0).
	 * @return a
	 */
	public static String getZeroPadded(final int n) {
		assert n >= 0;

		String result = Integer.toString(n);
		while (result.length() < 4) {
			result = "0" + result;
		}
		return result;
	}

	private static final String SERVER_PREFIX = "server";
	private static final String ACTION_COMMON = "Schema_";
	private static final String ACTION_SERVER = "Server_";
	private static final String SEPARATOR = "_";

	public static void checkAndUpdate(final Connection c,
			final SchemaData schemaLoader, final boolean serverDB)
			throws SQLException, FutureDatabaseException {
		try {
			final int arrayLength = schemaLoader.getVersion() + 1;
			final DBType db = JDBCUtils.getDb(c);
			final URL[] scripts = new URL[arrayLength];
			final SchemaAction[] schemaActions = new SchemaAction[arrayLength];
			for (int i = 0; i < scripts.length; i++) {
				final String num = getZeroPadded(i);
				final Schema common = new Schema(schemaLoader, db, num, false);
				scripts[i] = common.script;

				schemaActions[i] = common.action;
				if (serverDB) {
					final Schema server = new Schema(schemaLoader, db, num,
							true);
					final boolean serverScriptOrAction = (server.script != null)
							|| (server.action != null);
					if (serverScriptOrAction) {
						final SchemaAction commonAction = common.action;
						final URL serverScript = server.script;
						final SchemaAction serverAction = server.action;
						schemaActions[i] = new SchemaAction() {
							@Override
              public void run(final Connection c)
									throws SQLException {
								/*
								 * This approach is very generic, the server may
								 * or may not define schema scripts and/or
								 * actions at each schema level. We only run
								 * them if they exist.
								 */
								if (commonAction != null) {
									SchemaUtility.runAction(commonAction, c);
								}

								if (serverScript != null) {
									final Statement st = c.createStatement();
									try {
										SchemaUtility.runScript(serverScript,
												st);
									} catch (final IOException e) {
										throw new SQLException(
												"IOException reading server script file "
														+ serverScript
																.getFile()
														+ " : " + e.toString());
									} finally {
										st.close();
									}
								}

								if (serverAction != null) {
									SchemaUtility.runAction(serverAction, c);
								}
							}
						};
					}
				} else if ((common.script == null) && (common.action == null)) {
					// not server, and there's no common script/action
					// Make sure that there's a server script/action
					final Schema server = new Schema(schemaLoader, db, num,
							true);
					final boolean serverScriptOrAction = (server.script != null)
							|| (server.action != null);
					if (serverScriptOrAction) {
						// Dummy action to satisfy constraint in
						// SchemaUtility.checkAndUpdate()
						// that there be an action or script for each version
						schemaActions[i] = new SchemaAction() {
							@Override
              public void run(final Connection c)
									throws SQLException {
								SLLogger.getLogger().fine(
										"Nothing to do in client; only server-side changes for version "
												+ num + ".");
							}
						};
					} else {
						throw new IllegalArgumentException(
								"No scripts/actions for version " + num);
					}
				}
			}
			SchemaUtility.checkAndUpdate(c, scripts, schemaActions);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static class Schema {

		final URL script;
		final SchemaAction action;

		Schema(final SchemaData schemaLoader, final DBType db,
				final String num, final boolean server) {
			final String name = db.getPrefix()
					+ (server ? SEPARATOR + SERVER_PREFIX : "") + SEPARATOR
					+ num + ".sql";
			script = schemaLoader.getSchemaResource(name);

			action = schemaLoader.getSchemaAction((server ? ACTION_SERVER
					: ACTION_COMMON)
					+ num);
		}
	}

}
