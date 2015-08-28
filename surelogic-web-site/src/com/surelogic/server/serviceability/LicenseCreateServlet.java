package com.surelogic.server.serviceability;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseCreateServlet extends HttpServlet {

  /*
   * Duration of licenses. The duration for community license is for renewal
   * (not expiration).
   */
  static private final int DURATION_TRIAL = 60; // days
  static private final int DURATION_COMMUNITY = 180; // days (~6 months)

  /*
   * The installation limit (which can be increased in the administration
   * section of this site) for trial and community licenses.
   */
  static private final int INSTALLATION_LIMIT_TRIAL = 2;
  static private final int INSTALLATION_LIMIT_COMMUNITY = 4;

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

    // set to anything for community license (alternative is trial license)
    final boolean communityLicense = req.getParameter(I18N.msg("web.license.param.community")) != null;
    final String licenseType = communityLicense ? "Community" : "Trial";

    // set to anything to indicate preference for no email from SureLogic, ever.
    final boolean noEmail = req.getParameter(I18N.msg("web.license.param.noemail")) != null;

    // start response
    out.println(I18N.msg("web.license.response.html.header", licenseType));

    /*
     * Check inputs passed by the client (don't trust anything)
     */
    String email = req.getParameter(I18N.msg("web.license.param.email"));
    email = email == null ? "" : email.trim();
    boolean emailLooksValid = email.contains("@") && email.length() > 2 && email.length() <= 254;
    if (!emailLooksValid) {
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.badEmail", email));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    String name = req.getParameter(I18N.msg("web.license.param.name"));
    name = name == null ? "" : name.trim();
    boolean nameLooksValid = name.length() > 2 && name.length() <= 100;
    if (!nameLooksValid) {
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.badName", name));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    String company = req.getParameter(I18N.msg("web.license.param.company"));
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
     * Construct information for license and database.
     * 
     * We need to transfer the information into final variables so that they can
     * be referenced in the database query below.
     */
    final String holder = name + " (" + licenseType + " License)";
    final String emailForDb = email;
    final String nameForDb = name;
    final Object companyForDb = companyEntered ? company : Nulls.STRING;
    final int durationInDays = communityLicense ? DURATION_COMMUNITY : DURATION_TRIAL;
    final SLLicenseType type = communityLicense ? SLLicenseType.PERPETUAL : SLLicenseType.USE;
    final int installationLimit = communityLicense ? INSTALLATION_LIMIT_COMMUNITY : INSTALLATION_LIMIT_TRIAL;
    final String noEmailForDB = noEmail ? "true" : "false";

    final SLLicense license = new SLLicense(holder, emailForDb, companyEntered ? company : null, SLLicenseProduct.ALL_TOOLS,
        durationInDays, null, type, installationLimit, true);
    final SignedSLLicense sLicense = SignedSLLicense.getInstance(license, SiteUtil.getKey());
    final String licenseHexString = SLUtility.wrap(sLicense.getSignedHexString(), 58);

    final ServicesDBConnection conn = ServicesDBConnection.getInstance();
    final String result = conn.withTransaction(new DBQuery<String>() {
      public String perform(Query q) {
        /*
         * Restrict multiple trial licenses by email address. This check ignores
         * any community licenses provided to the email.
         * 
         * If the trial was over a year ago allow a new trial license.
         */
        if (!communityLicense) {
          final List<Timestamp> result = q.prepared("WebServices.getLatestLicenseWebRequest", new AllowLicenseHandler())
              .call(emailForDb, licenseType);
          result.remove(null); // so list will be empty if no previous trial
          if (!result.isEmpty()) {
            final Timestamp lastTrialRequestTimestamp = result.get(0);
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            final Date aYearAgo = cal.getTime();
            if (aYearAgo.before(lastTrialRequestTimestamp)) {
              /*
               * Too soon for a new trial
               * 
               * We return date of the most recent trial license to display in
               * the message to the user
               */
              final String pastLicenseDate = (new SimpleDateFormat("dd MMM yyyy")).format(lastTrialRequestTimestamp);
              return pastLicenseDate;
            }
          }
        }
        q.prepared("WebServices.insertLicenseWebRequest").call(license.getUuid().toString(), emailForDb, nameForDb, companyForDb,
            licenseType, noEmailForDB);
        return SLUtility.YES;
      }
    });

    final boolean pastTrialLicenseRequestByThisEmail = !SLUtility.YES.equals(result);
    if (pastTrialLicenseRequestByThisEmail) {
      out.println(I18N.msg("web.license.problem.title"));
      out.println(I18N.msg("web.license.problem.pastTrialLicense", emailForDb, result));
      out.println(I18N.msg("web.license.problem.goBack"));
      return;
    }

    final String subject = I18N.msg("web.license.email.subject", licenseType);
    final String downloadUrl = I18N.msg("web.download.url", SLUtility.SERVICEABILITY_SERVER);
    final String text = I18N.msg("web.license.email", holder, downloadUrl, licenseHexString);
    Email.sendEmail(subject, text, emailForDb);
    out.println(I18N.msg("web.license.success", licenseType, emailForDb, SLUtility.SERVICEABILITY_SERVER));
  }

  static class AllowLicenseHandler implements RowHandler<Timestamp> {
    public Timestamp handle(Row r) {
      return r.nextTimestamp();
    }
  }
}
