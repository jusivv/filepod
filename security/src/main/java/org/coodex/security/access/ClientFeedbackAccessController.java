package org.coodex.security.access;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ClientFeedbackAccessController extends ScopeAccessController {
    private static Logger log = LoggerFactory.getLogger(ClientFeedbackAccessController.class);

    private OkHttpClient client;

    public ClientFeedbackAccessController() {
        this.client = new OkHttpClient();
    }

    /**
     * request success or not
     * @param code  HTTP status code
     * @return      success or not
     */
    private boolean successStatus(int code) {
        return code >= 200 && code < 300;
    }

    /**
     * build a okHttp request
     * @param url       request URL
     * @param token     client token
     * @param fileId    file identifier
     * @return          okHttp request
     */
    protected abstract Request buildRequest(String url, String token, String fileId);

    /**
     * parse client response to boolean
     * @param response
     * @return
     */
    protected abstract boolean parseResponse(Response response);

    protected boolean getFeedback(String host, String path, String token, String fileId) {
        String url = (!host.endsWith("/") ? host : host.substring(0, host.length() - 1)) + path;
        log.debug("request authentication to {}", url);
        Request request = buildRequest(url, token, fileId);
        Response response = null;
        try {
            response = client.newCall(request).execute();
            log.debug("response status code: {}", response.code());
            log.debug("response message: {}", response.message());
            if (successStatus(response.code())) {
                return parseResponse(response);
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String getHost(String clientId) {
        return clientConfigGetter.getParameterValue(clientId, "clientUrl",
                "http://127.0.0.1");
    }

    @Override
    public boolean canWrite(String clientId, String token) {
        return getFeedback(getHost(clientId), "/writable", token, null);
    }

    @Override
    public boolean canRead(String clientId, String token, String fileId) {
        return getFeedback(getHost(clientId), "/readable", token, fileId);
    }

    @Override
    public boolean canDelete(String clientId, String token, String fileId) {
        return getFeedback(getHost(clientId),"/deletable", token, fileId);
    }

    @Override
    public boolean notify(String clientId, String token, String fileId) {
        return getFeedback(getHost(clientId), "/notify", token, fileId);
    }
}
