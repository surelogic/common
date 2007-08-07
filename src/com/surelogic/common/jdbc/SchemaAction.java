package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaAction {

	void run(final Connection c) throws SQLException;
}
