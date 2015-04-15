package com.surelogic.server.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;

/**
 * This context listener initializes the derby network server, so that
 * <tt>ij</tt> and other applications can run against this instance of derby.
 */
public final class NetworkServerContextListener implements ServletContextListener {

  private static final Logger log = SLLogger.getLoggerFor(NetworkServerContextListener.class);

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    try {
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (final SQLException e) {
      log.log(Level.INFO, "Derby shutdown", e);
    }
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    try {
      ServicesDBConnection.getInstance().bootAndCheckSchema();

    } catch (final Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
