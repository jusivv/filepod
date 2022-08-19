package org.coodex.filepod.webapp.repo;

import org.coodex.filerepository.api.IFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class FileRepoManager {
    private static Logger log = LoggerFactory.getLogger(FileRepoManager.class);
    private static String defaultKey;
    private static final Map<String, IFileRepository> fileRepos = new HashMap<>();

    public static void register(String name, Supplier<IFileRepository> supplier, boolean isDefault) {
        fileRepos.put(name, supplier.get());
        if (isDefault) {
            defaultKey = name;
        }
        log.info("register file repository [{}]{}", name, isDefault ? " as default." : ".");
    }

    public static void register(String name, Supplier<IFileRepository> supplier) {
        register(name, supplier, false);
    }

    public static void registerAsDefault(String name, Supplier<IFileRepository> supplier) {
        register(name, supplier, true);
    }

    public static Optional<IFileRepository> getRepo(String name) {
        IFileRepository repository = fileRepos.get(name);
        if (repository != null) {
            return Optional.of(repository);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<IFileRepository> getRepo() {
        return getRepo(defaultKey);
    }

    public static boolean setDefault(String repoName) {
        if (fileRepos.containsKey(repoName)) {
            defaultKey = repoName;
            return true;
        } else {
            return false;
        }
    }
}
