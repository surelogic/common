package com.surelogic.server.serviceability;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicensePersistence;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.server.SiteUtil;

public class CreateLicenseRequestServlet extends HttpServlet {

  static final String PARAM_NAME = "name";
  static final String PARAM_EMAIL = "email";
  static final String PARAM_COMPANY = "company";
  static final String PARAM_COMMUNITY = "community";

  static final String PROBLEM = "<h3>There is a problem with your request</h3>";
  static final String GO_BACK = "<p>Please press the back button and fix your information";

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
    String email = req.getParameter(PARAM_EMAIL);
    boolean emailLooksValid = email != null && email.contains("@") && email.length() <= 254;
    if (!emailLooksValid) {
      resp.getWriter().println(PROBLEM);
      resp.getWriter().println("The email address you provided <tt>" + email + "</tt> is not valid.");
      resp.getWriter().println(GO_BACK);
      return;
    }
    email = email.trim();

    String name = req.getParameter(PARAM_NAME);
    boolean nameLooksValid = name != null && name.length() > 2 && name.length() <= 100;
    if (!nameLooksValid) {
      resp.getWriter().println(PROBLEM);
      resp.getWriter().println("The name you provided <tt>" + name + "</tt> is not valid. (It must be less than 100 characters.)");
      resp.getWriter().println(GO_BACK);
      return;
    }
    name = name.trim();

    String company = req.getParameter(PARAM_NAME);
    boolean companyLooksValid = company == null || (company != null && company.length() <= 100);
    if (!companyLooksValid) {
      resp.getWriter().println(PROBLEM);
      resp.getWriter()
          .println("The company you provided <tt>" + company + "</tt> is is not valid. (It must be less than 100 characters.)");
      resp.getWriter().println(GO_BACK);
      return;
    }
    company = company == null ? "" : company.trim();

    final boolean communityLicense = req.getParameter(PARAM_COMMUNITY) != null;

    final String holder = name + " (" + email + (company == null ? ")" : ") " + company)
        + (communityLicense ? " - Community License" : " - Trial License");

    int durationInDays = 90;
    Date installBeforeDate = new Date();
    SLLicenseType type = SLLicenseType.PERPETUAL;
    int installationLimit = 2;

    final SLLicense license = new SLLicense(UUID.randomUUID(), holder, SLLicenseProduct.ALL_TOOLS, durationInDays,
        installBeforeDate, type, installationLimit, true);
    final SignedSLLicense sLicense = SignedSLLicense.getInstance(license, SiteUtil.getKey());

    String licenseHexString = sLicense.getSignedHexString();

    if (emailLooksValid) {
      email = email.trim();
      Email.sendEmail("Test of CL", "Your license:\n\n" + licenseHexString, email);
      resp.getWriter().println("Success email sent to " + email);
    } else {
      resp.getWriter().println("Problem...Please press the back button and check your information.");
    }
  }
}
