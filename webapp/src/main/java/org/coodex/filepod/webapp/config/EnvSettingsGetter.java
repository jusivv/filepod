package org.coodex.filepod.webapp.config;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EnvSettingsGetter {
    private static Logger log = LoggerFactory.getLogger(EnvSettingsGetter.class);
    public static final String CONFIGURATION_PATH_KEY = "FilepodConfigPath";
    private static Options options = new Options();
    private static CommandLine cmd;

    public static String getValue(String name, String defaultValue) {
        String value = null;
        if (cmd != null) {
            value = cmd.getOptionValue(name);
        }
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(name);
        }
        if (StringUtils.isEmpty(value)) {
            value = System.getenv(name);
        }
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    public static String getValue(String name) {
        return getValue(name, null);
    }

    public static boolean hasArgument(String name) {
        return cmd.hasOption(name);
    }

    public static String configurationPath() {
        String path = getValue(CONFIGURATION_PATH_KEY);
        if (StringUtils.isEmpty(path)) {
            path = new File(".").getAbsolutePath();
            path += (path.endsWith(File.separator) ? "config" : "/config");
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    public static void addArgumentDef(String opt, String longOpt, String desc) {
        options.addOption(opt, longOpt, true, desc);
    }

    public static void parseArgs(String[] args) {
        options.addOption("V", "version", false, "show version");
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.warn(e.getLocalizedMessage(), e);
        }

    }
}
