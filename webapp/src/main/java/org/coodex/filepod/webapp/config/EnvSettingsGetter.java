package org.coodex.filepod.webapp.config;

import org.coodex.util.Common;

import java.io.File;

public class EnvSettingsGetter {
    public static final String CONFIGURATION_PATH_KEY = "CONFIGURATION_PATH";
    public static final String LOGBACK_CONFIG_FILE_KEY = "LOGBACK_CONFIG_FILE";
    public static String getValue(String name, String defaultValue) {
        String value = System.getenv(name);
        if (Common.isBlank(value)) {
            value = System.getProperty(name, defaultValue);
        }
        return value;
    }

    public static String getValue(String name) {
        return getValue(name, null);
    }

    public static String configurationPath() {
        String path = getValue(CONFIGURATION_PATH_KEY);
        if (Common.isBlank(path)) {
            path = EnvSettingsGetter.class.getClassLoader().getResource("").getPath();
            path += (path.endsWith(File.separator) ? "config" : "/config");
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    public static String logbackConfigFile() {
        return getValue(LOGBACK_CONFIG_FILE_KEY, "logback.xml");
    }
}
