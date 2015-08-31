package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.CENTER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.RIGHT;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.NonNull;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseCountsServlet extends HttpServlet {

  private static final long serialVersionUID = -667963344728794256L;

  static class Counts {

    Counts(@NonNull String month) {
      this.month = month;
    }

    @NonNull
    final String month;
    long activeLicenses; // not just web
    long totalWebLicenseRequests;
    long activatedWebLicenseRequests;
    long totalWebTrialLicenseRequests;
    long activatedWebTrialLicenseRequests;
    long totalWebCommunityLicenseRequests;
    long activatedWebCommunityLicenseRequests;
    // calculated
    long totalWebLicenseRequestsThisMonth;
    long activatedWebLicenseRequestsThisMonth;
    long totalWebTrialLicenseRequestsThisMonth;
    long activatedWebTrialLicenseRequestsThisMonth;
    long totalWebCommunityLicenseRequestsThisMonth;
    long activatedWebCommunityLicenseRequestsThisMonth;

    void lastMonth(@NonNull Counts p) {
      totalWebLicenseRequestsThisMonth = totalWebLicenseRequests - p.totalWebLicenseRequests;
      activatedWebLicenseRequestsThisMonth = activatedWebLicenseRequests - p.activatedWebLicenseRequests;
      totalWebTrialLicenseRequestsThisMonth = totalWebTrialLicenseRequests - p.totalWebTrialLicenseRequests;
      activatedWebTrialLicenseRequestsThisMonth = activatedWebTrialLicenseRequests - p.activatedWebTrialLicenseRequests;
      totalWebCommunityLicenseRequestsThisMonth = totalWebCommunityLicenseRequests - p.totalWebCommunityLicenseRequests;
      activatedWebCommunityLicenseRequestsThisMonth = activatedWebCommunityLicenseRequests - p.activatedWebCommunityLicenseRequests;
    }
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
      tableRow(CENTER.thRowspan("Date", 4), RIGHT.thRowspan("In Use", 4), CENTER.thColspan("Website Requests", 12));
      tableRow(CENTER.thColspan("Trial", 4), CENTER.thColspan("Community", 4), CENTER.thColspan("Total", 4));
      tableRow(CENTER.thColspan("This Month", 2), CENTER.thColspan("All Time", 2), CENTER.thColspan("This Month", 2),
          CENTER.thColspan("All Time", 2), CENTER.thColspan("This Month", 2), CENTER.thColspan("All Time", 2));
      tableRow(RIGHT.th("Requested"), RIGHT.th("Activated"), RIGHT.th("Requested"), RIGHT.th("Activated"), RIGHT.th("Requested"),
          RIGHT.th("Activated"), RIGHT.th("Requested"), RIGHT.th("Activated"), RIGHT.th("Requested"), RIGHT.th("Activated"),
          RIGHT.th("Requested"), RIGHT.th("Activated"));

      final SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
      Calendar c = Calendar.getInstance();
      final int year = c.get(Calendar.YEAR);
      final int month = c.get(Calendar.MONTH);
      c.clear();
      c.set(Calendar.MONTH, month);
      c.set(Calendar.YEAR, year);
      c.add(Calendar.MONTH, 1);
      c.add(Calendar.MILLISECOND, -1); // last instant of month
      final LinkedList<Counts> countsList = new LinkedList<>();
      do {
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        final Counts counts = new Counts(sdf.format(ts));
        counts.activeLicenses = q.prepared("WebServices.activeLicensesOn", handler).call(ts);
        counts.totalWebLicenseRequests = q.prepared("WebServices.totalWebLicenseRequestsOn", handler).call(ts);
        counts.activatedWebLicenseRequests = q.prepared("WebServices.activatedWebLicenseRequestsOn", handler).call(ts);
        counts.totalWebTrialLicenseRequests = q.prepared("WebServices.totalWebTrialLicenseRequestsOn", handler).call(ts);
        counts.activatedWebTrialLicenseRequests = q.prepared("WebServices.activatedWebTrialLicenseRequestsOn", handler).call(ts);
        counts.totalWebCommunityLicenseRequests = q.prepared("WebServices.totalWebCommunityLicenseRequestsOn", handler).call(ts);
        counts.activatedWebCommunityLicenseRequests = q.prepared("WebServices.activatedWebCommunityLicenseRequestsOn", handler)
            .call(ts);
        countsList.addFirst(counts);
        c.add(Calendar.MONTH, -1);
      } while (c.get(Calendar.MONTH) != month);
      // calculate this month values where we can
      Counts prev = null;
      for (Counts curr : countsList) {
        if (prev != null)
          curr.lastMonth(prev);
        prev = curr;
      }
      Collections.reverse(countsList);
      for (Counts counts : countsList) {
        tableRow(RIGHT.td(counts.month), RIGHT.tdL(counts.activeLicenses),

        RIGHT.tdL(counts.totalWebTrialLicenseRequestsThisMonth), RIGHT.tdL(counts.activatedWebTrialLicenseRequestsThisMonth),
            RIGHT.tdL(counts.totalWebTrialLicenseRequests), RIGHT.tdL(counts.activatedWebTrialLicenseRequests),

        RIGHT.tdL(counts.totalWebCommunityLicenseRequestsThisMonth),
            RIGHT.tdL(counts.activatedWebCommunityLicenseRequestsThisMonth), RIGHT.tdL(counts.totalWebCommunityLicenseRequests),
            RIGHT.tdL(counts.activatedWebCommunityLicenseRequests),

        RIGHT.tdL(counts.totalWebLicenseRequestsThisMonth), RIGHT.tdL(counts.activatedWebLicenseRequestsThisMonth),
            RIGHT.tdL(counts.totalWebLicenseRequests), RIGHT.tdL(counts.activatedWebLicenseRequests));
      }

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
