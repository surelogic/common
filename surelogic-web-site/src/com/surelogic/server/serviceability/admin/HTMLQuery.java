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
      final String value = date == null ? "null" : format.get().format(date);
      return String.format("<td style=\"text-align: %s\">%s</td>", align, value);
    }

    String td(final String column, final Object... args) {
      if (column == null)
        return "<td>&nbsp;</td>";
      else
        return String.format("<td style=\"text-align: %s\">%s</td>", align, String.format(column, args));
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
    writer.println(String.format(
        "<html><head><title>%1$s</title><style>table {  border-collapse: collapse; } td, th { border: thin solid grey;}</style></head><body><h1 align=\"center\">%1$s</h1>",
        title));
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
