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
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseWebRequestLogServlet extends HttpServlet {

  private static final long serialVersionUID = -9077965307391367048L;
  private static final String TIME = "t";

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    ServicesDBConnection.getInstance().withReadOnly(new LogQuery(resp.getWriter(), req.getParameter(TIME)));
  }

  static class LogQuery extends HTMLQuery {

    final long time;

    public LogQuery(final PrintWriter writer, @Nullable final String time) {
      super(writer);
      if (time == null) {
        this.time = System.currentTimeMillis();
      } else {
        this.time = Long.parseLong(time);
      }
    }

    @Override
    public void doPerform(final Query q) {
      prequel("Recent Web License Request Activity");
      tableBegin();
      tableRow(DATE.th("Date"), STRING.th("License"), STRING.th("Name"), STRING.th("Email"), STRING.th("Company"),
          STRING.th("License Type"));
      long latest = q.prepared("WebServices.selectLicenseWebRequestsBefore", new ResultHandler<Long>() {
        @Override
        public Long handle(final Result result) {
          long latest = time;
          int count = 0;
          for (Row r : result) {
            if (++count > ROWS) {
              break;
            }
            Timestamp t = r.nextTimestamp();
            latest = t.getTime();
            tableRow(DATE.td(t), STRING.td(uuid(r.nextString())), STRING.td(r.nextString()), STRING.td(r.nextString()),
                STRING.td(r.nextString()), STRING.td(r.nextString()));
          }
          return latest;
        }
      }).call(new Timestamp(time));
      tableRow(STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""),
          STRING.td("<a href=\"weblog?%s=%d\">Next</a>", TIME, latest));
      tableEnd();
      finish();
    }
  }

}