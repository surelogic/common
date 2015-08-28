package com.surelogic.server.serviceability.admin;

import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.CENTER;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.LEFT;
import static com.surelogic.server.serviceability.admin.HTMLQuery.HeaderType.RIGHT;

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
      tableRow(CENTER.th("Date"), LEFT.th("License"), LEFT.th("Name"), LEFT.th("Email"), LEFT.th("Company"),
          LEFT.th("License Type"));
      long latest = q.prepared("WebServices.selectLicenseWebRequestsBefore", new ResultHandler<Long>() {
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
            tableRow(CENTER.td(t), LEFT.td(uuid(r.nextString())), LEFT.td(r.nextString()), LEFT.td(r.nextString()),
                LEFT.td(r.nextString()), LEFT.td(r.nextString()));
          }
          return rowsRemaining ? latest : -1; // -1 means no rows remain
        }
      }).call(new Timestamp(time));
      if (latest != -1) {
        tableRow(LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""),
            RIGHT.td("<a href=\"weblog?%s=%d\">Next&gt;</a>", TIME, latest));
      }
      tableEnd();
      finish();
    }
  }

}
