package org.coodex.filepod.webapp.servlet;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.coodex.filepod.webapp.config.ClientSettings;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.coodex.filepod.webapp.config.FileRepoConfigManager;
import org.coodex.filepod.webapp.config.FileRepoLocalConfig;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.coodex.filerepository.local.LocalRepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileNotFoundException;

@WebServlet(urlPatterns = {"/load/file/repository"}, loadOnStartup = 1)
public class FileRepoLoader extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileRepoLoader.class);

    @Override
    public void init() throws ServletException {
        final String fileRepoName = "local";
        FileRepoManager.registerAsDefault(fileRepoName, () -> {
            try {
                FileRepoLocalConfig localConfig = FileRepoConfigManager.load(EnvSettingsGetter.configurationPath(),
                        fileRepoName, FileRepoLocalConfig.class);
                return new LocalFileRepository(buildBasePath(localConfig), new HashPathGenerator());
            } catch (FileNotFoundException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        });
        try {
            ClientSettings.load(EnvSettingsGetter.configurationPath() + "client.yml");
        } catch (ConfigurationException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new ServletException("fail to read client configuration", e);
        }
    }

    private LocalRepositoryPath[] buildBasePath(FileRepoLocalConfig config) {
        LocalRepositoryPath[] paths = config.getPaths();
        for (LocalRepositoryPath path : paths) {
            File basePath = new File(path.getLocation());
            if (!basePath.exists()) {
                basePath.mkdirs();
            }
        }
        return paths;
    }
}
