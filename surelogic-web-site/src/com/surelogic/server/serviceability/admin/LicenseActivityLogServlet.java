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

public class LicenseActivityLogServlet extends HttpServlet {

  private static final long serialVersionUID = 1584106224306833877L;
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

    final boolean useSystemTime;
    final long time;

    public LogQuery(final PrintWriter writer, @Nullable final String time) {
      super(writer);
      useSystemTime = time == null;
      this.time = useSystemTime ? System.currentTimeMillis() : Long.parseLong(time);
    }

    @Override
    public void doPerform(final Query q) {
      prequel("Recent License Activity");
      tableBegin();
      tableRow(DATE.th("Date"), STRING.th("IP"), STRING.th("License"), STRING.th("Event"), STRING.th("Holder"), STRING.th("Email"),
          STRING.th("Company"));
      final long latest = q.prepared("WebServices.selectNetChecksBefore", new ResultHandler<Long>() {
        @Override
        public Long handle(final Result result) {
          long latest = time;
          int count = 0;
          boolean rowsRemaining = false;
          for (Row r : result) {
            count++;
            Timestamp t = r.nextTimestamp();
            final long tTime = t.getTime();
            // continue output until we see a distinct time
            if (latest != tTime) {
              latest = tTime;
              if (count > ROWS) {
                rowsRemaining = true;
                break;
              }
            }
            tableRow(DATE.td(t), STRING.td(ip(r.nextString())), STRING.td(uuid(r.nextString())), STRING.td(r.nextString()),
                STRING.td(r.nextString()), STRING.td(r.nextString()), STRING.td(r.nextString()));
          }
          return rowsRemaining ? latest : -1; // -1 means no rows remain
        }
      }).call(new Timestamp(time));
      StringBuilder b = new StringBuilder();
      if (!useSystemTime) {
        b.append("<a href=\"log?").append(TIME).append('=').append(time).append("\">&lt;Prev</a>&nbsp;&nbsp;");
      }
      if (latest != -1) {
        b.append("<a href=\"log?").append(TIME).append('=').append(latest).append("\">Next&gt;</a>");
      }
      if (b.length() > 0)
        tableRow(STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(b.toString()));
      tableEnd();
      finish();
    }
  }

}
