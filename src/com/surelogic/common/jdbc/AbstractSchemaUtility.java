/*
 * Created on Dec 7, 2007
 */
package com.surelogic.common.jdbc;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractSchemaUtility implements ISchemaUtility {
  public void checkAndUpdate(final Connection c, final boolean serverDB)
    throws SQLException, IOException 
  {
    final int arrayLength = getSchemaVersion() + 1;
    final IDBType db = getDBType(c);
    final URL[] scripts = new URL[arrayLength];
    final SchemaAction[] schemaActions = new SchemaAction[arrayLength];
    for (int i = 0; i < scripts.length; i++) {
      final String num = SchemaUtility.getZeroPadded(i);
      scripts[i] = getSchemaScript(db.getPrefix(), num);
      schemaActions[i] = getSchemaAction(db.getPrefix(), num, serverDB);
    }
    SchemaUtility.checkAndUpdate(c, scripts, schemaActions);
  }
  
  protected abstract SchemaAction getSchemaAction(String prefix, String num, boolean isServer);
  protected abstract URL getSchemaScript(String prefix, String num);

  protected abstract int getSchemaVersion();
  protected abstract IDBType getDBType(Connection c) throws SQLException;
}
