package org.coodex.filepod.webapp.repo;

import org.coodex.filepod.api.IFileRepositorySupplier;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.local.LocalRepositoryPath;

import java.io.File;
import java.util.function.Supplier;

public class LocalRepoSupplier implements IFileRepositorySupplier<LocalRepositoryPath[]> {
    @Override
    public String getRepositoryName() {
        return "local";
    }

    @Override
    public Class<LocalRepositoryPath[]> getArgumentType() {
        return LocalRepositoryPath[].class;
    }

    @Override
    public Supplier<IFileRepository> getSupplier(LocalRepositoryPath[] arg) {
        return () -> new LocalFileRepository(buildBasePath(arg), new HashPathGenerator());
    }

    private LocalRepositoryPath[] buildBasePath(LocalRepositoryPath[] paths) {
        for (LocalRepositoryPath path : paths) {
            File basePath = new File(path.getLocation());
            if (!basePath.exists()) {
                basePath.mkdirs();
            }
        }
        return paths;
    }
}
