package com.spartez.mazio.help;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

public class HelpServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out;

		response.setContentType("text/html");

		out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>");
		out.println("Mazio Plugin for JIRA - Help");
		out.println("</TITLE></HEAD><BODY>");
		out.println(TXT);
		out.println("</BODY></HTML>");
		out.close();
	}

	private static final String TXT =
			"Launching Mazio is done by invoking the \"mazio:\" pseudo-protocol. Such a protocol has to be registered "
			+ "by the operating system or by the browser itself.<br/><br/>"
			+ "This pseudo-protocol is registered by the Mazio application's setup program.<br/><br/>"
			+ "Mazio can be installed from <a href=\"http://spartez.com/eng/products.html\">SPARTEZ web site</a>.";
}
