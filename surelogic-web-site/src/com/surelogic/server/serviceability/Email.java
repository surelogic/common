package com.surelogic.server.serviceability;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;

import com.surelogic.common.logging.SLLogger;

public class Email {

  /**
   * This servlet's configuration, set up in the {@link #init(ServletConfig)}
   * method.
   */
  static final AtomicReference<EmailConfig> emailConfig = new AtomicReference<EmailConfig>();
  private static ExecutorService executor;

  static void start(final EmailConfig config) {
    emailConfig.set(config);
    executor = Executors.newSingleThreadExecutor();
  }

  static void stop() {
    executor.shutdown();
  }

  /**
   * Send an email to the surelogic support address.
   * 
   * @param subject
   * @param content
   */
  static void adminEmail(final String subject, final String content) {
    sendEmail(subject, content, null, false);
  }

  /**
   * Sends an email using the {@link EmailConfig} settings.
   * 
   * @param subject
   *          the subject for the email
   * @param content
   *          the content of the request
   * @param to
   *          the email address of the target
   * @param sendBCC
   *          whether or not to send as a blind carbon-copy
   */
  static void sendEmail(final String subject, final String content, final String to, final boolean sendBCC) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        final EmailConfig config = emailConfig.get();
        try {
          // get the email receiver config and create a JavaMail
          // session
          final Session session = Session.getInstance(config.getJavaMailProperties());

          // create and populate a JavaMail email
          final MimeMessage msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(config.getFrom()));
          if (to == null) {
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(config.getTo()));
          } else {
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            if (sendBCC) {
              msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(config.getTo()));
            }
          }
          msg.setSubject(subject);
          msg.setSentDate(new Date());
          msg.setContent(content, "text/plain");

          // transmit the email
          Transport.send(msg);
        } catch (final MessagingException me) {
          final StringBuilder msg = new StringBuilder("Error emailing support request to: ");
          if (to != null) {
            msg.append(to);
            if (sendBCC) {
              msg.append(", ").append(config.getTo());
            }
          } else {
            msg.append(config.getTo());
          }
          SLLogger.getLogger().log(Level.WARNING, msg.toString(), me);
        }
      }
    });
  }

  /**
   * Created in {@link SupportRequestServlet#init(ServletConfig)} to store
   * parameters needed to relay support requests via email. This class is
   * thread-safe, but should be considered immutable and not modified after
   * initial construction.
   * 
   */
  static final class EmailConfig {
    /**
     * Properties needed to send emails via JavaMail.
     */
    private final Properties javaMailProperties;

    /**
     * The email's sender.
     */

    private final String from;

    /**
     * The email's recipient.
     */
    private final String to;

    private final Properties bugzillaProps;

    public EmailConfig(final Properties javaMailProperties, final String from, final String to, final Properties bugProps) {
      super();
      this.javaMailProperties = javaMailProperties;
      this.from = from;
      this.to = to;
      bugzillaProps = bugProps;
    }

    public Properties getJavaMailProperties() {
      return javaMailProperties;
    }

    public Properties getBugzillaProperties() {
      return bugzillaProps;
    }

    public String getFrom() {
      return from;
    }

    public String getTo() {
      return to;
    }
  }

}
