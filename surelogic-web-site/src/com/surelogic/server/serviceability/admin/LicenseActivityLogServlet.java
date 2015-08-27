package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.DATE;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.STRING;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.Nullable;
import com.surelogic.common.Pair;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseActivityLogServlet extends HttpServlet {

  private static final long serialVersionUID = 1584106224306833877L;
  private static final String TIME = "t";
  private static final String SKIP_COUNT = "skip";

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    ServicesDBConnection.getInstance()
        .withReadOnly(new LogQuery(resp.getWriter(), req.getParameter(TIME), req.getParameter(SKIP_COUNT)));
  }

  static class LogQuery extends HTMLQuery {

    final long time;
    long skipThisTime;

    public LogQuery(final PrintWriter writer, @Nullable final String time, @Nullable final String skipCount) {
      super(writer);
      if (time == null) {
        this.time = System.currentTimeMillis();
      } else {
        this.time = Long.parseLong(time);
      }
      this.skipThisTime = skipCount == null ? 0 : Long.parseLong(skipCount);
    }

    @Override
    public void doPerform(final Query q) {
      prequel("Recent License Activity");
      tableBegin();
      tableRow(DATE.th("Date"), STRING.th("IP"), STRING.th("License"), STRING.th("Event"), STRING.th("Holder"), STRING.th("Email"),
          STRING.th("Company"));
      final Pair<Long, Long> result = q.prepared("WebServices.selectNetChecksBefore", new ResultHandler<Pair<Long, Long>>() {
        @Override
        public Pair<Long, Long> handle(final Result result) {
          long latest = time;
          long skipCountNextTime = 0;
          int count = 0;
          for (Row r : result) {
            if (skipThisTime-- > 0)
              continue;
            if (++count > ROWS) {
              break;
            }
            Timestamp t = r.nextTimestamp();
            final long tTime = t.getTime();
            if (tTime == latest)
              skipCountNextTime++;
            else {
              latest = tTime;
              skipCountNextTime = 0; // reset
            }
            tableRow(DATE.td(t), STRING.td(ip(r.nextString())), STRING.td(uuid(r.nextString())), STRING.td(r.nextString()),
                STRING.td(r.nextString()), STRING.td(r.nextString()), STRING.td(r.nextString()));
          }
          return new Pair<>(latest, skipCountNextTime);
        }
      }).call(new Timestamp(time));
      final long latest = result.first();
      final long skipCount = result.second();
      System.out.println("LATEST TO USE IN LINK IS " + latest);
      tableRow(STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""),
          STRING.td("<a href=\"log?%s=%d&%s=%d\">Next</a>", TIME, latest, SKIP_COUNT, skipCount));
      tableEnd();
      finish();
    }
  }

}
