package com.surelogic.server.serviceability;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class EmailContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    Email.start();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    Email.stop();
  }

}
