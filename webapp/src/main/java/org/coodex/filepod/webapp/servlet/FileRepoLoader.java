package org.coodex.filepod.webapp.servlet;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.coodex.filepod.api.IFileRepositorySupplier;
import org.coodex.filepod.webapp.config.ClientSettings;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filepod.webapp.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(urlPatterns = {"/load/file/repository"}, loadOnStartup = 1)
public class FileRepoLoader extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileRepoLoader.class);

    @Override
    public void init() throws ServletException {
        final Yaml yaml = new Yaml();
        ServiceHelper.iterateProvider(IFileRepositorySupplier.class, repoSupplier -> {
            String fileRepoName = repoSupplier.getRepositoryName();
            String filename = EnvSettingsGetter.configurationPath() + "file-repository-" + fileRepoName + ".yml";
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                try {
                    FileRepoManager.register(fileRepoName,
                            repoSupplier.getSupplier(yaml.loadAs(Files.newInputStream(path), repoSupplier.getArgumentType())));
                } catch (IOException e) {
                    log.error("fail to load file: {}", fileRepoName, e);
                }
            }
        });
        try {
            ClientSettings.load(EnvSettingsGetter.configurationPath() + "client.yml");
        } catch (ConfigurationException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new ServletException("fail to read client configuration", e);
        }
    }
}
