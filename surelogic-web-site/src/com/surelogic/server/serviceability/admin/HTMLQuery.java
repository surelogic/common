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

  protected final int ROWS = 30;

  HTMLQuery(final PrintWriter writer) {
    this.writer = writer;
  }

  enum HeaderType {

    LEFT("left"), RIGHT("right"), CENTER("center");

    private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
      @Override
      protected DateFormat initialValue() {
        return new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
      }
    };

    private final String align;

    HeaderType(final String align) {
      this.align = align;
    }

    String th(final String value) {
      return String.format("<th>%s</th>", value);
    }

    /**
     * Use to span columns and rows
     */
    String thSpan(String value, int rowspan, int colspan) {
      final StringBuilder b = new StringBuilder();
      b.append("<th");
      if (rowspan > 1)
        b.append(" rowspan=\"").append(rowspan).append("\"");
      if (colspan > 1)
        b.append(" colspan=\"").append(colspan).append("\"");
      b.append('>').append(value).append("</th>");
      return b.toString();
    }

    String thRowspan(String value, int rowspan) {
      return thSpan(value, rowspan, 1);
    }

    String thColspan(String value, int colspan) {
      return thSpan(value, 1, colspan);
    }

    String td(Date value) {
      final String s = value == null ? "&nbsp;" : format.get().format(value);
      return String.format("<td style=\"text-align: %s\">%s</td>", align, s);
    }

    String td(String value, final Object... args) {
      final String s = value == null ? "&nbsp;" : String.format(value, args);
      return String.format("<td style=\"text-align: %s\">%s</td>", align, s);
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
    writer.println("<p class=\"header\">SURELOGIC LICENSE ADMINSTRATION</p>");
    writer.println(String.format("<h1>%s</h1>", title));
    navBar();
  }

  void style() {
    writer.println("<style>");
    writer.println(" body {  background-color: #000022; color: #FFFFFF; }");
    writer.println(" table {  border-collapse: collapse; }");
    writer.println(" td, th { border: thin solid grey; font: 12px arial, sans-serif; }");
    writer.println(" th { color: #FFFF00; }");
    writer.println(" a { color: #CCCCCC; }");
    writer.println(" p { font: 15px arial, sans-serif; }");
    writer.println(" p.header { text-align: center; font: 10px arial, sans-serif; color: #AAAAAA; }");
    writer.println(" h1 { text-align: center; font: 20px arial, sans-serif; color: #FFFF00; }");
    writer.println("</style>");
  }

  void navBar() {
    writer.print("<hr><p align=\"center\">( ");
    writer.print("<a href=\"counts\">License Counts</a>");
    writer.print(" | ");
    writer.print("<a href=\"log\">Recent License Activity</a>");
    writer.print(" | ");
    writer.print("<a href=\"search\">License Search</a>");
    writer.print(" | ");
    writer.print("<a href=\"blacklist\">License Blacklist</a>");
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
