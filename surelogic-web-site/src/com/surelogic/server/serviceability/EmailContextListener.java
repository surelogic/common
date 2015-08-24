package com.surelogic.server.serviceability;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class EmailContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    ServletContext config = event.getServletContext();

    // create and store the EmailConfig
    final String as = config.getInitParameter("emailAs");
    final String secret = config.getInitParameter("emailSecret");
    Email.start(as, secret);
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    Email.stop();
  }

}
