package com.surelogic.server.serviceability;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * This servlet accepts a POST where the body of data is emailed to the
 * SureLogic engineers. The source of data will be improvement suggestions, and
 * bug reports sent within Eclipse by users.
 * <p>
 * A GET request to this servlet returns a redirect to the surelogic.com home
 * page. <br>
 * 
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
   * Receives a body of data and emails it to the SureLogic engineers.
   */
  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      // create the email body buffer, and append the requester's IP
      final StringBuilder emailBody = new StringBuilder();

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
          emailBody.append(line).append(CRLF);
        }
      } finally {
        rd.close();
      }

      final String emailBodyText = emailBody.toString();

      final StringBuilder emailSubject = new StringBuilder("SUPPORT_REQUEST ");
      final String summary = requestFields.get("summary");
      emailSubject.append(summary == null ? "No Summary" : summary);
      final String subject = requestFields.get("subject");
      if (subject != null) {
        emailSubject.append(" (").append(subject).append(')');
      }
      Email.sendEmail(emailSubject.toString(), emailBodyText, null, tryToGetEmail(requestFields));
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, "Error processing SureLogic support request", e);
    }
  }

  /**
   * Parses a key/value pair from a string, and puts the key/value pair into the
   * fields map. If the key/value separator is not present, the entire line is
   * used as the key with a null value.
   * 
   * @param fields
   *          from the top of the message
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
   * Tries to find a reply-to email address. If none can be dtermined null is
   * returned.
   * 
   * @param fields
   *          from the top of the message
   * @return a reply-to address or null
   */
  @Nullable
  private String tryToGetEmail(final Map<String, String> fields) {
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
}