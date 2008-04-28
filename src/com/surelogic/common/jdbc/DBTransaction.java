package com.surelogic.common.jdbc;

import java.sql.Connection;

public interface DBTransaction<T> {
	T perform(Connection conn) throws Exception;
}
