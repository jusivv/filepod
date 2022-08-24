package org.coodex.filepod.webapp.servlet;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.coodex.filepod.api.IAccessController;
import org.coodex.filepod.api.ICryptoStreamWrapper;
import org.coodex.filepod.pojo.FilepodMetaInf;
import org.coodex.filepod.webapp.config.ClientSettings;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filepod.webapp.util.*;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.util.Common;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@WebServlet(urlPatterns = {"/attachments/upload/byform/*"}, asyncSupported = true)
public class FileUploadServlet extends HttpServlet {
    private Executor executor = Executors.newCachedThreadPool();

    private Base64 base64 = new Base64();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();
        CompletableFuture.runAsync(AsyncServletBuilder.build((request, response) -> {
            // parameter
            FilepodServletContext context = getContext(request);
            // access
            IAccessController accessController = ServiceHelper.getProvider(
                    ClientSettings.getString(context.getClientId() + ".accessController", "forbidden"),
                    IAccessController.class);
            accessController.load((clientId, paramName, defaultValue) ->
                    ClientSettings.getString(clientId + "." + paramName, defaultValue));
            if (accessController.writable(context.getClientId(), context.getToken())) {
                // upload
                //// repo name
                String repo = ClientSettings.getString(context.getClientId() + ".fileRepository",
                        "local");
                //// get repo
                IFileRepository fileRepository = FileRepoManager.getRepo(repo).orElseThrow(
                        () -> new RuntimeException("no file repository for " + repo));
                ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
                List<FileItem> items = uploadHandler.parseRequest(request);
                List<FilepodMetaInf> metaInfos = new ArrayList<>();
                for (FileItem item : items) {
                    if (!item.isFormField() && !Common.isBlank(item.getName())) {
                        // meta inf
                        FilepodMetaInf metaInf = new FilepodMetaInf();
                        metaInf.setFileName(item.getName());
                        metaInf.setExtName(FilenameUtils.getExtension(item.getName()));
                        metaInf.setContentType(item.getContentType());
                        metaInf.setFileSize(item.getSize());
                        metaInf.setClientId(context.getClientId());
                        // file store
                        InputStream is = item.getInputStream();
                        if (context.isEncrypt()) {
                            // crypto
                            metaInf.setCipherModel(
                                    ClientSettings.getString(context.getClientId() + ".defaultCipher", "aes.v2")
                            );
                            String serverKey = ClientSettings.getString(
                                    context.getClientId() + ".serverKey", null);
                            ICryptoStreamWrapper streamWrapper = ServiceHelper.getProvider(metaInf.getCipherModel(),
                                    ICryptoStreamWrapper.class);
                            streamWrapper.init(serverKey != null ? base64.decode(serverKey) : new byte[0],
                                    metaInf);
                            is = streamWrapper.getInput(is);
                        }
                        metaInf.setFileId(fileRepository.save(is, metaInf));
                        metaInfos.add(metaInf);
                        // notify
                        CompletableFuture.runAsync(
                                () -> accessController.notify(context.getClientId() , context.getToken(), metaInf.getFileId()),
                                executor);
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().write(JSON.toJSONString(metaInfos).getBytes(StandardCharsets.UTF_8));
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                        "reject to upload from " + context.getClientId());
            }
        }, asyncContext), executor);
    }

    private FilepodServletContext getContext(HttpServletRequest request) throws UnsupportedEncodingException {
        final FilepodServletContext context = new FilepodServletContext();
        UriParamHelper.pathIterator(request, (index, value) -> {
            switch (index) {
                case 1:
                    context.setClientId(value);
                    break;
                case 2:
                    context.setToken(value);
                    break;
                case 3:
                    context.setEncrypt("1".equals(value));
            }
            return true;
        });
        return context;
    }
}
