package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.CENTER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.RIGHT;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.SLUtility;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseCountsServlet extends HttpServlet {

  private static final long serialVersionUID = -667963344728794256L;

  static class Counts {
    long activeLicenses; // not just web
    long totalWebLicenseRequests;
    long activatedWebLicenseRequests;
    long totalWebTrialLicenseRequests;
    long activatedWebTrialLicenseRequests;
    long totalWebCommunityLicenseRequests;
    long activatedWebCommunityLicenseRequests;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    ServicesDBConnection.getInstance().withReadOnly(new CountsQuery(resp.getWriter()));
  }

  static class CountsQuery extends HTMLQuery {

    public CountsQuery(final PrintWriter writer) {
      super(writer);
    }

    @Override
    public void doPerform(final Query q) {
      final CountHandler handler = new CountHandler();
      prequel("License Counts");
      tableBegin();
      tableRow(CENTER.th("Date"), RIGHT.th("Active"), RIGHT.th("Web"), RIGHT.th("Activated Web"), RIGHT.th("Trial"),
          RIGHT.th("Activated Trial"), RIGHT.th("Community"), RIGHT.th("Activated Community"));
      final Timestamp ts = new Timestamp(System.currentTimeMillis());
      final Counts counts = new Counts();
      counts.activeLicenses = q.prepared("WebServices.activeLicensesOn", handler).call(ts);
      counts.totalWebLicenseRequests = q.prepared("WebServices.totalWebLicenseRequestsOn", handler).call(ts);
      counts.activatedWebLicenseRequests = q.prepared("WebServices.activatedWebLicenseRequestsOn", handler).call(ts);
      counts.totalWebTrialLicenseRequests = q.prepared("WebServices.totalWebTrialLicenseRequestsOn", handler).call(ts);
      counts.activatedWebTrialLicenseRequests = q.prepared("WebServices.activatedWebTrialLicenseRequestsOn", handler).call(ts);
      counts.totalWebCommunityLicenseRequests = q.prepared("WebServices.totalWebCommunityLicenseRequestsOn", handler).call(ts);
      counts.activatedWebCommunityLicenseRequests = q.prepared("WebServices.activatedWebCommunityLicenseRequestsOn", handler)
          .call(ts);
      tableRow(CENTER.td(ts), RIGHT.td(SLUtility.toStringHumanWithCommas(counts.activeLicenses)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.totalWebLicenseRequests)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.activatedWebLicenseRequests)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.totalWebTrialLicenseRequests)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.activatedWebTrialLicenseRequests)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.totalWebCommunityLicenseRequests)),
          RIGHT.td(SLUtility.toStringHumanWithCommas(counts.activatedWebCommunityLicenseRequests)));
      tableEnd();
      finish();
    }
  }

  static class CountHandler implements ResultHandler<Long> {
    @Override
    public Long handle(final Result result) {
      for (Row r : result) {
        return r.nextLong();
      }
      return 0L;
    }
  }

}
