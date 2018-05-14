/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openshift.quickstarts.undertow.servlet;

//import com.digia.ia.questionnaire.digiaiaq.DigiaIAQServletListener;
//import com.digia.ia.questionnaire.service.DigiaIABase;
//import com.ssg.common.collections.SetWithCounters;
//import com.ssg.x.xrestservlet.RESTServletHelper;
//import com.ssg.x.xrestservlet.base.RESTContext;
//import com.ssg.x.xrestservlet.base.RESTMethod;
import com.ssg.common.collections.SetWithCounters;
import com.ssg.x.xrestservlet.RESTServletHelper;
import com.ssg.x.xrestservlet.base.RESTContext;
import com.ssg.x.xrestservlet.base.RESTMethod;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sesidoro
 */
@WebServlet(name = "REST", urlPatterns = {"/REST/*"})
public class REST extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static RESTServletHelper restHelper = new RESTServletHelper() {
        @Override
        public boolean afterRESTMethodInvoke(RESTContext ctx, RESTMethod m, Object[] values, Object result) {
            boolean r = super.afterRESTMethodInvoke(ctx, m, values, result);

//            if (ctx.service() instanceof DigiaIABase) {
//                ((DigiaIABase) ctx.service()).setLastActivityAt(System.currentTimeMillis());
//            }

            return r;
        }

        @Override
        public boolean beforeRESTMethodInvoke(RESTContext ctx, RESTMethod m, Object[] values) {
            boolean r = super.beforeRESTMethodInvoke(ctx, m, values);

            return r;
        }
    };

    RESTStatistics statistics = new RESTStatistics();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public REST() {
        super();
    }

    public static RESTServletHelper getHelper() {
        return restHelper;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific message occurs
     * @throws IOException if an I/O message occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr() + " / " + request.getRemoteHost();
        System.out.println("REST request[" + request.getRemoteHost() + "]: " + request.getRequestURL());
        long started = System.nanoTime();
        statistics.register(request.getRemoteHost(), request.getRequestURL().toString());
        try {
            // check if services are not lost -> reinit...
            Enumeration<String> sans = (request != null && request.getSession() != null) ? request.getSession().getAttributeNames() : null;
//            if (sans == null || !sans.hasMoreElements()) {
//                DigiaIAQServletListener.initSession(request.getSession());
//            }
            HttpSession session = request.getSession();

            while (sans.hasMoreElements()) {
                String n = sans.nextElement();
                Object o = request.getSession().getAttribute(n);
//                if (o instanceof DigiaIABase) {
//                    DigiaIABase ia = (DigiaIABase) o;
//                    if (ia.getHost() == null) {
//                        ia.setHost(request.getRemoteHost());
//                    }
//                    if (session != null && ia.getSessionId() == null) {
//                        ia.setSessionId(session.getId());
//                    }
//                }
            }

            restHelper.processRESTRequest(request, response);
        } finally {
            int a = 0;
        }
        statistics.registerTime(request.getRequestURL().toString(), System.nanoTime() - started);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on
    // the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific message occurs
     * @throws IOException if an I/O message occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific message occurs
     * @throws IOException if an I/O message occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "REST interface for WebServlet and XType classes.";
    }// </editor-fold>

    public static class RESTStatistics {

        public SetWithCounters<String> clientHosts = new SetWithCounters<String>();
        public SetWithCounters<String> clientMethods = new SetWithCounters<String>();
        public SetWithCounters<String> methods = new SetWithCounters<String>();
        public Map<String, long[]> methodTimes = new LinkedHashMap<String, long[]>();

        public void register(String host, String url) {

        }

        public void registerTime(String url, long time) {

        }

        public String dump() {
            StringBuilder sb = new StringBuilder();

            return sb.toString();
        }
    }
}
