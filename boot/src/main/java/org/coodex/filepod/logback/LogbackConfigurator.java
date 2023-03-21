package org.coodex.filepod.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.coodex.filepod.boot.LauncherArgs.ARG_LOGBACK_CONFIG_FILE;

public class LogbackConfigurator {

    public static void load() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String configFile = EnvSettingsGetter.configurationPath() +
            EnvSettingsGetter.getValue(ARG_LOGBACK_CONFIG_FILE, "logback.xml");
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        try {
            configurator.doConfigure(configFile);
            StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).info("setting up logback with file {}", configFile);
        } catch (JoranException e) {
            throw new RuntimeException(e);
        }
    }
}
