package org.coodex.filepod.webapp.servlet;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@WebServlet(urlPatterns = {"/attachments/delete/*"}, asyncSupported = true)
public class FileDeleteServlet extends HttpServlet {
    private Executor executor = Executors.newCachedThreadPool();
    private Base64 base64 = new Base64();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        CompletableFuture.runAsync(AsyncServletBuilder.build((request, response) -> {
            // parameter
            FilepodServletContext context = getContext(request);
            // check if deletion is allowed
            if (!ClientSettings.get(context.getClientId() + ".deletable", Boolean.class, false)) {
                throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                    "reject to delete from " + context.getClientId());
            }
            // access
            IAccessController accessController = ServiceHelper.getProvider(
                ClientSettings.getString(context.getClientId() + ".accessController", "forbidden"),
                IAccessController.class);
            accessController.load((clientId, parameterName, defaultValue) ->
                ClientSettings.getString(clientId + "." + parameterName, defaultValue));
            if (accessController.deletable(context.getClientId(), context.getToken(), context.getFileId())) {
                // repo name
                String repo = ClientSettings.getString(context.getClientId() + ".fileRepository",
                    "local");
                // get repo
                IFileRepository fileRepository = FileRepoManager.getRepo(repo).orElseThrow(() -> {
                    return new RuntimeException("no file repository for " + repo);
                });
                // get repo config
                String[] fileIds = context.getFileId().split(",");
                List<FilepodMetaInf> metaInfs = new ArrayList<>();
                for (String fileId : fileIds) {
                    FilepodMetaInf metaInf = fileRepository.getMetaInf(fileId, FilepodMetaInf.class);
                    if (metaInf == null) {
                        throw new FilepodServletException(HttpServletResponse.SC_NOT_FOUND,
                            "file not found " + fileId);
                    }
                    metaInf.setFileId(fileId);
                    // in scope
                    if (accessController.inScope(context.getClientId(), metaInf.getClientId())) {
                        metaInfs.add(metaInf);
                    } else {
                        throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                            "reject to delete file " + fileId);
                    }
                }
                for (FilepodMetaInf metaInf : metaInfs) {
                    fileRepository.delete(metaInf.getFileId());
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().write(JSON.toJSONString(metaInfs).getBytes(StandardCharsets.UTF_8));
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                    "reject to delete from " + context.getClientId());
            }
        }, asyncContext), executor);
    }

    private FilepodServletContext getContext(HttpServletRequest request) throws UnsupportedEncodingException,
        FilepodServletException {
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
