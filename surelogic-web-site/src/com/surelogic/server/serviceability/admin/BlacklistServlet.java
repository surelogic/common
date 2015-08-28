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
      tableBegin();
      tableRow(CENTER.th("Latest Activity"), LEFT.th("License"), LEFT.th("Holder"), LEFT.th("Email"), LEFT.th("Company"),
          LEFT.th("Product"), RIGHT.th("Installs"), RIGHT.th("Renewals"), RIGHT.th("Removals"), RIGHT.th("Blacklists"),
          RIGHT.th("Too Many Installs"));
      q.prepared("WebServices.listBlacklistedUUIDs", new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          Date latest = r.nextTimestamp();
          String uuid = r.nextString();
          SLLicenseProduct p = SLLicenseProduct.fromString(r.nextString());
          String holder = r.nextString();
          String email = r.nextString();
          String company = r.nextString();
          String installs = r.nextString();
          String renewals = r.nextString();
          String removals = r.nextString();
          String blacklisted = r.nextString();
          String tooMany = r.nextString();
          tableRow(CENTER.td(latest), LEFT.td(uuid(uuid)), LEFT.td(holder), LEFT.td(email), LEFT.td(company),
              LEFT.td(p.toString()), RIGHT.td(installs), RIGHT.td(renewals), RIGHT.td(removals), RIGHT.td(blacklisted),
              RIGHT.td(tooMany));
        }
      }).call();
      tableEnd();
      finish();
    }
  }
}
