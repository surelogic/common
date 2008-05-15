package com.surelogic.common.jdbc;

import java.sql.Connection;

/**
 * A helper implementation of {@link DBTransaction} that produces no result.
 */
public abstract class DBTransactionNoResult implements DBTransaction<Void> {

	public final Void perform(Connection conn) throws Exception {
		doPerform(conn);
		return null;
	}

	abstract public void doPerform(Connection conn) throws Exception;
}
