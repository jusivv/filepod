package org.coodex.filepod.webapp.config;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FileRepoConfigManager {
    private static Logger log = LoggerFactory.getLogger(FileRepoConfigManager.class);
    private static final Map<String, Object> configBeans = new HashMap<>();
    public static <T> T load(String configPath, String configName, Class<T> clazz) throws FileNotFoundException {
        String filename = configPath + "file-repository-" + configName + ".yml";
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(filename);
        try {
            T config = yaml.loadAs(inputStream, clazz);
            configBeans.put(configName, config);
            return config;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {

            }
        }
    }

    public static <T> T get(String configName, T config) {
        Object o = configBeans.get(configName);
        if (o != null) {
            try {
                BeanUtils.copyProperties(config, o);
            } catch (IllegalAccessException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (InvocationTargetException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return config;
    }
}
