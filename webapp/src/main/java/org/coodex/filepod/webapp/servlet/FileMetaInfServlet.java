package org.coodex.filepod.webapp.servlet;

import com.alibaba.fastjson.JSON;
import org.coodex.filepod.api.IAccessController;
import org.coodex.filepod.pojo.FilepodMetaInf;
import org.coodex.filepod.webapp.config.ClientSettings;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filepod.webapp.util.*;
import org.coodex.filerepository.api.IFileRepository;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@WebServlet(name = "fileMetaInfo", urlPatterns = "/attachments/metaInfo/*", asyncSupported = true)
public class FileMetaInfServlet extends HttpServlet {
    private static final Executor executor = Executors.newCachedThreadPool();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        CompletableFuture.runAsync(AsyncServletBuilder.build((request, response) -> {
            // read parameter
            FilepodServletContext filepodServletContext = getContext(request);
            // access
            IAccessController accessController = ServiceHelper.getProvider(
                ClientSettings.getString(filepodServletContext.getClientId() + ".accessController",
                    "forbidden"),
                IAccessController.class);
            accessController.load((clientId, parameterName, defaultValue) ->
                ClientSettings.getString(clientId + "." + parameterName, defaultValue));
            if (accessController.readable(filepodServletContext.getClientId(), filepodServletContext.getToken(),
                filepodServletContext.getFileId())) {
                // repo name
                String repo = ClientSettings.getString(filepodServletContext.getClientId() + ".fileRepository",
                    "local");
                // get repo
                IFileRepository fileRepository = FileRepoManager.getRepo(repo).orElseThrow(() -> {
                    return new RuntimeException("no file repository for " + repo);
                });
                // get meta info
                FilepodMetaInf metaInf = fileRepository.getMetaInf(filepodServletContext.getFileId(),
                    FilepodMetaInf.class);
                // in scope
                if (accessController.inScope(filepodServletContext.getClientId(), metaInf.getClientId())) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON);
                    response.getOutputStream().write(JSON.toJSONString(metaInf).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                        "reject to read file " + filepodServletContext.getFileId() + " from client "
                            + filepodServletContext.getClientId());
                }
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                    "reject to read file " + filepodServletContext.getFileId() + " from client "
                        + filepodServletContext.getClientId());
            }
        }, asyncContext), executor);
    }

    private FilepodServletContext getContext(HttpServletRequest request) throws FilepodServletException,
        UnsupportedEncodingException {
        String[] paths = UriParamHelper.getPathParameters(request);
        if (paths.length < 4) {
            throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST, "Illegal request parameter");
        }
        FilepodServletContext context = new FilepodServletContext();
        context.setClientId(paths[1]);
        context.setToken(paths[2]);
        context.setFileId(paths[3]);
        return context;
    }
}
