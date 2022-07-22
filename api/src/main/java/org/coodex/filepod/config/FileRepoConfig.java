package org.coodex.filepod.config;

import org.coodex.filepod.api.IProviderSelector;

public abstract class FileRepoConfig implements IProviderSelector {
    private String defaultCipher;
    private String serverKey;

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
