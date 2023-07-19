package org.coodex.filepod.supplier;

import org.coodex.filerepository.local.LocalRepositoryPath;

public class LocalRepoConfig {
    private LocalRepositoryPath[] directories;
    private String[] pathGenerators;

    public LocalRepositoryPath[] getDirectories() {
        return directories;
    }

    public void setDirectories(LocalRepositoryPath[] directories) {
        this.directories = directories;
    }

    public String[] getPathGenerators() {
        return pathGenerators;
    }

    public void setPathGenerators(String[] pathGenerators) {
        this.pathGenerators = pathGenerators;
    }
}
