package org.coodex.security.access;

import okhttp3.*;

import java.nio.charset.StandardCharsets;

public class ConcreteV040AccessController extends ConcreteAccessController {
    @Override
    public boolean accept(String tag) {
        return "concrete_v0.4.x".equalsIgnoreCase(tag) || "concrete_v0.4.0".equalsIgnoreCase(tag);
    }

    @Override
    protected Request buildRequest(String url, String token, String fileId) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        String content = fileId != null ?
                String.format("{\"attachmentId\":\"%s\", \"token\":\"%s\"}", fileId, token) :
                token;
        return new Request.Builder().url(httpUrl)
                .post(RequestBody.create(content.getBytes(StandardCharsets.UTF_8), MediaType.get("application/json")))
                .build();
    }
}
