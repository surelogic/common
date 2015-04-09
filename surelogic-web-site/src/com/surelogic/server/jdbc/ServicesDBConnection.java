package com.surelogic.server.jdbc;

import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.server.jdbc.schema.ServicesSchemaData;

public final class ServicesDBConnection extends DerbyConnection {
  private static final ServicesDBConnection INSTANCE = new ServicesDBConnection();
  private static final String DB_LOCATION_KEY = "surelogic.services.db.location";

  public static ServicesDBConnection getInstance() {
    return INSTANCE;
  }

  private ServicesDBConnection() {
    // singleton
  }

  @Override
  protected String getDatabaseLocation() {
    final String dbLoc = System.getProperty(DB_LOCATION_KEY);
    if (dbLoc == null || "".equals(dbLoc)) {
      throw new IllegalStateException(DB_LOCATION_KEY + " System property must be set.");
    }
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
