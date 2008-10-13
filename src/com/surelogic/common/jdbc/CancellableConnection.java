package com.surelogic.common.jdbc;

import java.sql.Connection;

public interface CancellableConnection extends Connection {
	public void cancelRunningStatement();
}
