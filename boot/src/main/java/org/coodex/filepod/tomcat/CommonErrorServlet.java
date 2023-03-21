package org.coodex.filepod.tomcat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {"/CommonErrorServlet"}, asyncSupported = true)
public class CommonErrorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.addHeader("Content-Type", "application/json");
        resp.getOutputStream()
            .write("{\"code\":403,\"message\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8));
    }
}
