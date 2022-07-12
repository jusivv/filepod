package org.coodex.filepod.webapp.servlet;

import org.coodex.filepod.webapp.util.UriParamHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/attachments/upload/byform/*"}, asyncSupported = true)
public class FileUploadServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileUploadServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("path info: {}", req.getPathInfo());
        UriParamHelper.pathIterator(req, (index, value) -> {
            switch (index) {
                case 1:
                    log.debug("client id: {}", value);
                    break;
                case 2:
                    log.debug("token id: {}", value);
                    break;
                case 3:
                    log.debug("encrypt: {}", value);
            }
            return true;
        });
        resp.getOutputStream().println(req.getRequestURI());
    }
}
