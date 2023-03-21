package org.coodex.filepod.boot;

import org.coodex.filepod.webapp.config.EnvSettingsGetter;

import java.util.function.Consumer;

import static org.coodex.filepod.boot.LauncherArgs.*;

public class ArgDefIterator {

    private static final ArgumentDefine[] arguments = {
        new ArgumentDefine("c", EnvSettingsGetter.CONFIGURATION_PATH_KEY,
            "Specify path of filepod configurations"),
        new ArgumentDefine("h", ARG_SERVER_ADDRESS,
            "Specify server binding address, default 0.0.0.0"),
        new ArgumentDefine("p", ARG_SERVER_PORT, "Specify server listening port, default 8080"),
        new ArgumentDefine("b", ARG_SERVER_BASE_DIR, "Specify server base directory"),
        new ArgumentDefine("s", ARG_SERVER_CONTEXT_PATH, "Specify server context path, default /"),
        new ArgumentDefine("l", ARG_LOGBACK_CONFIG_FILE,
            "Specify logback config file, default logback.xml")
    };

    public static void iterate(Consumer<ArgumentDefine> consumer) {
        for (ArgumentDefine argumentDefine : arguments) {
            consumer.accept(argumentDefine);
        }
    }
}
