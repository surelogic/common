package com.surelogic.server.serviceability;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FreeLicenseServlet extends HttpServlet {

  private static final long serialVersionUID = 5071297227487022607L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handle(req, resp);
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    String email = req.getParameter("email");
    boolean emailLooksValid = email != null && email.contains("@") && email.length() <= 254;

    if (emailLooksValid) {
      email = email.trim();
      Email.sendEmail("Test of CL", "some content", email);
      resp.getWriter().println("Success email sent to " + email);
    } else {
      resp.getWriter().println("Problem...Please press the back button and check your information.");
    }
  }
}
