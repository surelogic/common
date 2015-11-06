package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.CENTER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.LEFT;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseWebRequestSearchServlet extends HttpServlet {

  private static final long serialVersionUID = 481802527198080240L;

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
      prequel("Web License Request Search");
      writer.println(String.format(
          "<form name=\"search\" method=\"post\"><p>Search: <input type=\"test\" name=\"search\" value=\"%s\" /></p></form>",
          search == null ? "" : search));
      tableBegin();
      tableRow(CENTER.th("Date"), LEFT.th("License"), LEFT.th("Name"), LEFT.th("Email"), LEFT.th("Company"),
          CENTER.th("License Type"), CENTER.th("Ignore Trial"));
      NullRowHandler handler = new NullRowHandler() {
        @Override
        protected void doHandle(final Row r) {
          Date latest = r.nextTimestamp();
          String uuid = r.nextString();
          String name = r.nextString();
          String email = r.nextString();
          String company = r.nextString();
          String licenseType = r.nextString();
          String ignoreTrial = "true".equals(r.nextString()) && "Trial".equals(licenseType) ? "X" : "";
          tableRow(CENTER.td(latest), LEFT.td(uuid(uuid)), LEFT.td(name), LEFT.td(email), LEFT.td(company), CENTER.td(licenseType),
              CENTER.td(ignoreTrial));
        }
      };
      String jdbcSearch = "%" + search + "%";
      q.prepared("WebServices.searchLicenseWebRequests", handler).call(jdbcSearch, jdbcSearch, jdbcSearch, jdbcSearch);
      tableEnd();
      finish();
    }
  }
}
