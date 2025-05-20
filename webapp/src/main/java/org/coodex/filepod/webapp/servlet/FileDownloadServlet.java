package org.coodex.filepod.webapp.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.coodex.filepod.api.IAccessController;
import org.coodex.filepod.api.ICryptoStreamWrapper;
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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/attachments/download/*"}, asyncSupported = true)
public class FileDownloadServlet extends HttpServlet {
    private Executor executor = Executors.newCachedThreadPool();

    private Base64 base64 = new Base64();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
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
            if (accessController.readable(context.getClientId(), context.getToken(), context.getFileId())) {
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
                    // in scope
                    if (accessController.inScope(context.getClientId(), metaInf.getClientId())) {
                        // fill meta info
                        metaInf.setFileId(fileId);
                        metaInf.setFileSize(Math.max(metaInf.getFileSize(), metaInf.getSize()));
                        metaInfs.add(metaInf);
                    } else {
                        throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                                "reject to download file " + fileId);
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
                // accept range
                String range = request.getHeader("RANGE");

                boolean supportRange = !StringUtils.isEmpty(range);
                if (metaInfs.size() > 1) {
                    if (supportRange) {
                        throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST,
                                "do not support range option when downloading multi-files");
                    }
                    // multi files
                    response.setHeader("Content-Type", "application/zip");
                    response.setHeader("Content-Disposition",
                                    String.format("attachment;filename=\"%d.zip\"",
                                            System.currentTimeMillis()));
                    ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(response.getOutputStream());
                    try {
                        for (FilepodMetaInf metaInf : metaInfs) {
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(
                                    metaInf.getFileId() + "_" + getFileName(metaInf)
                            );
                            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                            // crypto
                            OutputStream os = zipArchiveOutputStream;
                            if (!StringUtils.isEmpty(metaInf.getCipherModel())) {
                                ICryptoStreamWrapper streamWrapper = ServiceHelper.getProvider(metaInf.getCipherModel(),
                                        ICryptoStreamWrapper.class);
                                String serverKey = ClientSettings.getString(
                                        context.getClientId() + ".serverKey", null);
                                streamWrapper.init(
                                        serverKey != null ? base64.decode(serverKey) : new byte[0], metaInf);
                                os = streamWrapper.getOutput(os);
                            }
                            fileRepository.get(metaInf.getFileId(), os);
                            os.flush();
                            zipArchiveOutputStream.closeArchiveEntry();
                        }
                    } finally {
                        zipArchiveOutputStream.close();
                    }
                } else if (metaInfs.size() == 1) {
                    // single file
                    FilepodMetaInf metaInf = metaInfs.get(0);
                    response.setHeader("Accept-Ranges", "bytes");
                    response.setHeader("Content-Type", metaInf.getContentType());
                    response.setHeader("Content-Disposition",
                                    String.format("%s;filename=\"%s\"", getContentDispType(metaInf),
                                            URLEncoder.encode(getFileName(metaInf), "UTF-8")));
                    OutputStream outputStream = response.getOutputStream();
                    try {
                        // range
                        long offset = 0;
                        long length = 0;
                        if (supportRange) {
                            if (!(metaInf.getFileSize() > 0)) {
                                throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST,
                                    "do not support range option, file meta info error.");
                            }
                            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                            String pattern = "bytes\\s*=\\s*(\\d+)\\s*-\\s*(\\d+).*";
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(range);
                            if (m.find()) {
                                offset = Math.min(Math.max(0L, Long.parseLong(m.group(1))), metaInf.getFileSize());
                                length = Math.max(0L,
                                        Math.min(metaInf.getFileSize(), Long.parseLong(m.group(2))) - offset);
                                response.setHeader("Content-Range",
                                        "bytes " + offset + "-" + (offset + length) + "/" + length);
                                response.setHeader("Content-Length", Long.toString(length));

                            } else {
                                throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST,
                                        "Illegal range value in header");
                            }
                        } else {
                            if (metaInf.getFileSize() > 0) {
                                response.setHeader("Content-Length", Long.toString(metaInf.getFileSize()));
                            }
                        }
                        // crypto
                        OutputStream os = outputStream;
                        if (!StringUtils.isEmpty(metaInf.getCipherModel())) {
                            ICryptoStreamWrapper streamWrapper = ServiceHelper.getProvider(metaInf.getCipherModel(),
                                    ICryptoStreamWrapper.class);
                            String serverKey = ClientSettings.getString(
                                    context.getClientId() + ".serverKey", null);
                            streamWrapper.init(
                                    serverKey != null ? base64.decode(serverKey) : new byte[0], metaInf);
                            os = streamWrapper.getOutput(os);
                        }
                        fileRepository.get(metaInf.getFileId(), offset, (int)length, os);
                    } finally {
                        outputStream.close();
                    }
                }
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_FORBIDDEN,
                        "reject to download from " + context.getClientId());
            }
        }, asyncContext), executor);
    }

    private FilepodServletContext getContext(HttpServletRequest request) throws FilepodServletException,
            UnsupportedEncodingException {
        FilepodServletContext context = new FilepodServletContext();
        context.setFileId(UriParamHelper.getPathParameter(1, request));
        if (context.getFileId() != null) {
            Map<String, String[]> matrixMap = UriParamHelper.getMatrixParamMap(context.getFileId(), request);
            String[] values = matrixMap.get("c");
            if (values != null && values.length > 0) {
                context.setClientId(values[0]);
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST, "Illegal client id");
            }
            values = matrixMap.get("t");
            if (values != null && values.length > 0) {
                context.setToken(values[0]);
            } else {
                throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST, "Illegal token");
            }
        } else {
            throw new FilepodServletException(HttpServletResponse.SC_BAD_REQUEST, "Illegal file id");
        }
        return context;
    }

    private String getContentDispType(FilepodMetaInf metaInf) {
        String contentType = metaInf.getContentType();
        return contentType.startsWith("text") || contentType.startsWith("image") ? "inline"
                : "attachment";
    }

    private String getFileName(FilepodMetaInf metaInf) {
        return !StringUtils.isEmpty(metaInf.getFileName()) ? metaInf.getFileName() : metaInf.getName();
    }
}
