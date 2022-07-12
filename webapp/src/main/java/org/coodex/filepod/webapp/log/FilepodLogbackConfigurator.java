package org.coodex.filepod.webapp.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;

public class FilepodLogbackConfigurator extends ContextAwareBase implements Configurator {
    @Override
    public void configure(LoggerContext loggerContext) {
        String configFile = EnvSettingsGetter.configurationPath() + EnvSettingsGetter.logbackConfigFile();
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
