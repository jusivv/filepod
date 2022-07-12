package org.coodex.filepod.webapp.config;

import org.coodex.filerepository.local.LocalRepositoryPath;

public class FileRepoLocalConfig {
    private LocalRepositoryPath[] paths;
    private String defaultCipher;
    private String serverKey;

    public LocalRepositoryPath[] getPaths() {
        return paths;
    }

    public void setPaths(LocalRepositoryPath[] paths) {
        this.paths = paths;
    }

    public String getDefaultCipher() {
        return defaultCipher;
    }

    public void setDefaultCipher(String defaultCipher) {
        this.defaultCipher = defaultCipher;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }
}
