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
      prequel("License Counts");
      tableBegin();
      tableRow(CENTER.th("Date"), RIGHT.th("Active Licenses"));
      final Timestamp ts = new Timestamp(System.currentTimeMillis());
      final Counts counts = new Counts();
      counts.activeLicenses = q.prepared("WebServices.activeLicensesOn", new ResultHandler<Long>() {
        @Override
        public Long handle(final Result result) {
          for (Row r : result) {
            return r.nextLong();
          }
          return 0L;
        }
      }).call(ts);
      tableRow(CENTER.td(ts), RIGHT.td(SLUtility.toStringHumanWithCommas(counts.activeLicenses)));
      tableEnd();
      finish();
    }
  }

}
