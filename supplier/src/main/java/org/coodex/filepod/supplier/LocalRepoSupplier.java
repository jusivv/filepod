package org.coodex.filepod.supplier;

import org.coodex.filepod.api.IFileRepositorySupplier;
import org.coodex.filerepository.api.IFileRepository;
import org.coodex.filerepository.local.IPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.local.LocalRepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LocalRepoSupplier implements IFileRepositorySupplier<LocalRepoConfig> {
    private static Logger log = LoggerFactory.getLogger(LocalRepoSupplier.class);
    @Override
    public String getRepositoryName() {
        return "local";
    }

    @Override
    public Class<LocalRepoConfig> getArgumentType() {
        return LocalRepoConfig.class;
    }

    @Override
    public Supplier<IFileRepository> getSupplier(LocalRepoConfig arg) {
        return () -> new LocalFileRepository(buildBasePath(arg.getDirectories()),
            getPathGenerators(arg.getPathGenerators()));
    }

    private LocalRepositoryPath[] buildBasePath(LocalRepositoryPath[] paths) {
        if (paths != null) {
            for (LocalRepositoryPath path : paths) {
                File basePath = new File(path.getLocation());
                if (!basePath.exists()) {
                    basePath.mkdirs();
                }
            }
        }
        return paths;
    }

    private IPathGenerator[] getPathGenerators(String[] classes) {
        List<IPathGenerator> generators = new ArrayList<>();
        if (classes != null) {
            for (String c : classes) {
                try {
                    generators.add((IPathGenerator) Class.forName(c).getConstructor().newInstance());
                } catch (InstantiationException e) {
                    log.error("error to create PathGenerator, {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    log.error("error to create PathGenerator, {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    log.error("error to create PathGenerator, {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    log.error("error to create PathGenerator, {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    log.error("error to create PathGenerator, {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        return generators.toArray(new IPathGenerator[0]);
    }
}
