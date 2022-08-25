package org.coodex.filepod.webapp.config;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class EnvSettingsGetter {
    public static final String CONFIGURATION_PATH_KEY = "CONFIGURATION_PATH";
    public static String getValue(String name, String defaultValue) {
        String value = System.getenv(name);
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(name, defaultValue);
        }
        return value;
    }

    public static String getValue(String name) {
        return getValue(name, null);
    }

    public static String configurationPath() {
        String path = getValue(CONFIGURATION_PATH_KEY);
        if (StringUtils.isEmpty(path)) {
            path = EnvSettingsGetter.class.getClassLoader().getResource("").getPath();
            path += (path.endsWith(File.separator) ? "config" : "/config");
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }
}
