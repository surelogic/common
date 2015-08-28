package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.CENTER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.RIGHT;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.LEFT;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseAdminServlet extends HttpServlet {

  private static final String YES = "yes";
  private static final String NO = "no";

  private static final long serialVersionUID = -7449790315955833400L;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    String uuid = req.getParameter("uuid");
    if (uuid == null) {
      resp.getWriter().println("uuid cannot be null");
      return;
    }
    String installs = req.getParameter("installs");
    if (installs != null) {
      try {
        int parseInt = Integer.parseInt(installs);
        if (parseInt >= 0) {
          ServicesDBConnection.getInstance().withTransaction(new UpdateInstallCount(uuid, parseInt));
        }
      } catch (NumberFormatException e) {
        // Do nothing
      }
    }
    String blacklist = req.getParameter("blacklist");
    if (blacklist != null && (YES.equals(blacklist) || NO.equals(blacklist))) {
      ServicesDBConnection.getInstance().withTransaction(new UpdateBlacklisted(uuid, YES.equals(blacklist)));
    }
    ServicesDBConnection.getInstance().withReadOnly(new ShowLicense(uuid, resp.getWriter()));
  }

  private static class UpdateBlacklisted extends NullDBQuery {

    private final boolean blacklist;
    private final String uuid;

    UpdateBlacklisted(final String uuid, final boolean blacklist) {
      this.blacklist = blacklist;
      this.uuid = uuid;
    }

    @Override
    public void doPerform(final Query q) {
      q.prepared("WebServices.removeFromBlacklist").call(uuid);
      if (blacklist) {
        q.prepared("WebServices.addToBlacklist").call(uuid);
      }
    }

  }

  private static class UpdateInstallCount extends NullDBQuery {

    private final int parseInt;
    private final String uuid;

    public UpdateInstallCount(final String uuid, final int parseInt) {
      this.uuid = uuid;
      this.parseInt = parseInt;
    }

    @Override
    public void doPerform(final Query q) {
      q.prepared("WebServices.updateInstallCount").call(parseInt, uuid);
    }

  }

  private static class ShowLicense extends HTMLQuery {
    private final String uuid;

    ShowLicense(final String uuid, final PrintWriter writer) {
      super(writer);
      this.uuid = uuid;
    }

    @Override
    public void doPerform(final Query q) {
      prequel(String.format("License %s", uuid));
      writer.print("<form action=\"license\" method=\"post\" ><input type=\"hidden\" name=\"uuid\" value=\"");
      writer.print(uuid);
      writer.println("\" />");
      writer.println("<h3>Description</h3>");
      tableBegin();
      tableRow(LEFT.th("Product"), LEFT.th("Holder"), LEFT.th("Email"), LEFT.th("Company"), RIGHT.th("Duration"),
          CENTER.th("Install Before"), LEFT.th("Type"), RIGHT.th("Max Active"), LEFT.th("Blacklist"));
      q.prepared("WebServices.selectLicenseInfoById", new NullRowHandler() {

        @Override
        protected void doHandle(final Row r) {
          tableRow(LEFT.td(r.nextString()), LEFT.td(r.nextString()), LEFT.td(r.nextString()), LEFT.td(r.nextString()),
              RIGHT.td(r.nextString()), CENTER.td(r.nextTimestamp()), LEFT.td(r.nextString()),
              RIGHT.td(String.format(
                  "<input style=\"text-align: right\" type=\"text\" name=\"installs\" value=\"%d\" /><input type=\"submit\" value=\"Change\" />",
                  r.nextInt())),
              LEFT.td(blacklistLink(r.nextString())));
        }
      }).call(uuid);
      tableEnd();
      writer.println("</form>");

      writer.println("<h3>Counts</h3>");
      tableBegin();
      tableRow(RIGHT.th("Installs"), RIGHT.th("Renewals"), RIGHT.th("Removals"), RIGHT.th("Blacklists"), RIGHT.th("Too Many"));
      q.prepared("WebServices.selectCheckCount", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          tableRow(RIGHT.td(r.nextString()), RIGHT.td(r.nextString()), RIGHT.td(r.nextString()), RIGHT.td(r.nextString()),
              RIGHT.td(r.nextString()));
        }
      }).call(uuid);
      tableEnd();
      writer.println("<h3>Activity</h3>");
      tableBegin();
      tableRow(CENTER.th("Date"), LEFT.th("IP"), LEFT.th("Event"));
      q.prepared("WebServices.selectNetChecksByID", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          tableRow(LEFT.td(r.nextTimestamp()), LEFT.td(ip(r.nextString())), LEFT.td(r.nextString()));
        }
      }).call(uuid);
      tableEnd();
      writer.println("<h3>Web License Requests</h3>");
      tableBegin();
      tableRow(CENTER.th("Date"), LEFT.th("Name"), LEFT.th("Email"), LEFT.th("Company"), LEFT.th("License Type"),
          CENTER.th("Ignore Trial"), CENTER.th("No Email"));
      q.prepared("WebServices.selectWebLicenseRequestByID", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          Date latest = r.nextTimestamp();
          String name = r.nextString();
          String email = r.nextString();
          String company = r.nextString();
          String licenseType = r.nextString();
          String ignoreTrial = r.nextString();
          if ("Community".equals(licenseType))
            ignoreTrial = "";
          else
            ignoreTrial = ignoreTrialLink(ignoreTrial);
          String noEmail = "true".equals(r.nextString()) ? "X" : "";
          tableRow(CENTER.td(latest), LEFT.td(name), LEFT.td(email), LEFT.td(company), LEFT.td(licenseType), CENTER.td(ignoreTrial),
              CENTER.td(noEmail));
        }
      }).call(uuid);
      tableEnd();
      finish();
    }

    String blacklistLink(final String blString) {
      boolean blacklist = YES.equals(blString);
      return String.format("<a href=\"license?uuid=%s&blacklist=%s\">%s</a>", uuid, blacklist ? NO : YES,
          blacklist ? "Remove from Blacklist" : "Add to Blacklist");
    }

    String ignoreTrialLink(final String ignoreTrial) {
      boolean ignore = "true".equals(ignoreTrial);
      return String.format("<a href=\"license?uuid=%s&ignoreTrial=%s\">%s (press to toggle)</a>", uuid, ignore ? "false" : "true",
          ignore ? "true" : "false");
    }
  }
}
