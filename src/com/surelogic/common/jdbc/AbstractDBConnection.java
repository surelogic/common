package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public abstract class AbstractDBConnection implements DBConnection {

	public Connection readOnlyConnection() throws SQLException {
		final Connection conn = getConnection();
		conn.setReadOnly(true);
		return conn;
	}

	public Connection transactionConnection() throws SQLException {
		final Connection conn = getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withReadOnly(final DBQuery<T> action) {
		try {
			return with(readOnlyConnection(), action, true);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query in read-only mode
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withReadOnly(final DBTransaction<T> action) {
		try {
			return with(readOnlyConnection(), action, true);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query transaction
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withTransaction(final DBQuery<T> action) {
		try {
			return with(transactionConnection(), action, false);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	/**
	 * Perform a query transaction.
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 * @throws TransactionException
	 *             if an exception occurs.
	 */
	public <T> T withTransaction(final DBTransaction<T> action) {
		try {
			return with(transactionConnection(), action, false);
		} catch (final SQLException e) {
			throw new TransactionException("Could not establish connection.", e);
		}
	}

	private <T> T with(final Connection conn, final DBTransaction<T> t,
			final boolean readOnly) {
		Throwable exc = null;
		T val = null;
		try {
			val = t.perform(conn);
			if (!readOnly) {
				conn.commit();
			}
		} catch (final Throwable exc0) {
			exc = exc0;
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e) {
					SLLogger.getLogger().log(Level.WARNING, e.getMessage(), e);
				}
			}
		} finally {
			try {
				conn.close();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					SLLogger.getLogger().log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		if (exc != null) {
			throw new TransactionException(exc);
		}
		return val;
	}

	private <T> T with(final Connection conn, final DBQuery<T> t,
			final boolean readOnly) {
		Throwable exc = null;
		T val = null;
		try {
			val = t.perform(new ConnectionQuery(conn));
			if (!readOnly) {
				conn.commit();
			}
		} catch (final Throwable exc0) {
			exc = exc0;
			if (!readOnly) {
				try {
					conn.rollback();
				} catch (final SQLException e) {
					SLLogger.getLogger().log(Level.WARNING, e.getMessage(), e);
				}
			}
		} finally {
			try {
				conn.close();
			} catch (final SQLException e) {
				if (exc == null) {
					exc = new TransactionException(e);
				} else {
					SLLogger.getLogger().log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		if (exc != null) {
			throw new TransactionException(exc);
		}
		return val;
	}
}
