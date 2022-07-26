package org.coodex.filepod.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;

public class FilepodLogbackConfigurator extends ContextAwareBase implements Configurator {
    public static final String LOGBACK_CONFIG_FILE_KEY = "LOGBACK_CONFIG_FILE";
    @Override
    public void configure(LoggerContext loggerContext) {
        String configFile = EnvSettingsGetter.configurationPath() +
                EnvSettingsGetter.getValue(LOGBACK_CONFIG_FILE_KEY, "logback.xml");
        addInfo("setting up filepod logback configuration with file: " + configFile);

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        try {
            configurator.doConfigure(configFile);
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).info("setting up logback with file {}", configFile);
        } catch (JoranException e) {
            throw new RuntimeException(e);
        }
    }
}
