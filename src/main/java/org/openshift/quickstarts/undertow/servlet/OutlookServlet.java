package org.openshift.quickstarts.undertow.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OutlookServlet extends HttpServlet {
	public static final String M = "m";
	public static final String U = "u";
	public static final String P = "p";

	private String m;
	private String u;
	private String p;

	String outlookToken;

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		try {
			m = config.getInitParameter(M);
		} catch (Throwable th) {
		}
		try {
			u = config.getInitParameter(U);
		} catch (Throwable th) {
		}
		try {
			p = config.getInitParameter(P);
		} catch (Throwable th) {
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		writer.write("<html>header>");
		writer.write("</header><body>");
		writer.write("<h1>Outlook OAuth test</h1>");
		writer.write("<form method='POST'>");
		writer.write("<table><caption>Outlook login</caption>");
		writer.write("<tr><th>User</th><td><input type='text' name='user' value='" + u + "'></td></tr>");
		writer.write("<tr><th>User</th><td><input type='text' name='user' value='" + u + "'></td></tr>");
		writer.write("</table>");
		writer.write("</form>");
		writer.write("");
		writer.write("");
		writer.write("");
		writer.write("");
		writer.write("");
		writer.write("</body></html>");
		writer.close();
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
