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

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public class Email {

  static final AtomicReference<ExecutorService> executor = new AtomicReference<>(null);

  static void start() {
    executor.set(Executors.newSingleThreadExecutor());
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
   * @param msgBody
   *          the content of the request
   * @param to
   *          the email address to send the message to&mdash;if null the value
   *          of {@link SLUtility#SERVICEABILITY_EMAIL} is used.
   * @param replyTo
   *          the email address to send a reply to&mdash;if null the value of
   *          {@link SLUtility#SERVICEABILITY_EMAIL} is used.
   */
  static void sendEmail(@NonNull final String subject, @NonNull final String msgBody, @Nullable String to,
      @Nullable String replyTo) {
    if (subject == null)
      throw new IllegalArgumentException(I18N.err(44, "subject"));
    if (msgBody == null)
      throw new IllegalArgumentException(I18N.err(44, "msgBody"));
    final String actualTo = to != null ? to : SLUtility.SERVICEABILITY_EMAIL;
    final String actualReplyTo = replyTo != null ? replyTo : SLUtility.SERVICEABILITY_EMAIL;
    executor.get().execute(new Runnable() {
      @Override
      public void run() {
        try {
          final Properties props = new Properties();
          final Session session = Session.getInstance(props, null);

          // create and populate a JavaMail email
          final MimeMessage msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(actualReplyTo));
          msg.setRecipient(Message.RecipientType.TO, new InternetAddress(actualTo));
          msg.setSubject(subject);
          msg.setSentDate(new Date());
          msg.setContent(msgBody, "text/plain");
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
