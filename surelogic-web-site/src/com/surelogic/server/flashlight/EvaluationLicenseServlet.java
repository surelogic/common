package com.surelogic.server.flashlight;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.Date;
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

import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.license.PossiblyActivatedSLLicense;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseNetCheck;
import com.surelogic.common.license.SLLicensePersistence;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.common.license.SignedSLLicenseNetCheck;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class EvaluationLicenseServlet extends HttpServlet {
    /* 15 Days */
    private static final int EVALUATION_DURATION = 15;
    /* 30 Days */
    private static final long INSTALL_BEFORE_PERIOD = 1000L * 60 * 60 * 24 * 30;
    private final Logger log = SLLogger
            .getLoggerFor(EvaluationLicenseServlet.class);
    private String fromEmail;
    private Properties jmProps;

    /**
	 * 
	 */
    private static final long serialVersionUID = -6106318445466861039L;

    @Override
    protected void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException,
            IOException {
        final String firstName = req.getParameter("firstName").trim();
        final String lastName = req.getParameter("lastName").trim();
        final String company = req.getParameter("company").trim();
        final String email = req.getParameter("email").trim();
        if (valid(firstName, lastName, email)) {
            if (generateAndEmailLicense(firstName, lastName, company, email)) {
                resp.sendRedirect("success.html");
            } else {
                resp.sendRedirect("fail.html");
            }
        } else {
            resp.getWriter()
                    .println(
                            "Oops.  A required parameter was not filled out.  Please press the back button and fill in your first name, last name, and email address.");
        }
    }

    private boolean valid(final String... strings) {
        for (final String s : strings) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
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
        jmProps.setProperty("mail.smtp.host",
                config.getInitParameter("emailServerHost"));
        jmProps.setProperty("mail.smtp.port",
                config.getInitParameter("emailServerPort"));

        // create and store the EmailConfig
        fromEmail = config.getInitParameter("fromAddress");
    }

    private static final String SUBJECT = "Flashlight License";

    private static final String CONTENT = "Thank you for taking the time to evaluate Flashlight.  Attached to this email is a license file valid for the next 15 days.  You will need to install this license after you download the Eclipse plugin.  Instructions on how to download and install Flashlight can be found at the following URL:"
            + "\n\nhttp://surelogic.com/static/eclipse/install.html\n\n"
            + "Once you have installed our tool, we encourage you to run through the tutorial, which is available through the Eclipse help documentation.  Please feel free to email any questions/suggestions you may have to support@surelogic.com, or simply send a tip for improvement through Eclipse from the Flashlight menu.";

    /*
     * Returns false if we can't find the address, true otherwise
     */
    private boolean generateAndEmailLicense(final String firstName,
            final String lastName, final String company, final String toEmail) {
        // Generate the license file
        try {
            final File lic = File.createTempFile("flashlight", ".lic");
            try {

                final SLLicense license = new SLLicense(UUID.randomUUID(),
                        toEmail, SLLicenseProduct.FLASHLIGHT,
                        EVALUATION_DURATION, new Date(
                                System.currentTimeMillis()
                                        + INSTALL_BEFORE_PERIOD),
                        SLLicenseType.USE, 1, true);
                final SignedSLLicense sLicense = SignedSLLicense.getInstance(
                        license, getKey());
                SignedSLLicenseNetCheck sLicenseNetCheck;
                if (!license.performNetCheck()) {
                    /*
                     * We need to generate the network check as well
                     */
                    final Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, license.getDurationInDays());
                    final SLLicenseNetCheck nc = new SLLicenseNetCheck(
                            license.getUuid(), cal.getTime());
                    sLicenseNetCheck = SignedSLLicenseNetCheck.getInstance(nc,
                            getKey());
                } else {
                    sLicenseNetCheck = null;
                }
                final PossiblyActivatedSLLicense iLicense = new PossiblyActivatedSLLicense(
                        sLicense, sLicenseNetCheck);
                SLLicensePersistence.writeLicenseToFile(lic, iLicense);
                // get the email receiver config and create a JavaMail session
                final Session session = Session.getInstance(jmProps);

                // create and populate a JavaMail email
                final MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(fromEmail));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(
                        toEmail));
                msg.setSubject(SUBJECT);
                msg.setSentDate(new Date());

                final Multipart multiPart = new MimeMultipart();
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(CONTENT);
                multiPart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();
                final DataSource source = new FileDataSource(lic);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName("flashlight.lic");
                multiPart.addBodyPart(messageBodyPart);
                msg.setContent(multiPart);
                // transmit the email
                Transport.send(msg);
                ServicesDBConnection.getInstance().withTransaction(
                        new NullDBQuery() {
                            @Override
                            public void doPerform(final Query q) {
                                q.prepared(
                                        "WebServices.insertEvaluationLicense")
                                        .call(new Date(), toEmail, firstName,
                                                lastName, company, lic);
                            }
                        });
                return true;
            } catch (final SendFailedException e) {
                log("Error emailing flashlight license request to: " + toEmail,
                        e);
                return false;
            } catch (final Exception e) {
                log("Error emailing flashlight license request to: " + toEmail,
                        e);
                throw new IllegalStateException(e);
            } finally {
                lic.delete();
            }
        } catch (final IOException e) {
            log.log(Level.SEVERE, "Error creating temporarty license file", e);
            throw new IllegalStateException(e);
        }
    }

    private PrivateKey getKey() {
        try {
            Class<?> pkUtility = Class
                    .forName("com.surelogic.key.SLPrivateKeyUtility");
            Method getKey = pkUtility.getMethod("getKey", new Class<?>[0]);
            return (PrivateKey) getKey.invoke(pkUtility, new Object[] {});
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (SecurityException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        throw new IllegalStateException(
                "The server failed in an unexpected fashion. Check server logs.");
    }

}
