package com.surelogic.server.serviceability;

import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.DATE;
import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.NUMBER;
import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.STRING;

import java.io.IOException;
import java.io.PrintWriter;

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
      writer.println("<h3><a href=\"admin\">To License Overview</a></h3>");
      writer.println("<h3><a href=\"log\">To Recent License Activity</a></h3>");
      writer.println("<h3><a href=\"search\">To Blacklist</a></h3>");
      writer.println("<h3><a href=\"search\">To License Search</a></h3>");
      writer.print("<form action=\"license\" method=\"post\" ><input type=\"hidden\" name=\"uuid\" value=\"");
      writer.print(uuid);
      writer.println("\" />");
      writer.println("<h3>Description</h3>");
      tableBegin();
      tableRow(STRING.th("Product"), STRING.th("Holder"), NUMBER.th("Duration"), DATE.th("Install Before"), STRING.th("Type"),
          NUMBER.th("Max Active"), STRING.th("Blacklist"));
      q.prepared("WebServices.selectLicenseInfoById", new NullRowHandler() {

        @Override
        protected void doHandle(final Row r) {
          tableRow(
              STRING.td(r.nextString()),
              STRING.td(r.nextString()),
              NUMBER.td(r.nextString()),
              DATE.td(r.nextTimestamp()),
              STRING.td(r.nextString()),
              NUMBER.td(String
                  .format(
                      "<input style=\"text-align: right\" type=\"text\" name=\"installs\" value=\"%d\" /><input type=\"submit\" value=\"Change\" />",
                      r.nextInt())), STRING.td(blacklistLink(r.nextString())));
        }
      }).call(uuid);
      tableEnd();
      writer.println("</form>");

      writer.println("<h3>Counts</h3>");
      tableBegin();
      tableRow(NUMBER.th("Installs"), NUMBER.th("Renewals"), NUMBER.th("Removals"), NUMBER.th("Blacklists"), NUMBER.th("Too Many"));
      q.prepared("WebServices.selectCheckCount", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          tableRow(NUMBER.td(r.nextString()), NUMBER.td(r.nextString()), NUMBER.td(r.nextString()), NUMBER.td(r.nextString()),
              NUMBER.td(r.nextString()));
        }
      }).call(uuid);
      tableEnd();
      writer.println("<h3>Activity</h3>");
      tableBegin();
      tableRow(DATE.th("Date"), STRING.th("IP"), STRING.th("Event"));
      q.prepared("WebServices.selectNetChecksByID", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          tableRow(STRING.td(r.nextTimestamp()), STRING.td(ip(r.nextString())), STRING.td(r.nextString()));
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
  }
}
