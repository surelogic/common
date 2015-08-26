package com.surelogic.server.serviceability;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class CreateLicenseRequestServlet extends HttpServlet {

  static private final String PARAM_NAME = "name";
  static private final String PARAM_EMAIL = "email";
  static private final String PARAM_COMPANY = "company";
  static private final String PARAM_COMMUNITY = "community";

  static private final int DURATION_TRIAL_LICENSE = 60; // days
  static private final int DURATION_COMMUNITY_LICENSE = 180; // days (~6 months)

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
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.badEmail", email));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    String name = req.getParameter(PARAM_NAME);
    name = name == null ? "" : name.trim();
    boolean nameLooksValid = name.length() > 2 && name.length() <= 100;
    if (!nameLooksValid) {
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.badName", name));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    String company = req.getParameter(PARAM_COMPANY);
    company = company == null ? "" : company.trim();
    boolean companyLooksValid = company.length() <= 100;
    if (!companyLooksValid) {
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.badCompany", company));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }
    boolean companyEntered = company.length() > 0;

    /*
     * Construct information for license and database
     */
    final boolean communityLicense = req.getParameter(PARAM_COMMUNITY) != null;
    final String licenseType = communityLicense ? "Community" : "Trial";
    final String holder = name + " (" + email + ") " + (companyEntered ? company + " " : "") + licenseType + " License";
    final String emailForDb = email;
    final String nameForDb = name;
    final String companyForDb = companyEntered ? company : "Personal Copy";
    final int durationInDays = communityLicense ? DURATION_COMMUNITY_LICENSE : DURATION_TRIAL_LICENSE;
    final SLLicenseType type = communityLicense ? SLLicenseType.PERPETUAL : SLLicenseType.USE;
    final int installationLimit = communityLicense ? 4 : 2;

    final SLLicense license = new SLLicense(UUID.randomUUID(), holder, SLLicenseProduct.ALL_TOOLS, durationInDays, null, type,
        installationLimit, true);
    final SignedSLLicense sLicense = SignedSLLicense.getInstance(license, SiteUtil.getKey());
    final String licenseHexString = SLUtility.wrap(sLicense.getSignedHexString(), 58);

    final ServicesDBConnection conn = ServicesDBConnection.getInstance();
    final AllowLicenseHandler allowLicense = new AllowLicenseHandler();
    final String result = conn.withTransaction(new DBQuery<String>() {
      public String perform(Query q) {
        q.prepared("WebServices.getLatestLicenseWebRequest", allowLicense).call(emailForDb);
        if (allowLicense.result) {
          q.prepared("WebServices.insertLicenseWebRequest").call(license.getUuid().toString(), emailForDb, nameForDb, companyForDb,
              licenseType);
          return "okay";
        } else
          return "failed due to previous trial";
      }
    });

    if (result.startsWith("failed")) {
      final String pastLicenseDate = (new SimpleDateFormat("dd MMM yyyy")).format(allowLicense.last);
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.pastTrialLicense", emailForDb, pastLicenseDate));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    Email.sendEmail("Your SureLogic " + licenseType + " License", "Your license:\n\n" + licenseHexString, email);
    out.println(I18N.msg("web.license.success.title"));
    out.println("<p>We appreciate your interest in SureLogic.</p>");
    out.println(
        "<p>Your " + licenseType + " License has been emailed to " + emailForDb + " &mdash; keep an eye on your inbox.</p>");
    out.println("<p><a href=\"http://surelogic.com\">Return to the SureLogic website</a></p></html>");
  }

  static class AllowLicenseHandler extends NullRowHandler {
    boolean result = false;
    @Nullable
    Timestamp last = null;

    @Override
    protected void doHandle(Row r) {
      final Timestamp t = r.nextTimestamp();
      if (t == null)
        result = true;
      else
        last = t;
    }
  }
}
