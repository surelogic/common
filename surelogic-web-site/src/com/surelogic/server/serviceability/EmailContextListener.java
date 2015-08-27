package com.surelogic.server.serviceability;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.SLUtility;

public class EmailContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    ServletContext config = event.getServletContext();

    // create and store the EmailConfig
    final String as = config.getInitParameter("emailAs");
    final String secret = SLUtility.decodeBase64(config.getInitParameter("emailSecret"));
    Email.start(as, secret);
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    Email.stop();
  }

}
