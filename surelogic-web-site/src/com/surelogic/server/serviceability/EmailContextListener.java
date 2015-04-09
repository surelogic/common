package com.surelogic.server.serviceability;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.serviceability.Email.EmailConfig;

public class EmailContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    // nothing to do
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    ServletContext config = event.getServletContext();
    // build the JavaMail configuration
    final Properties jmProps = new Properties();
    jmProps.setProperty("mail.transport.protocol", "smtp");
    jmProps.setProperty("mail.smtp.host", config.getInitParameter("emailServerHost"));
    jmProps.setProperty("mail.smtp.port", config.getInitParameter("emailServerPort"));

    // create and store the EmailConfig
    final String from = config.getInitParameter("fromAddress");
    final String to = config.getInitParameter("toAddress");

    SLLogger.getLogger().info("Setting up Bugzilla properties");
    final Properties bugProps = new Properties();
    bugProps.setProperty("bugzillaUser", to);
    bugProps.setProperty("bugzillaPassword", config.getInitParameter("bugzillaPassword"));
    bugProps.setProperty("bugzillaURL", config.getInitParameter("bugzillaURL"));

    Email.start(new EmailConfig(jmProps, from, to, bugProps));
  }

}
