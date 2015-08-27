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

public class LogServlet extends HttpServlet {

  private static final long serialVersionUID = 1584106224306833877L;
  private static final String TIME = "t";
  private static final int ROWS = 20;

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
      prequel("Recent License Activity");
      writer.println("<h3><a href=\"home\">To License Overview</a></h3>");
      writer.println("<h3><a href=\"blacklist\">To Blacklist</a></h3>");
      writer.println("<h3><a href=\"search\">To License Search</a></h3>");
      tableBegin();
      tableRow(DATE.th("Date"), STRING.th("IP"), STRING.th("License"), STRING.th("Event"), STRING.th("Holder"), STRING.th("Email"),
          STRING.th("Company"));
      long latest = q.prepared("WebServices.selectNetChecksBefore", new ResultHandler<Long>() {
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
            tableRow(DATE.td(t), STRING.td(ip(r.nextString())), STRING.td(uuid(r.nextString())), STRING.td(r.nextString()),
                STRING.td(r.nextString()), STRING.td(r.nextString()), STRING.td(r.nextString()));
          }
          return latest;
        }
      }).call(new Timestamp(time));
      tableRow(STRING.td(""), STRING.td(""), STRING.td(""), STRING.td(""),
          STRING.td("<a href=\"log?%s=%d\">Next</a>", TIME, latest));
      tableEnd();
      finish();
    }
  }

}
