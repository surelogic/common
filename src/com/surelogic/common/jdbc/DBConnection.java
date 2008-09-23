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

}
