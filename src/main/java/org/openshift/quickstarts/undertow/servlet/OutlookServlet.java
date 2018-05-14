package org.openshift.quickstarts.undertow.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openshift.quickstarts.undertow.servlet.OutlookAuth.RoomLists;
import org.openshift.quickstarts.undertow.servlet.OutlookAuth.TIME_PERIOD;

public class OutlookServlet extends HttpServlet {

    public static final String M = "m";
    public static final String U = "u";
    public static final String P = "p";

    private String m;
    private String u;
    private String p;

    OutlookAuth oa = new OutlookAuth("86b0f61c-2e69-41df-bdbe-49ebce3f7795");
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
        String extra = "";
        if ("login".equals(m)) {
            //req.getRequestDispatcher(oa.getAuthURL().toString()).forward(req, resp);
            resp.sendRedirect(oa.getAuthURL().toString());
            return;
        } else if ("token".equals(m)) {
            oa.clientSecret = "wGPTTH123+}@ojfukoJK03=";
            Map<String, Object> tresp = oa.requestToken("" + req.getParameter("code"));
            extra = "Extra:\n";
            for (Map.Entry entry : tresp.entrySet()) {
                extra += "\n  " + entry.getKey() + ": " + ("" + entry.getValue()).replace("\n", "\n  ");
            }
            if (tresp.containsKey("token")) {
                req.getSession().setAttribute("token_type", tresp.get("token_type"));
                req.getSession().setAttribute("token", tresp.get("token"));
                req.getSession().setAttribute("roomLists", null);
            }
        } else if ("roomslists".equals(m)) {
            RoomLists rl = null;
            try {
                rl = (RoomLists) req.getSession().getAttribute("roomLists");
            } catch (Throwable th) {
            }
            if (rl == null) {
                try {
                    rl = oa.fetchRooms((String) req.getSession().getAttribute("token"), TIME_PERIOD.today);
                    req.getSession().setAttribute("roomLists", rl);
                    extra = "Rooms lists:\n  " + ("" + rl).replace("\n", "\n  ");

                    try {
                        long[] range = rl.timeSlotsRange();
                        range[0] = OutlookAuth.roundTimeHour(null, range[0], true);
                        range[1] = OutlookAuth.roundTimeHour(null, range[1], false);
                        extra += "\n" + ("ASCII: " + rl.toASCII(null, range[0], range[1], 1000 * 60 * 15 * 2 / 6));
                    } catch (Throwable th) {
                    }
                } catch (Throwable th) {
                    extra = "Rooms lists: ERROR:\n  " + th;
                }
            }
        } else if ("messages".equals(m)) {
            String rrl = null;
            try {
                rrl = oa.messages((String) req.getSession().getAttribute("token"));
                Map map = xJSON.read(rrl);
                extra = "Messages:\n  " + Dump.dump(map, true, true).replace("\n", "\n  ");
            } catch (Throwable th) {
                extra = "Messages:\n" + rrl + "\n---  ERROR:\n  " + th;
            }
        } else if ("manifests".equals(m)) {
            try {
                extra = Dump.findAllManifests();
            } catch (Throwable th) {
                extra = th.toString();
            }
        }

        PrintWriter writer = resp.getWriter();
        writer.write("<html><header>");
        writer.write("</header><body>");
        writer.write("<h1>Outlook OAuth test 0x001</h1>");
        writer.write("<form method='POST'>");
        writer.write("<table><caption>Outlook login (" + m + ")</caption>");
        writer.write("<tr><th>User</th><td><input type='text' name='user' value='" + u + "'></td></tr>");
        writer.write("<tr><th>login</th><td><a href='/login'>LOGIN</a></td></tr>");
        writer.write("<tr><th>manifests</th><td><a href='/manifests'>Java library manifests</a></td></tr>");
        writer.write("<tr><th>messages</th><td><a href='/messages'>MESSAGES</a></td></tr>");
        writer.write("<tr><th>roomlists</th><td><a href='/roomslists'>ROOM LISTS</a>");
        try {
            RoomLists rl = (RoomLists) req.getSession().getAttribute("roomLists");
            if (rl != null) {
                writer.write("<pre>");
                try {
                    long[] range = rl.timeSlotsRange();
                    range[0] = OutlookAuth.roundTimeHour(null, range[0], true);
                    range[1] = OutlookAuth.roundTimeHour(null, range[1], false);
                    writer.write("ASCII: " + rl.toASCII(null, range[0], range[1], 1000 * 60 * 15 * 2 / 6));
                } catch (Throwable th) {
                } finally {
                    writer.write("</pre>");
                }
            }
        } catch (Throwable th) {
        }
        writer.write("</td></tr>");
        writer.write("<tr><th>session</th><td><pre>");
        {
            HttpSession sess = req.getSession();
            try {
                List<String> ans = Collections.list(sess.getAttributeNames());
                writer.write("attributes(" + ans.size() + "): " + ans);
                for (String an : ans) {
                    writer.write("\n  " + an + ": " + ("" + sess.getAttribute(an)).replace("\n", "\n  "));
                }
            } catch (Throwable th) {
            }
        }
        writer.write("</pre></td></tr>");
        writer.write("</table>");
        writer.write("</form>");
        writer.write("<br/>");
        writer.write("<hr/>");
        writer.write("Extra:");
        writer.write("<pre>");
        writer.write(extra);
        writer.write("</pre>");
        writer.write("<hr/>");
        writer.write("<br/>");
        writer.write("<pre>");
        writer.write("Request:");
        try {
            writer.write("\n  RequestURL=" + req.getRequestURL());
            writer.write("\n  Protocol=" + req.getProtocol());
            writer.write("\n  Scheme=" + req.getScheme());
            writer.write("\n  ServerName=" + req.getServerName());
            writer.write("\n  ServerPort=" + req.getServerPort());
            writer.write("\n  PathInfo=" + req.getPathInfo());
            writer.write("\n  PathTranslated=" + req.getPathTranslated());
            writer.write("\n  ServletPath=" + req.getServletPath());
            writer.write("\n  ContextPath=" + req.getContextPath());
            writer.write("\n  QueryString=" + req.getQueryString());
            try {
                writer.write("\n  LocalAddr=" + req.getLocalAddr());
                writer.write("\n  LocalPort=" + req.getLocalPort());
                writer.write("\n  LocalName=" + req.getLocalName());
            } catch (Throwable th) {
            }
        } catch (Throwable th) {
        }

        try {
            writer.write("\n  RemoteAddr=" + req.getRemoteAddr());
            writer.write("\n  RemotePort=" + req.getRemotePort());
            writer.write("\n  RemoteHost=" + req.getRemoteHost());
            writer.write("\n  RemoteUser=" + req.getRemoteUser());
        } catch (Throwable th) {
        }

        try {
            writer.write("\n  Method=" + req.getMethod());
            writer.write("\n  RequestedSessionId=" + req.getRequestedSessionId());
        } catch (Throwable th) {
        }

        try {
            writer.write("\n  AuthType=" + req.getAuthType());
        } catch (Throwable th) {
        }

        writer.write("\n  CharacterEncoding=" + req.getCharacterEncoding());
        writer.write("\n  ContentType=" + req.getContentType());
        writer.write("\n  ContentLengthLong=" + req.getContentLengthLong());
        writer.write("\n  Locale=" + req.getLocale());

        try {
            List<String> hns = Collections.list(req.getHeaderNames());
            writer.write("\n  headers(" + hns.size() + "): " + hns);
            for (String hn : hns) {
                writer.write("\n    " + hn + ": " + ("" + Collections.list(req.getHeaders(hn))).replace("\n", "\n    "));
            }
        } catch (Throwable th) {
        }

        try {
            List<String> pns = Collections.list(req.getParameterNames());
            writer.write("\n  parameters(" + pns.size() + "): " + pns);
            for (String pn : pns) {
                String[] pvs = req.getParameterValues(pn);
                writer.write("\n    " + pn + ": " + ("" + ((pvs != null) ? Arrays.asList(pvs) : "")).replace("\n", "\n    "));
            }
        } catch (Throwable th) {
        }

        try {
            List<String> ans = Collections.list(req.getAttributeNames());
            writer.write("\n  attributes(" + ans.size() + "): " + ans);
            for (String an : ans) {
                writer.write("\n    " + an + ": " + ("" + req.getAttribute(an)).replace("\n", "\n    "));
            }
        } catch (Throwable th) {
        }

        try {
            writer.write("\n  AsyncContext=" + ("" + req.getAsyncContext()).replace("\n", "\n  "));
        } catch (Throwable th) {
        }

        try {
            Cookie[] cqs = req.getCookies();
            writer.write("\n  Cookies=" + ((cqs != null) ? "" + Arrays.asList(cqs) : "").replace("\n", "\n  "));
        } catch (Throwable th) {
        }
        writer.write("</pre>");
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
