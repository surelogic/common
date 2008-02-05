package com.surelogic.common.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface ISchemaUtility {
	static final String SQL_SCRIPT_SUFFIX = ".sql";
	static final String SEPARATOR = "_";

	void checkAndUpdate(final Connection c, boolean isServer)
			throws IOException, SQLException, FutureDatabaseException;
}
