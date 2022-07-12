package org.coodex.security.access;

import okhttp3.*;

import java.nio.charset.StandardCharsets;

public class SessionBasedAccessController extends ClientFeedbackAccessController {
    @Override
    public boolean accept(String tag) {
        return "session".equalsIgnoreCase(tag);
    }

    @Override
    protected Request buildRequest(String url, String token, String fileId) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        String content = fileId != null ?
                String.format("{\"fileId\":\"%s\", \"token\":\"%s\"}", fileId, token) :
                String.format("{\"token\":\"%s\"}", token);
        return new Request.Builder().url(httpUrl)
                .post(RequestBody.create(content.getBytes(StandardCharsets.UTF_8), MediaType.get("application/json")))
                .build();
    }

    @Override
    protected boolean parseResponse(Response response) {
        return true;
    }
}
