package org.coodex.filepod.webapp.servlet;

import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.coodex.filepod.webapp.config.FileRepoConfig;
import org.coodex.filepod.webapp.config.FileRepoLocalConfig;
import org.coodex.filepod.webapp.repo.FileRepoManager;
import org.coodex.filerepository.local.HashPathGenerator;
import org.coodex.filerepository.local.LocalFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.FileNotFoundException;

@WebServlet(urlPatterns = {"/load/file/repository"}, loadOnStartup = 1)
public class FileRepoLoader extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(FileRepoLoader.class);

    @Override
    public void init() throws ServletException {
        final String fileRepoName = "local";

        FileRepoManager.registerAsDefault(fileRepoName, () -> {
            try {
                FileRepoLocalConfig localConfig = FileRepoConfig.load(
                        EnvSettingsGetter.configurationPath() + "file-repository-" + fileRepoName + ".yml",
                        FileRepoLocalConfig.class);
                return new LocalFileRepository(localConfig.getPaths(), new HashPathGenerator());
            } catch (FileNotFoundException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }
}
