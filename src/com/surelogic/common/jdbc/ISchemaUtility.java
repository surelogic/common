/*
 * Created on Dec 7, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.common.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface ISchemaUtility {
  void checkAndUpdate(final Connection c, boolean isServer) throws IOException, SQLException;
}
