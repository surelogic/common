package com.surelogic.server.serviceability;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.mail.Address;
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

  @NonNull
  static final AtomicReference<ExecutorService> executor = new AtomicReference<>(null);

  /**
   * Call before sending the first email. This method starts up the background
   * thread.
   * <p>
   * Later invoke {@link #stop()} to shutdown the background thread.
   */
  static void start() {
    executor.set(Executors.newSingleThreadExecutor());
  }

  /**
   * Call on orderly shutdown. This method stops the background thread.
   */
  static void stop() {
    final ExecutorService toStop = executor.getAndSet(null);
    toStop.shutdown();
  }

  /**
   * Send an email to the SureLogic support address&mdash;value of
   * {@link SLUtility#SERVICEABILITY_EMAIL}. This method returns immediately, it
   * does not block. The email is sent on a background thread and is sent from
   * the value of {@link SLUtility#SERVICEABILITY_EMAIL}.
   * 
   * @param subject
   *          the subject for the email
   * @param msgBody
   *          the content of the email
   * 
   * @throws IllegalArgumentException
   *           if a non-null parameter is null.
   * @throws IllegalStateException
   *           if {@link #start()} has not been invoked prior to this call.
   */
  static void sendSupportEmail(final String subject, final String msgBody) {
    sendEmail(subject, msgBody, null);
  }

  /**
   * Sends an email constructed from the passed information. This method returns
   * immediately, it does not block. The email is sent on a background thread
   * and is sent from the value of {@link SLUtility#SERVICEABILITY_EMAIL}.
   * 
   * @param subject
   *          the subject for the email
   * @param msgBody
   *          the content of the email
   * @param to
   *          the email address to send the message to&mdash;if null the value
   *          of {@link SLUtility#SERVICEABILITY_EMAIL} is used.
   * 
   * @throws IllegalArgumentException
   *           if a non-null parameter is null.
   * @throws IllegalStateException
   *           if {@link #start()} has not been invoked prior to this call.
   */
  static void sendEmail(@NonNull final String subject, @NonNull final String msgBody, @Nullable final String to) {
    sendEmail(subject, msgBody, to, null);
  }

  /**
   * Sends an email constructed from the passed information. This method returns
   * immediately, it does not block. The email is sent on a background thread
   * and is sent from the value of {@link SLUtility#SERVICEABILITY_EMAIL}.
   * 
   * @param subject
   *          the subject for the email
   * @param msgBody
   *          the content of the email
   * @param to
   *          the email address to send the message to&mdash;if null the value
   *          of {@link SLUtility#SERVICEABILITY_EMAIL} is used.
   * @param replyTo
   *          the email address to send a reply to&mdash;if null reply to is not
   *          set for the email.
   * 
   * @throws IllegalArgumentException
   *           if a non-null parameter is null.
   * @throws IllegalStateException
   *           if {@link #start()} has not been invoked prior to this call.
   */
  static void sendEmail(@NonNull final String subject, @NonNull final String msgBody, @Nullable String to,
      @Nullable final String replyTo) {
    if (subject == null)
      throw new IllegalArgumentException(I18N.err(44, "subject"));
    if (msgBody == null)
      throw new IllegalArgumentException(I18N.err(44, "msgBody"));
    final String actualTo = to != null ? to : SLUtility.SERVICEABILITY_EMAIL;
    @Nullable
    final ExecutorService es = executor.get();
    if (es == null) // start() called?
      throw new IllegalStateException(I18N.err(369));
    es.execute(new Runnable() {
      @Override
      public void run() {
        try {
          final Properties props = new Properties();
          final Session session = Session.getInstance(props, null);

          // create and populate a JavaMail email
          final MimeMessage msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(SLUtility.SERVICEABILITY_EMAIL));
          msg.setRecipient(Message.RecipientType.TO, new InternetAddress(actualTo));
          msg.setSubject(subject);
          msg.setSentDate(new Date());
          msg.setContent(msgBody, "text/plain");
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
