package com.surelogic.server.serviceability;

import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.DATE;
import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.NUMBER;
import static com.surelogic.server.serviceability.HTMLQuery.HeaderType.STRING;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class BlacklistServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    ServicesDBConnection.getInstance().withReadOnly(new BlacklistQuery(resp.getWriter()));
  }

  private static class BlacklistQuery extends HTMLQuery {

    BlacklistQuery(final PrintWriter writer) {
      super(writer);
    }

    @Override
    public void doPerform(final Query q) {
      prequel("License Blacklist");
      writer.println("<h3><a href=\"admin\">To License Overview</a></h3>");
      writer.println("<h3><a href=\"admin/log\">To Recent License Activity</a></h3>");
      writer.println("<h3><a href=\"admin/search\">To License Search</a></h3>");
      tableBegin();
      tableRow(DATE.th("Latest Activity"), STRING.th("License"), STRING.th("Holder"), STRING.th("Product"), NUMBER.th("Installs"),
          NUMBER.th("Renewals"), NUMBER.th("Removals"), NUMBER.th("Blacklists"), NUMBER.th("Too Many Installs"));
      q.prepared("WebServices.listBlacklistedUUIDs", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          Date latest = r.nextTimestamp();
          String uuid = r.nextString();
          SLLicenseProduct p = SLLicenseProduct.fromString(r.nextString());
          String holder = r.nextString();
          String installs = r.nextString();
          String renewals = r.nextString();
          String removals = r.nextString();
          String blacklisted = r.nextString();
          String tooMany = r.nextString();
          tableRow(DATE.td(latest), STRING.td(uuid(uuid)), STRING.td(holder), STRING.td(p.toString()), NUMBER.td(installs),
              NUMBER.td(renewals), NUMBER.td(removals), NUMBER.td(blacklisted), NUMBER.td(tooMany));
        }
      }).call();
      tableEnd();
      finish();
    }

  }

}
