package com.surelogic.common.derby;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jdbc.AbstractDBConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.common.logging.SLLogger;

public abstract class DerbyConnection extends AbstractDBConnection {

    /**
     * Gets if the embedded database should be deleted upon startup of the IDE.
     * <p>
     * This implementation never deletes the database, however, subclasses are
     * allowed to override this method.
     *
     * @return {@code true} if the embedded database should be deleted upon
     *         startup of the IDE, {@code false} otherwise.
     */
    protected boolean deleteDatabaseOnStartup() {
        return false;
    }

    /**
     * Sets, for use upon IDE restart, if the embedded database should be
     * deleted.
     *
     * @param value
     *            {@code true} indicates the embedded database should be deleted
     *            when the IDE restarts, {@code false} indicates that it should
     *            not.
     */
    protected void setDeleteDatabaseOnStartup(final boolean value) {
        // do nothing
    }

    /**
     * Gets the path to the embedded database.
     *
     * @return the path to the embedded database.
     */
    protected abstract String getDatabaseLocation();

    /**
     * Gets the schema name for the embedded database.
     *
     * @return the schema name for the embedded database.
     */
    protected abstract String getSchemaName();

    /**
     * Template method that constructs the JDBC connection URL.
     *
     * @return the JDBC connection URL.
     * @see #getDatabaseLocation()
     * @see #getSchemaName()
     */
    protected final String getConnectionURL() {
        return JDBC_PRE + getDatabaseLocation()
                + (getSchemaName() == null ? "" : JDBC_POST + getSchemaName());
    }

    protected static final String JDBC_PRE = "jdbc:derby:";
    protected static final String JDBC_POST = ";user=";
    protected static final String JDBC_SHUTDOWN = ";shutdown=true";

    /**
     * Boots and checks the embedded database but logs any problems rather than
     * throwing an exception.
     * <p>
     * Multiple calls to this method are benign, only the first call boots and
     * checks the embedded database.
     *
     * @see #bootAndCheckSchema()
     */
    @Override
    public void loggedBootAndCheckSchema() {
        try {
            bootAndCheckSchema();
        } catch (final Exception e) {
            SLLogger.getLogger().log(Level.SEVERE,
                    "Failure to boot and check schema.", e);
        }
    }

    /**
     * Flag to indicate if the embedded database has been booted and checked.
     */
    protected boolean f_booted = false;

    /**
     * Boots and checks the embedded database. This method is suitable to call
     * within an Eclipse {@code Activator} because if it fails it will stop the
     * plug-in from loading. Within NetBeans it is better to call
     * {@link #loggedBootAndCheckSchema()} from the {@code getInstance()} method
     * of the {@code Data} class.
     * <p>
     * Multiple calls to this method are benign, only the first call boots and
     * checks the embedded database.
     *
     * @throws Exception
     *             if a failure occurs.
     * @see #loggedBootAndCheckSchema()
     */
    @Override
    public synchronized void bootAndCheckSchema() throws Exception {
        if (!f_booted) {
            final File dbDir = new File(getDatabaseLocation());
            if (deleteDatabaseOnStartup()) {
                /*
                 * Delete the database
                 */
                try {
                    if (dbDir.exists()) {
                        if (FileUtility.recursiveDelete(dbDir)) {
                            SLLogger.getLogger().info(
                                    "Database deleted at startup : "
                                            + getDatabaseLocation());
                        } else {
                            SLLogger.getLogger().log(
                                    Level.SEVERE,
                                    "Unable to delete database at startup : "
                                            + getDatabaseLocation());
                        }
                    }
                } finally {
                    setDeleteDatabaseOnStartup(false);
                }
            }
            final SchemaData loader = getSchemaLoader();
            final URL derbyProps = loader.getSchemaResource("derby.properties");
            if (derbyProps != null) {
                final InputStream props = derbyProps.openStream();
                try {
                    System.getProperties().load(props);
                } finally {
                    props.close();
                }
            } else {
                SLLogger.getLogger().warning("Couldn't find derby.properties");
            }
            Derby.bootEmbedded();

            final String connectionURL = getConnectionURL() + ";create=true";
            final Connection c = DriverManager.getConnection(connectionURL);
            SLLogger.getLogger().fine("Booting " + connectionURL + ".");
            Exception e = null;
            try {
                c.setAutoCommit(false);
                SchemaUtility.checkAndUpdate(c, loader, false);
                c.commit();
            } catch (final Exception exc) {
                e = exc;
            } finally {
                try {
                    c.close();
                } catch (final Exception exc) {
                    if (e == null) {
                        e = exc;
                    }
                }
            }
            if (e != null) {
                throw e;
            }
            f_booted = true;
        }
    }

    /**
     * Gets if the embedded database has been booted and checked.
     *
     * @return {@code true} if the embedded database has been booted and
     *         checked, {@code false} otherwise.
     */
    public synchronized boolean isBooted() {
        return f_booted;
    }

    @Override
    public Connection getConnection() throws SQLException {
        final Connection conn = LazyPreparedStatementConnection
                .wrap(DriverManager.getConnection(getConnectionURL()));
        return conn;
    }

    @Override
    public synchronized void shutdown() {
        if (f_booted) {
            try {
                synchronized (this) {
                    DriverManager.getConnection(getConnectionURL()
                            + JDBC_SHUTDOWN);
                }
                throw new IllegalStateException("The database at "
                        + getConnectionURL() + " did not shut down properly.");
            } catch (final SQLException e) {
                if (e.getErrorCode() == 45000) {
                    SLLogger.getLogger().log(Level.FINE, e.getMessage());
                } else {
                    throw new IllegalStateException("The database at "
                            + getConnectionURL()
                            + " did not shut down properly.", e);
                }
            }
            f_booted = false;
        }
    }

    @Override
    public synchronized void destroy() {
        shutdown();

        final File dbLoc = new File(getDatabaseLocation());
        if (dbLoc.exists()) {
            FileUtility.recursiveDelete(dbLoc);
        }
    }

}
