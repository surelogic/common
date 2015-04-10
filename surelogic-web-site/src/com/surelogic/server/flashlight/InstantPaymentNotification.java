package com.surelogic.server.flashlight;

import static com.surelogic.common.jdbc.Nulls.coerce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.license.PossiblyActivatedSLLicense;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseNetCheck;
import com.surelogic.common.license.SLLicensePersistence;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.common.license.SignedSLLicenseNetCheck;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

/**
 * This servlet handles the Instant Payment Notification callback that PayPal
 * makes once it has processed a transaction. The PayPal account must be set up
 * to do this manually.
 * 
 * @author nathan
 */
public class InstantPaymentNotification extends HttpServlet {

  private static final String SUBJECT = "Flashlight License";
  private static final String CONTENT = "Thank you for purchasing Flashlight.  Attached to this email is a license file valid for the next year (365 days).  You will need to install this license after you download the Eclipse plugin.  Instructions on how to download and install Flashlight can be found at the following URL:"
      + "\n\nhttp://surelogic.com/static/eclipse/install.html\n\n"
      + "Once you have installed our tool, we encourage you to run through the tutorial, which is available through the Eclipse help documentation.  Please feel free to email any questions/suggestions you may have to support@surelogic.com, or simply send a tip for improvement through Eclipse from the Flashlight menu.";
  private static final long ONE_MONTH = 1000L * 60 * 60 * 24 * 30;
  private String fromEmail;
  private Properties jmProps;

  final Logger log = SLLogger.getLoggerFor(InstantPaymentNotification.class);
  /**
   * The email address of our test PayPal account
   */
  private final String OUR_EMAIL = "suppor_1241627208_biz@surelogic.com";
  /**
   * The URL of the PayPal sandbox
   */
  private final String PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr";
  volatile String lastPost = "None";
  private static final long serialVersionUID = -6559521396125186934L;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    final Map<String, String[]> paramMap = req.getParameterMap();
    final boolean isTest = "1".equals(req.getParameter("test_ipn"));
    if (isTest) {
      // Record the parameter list for debugging purposes
      final StringBuilder b = new StringBuilder();
      b.append("<HTML><HEAD></HEAD><BODY>\n");
      final List<Entry<String, String[]>> params = new ArrayList<Entry<String, String[]>>(paramMap.entrySet());
      Collections.sort(params, new Comparator<Entry<String, String[]>>() {
        @Override
        public int compare(final Entry<String, String[]> e1, final Entry<String, String[]> e2) {
          return String.CASE_INSENSITIVE_ORDER.compare(e1.getKey(), e2.getKey());
        }
      });
      for (Entry<String, String[]> entry : params) {
        b.append("<P>");
        b.append(entry.getKey());
        b.append('=');
        String[] val = entry.getValue();
        b.append(val.length == 1 ? val[0] : Arrays.asList(val));
        b.append("</P>\n");
      }
      b.append("</BODY></HEAD>");
      lastPost = b.toString();
    }

    final HttpClient http = new HttpClient();
    final PostMethod post = new PostMethod(PAYPAL_URL);
    for (final Entry<String, String[]> en : paramMap.entrySet()) {
      final String name = en.getKey();
      final String[] values = en.getValue();
      for (final String value : values) {
        post.addParameter(new NameValuePair(name, value));
      }
    }
    post.addParameter(new NameValuePair("cmd", "_notify-validate"));
    http.executeMethod(post);
    final String response = post.getResponseBodyAsString();
    if ("VERIFIED".equals(response)) {
      // PayPal assures us that this is the correct information, so we act
      // on it
      final String txnId = req.getParameter("txn_id");
      final Date now = new Date();
      final String itemName = req.getParameter("item_name");
      final String itemNumber = req.getParameter("item_number");
      final String itemOption = req.getParameter("option_selection1");
      final String paymentStatus = req.getParameter("payment_status");
      final String paymentAmount = req.getParameter("mc_gross");
      final String paymentCurrency = req.getParameter("mc_currency");
      final String payerEmail = req.getParameter("payer_email");
      final String payerId = req.getParameter("payer_id");
      final String receiverEmail = req.getParameter("receiver_email");
      final String sendToEmail = req.getParameter("option_selection2");
      final String addressCountry = req.getParameter("address_country");
      final String addressCity = req.getParameter("address_city");
      final String addressCC = req.getParameter("address_country_code");
      final String addressName = req.getParameter("address_name");
      final String addressState = req.getParameter("address_state");
      final String addressStatus = req.getParameter("address_status");
      final String addressStreet = req.getParameter("address_street");
      final String addressZip = req.getParameter("address_zip");
      final String contactPhone = req.getParameter("contact_phone");
      final String firstName = req.getParameter("first_name");
      final String lastName = req.getParameter("lastName");
      final String payerBusinessName = req.getParameter("payer_business_name");
      // Confirm the transaction
      boolean gtg = true;
      if (!OUR_EMAIL.equals(receiverEmail)) {
        log.warning(String.format("The transmitted receiver email '%s' did not match ours.", receiverEmail));
        gtg = false;
      }
      final LicenseType type = LicenseType.checkItemCode(itemNumber, itemOption, paymentAmount, paymentCurrency);
      if (type == null) {
        log.warning(String
            .format(
                "We received a payment of %s %s for item %s, option %s.  This is either the wrong amount for this item, the item is not recognized, or this is an unknown currency.",
                paymentAmount, paymentCurrency, itemNumber, itemOption));
        gtg = false;
      }
      // check against all transaction id's to make sure it's not a dup
      gtg &= ServicesDBConnection.getInstance().withTransaction(new DBQuery<Boolean>() {

        @Override
        public Boolean perform(final Query q) {
          final String check = q.prepared("Payment.checkTxn", new StringResultHandler()).call(txnId);
          if (check == null) {
            q.prepared("Payment.insertPayment").call(txnId, now, itemName, itemNumber, coerce(itemOption), paymentStatus,
                paymentAmount, payerEmail, payerId, receiverEmail, coerce(sendToEmail), coerce(addressCountry),
                coerce(addressCity), coerce(addressCC), coerce(addressName), coerce(addressState), coerce(addressStatus),
                coerce(addressStreet), coerce(addressZip), coerce(contactPhone), coerce(firstName), coerce(lastName),
                coerce(payerBusinessName));
            return true;
          } else {
            log.warning(String.format(
                "We have already received an IPN notification for transaction %s.  This notification had status %s.", txnId,
                paymentStatus));
            return false;
          }
        }
      });
      if (gtg) {
        String destEmail;
        // We are good to go, let's send the email
        if (sendToEmail != null && sendToEmail.trim().length() != 0) {
          destEmail = sendToEmail.trim();
        } else {
          destEmail = payerEmail;
        }
        generateAndEmailLicense(destEmail, txnId, type);
      }
      log.info("Verified");
    } else if ("INVALID".equals(response)) {
      log.severe("Paypal indicated we received an invalid IPN.");
    } else {
      log.severe(String.format("%s is an unrecognized IPN validation response.", response));
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    // A GET to this servlet will respond with the last test post.
    resp.getWriter().print(lastPost);
  }

  /*
   * Returns false if we can't find the address, true otherwise
   */
  private boolean generateAndEmailLicense(final String toEmail, final String txnId, final LicenseType type) {
    // Generate the license file
    final File[] lics = new File[type.getLicenseCount()];
    try {
      for (int i = 0; i < lics.length; i++) {
        lics[i] = File.createTempFile("flashlight", ".lic");
      }

      final Date installBefore = new Date(System.currentTimeMillis() + ONE_MONTH);
      for (final File lic : lics) {
        final SLLicense license = new SLLicense(UUID.randomUUID(), toEmail, type.getLicenseProduct(), type.getDurationInDays(),
            installBefore, SLLicenseType.SUPPORT, 1, true);

        final SignedSLLicense sLicense = SignedSLLicense.getInstance(license, SiteUtil.getKey());
        final SignedSLLicenseNetCheck sLicenseNetCheck;
        if (!license.performNetCheck()) {
          /*
           * We need to generate the network check as well
           */
          final Calendar cal = Calendar.getInstance();
          cal.add(Calendar.DATE, license.getDurationInDays());
          final SLLicenseNetCheck nc = new SLLicenseNetCheck(license.getUuid(), cal.getTime());
          sLicenseNetCheck = SignedSLLicenseNetCheck.getInstance(nc, SiteUtil.getKey());
        } else {
          sLicenseNetCheck = null;
        }
        final PossiblyActivatedSLLicense iLicense = new PossiblyActivatedSLLicense(sLicense, sLicenseNetCheck);
        SLLicensePersistence.writeLicenseToFile(lic, iLicense);
      }
      // get the email receiver config and create a JavaMail session
      final Session session = Session.getInstance(jmProps);

      // create and populate a JavaMail email
      final MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(fromEmail));
      msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
      msg.setSubject(SUBJECT);
      msg.setSentDate(new Date());

      final Multipart multiPart = new MimeMultipart();
      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setText(CONTENT);
      multiPart.addBodyPart(messageBodyPart);
      if (lics.length == 1) {
        messageBodyPart = new MimeBodyPart();
        final DataSource source = new FileDataSource(lics[0]);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("flashlight.lic");
        multiPart.addBodyPart(messageBodyPart);
      } else {
        for (int i = 0; i < lics.length; i++) {
          messageBodyPart = new MimeBodyPart();
          final DataSource source = new FileDataSource(lics[i]);
          messageBodyPart.setDataHandler(new DataHandler(source));
          messageBodyPart.setFileName("flashlight-" + i + ".lic");
          multiPart.addBodyPart(messageBodyPart);
        }
      }
      msg.setContent(multiPart);
      // transmit the email
      Transport.send(msg);
      for (final File lic : lics) {
        ServicesDBConnection.getInstance().withTransaction(new NullDBQuery() {
          @Override
          public void doPerform(final Query q) {
            q.prepared("Payment.insertLicense").call(txnId, new Date(), toEmail, type.name(), lic);
          }
        });
      }
      return true;
    } catch (final SendFailedException e) {
      log.log(Level.INFO, "Error emailing flashlight license request to: " + toEmail, e);
      return false;
    } catch (final IOException e) {
      log.log(Level.SEVERE, "Error creating temporarty license file", e);
      throw new IllegalStateException(e);
    } catch (final Exception e) {
      log("Error emailing flashlight license request to: " + toEmail, e);
      throw new IllegalStateException(e);
    } finally {
      RuntimeException exc = null;
      for (final File lic : lics) {
        try {
          if (lic != null) {
            lic.delete();
          }
        } catch (final RuntimeException e) {
          if (exc != null) {
            exc = e;
          }
        }
        if (exc != null) {
          throw exc;
        }
      }
    }
  }

  /**
   * Initialize the {@link EmailConfig} for this servlet.
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    // build the JavaMail configuration
    jmProps = new Properties();
    jmProps.setProperty("mail.transport.protocol", "smtp");
    jmProps.setProperty("mail.smtp.host", config.getInitParameter("emailServerHost"));
    jmProps.setProperty("mail.smtp.port", config.getInitParameter("emailServerPort"));

    // create and store the EmailConfig
    fromEmail = config.getInitParameter("fromAddress");
  }
}
