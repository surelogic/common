package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnection {

	public Connection readOnlyConnection() throws SQLException;

	public Connection transactionConnection() throws SQLException;

	public Connection getConnection() throws SQLException;

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withReadOnly(final DBQuery<T> action);

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withReadOnly(final DBTransaction<T> action);

	/**
	 * Perform a query transaction
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withTransaction(final DBQuery<T> action);

	/**
	 * Perform a query transaction.
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withTransaction(final DBTransaction<T> action);

	/**
	 * Returns an object that allows access to the schema and version
	 * information for this database.
	 * 
	 * @return
	 */
	SchemaData getSchemaLoader();

	/**
	 * Disconnect the database
	 */
	void shutdown();
	
	/**
	 * Unload and delete the database.
	 */
	void destroy();

	/**
	 * Boots and checks the embedded database but logs any problems rather than
	 * throwing an exception.
	 * <p>
	 * Multiple calls to this method are benign, only the first call boots and
	 * checks the embedded database.
	 * 
	 * @see #bootAndCheckSchema()
	 */
	public void loggedBootAndCheckSchema();

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
	public void bootAndCheckSchema() throws Exception;
}
