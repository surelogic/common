package com.surelogic.server.serviceability.admin;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.surelogic.common.jdbc.NullDBQuery;

public abstract class HTMLQuery extends NullDBQuery {
  protected final PrintWriter writer;

  HTMLQuery(final PrintWriter writer) {
    this.writer = writer;
  }

  enum HeaderType {

    STRING("left"), NUMBER("right"), DATE("center");

    private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
      @Override
      protected DateFormat initialValue() {
        return new SimpleDateFormat("dd MMM yyyy");
      }
    };

    private final String align;

    HeaderType(final String align) {
      this.align = align;
    }

    String th(final String column) {
      return String.format("<th>%s</th>", column);
    }

    String td(final Date date) {
      final String value = date == null ? "&nbsp;" : format.get().format(date);
      return String.format("<td style=\"text-align: %s\">%s</td>", align, value);
    }

    String td(final String column, final Object... args) {
      final String value = column == null ? "&nbsp;" : String.format(column, args);
      return String.format("<td style=\"text-align: %s\">%s</td>", align, value);
    }
  }

  void tableRow(final String... elems) {
    writer.print("\t<tr>");
    for (String h : elems) {
      writer.print(h);
    }
    writer.println("</tr>");
  }

  void tableBegin() {
    writer.println("<table>");

  }

  void tableEnd() {
    writer.println("</table>");
  }

  void prequel(final String title) {
    writer.println("<!DOCTYPE html>");
    writer.println("<html lang=\"en\">");
    writer.println("<head>");
    writer.println(String.format("<title>%s</title>", title));
    style();
    writer.println("<body>");
    writer.println(String.format("<h1 align=\"center\">%s</h1>", title));
    navBar();
  }

  void style() {
    writer.println("<style>");
    writer.println(" body {  background-color: #003399; color: #FFFFFF; }");
    writer.println(" table {  border-collapse: collapse; }");
    writer.println(" td, th { border: thin solid grey; font: 12px arial, sans-serif; }");
    writer.println(" p { font: 15px arial, sans-serif; }");
    writer.println(" h1 { font: 20px arial, sans-serif; }");
    writer.println("</style>");
  }

  void navBar() {
    writer.print("<hr><p align=\"center\">( ");
    writer.print("<a href=\"home\">License Overview</a>");
    writer.print(" | ");
    writer.print("<a href=\"log\">Recent License Activity</a>");
    writer.print(" | ");
    writer.print("<a href=\"blacklist\">License Blacklist</a>");
    writer.print(" | ");
    writer.print("<a href=\"search\">License Search</a>");
    writer.print(" | ");
    writer.print("<a href=\"weblog\">Recent Web License Request Activity</a>");
    writer.print(" | ");
    writer.print("<a href=\"websearch\">Web License Request Search</a>");
    writer.println(" )</p><hr>");
  }

  String uuid(final String license) {
    return String.format("<a href=license?uuid=%1$s>%1$s</a>", license);
  }

  String ip(final String ip) {
    InetAddress net;
    try {
      net = InetAddress.getByName(ip);
      return net.getHostName();
    } catch (UnknownHostException e) {
      return ip;
    }
  }

  void finish() {
    writer.println("</body></html>");
  }
}
