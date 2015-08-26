package com.surelogic.server.serviceability;

import static com.surelogic.common.serviceability.ServiceabilityConstants.TITLE_PREFIX;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.LongIdHandler;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.jdbc.ServicesDBConnection;

/**
 * This servlet accepts a POST where the body of data is persisted to a
 * database, and relayed via email to SureLogic staff. The main source of data
 * will be license events, improvement suggestions, and bug reports via our
 * client applications.<br>
 * A GET request to this servlet returns a redirect to the surelogic.com home
 * page. <br>
 * Here is a sample configuration for this servlet:<br>
 * 
 * <pre>
 * &lt;servlet&gt;
 * 		&lt;servlet-name&gt;Support Request&lt;/servlet-name&gt;
 * 		&lt;servlet-class&gt;
 * 			com.surelogic.server.serviceability.SupportRequestServlet
 * 		&lt;/servlet-class&gt;
 * 		
 * 		&lt;!-- The email address that will receive all support request submissions --&gt;
 * 		&lt;init-param&gt;
 * 			&lt;param-name&gt;toAddress&lt;/param-name&gt;
 * 			&lt;param-value&gt;engineering@surelogic.com&lt;/param-value&gt;
 * 		&lt;/init-param&gt;
 * 		&lt;init-param&gt;
 * 			&lt;param-name&gt;fromAddress&lt;/param-name&gt;
 * 			&lt;param-value&gt;support-request-service@surelogic.com&lt;/param-value&gt;
 * 		&lt;/init-param&gt;
 * 		&lt;init-param&gt;
 * 		    &lt;param-name&gt;emailServerHost&lt;/param-name&gt;
 * 		    &lt;param-value&gt;aspmx.l.google.com&lt;/param-value&gt;
 * 		&lt;/init-param&gt;
 * 		&lt;init-param&gt;
 * 			&lt;param-name&gt;emailServerPort&lt;/param-name&gt;
 * 			&lt;param-value&gt;25&lt;/param-value&gt;
 * 		&lt;/init-param&gt;
 * 	&lt;/servlet&gt;
 * </pre>
 */
public class SupportRequestServlet extends HttpServlet {
  private static final long serialVersionUID = -890724069440107378L;

  /**
   * Line-feed used when building email body.
   */
  private static final String CRLF = "\r\n";

  /**
   * Get requests just respond with a redirect to the SureLogic home page.
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    final String redirectUrl = resp.encodeRedirectURL(SLUtility.SERVICEABILITY_SERVER);
    resp.sendRedirect(redirectUrl);
  }

  /**
   * Receives a body of data, logs the request, and forwards it to a
   * pre-configured email address.
   */
  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    // log the IP and hostname of the requester
    final StringBuilder remoteLocation = new StringBuilder();
    remoteLocation.append(req.getRemoteAddr());
    remoteLocation.append(" (").append(req.getRemoteHost()).append(")");
    SLLogger.getLogger().info("Request received from: " + remoteLocation.toString());

    try {
      String bugzillaDescription = null;
      String logContents = null;
      StringBuilder logContentsBuilder = null;

      // create the email body buffer, and append the requester's IP
      final StringBuilder emailBody = new StringBuilder();
      emailBody.append("Received from: ").append(remoteLocation).append(CRLF).append(CRLF);

      // Copy the support request contents to an email body, and parse
      // request fields into a Map
      final Map<String, String> requestFields = new HashMap<>();
      final BufferedReader rd = req.getReader();
      try {
        String line;
        int blankLineCount = 0;
        while ((line = rd.readLine()) != null) {
          line = line.trim();
          if ("".equals(line)) {
            blankLineCount++;
          } else if (blankLineCount < 3) {
            putFieldLine(requestFields, line, ":");
          }
          // Check if I've hit an inline attachment
          if (bugzillaDescription == null && line.startsWith(TITLE_PREFIX)) {
            // Only need the body up to this point
            bugzillaDescription = emailBody.toString();
            SLLogger.getLogger().info("Got Bugzilla description");
          }
          if (logContents == null && line.startsWith(TITLE_PREFIX)) {
            SLLogger.getLogger().info("Started collecting the log");
            logContents = "";
            logContentsBuilder = new StringBuilder();

          }
          // But don't include the title
          else if (logContentsBuilder != null) {
            if (line.startsWith(TITLE_PREFIX)) {
              // Starting new attachment, so end log
              logContents = logContentsBuilder.toString();
              logContentsBuilder = null;
              SLLogger.getLogger().info("Done collecting the log");
            } else {
              logContentsBuilder.append(line).append(CRLF);
            }
          }
          emailBody.append(line).append(CRLF);
        }
        // Create the description, if not already done
        if (bugzillaDescription == null) {
          bugzillaDescription = emailBody.toString();
          SLLogger.getLogger().info("Got Bugzilla description");
        }
        // Finish off the log, if not already done
        if (logContentsBuilder != null) {
          logContents = logContentsBuilder.toString();
          logContentsBuilder = null;
          SLLogger.getLogger().info("Done collecting the log");
        }
      } finally {
        rd.close();
      }

      // persist the request to the database
      final String emailBodyText = emailBody.toString();
      final Long requestId = recordSupportRequest(req, requestFields, emailBodyText);

      // send the request contents to the email address configured in the
      // web.xml
      final StringBuilder emailSubject = new StringBuilder();
      emailSubject.append("[SR");
      if (requestId != null) {
        emailSubject.append(" #").append(requestId);
      }
      emailSubject.append("] ");
      final String subject = requestFields.get("subject");
      emailSubject.append(subject == null ? "No Subject" : subject);
      final String from = requestFields.get("from");
      emailSubject.append(from == null ? "" : " from " + from);
      Email.sendEmail(emailSubject.toString(), emailBodyText, null, tryToGetEmail(requestFields));
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, "Error processing support request from: " + remoteLocation.toString(), e);
    }
  }

  /**
   * Parses a key/value pair from a string, and puts the key/value pair into the
   * fields map. If the key/value separator is not present, the entire line is
   * used as the key with a null value.
   * 
   * @param fields
   *          the map that stores key/value pairs
   * @param line
   *          the line to parse
   * @param fieldSeparator
   *          the separator between the key and value
   */
  private void putFieldLine(final Map<String, String> fields, final String line, final String fieldSeparator) {
    final int separatorIndex = line.indexOf(fieldSeparator);
    if (separatorIndex == -1) {
      fields.put(line.trim(), null);
    } else {
      final String key = line.substring(0, separatorIndex).trim().toLowerCase();
      final String value = line.length() <= separatorIndex + 1 ? "" : line.substring(separatorIndex + 1).trim();
      fields.put(key, value);
    }
  }

  /**
   * Persists the request headers and data to a database
   * 
   * @param req
   *          the http request object, used to extract http headers
   * @param requestType
   *          the request's type ("Tip for improvement", "License Installation",
   *          etc)
   * @param body
   *          the request body, as a byte array
   * @param from
   *          the sender of the support request
   */
  private Long recordSupportRequest(final HttpServletRequest req, final Map<String, String> requestFields, final String body) {
    try {
      final ServicesDBConnection dbConn = ServicesDBConnection.getInstance();
      final Long requestId = dbConn.withTransaction(new DBQuery<Long>() {

        @Override
        public Long perform(final Query q) {
          final Queryable<Long> insertSR = q.prepared("WebServices.insertSupportRequest", new LongIdHandler());

          // copy the http request headers to a string
          final StringBuilder headersBuf = new StringBuilder();
          final Enumeration<?> headerEn = req.getHeaderNames();
          while (headerEn.hasMoreElements()) {
            final Object headerKey = headerEn.nextElement();
            if (headerKey != null) {
              headersBuf.append(headerKey).append(": ");
              headersBuf.append(req.getHeader(headerKey.toString()));
              headersBuf.append(CRLF);
            }
          }

          // trim the header and body data to valid sizes
          final Object headers = Nulls.coerce(clipString(headersBuf.toString(), 4096));
          final Object clippedBody = Nulls.coerce(clipString(body, 32672));

          // get the request context
          final Object remoteIP = Nulls.coerce(req.getRemoteAddr());
          final Object remoteHost = Nulls.coerce(req.getRemoteHost());

          // get the common support request fields
          final Object subject = Nulls.coerce(requestFields.get("subject"));
          final Object from = Nulls.coerce(requestFields.get("from"));
          final Object clientOS = Nulls.coerce(requestFields.get("os"));
          final Object clientJava = Nulls.coerce(requestFields.get("java"));
          final Object clientIDE = Nulls.coerce(requestFields.get("ide"));

          // get the license fields, if present
          final Object licenseID = Nulls.coerce(requestFields.get("id"));
          final Object licenseTool = Nulls.coerce(requestFields.get("tool"));
          final Object licenseHolder = Nulls.coerce(requestFields.get("holder"));
          final String licExpStr = requestFields.get("expires");
          Object licenseExpiration = Nulls.DATE;
          try {
            if (licExpStr != null) {
              licenseExpiration = Nulls.coerce(SLUtility.fromStringDay(licExpStr));
            }
          } catch (final ParseException pe) {
            // license expiration is already set to Nulls.DATE
          }

          // run the SQL insert to persist the support request
          return insertSR.call(remoteIP, remoteHost, subject, headers, clippedBody, from, clientOS, clientJava, clientIDE,
              licenseID, licenseTool, licenseHolder, licenseExpiration);
        }
      });
      return requestId;
    } catch (final Exception e) {
      log("Error persisting support request to the database", e);
      return null;
    }
  }

  @Nullable
  String tryToGetEmail(final Map<String, String> fields) {
    final String from = fields.get("from");
    final int angleBracket = from.indexOf('<');
    if (angleBracket >= 0) {
      final int endBracket = from.indexOf('>', angleBracket);
      if (endBracket >= 0) {
        final String email = from.substring(angleBracket + 1, endBracket).trim();
        return email;
      }
    }
    return null;
  }

  /**
   * Returns a string that is clipped to a maximum length. All characters above
   * the maximum length are removed.
   * 
   * @param value
   *          the string to clip
   * @param maxLength
   *          the maximum number of characters to return
   * @return
   */
  String clipString(final String value, final int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }
}
