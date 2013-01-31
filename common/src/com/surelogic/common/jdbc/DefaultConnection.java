package com.surelogic.common.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.surelogic.common.jdbc.AbstractDBConnection;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.SchemaData;

/**
 * Connects to the 'default' JDBC {@link Connection}. This is useful for using a
 * connection in functions or procedures.
 * 
 * @author nathan
 * 
 */
public class DefaultConnection extends AbstractDBConnection {

	private DefaultConnection() {
		// Do nothing
	}

	@Override
  public void bootAndCheckSchema() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
  public void shutdown() {
		throw new UnsupportedOperationException();
	}
	
	@Override
  public void destroy() {
		throw new UnsupportedOperationException();
	}

	@Override
  public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:default:connection");
	}

	@Override
  public SchemaData getSchemaLoader() {
		return null;
	}

	@Override
  public void loggedBootAndCheckSchema() {
		throw new UnsupportedOperationException();
	}

	public static DBConnection getInstance() {
		return new DefaultConnection();
	}

}
