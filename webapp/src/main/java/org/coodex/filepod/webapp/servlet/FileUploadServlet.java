package org.coodex.filepod.webapp.servlet;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.coodex.filepod.api.IAccessController;
import org.coodex.filepod.api.ICryptoStreamWrapper;
import org.coodex.filepod.config.FileRepoConfig;
import org.coodex.filepod.pojo.FilepodMetaInf;
import org.coodex.filepod.webapp.config.ClientSettings;
import org.coodex.filepod.webapp.config.FileRepoConfigManager;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filepod.webapp.util.*;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@WebServlet(urlPatterns = {"/attachments/upload/byform/*"}, asyncSupported = true)
public class FileUploadServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileUploadServlet.class);

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
            accessController.load((clientId, paramName, defaultValue) -> {
                return ClientSettings.getString(clientId + "." + paramName, defaultValue);
            });
            if (accessController.writable(context.getClientId(), context.getToken())) {
                // upload
                //// repo name
                String repo = ClientSettings.getString(context.getClientId() + ".fileRepository",
                        "local");
                //// get repo
                IFileRepository fileRepository = FileRepoManager.getRepo(repo).orElseThrow(() -> {
                    return new RuntimeException("no file repository for " + repo);
                });
                //// get repo config
                FileRepoConfig fileRepoConfig = ServiceHelper.getProvider(repo, FileRepoConfig.class);
                FileRepoConfigManager.get(repo, fileRepoConfig);
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
                        if (context.isEncrypt()) {
                            metaInf.setCipherModel(fileRepoConfig.getDefaultCipher());
                        }
                        // file store
                        InputStream is = item.getInputStream();
                        if (!Common.isBlank(metaInf.getCipherModel())) {
                            // crypto
                            ICryptoStreamWrapper streamWrapper = ServiceHelper.getProvider(metaInf.getCipherModel(),
                                    ICryptoStreamWrapper.class);
                            streamWrapper.init(base64.decode(fileRepoConfig.getServerKey()), metaInf);
                            is = streamWrapper.getInput(is);
                        }
                        metaInf.setFileId(fileRepository.save(is, metaInf));
                        metaInfos.add(metaInf);
                        // notify
                        CompletableFuture.runAsync(() -> {
                            accessController.notify(context.getClientId() , context.getToken(),
                                    metaInf.getFileId());
                        }, executor);
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().println(JSON.toJSONString(metaInfos));
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
