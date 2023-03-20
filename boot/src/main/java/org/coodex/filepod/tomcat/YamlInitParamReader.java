package org.coodex.filepod.tomcat;

import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.BiConsumer;

public class YamlInitParamReader {
    private static Logger log = LoggerFactory.getLogger(YamlInitParamReader.class);
    public static void read(IInitParamLoader initParamLoader, Class<?> clazz) {
        File file = new File(EnvSettingsGetter.configurationPath() + clazz.getName() + ".yml");
        iterateYaml(file, (name, value) -> {
            log.debug("{} init-param: {} = {}", clazz.getName(), name, value);
            initParamLoader.load(name, value);
        });
    }

    private static void iterateValue(String key, Object value, BiConsumer<String, String> consumer) {
        if (value instanceof Map) {
            ((Map<String, Object>) value).forEach((k, v) -> {
                iterateValue(key + "." + k, v, consumer);
            });
        } else if (value instanceof String){
            consumer.accept(key, (String) value);
        }
    }

    public static void iterateYaml(File file, BiConsumer<String, String> consumer) {
        if (file.isFile()) {
            Yaml yaml = new Yaml();
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                Map<String, Object> parameterMap = yaml.load(inputStream);
                parameterMap.forEach((key, value) -> {
                    iterateValue(key, value, consumer);
                });
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }
}
