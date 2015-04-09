package com.surelogic.server.flashlight;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CancelledServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7467818559232172713L;

	@Override
	protected void service(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		resp.sendRedirect("/checkout.html");
	}

}
