package org.coodex.filepod.webapp.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface RunnableHttpServlet {
    void run(HttpServletRequest request, HttpServletResponse response) throws Throwable;
}
