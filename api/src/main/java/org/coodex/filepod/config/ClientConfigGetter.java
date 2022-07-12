package org.coodex.filepod.config;

@FunctionalInterface
public interface ClientConfigGetter {
    String getParameterValue(String clientId, String parameterName, String defaultValue);
}
