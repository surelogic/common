package com.surelogic.common.jdbc;

import java.sql.Connection;

/**
 * A helper implementation of {@link DBTransaction} that produces no result.
 */
public abstract class NullDBTransaction implements DBTransaction<Void> {

	@Override
  public final Void perform(final Connection conn) throws Exception {
		doPerform(conn);
		return null;
	}

	abstract public void doPerform(Connection conn) throws Exception;
}
