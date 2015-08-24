package com.surelogic.server.serviceability;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
    jmProps.setProperty("mail.smtp.starttls.enable", "true");
    jmProps.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    jmProps.setProperty("mail.smtp.auth", "true");

    // create and store the EmailConfig
    final String from = config.getInitParameter("fromAddress");
    final String to = config.getInitParameter("toAddress");

    Email.start(new EmailConfig(jmProps, from, to));
  }

}
