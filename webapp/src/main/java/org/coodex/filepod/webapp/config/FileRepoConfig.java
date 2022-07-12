package org.coodex.filepod.webapp.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileRepoConfig {
    private static final Map<String, Object> configBeans = new HashMap<>();
    public static <T> T load(String filename, Class<T> clazz) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(filename);
        try {
            T config = yaml.loadAs(inputStream, clazz);
            configBeans.put(clazz.getName(), config);
            return config;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {

            }
        }
    }

    public static <T> T get(Class<T> clazz) {
        return (T) configBeans.get(clazz.getName());
    }
}
