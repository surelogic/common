package com.surelogic.server.flashlight;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CompletedServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2468661960060559936L;

	@Override
	protected void service(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		resp.sendRedirect("/completed.html");
	}

}
