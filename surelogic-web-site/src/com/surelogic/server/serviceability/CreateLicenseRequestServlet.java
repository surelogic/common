package com.surelogic.server.serviceability;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.SLUtility;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class CreateLicenseRequestServlet extends HttpServlet {

  static final String PARAM_NAME = "name";
  static final String PARAM_EMAIL = "email";
  static final String PARAM_COMPANY = "company";
  static final String PARAM_COMMUNITY = "community";

  static final String PROBLEM = "<h3>There is a problem with your request</h3>";
  static final String GO_BACK = "<p>Please press the back button and fix your information</p></html>";

  static final String SUCCESS = "<h3>Thanks!</h3>";

  private static final long serialVersionUID = 5071297227487022607L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    final PrintWriter out = resp.getWriter();

    out.println("<html>");
    out.println("<head>");
    out.println("<title>SureLogic License Request</title>");
    out.println("</head>");
    String email = req.getParameter(PARAM_EMAIL);
    email = email == null ? "" : email.trim();
    boolean emailLooksValid = email.contains("@") && email.length() > 2 && email.length() <= 254;
    if (!emailLooksValid) {
      out.println(PROBLEM);
      out.println("The email address you provided <tt>" + email + "</tt> is not syntactically valid.");
      out.println(GO_BACK);
      return;
    }
    email = email.trim();

    String name = req.getParameter(PARAM_NAME);
    name = name == null ? "" : name.trim();
    boolean nameLooksValid = name.length() > 2 && name.length() <= 100;
    if (!nameLooksValid) {
      out.println(PROBLEM);
      out.println("The name you provided <tt>" + name + "</tt> is not valid. Your entry must be less than 100 characters.");
      out.println(GO_BACK);
      return;
    }

    String company = req.getParameter(PARAM_COMPANY);
    company = company == null ? "" : company.trim();
    boolean companyLooksValid = company.length() <= 100;
    if (!companyLooksValid) {
      out.println(PROBLEM);
      out.println(
          "The company you provided <tt>" + company + "</tt> is is not valid. Your entry must be less than 100 characters.");
      out.println(GO_BACK);
      return;
    }
    boolean companyEntered = company.length() > 0;

    final boolean communityLicense = req.getParameter(PARAM_COMMUNITY) != null;
    final String licenseType = communityLicense ? "Community" : "Trial";

    final String holder = name + " (" + email + ") " + (companyEntered ? company + " " : "") + licenseType + " License";
    final String emailForDb = email;
    final String nameForDb = name;
    final String companyForDb = companyEntered ? company : "Personal Copy";
    final int durationInDays = communityLicense ? 2 : 60;
    final SLLicenseType type = communityLicense ? SLLicenseType.PERPETUAL : SLLicenseType.USE;
    final int installationLimit = communityLicense ? 4 : 2;

    final SLLicense license = new SLLicense(UUID.randomUUID(), holder, SLLicenseProduct.ALL_TOOLS, durationInDays, null, type,
        installationLimit, true);
    final SignedSLLicense sLicense = SignedSLLicense.getInstance(license, SiteUtil.getKey());
    final String licenseHexString = SLUtility.wrap(sLicense.getSignedHexString(), 58);

    final ServicesDBConnection conn = ServicesDBConnection.getInstance();
    conn.withTransaction(new DBQuery<Void>() {
      public Void perform(Query q) {
        q.prepared("WebServices.insertLicenseWebRequest").call(license.getUuid().toString(), emailForDb, nameForDb, companyForDb,
            licenseType);
        return null;
      }
    });

    Email.sendEmail("Your SureLogic " + licenseType + " License", "Your license:\n\n" + licenseHexString, email);
    out.println(SUCCESS);
    out.println("<p>Your " + licenseType + " License has been emailed to " + emailForDb + " please check your inbox.</p>");
    out.println("<p><a href=\"http://surelogic.com\">Return to the SureLogic website</a></p></html>");
  }
}
