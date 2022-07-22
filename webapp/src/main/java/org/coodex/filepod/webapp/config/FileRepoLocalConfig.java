package org.coodex.filepod.webapp.config;

import org.coodex.filepod.config.FileRepoConfig;
import org.coodex.filerepository.local.LocalRepositoryPath;

public class FileRepoLocalConfig extends FileRepoConfig {
    private LocalRepositoryPath[] paths;

    public LocalRepositoryPath[] getPaths() {
        return paths;
    }

    public void setPaths(LocalRepositoryPath[] paths) {
        this.paths = paths;
    }

    @Override
    public boolean accept(String tag) {
        return "local".equalsIgnoreCase(tag);
    }
}
