package com.surelogic.server.serviceability;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.logging.SLLogger;

public class Email {

  static final AtomicReference<ExecutorService> executor = new AtomicReference<>(null);
  static final AtomicReference<String> emailAs = new AtomicReference<>(null);
  static final AtomicReference<String> emailSecret = new AtomicReference<>(null);

  static void start(String as, String secret) {
    executor.set(Executors.newSingleThreadExecutor());
    emailAs.set(as);
    emailSecret.set(secret);
  }

  static void stop() {
    final ExecutorService toStop = executor.getAndSet(null);
    toStop.shutdown();
  }

  /**
   * Send an email to the surelogic support address.
   * 
   * @param subject
   * @param content
   */
  static void sendSupportEmail(final String subject, final String content) {
    sendEmail(subject, content, null);
  }

  /**
   * Sends an email constructed from the passed information. This returns
   * immediately, it does not block. The email is sent on a background thread.
   * 
   * @param subject
   *          the subject for the email
   * @param content
   *          the content of the request
   * @param to
   *          the email address to send the message to or
   *          <tt>support@surelogic.com</tt> if null.
   */
  static void sendEmail(@NonNull final String subject, @NonNull final String content, @Nullable final String to) {
    sendEmail(subject, content, to, null);
  }

  /**
   * Sends an email constructed from the passed information. This returns
   * immediately, it does not block. The email is sent on a background thread.
   * 
   * @param subject
   *          the subject for the email
   * @param content
   *          the content of the request
   * @param to
   *          the email address to send the message to or
   *          <tt>support@surelogic.com</tt> if null.
   * @param replyTo
   *          the email address to reply to, this is ignored if the address
   *          invalid or null.
   */
  static void sendEmail(@NonNull final String subject, @NonNull final String content, @Nullable final String to,
      @Nullable final String replyTo) {
    final String actualTo = to != null ? to : "support@surelogic.com";
    executor.get().execute(new Runnable() {
      @Override
      public void run() {
        try {
          final Properties props = new Properties();
          props.setProperty("mail.transport.protocol", "smtp");
          props.setProperty("mail.smtp.host", "smtp.gmail.com");
          props.put("mail.smtp.port", 587);
          props.setProperty("mail.smtp.starttls.enable", "true");
          props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
          props.put("mail.smtp.auth", "true");
          Authenticator auth;
          final String from = emailAs.get();
          final String secret = emailSecret.get();
          if ((from != null) && (from.length() > 0)) {
            auth = new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, secret);
              }

            };
          } else {
            auth = null;
          }
          final Session session = Session.getInstance(props, auth);

          // create and populate a JavaMail email
          final MimeMessage msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(from));
          msg.setRecipient(Message.RecipientType.TO, new InternetAddress(actualTo));
          msg.setSubject(subject);
          msg.setSentDate(new Date());
          msg.setContent(content, "text/plain");

          // attempt to setup the reply to
          if (replyTo != null) {
            try {
              final Address[] rt = InternetAddress.parse(replyTo);
              msg.setReplyTo(rt);
            } catch (Exception ignore) {
              // just ignore, probably the user gave us a bad email address
            }
          }

          // transmit the email
          Transport.send(msg);
        } catch (final MessagingException me) {
          final StringBuilder msg = new StringBuilder("Error emailing support request to: ");
          msg.append(actualTo);
          SLLogger.getLogger().log(Level.WARNING, msg.toString(), me);
        }
      }
    });
  }
}
