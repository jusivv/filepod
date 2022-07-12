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
import java.util.Map;

@WebServlet(urlPatterns = {"/attachments/download/*"}, asyncSupported = true)
public class FileDownloadServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileDownloadServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("path info: {}", req.getPathInfo());
//        AsyncContext asyncContext = req.startAsync();

        String fileId = UriParamHelper.getPathParameter(1, req);
        log.debug("download file id: {}", fileId);
        if (fileId != null) {
            Map<String, String[]> matrixMap = UriParamHelper.getMatrixParamMap(fileId, req);
            for (String key : matrixMap.keySet()) {
                String[] values = matrixMap.get(key);
                for (String value : values) {
                    log.debug("matrix parameter: {} = {}", key, value);
                }
            }
        }
        resp.getOutputStream().println(req.getRequestURI());
    }
}
