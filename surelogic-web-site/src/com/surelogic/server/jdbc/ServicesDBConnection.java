package com.surelogic.server.jdbc;

import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.server.jdbc.schema.ServicesSchemaData;

public final class ServicesDBConnection extends DerbyConnection {
  private static final ServicesDBConnection INSTANCE = new ServicesDBConnection();

  public static ServicesDBConnection getInstance() {
    return INSTANCE;
  }

  private ServicesDBConnection() {
    // singleton
  }

  @Override
  protected String getDatabaseLocation() {
    final String DB_LOCATION_KEY = "surelogic.services.db.location";
    final String dbLoc = System.getProperty(DB_LOCATION_KEY);
    if (dbLoc == null || "".equals(dbLoc))
      throw new IllegalStateException(I18N.err(329, DB_LOCATION_KEY));
    else
      return System.getProperty(DB_LOCATION_KEY);
  }

  @Override
  protected String getSchemaName() {
    return "SERVICES";
  }

  public SchemaData getSchemaLoader() {
    return new ServicesSchemaData();
  }
}
