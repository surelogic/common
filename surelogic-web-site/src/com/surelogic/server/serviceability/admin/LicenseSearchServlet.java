package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.DATE;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.NUMBER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.STRING;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseSearchServlet extends HttpServlet {

  private static final long serialVersionUID = 658112454144528107L;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    ServicesDBConnection.getInstance().withReadOnly(new SearchQuery(req, resp));
  }

  private static class SearchQuery extends HTMLQuery {

    final String search;

    public SearchQuery(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
      super(resp.getWriter());
      search = req.getParameter("search");
    }

    @Override
    public void doPerform(final Query q) {
      prequel("License Search");
      writer.println(SiteUtil.getAdminNavBar());
      writer.println(String.format(
          "<form name=\"search\" method=\"post\"><p>Search: <input type=\"test\" name=\"search\" value=\"%s\" /></p></form>",
          search == null ? "" : search));
      tableBegin();
      tableRow(DATE.th("Latest Activity"), STRING.th("License"), STRING.th("Holder"), STRING.th("Email"), STRING.th("Company"),
          STRING.th("Product"), NUMBER.th("Installs"), NUMBER.th("Renewals"), NUMBER.th("Removals"), NUMBER.th("Blacklists"),
          NUMBER.th("Too Many Installs"));
      NullRowHandler handler = new NullRowHandler() {
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
          tableRow(DATE.td(latest), STRING.td(uuid(uuid)), STRING.td(holder), STRING.td(email), STRING.td(company),
              STRING.td(p.toString()), NUMBER.td(installs), NUMBER.td(renewals), NUMBER.td(removals), NUMBER.td(blacklisted),
              NUMBER.td(tooMany));
        }
      };
      String jdbcSearch = "%" + search + "%";
      q.prepared("WebServices.searchLicenses", handler).call(jdbcSearch, jdbcSearch, jdbcSearch, jdbcSearch);
      tableEnd();
      finish();
    }

  }
}
