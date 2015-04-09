package com.surelogic.server.serviceability;

import static com.surelogic.common.serviceability.ServiceabilityConstants.TITLE_PREFIX;
import static com.surelogic.server.serviceability.BugzillaConstants.COMPONENT;
import static com.surelogic.server.serviceability.BugzillaConstants.DESCRIPTION;
import static com.surelogic.server.serviceability.BugzillaConstants.ID;
import static com.surelogic.server.serviceability.BugzillaConstants.PRODUCT;
import static com.surelogic.server.serviceability.BugzillaConstants.SUMMARY;
import static com.surelogic.server.serviceability.BugzillaConstants.VERSION;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransport;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

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
    final String redirectUrl = resp.encodeRedirectURL("http://surelogic.com");
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
      final Map<String, String> requestFields = new HashMap<String, String>();
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
      Email.sendEmail(emailSubject.toString(), emailBodyText, null, sendBCC);

      log("Checking if related to licenses");
      if (!isLicenseRelated(requestFields)) {
        handleBugzilla(requestFields, bugzillaDescription, logContents);
      }
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

  private static final boolean sendBCC = false;

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

  private boolean isLicenseRelated(final Map<String, String> fields) {
    final String subject = fields.get("subject");
    return subject != null && subject.contains("License");
  }

  // Bugzilla code
  private boolean handleBugzilla(final Map<String, String> fields, final String description, final String log) {
    final Properties props = Email.emailConfig.get().getBugzillaProperties();
    try {
      final URL url = new URL(props.getProperty("bugzillaURL") + "xmlrpc.cgi");
      final XmlRpcClient client = setupBugzillaClient(url);

      // Login to Bugzilla
      final int userid = login(client, props.getProperty("bugzillaUser"), props.getProperty("bugzillaPassword"));
      if (userid < 0) {
        return false;
      }

      // Try to create user
      final String from = fields.get("from");
      final int angleBracket = from.indexOf('<');
      String fromEmail = null;
      if (angleBracket >= 0) {
        final int endBracket = from.indexOf('>', angleBracket);
        if (endBracket >= 0) {
          final String fullName = from.substring(0, angleBracket).trim();
          final String email = from.substring(angleBracket + 1, endBracket).trim();
          final int ccid = createUser(client, email, fullName);
          if (ccid >= 0) {
            fromEmail = email;
            if (ccid > 0) {
              sendNewAccountEmail(props, fromEmail);
            }
          }
        }
      }

      final int bugid = createBug(client, fromEmail, fields, description);
      if (bugid < 0) {
        return false;
      }

      try {
        addAttachment(client, bugid, log);
      } catch (Throwable t) {
        log("Unexpected error while adding attachment for bug#" + bugid, t);
      }
      /*
       * Apparently, Bugzilla is creating this email for me, so this is no
       * longer needed
       * 
       * // Send email to us, and to them, so first build content SLLogger.
       * getLogger().info("Sending bug creation email to "+fromEmail); final
       * StringBuilder content = new
       * StringBuilder(props.getProperty("bugzillaURL"));
       * content.append("show_bug.cgi?id="
       * ).append(bugid).append(CRLF).append(CRLF);
       * 
       * content.append(description);
       * sendEmail("[Bug "+bugid+"] New: "+getSummary(fields),
       * content.toString(), fromEmail);
       */
    } catch (final MalformedURLException e) {
      log("Couldn't parse URL", e);
    }
    return false;
  }

  private void sendNewAccountEmail(final Properties props, final String fromEmail) {
    SLLogger.getLogger().info("Sending new account email to " + fromEmail);
    final StringBuilder content = new StringBuilder();
    content.append("We've created a Bugzilla account for you to track your issues,").append(CRLF);
    content.append("so please follow the link below to reset the password:").append(CRLF);
    content.append(CRLF);
    content.append(props.getProperty("bugzillaURL"));
    content.append("index.cgi?GoAheadAndLogIn=1#forgot").append(CRLF);
    content.append(CRLF);
    Email.sendEmail("Surelogic Bugzilla Account Created", content.toString(), fromEmail, sendBCC);
  }

  private XmlRpcClient setupBugzillaClient(final URL url) {
    final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);
    /*
     * For HTTPS config.setBasicUserName("edwin");
     * config.setBasicPassword("scramble");
     */
    final HttpClient httpClient = new HttpClient();
    httpClient.getParams().setParameter("http.protocol.single-cookie-header", true);

    final XmlRpcClient client = new XmlRpcClient();
    final XmlRpcCommonsTransportFactory factory = new XmlRpcCommonsTransportFactory(client) {
      Cookie[] cookies;

      @Override
      public XmlRpcTransport getTransport() {
        return new XmlRpcCommonsTransport(this) {
          @Override
          protected XMLReader newXMLReader() throws XmlRpcException {
            final XMLReader r = SAXParsers.newXMLReader();
            try {
              r.setFeature("http://xml.org/sax/features/validation", false);
              r.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (final SAXNotRecognizedException e) {
              e.printStackTrace();
            } catch (final SAXNotSupportedException e) {
              e.printStackTrace();
            }
            return r;
          }

          @Override
          protected void initHttpHeaders(final XmlRpcRequest pRequest) throws XmlRpcClientException {
            super.initHttpHeaders(pRequest);
            if (cookies != null) {
              httpClient.getState().addCookies(cookies);
            }
          }

          @Override
          protected void close() throws XmlRpcClientException {
            cookies = httpClient.getState().getCookies();
            super.close();
          }
        };
      }
    };
    client.setConfig(config);
    client.setTransportFactory(factory);
    factory.setHttpClient(httpClient);
    return client;
  }

  /**
   * @return the id of the user
   */
  @SuppressWarnings("rawtypes")
  private int login(final XmlRpcClient client, final String login, final String password) {
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put("login", login);
    args.put("password", password);
    args.put("remember", Boolean.TRUE);
    args.put("Bugzilla_remember", Boolean.TRUE);
    Map result1;
    try {
      result1 = (Map) client.execute("User.login", new Object[] { args });
    } catch (final XmlRpcException e) {
      log("Unable to log into Bugzilla", e);
      return -1;
    }
    return (Integer) result1.get(ID);
  }

  /**
   * @return 0 if user exists, -1 if error, and otherwise, the new user id
   */
  @SuppressWarnings("rawtypes")
  private int createUser(final XmlRpcClient client, final String email, final String fullName) {
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put("email", email);
    args.put("full_name", fullName);
    args.put("password", "");
    Map result2;
    try {
      result2 = (Map) client.execute("User.create", new Object[] { args });
    } catch (final XmlRpcException e) {
      if (e.code == 501 || e.getMessage().contains("already an account with the login name")) {
        return 0; // User already exists
      }
      log("Unable to create user", e);
      return -1;
    }
    return (Integer) result2.get(ID);
  }

  @SuppressWarnings("rawtypes")
  private int createBug(final XmlRpcClient client, final String cc, final Map<String, String> fields, final String body) {
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put(COMPONENT, "Eclipse");
    setProductVersion(args, fields.get("subject"));
    args.put("op_sys", getOS(fields));
    args.put("platform", "All");
    args.put(SUMMARY, getSummary(fields));
    args.put(DESCRIPTION, body);
    if (cc != null) {
      args.put("cc", new String[] { cc });
    }
    SLLogger.getLogger().info("Trying to create Bugzilla entry");
    /*
     * for(Map.Entry<String,Object> e : args.entrySet()) {
     * SLLogger.getLogger().info(e.getKey()+" => "+e.getValue()); }
     */
    Map result3;
    try {
      result3 = (Map) client.execute("Bug.create", new Object[] { args });
    } catch (final XmlRpcException e) {
      // Try again with an unspecified version
      args.put(VERSION, "unspecified");
      try {
        result3 = (Map) client.execute("Bug.create", new Object[] { args });
      } catch (final XmlRpcException e2) {
        log("Unable to create Bugzilla entry", e2);
        return -1;
      }
    }
    return (Integer) result3.get(ID);
  }

  private void setProductVersion(final Map<String, Object> args, final String subject) {
    if (subject == null || subject.isEmpty()) {
      args.put(PRODUCT, "JSure");
      args.put(VERSION, "unspecified");
      args.put("severity", "normal");
      return;
    }
    final StringTokenizer st = new StringTokenizer(subject);
    final String product = st.nextToken();
    if ("Sierra".equals(product)) {
      args.put(PRODUCT, "Sierra");
    } else if ("Flashlight".equals(product)) {
      args.put(PRODUCT, "Flashlight");
    } else {
      args.put(PRODUCT, "JSure");
    }

    if (!st.hasMoreTokens()) {
      args.put(VERSION, "unspecified");
      args.put("severity", "normal");
      return;
    }
    final String version = st.nextToken();
    if (Character.isDigit(version.charAt(0))) {
      int i = 1;
      for (int dots = 0; i < version.length(); i++) {
        final char c = version.charAt(i);
        if (c == '.') {
          dots++;
          if (dots == 3) {
            break; // At most 3 version numbers
          }
        } else if (!Character.isDigit(c)) {
          break; // End of version
        }
      }
      // TODO check if there is such a version?
      args.put(VERSION, version.substring(0, i));
    } else {
      args.put(VERSION, "unspecified");
    }
    args.put("severity", subject.contains("roblem") ? "normal" : "enhancement");
  }

  private String getSummary(final Map<String, String> fields) {
    final String summary = fields.get("summary");
    if (summary == null || summary.isEmpty()) {
      return fields.get("subject");
    }
    return summary;
  }

  private String getOS(final Map<String, String> fields) {
    final String os = fields.get("os");
    if (os == null) {
      return "All";
    }
    if (os.contains("Windows")) {
      return "Windows";
    }
    if (os.contains("Linux")) {
      return "Linux";
    }
    return "All";
  }

  private int addAttachment(final XmlRpcClient client, int bug, String log) {
    final Map<String, Object> args = new HashMap<String, Object>();
    args.put("ids", new Object[] { bug });
    args.put("data", Base64.encodeBase64(StringUtils.getBytesUtf8(log)));
    args.put("file_name", "crash_logs.txt");
    args.put("summary", "Config/log files");
    args.put("content_type", "text/plain");

    SLLogger.getLogger().info("Trying to add Bugzilla attachment to bug#" + bug);
    /*
     * for(Map.Entry<String,Object> e : args.entrySet()) {
     * SLLogger.getLogger().info(e.getKey()+" => "+e.getValue()); }
     */
    Map<?, ?> result3;
    try {
      result3 = (Map<?, ?>) client.execute("Bug.add_attachment", new Object[] { args });
    } catch (final XmlRpcException e) {
      log("Unable to add Bugzilla attachment to bug#" + bug, e);
      return -1;
    }
    if (result3 == null) {
      log("No attachment data received for bug#" + bug);
      return -1;
    }
    return bug;
  }
}
