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
    private static ReloadingFileBasedConfigurationBuilder<YAMLConfiguration> builder;

    public static void load(String fileName) throws ConfigurationException {
        Parameters parameters = new Parameters();
        File configFile = new File(fileName);
        builder = new ReloadingFileBasedConfigurationBuilder<>(YAMLConfiguration.class);
        builder.configure(parameters.fileBased().setFile(configFile));
//        builder.getReloadingController().addEventListener(ReloadingEvent.ANY, event -> {
//            log.debug("get event {} on {}", event.getEventType().getName(), event.getData());
//            event.getController().resetReloadingState();
//        });
        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(),
                fileName, 1, TimeUnit.MINUTES);
        trigger.start();
        builder.getConfiguration();
        log.debug("load client settings from {}", fileName);
    }

    public static <T> T get(String key, Class<T> valueClass, T defaultValue) {
        try {
            return builder.getConfiguration().get(valueClass, key, defaultValue);
        } catch (ConfigurationException e) {
            log.error(e.getLocalizedMessage(), e);
            return defaultValue;
        }
    }

    public static String getString(String key, String defaultValue) {
        return get(key, String.class, defaultValue);
    }

    public static String[] getStringArray(String key) {
        try {
            return builder.getConfiguration().getStringArray(key);
        } catch (ConfigurationException e) {
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
