package org.coodex.filepod.webapp.config;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ClientSettings {
    private static Logger log = LoggerFactory.getLogger(ClientSettings.class);
    private static YAMLConfiguration configuration;

    public static void load(String fileName) throws ConfigurationException {
        Parameters parameters = new Parameters();
        File configFile = new File(fileName);
        ReloadingFileBasedConfigurationBuilder<YAMLConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(YAMLConfiguration.class);
        builder.configure(parameters.fileBased().setFile(configFile));
        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(),
                null, 1, TimeUnit.MINUTES);
        trigger.start();
        configuration = builder.getConfiguration();
        log.debug("load client settings from {}", fileName);
    }

    public static <T> T get(String key, Class<T> valueClass, T defaultValue) {
        return configuration.get(valueClass, key, defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        return configuration.getString(key, defaultValue);
    }

    public static String[] getStringArray(String key) {
        return configuration.getStringArray(key);
    }
}
