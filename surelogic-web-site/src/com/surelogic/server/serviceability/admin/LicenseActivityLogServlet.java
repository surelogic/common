package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.*;

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
  private static final String PAGE = "log";
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
      this.time = time == null ? System.currentTimeMillis() : Long.parseLong(time);
    }

    @Override
    public void doPerform(final Query q) {
      prequel("Recent License Activity");
      tableBegin();
      tableRow(CENTER.th("Date"), LEFT.th("IP"), LEFT.th("License"), LEFT.th("Event"), LEFT.th("Holder"), LEFT.th("Email"),
          LEFT.th("Company"), LEFT.th("OS"), LEFT.th("Java Version"), LEFT.th("Eclipse Version"), LEFT.th("Use Counts"));
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
            /*
             * Continue output until we see a new distinct time. Otherwise the
             * next page will repeat all the rows with the current "latest"
             * time.
             */
            if (latest != tTime) {
              latest = tTime;
              if (count > ROWS) {
                rowsRemaining = true;
                break;
              }
            }
            tableRow(CENTER.td(t), LEFT.td(ip(r.nextString())), LEFT.td(uuid(r.nextString())), LEFT.td(r.nextString()),
                LEFT.td(r.nextString()), LEFT.td(r.nextString()), LEFT.td(r.nextString()), LEFT.td(r.nextString()),
                LEFT.td(r.nextString()), LEFT.td(r.nextString()), LEFT.td(r.nextString()));
          }
          return rowsRemaining ? latest : -1; // -1 means no rows remain
        }
      }).call(new Timestamp(time));
      if (latest != -1) {
        tableRow(LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""),
            LEFT.td(""), LEFT.td(""), RIGHT.td("<a href=\"%s?%s=%d\">Next&gt;</a>", PAGE, TIME, latest));
      }
      tableEnd();
      finish();
    }
  }

}
