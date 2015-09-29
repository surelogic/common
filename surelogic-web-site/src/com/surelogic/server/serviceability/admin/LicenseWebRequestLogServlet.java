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
  private static final String PAGE = "weblog";
  private static final String TIME = "t";
  private static final String DELETE_ABANDONED = "deleteAbandoned";
  private static final String YES = "yes";

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
        .withReadOnly(new LogQuery(resp.getWriter(), req.getParameter(TIME), req.getParameter(DELETE_ABANDONED)));
  }

  static class LogQuery extends HTMLQuery {

    final long time;
    final boolean deleteAbandoned;

    public LogQuery(final PrintWriter writer, @Nullable final String time, @Nullable final String deleteAbandoned) {
      super(writer);
      if (time == null) {
        this.time = System.currentTimeMillis();
      } else {
        this.time = Long.parseLong(time);
      }
      this.deleteAbandoned = deleteAbandoned != null && YES.equals(deleteAbandoned);
    }

    @Override
    public void doPerform(final Query q) {
      prequel("Recent Web License Request Activity");
      if (deleteAbandoned)
        writer.println("<p>Deleted abandoned licenses...</p>");
      writer.print("<form align=\"right\" action=\"weblog\" method=\"post\" >");
      writer.print("<input type=\"hidden\" name=\"" + DELETE_ABANDONED + "\" value=\"" + YES + "\" />");
      writer.print("<input type=\"submit\" value=\"Delete abandoned web license requests older than two weeks\" />");
      writer.print("</form><p>");
      tableBegin();
      tableRow(CENTER.th("Date"), LEFT.th("License"), LEFT.th("Name"), LEFT.th("Email"), LEFT.th("Company"),
          CENTER.th("License Type"), CENTER.th("Ignore Trial"), CENTER.th("No Email"));
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
            String uuid = r.nextString();
            String name = r.nextString();
            String email = r.nextString();
            String company = r.nextString();
            String licenseType = r.nextString();
            String ignoreTrial = "true".equals(r.nextString()) && "Trial".equals(licenseType) ? "X" : "";
            String noEmail = "true".equals(r.nextString()) ? "X" : "";
            tableRow(CENTER.td(t), LEFT.td(uuid(uuid)), LEFT.td(name), LEFT.td(email), LEFT.td(company), CENTER.td(licenseType),
                CENTER.td(ignoreTrial), CENTER.td(noEmail));
          }
          return rowsRemaining ? latest : -1; // -1 means no rows remain
        }
      }).call(new Timestamp(time));
      if (latest != -1) {
        tableRow(LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""), LEFT.td(""),
            RIGHT.td("<a href=\"%s?%s=%d\">Next&gt;</a>", PAGE, TIME, latest));
      }
      tableEnd();
      finish();
    }
  }

}
