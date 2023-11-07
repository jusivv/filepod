package org.coodex.filepod.boot;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.coodex.filepod.logback.LogbackConfigurator;
import org.coodex.filepod.tomcat.CommonErrorServlet;
import org.coodex.filepod.tomcat.FilterLoader;
import org.coodex.filepod.tomcat.ServletLoader;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.coodex.filepod.webapp.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.coodex.filepod.boot.LauncherArgs.*;

public class Launcher {
    private static Logger log = LoggerFactory.getLogger(Launcher.class);
    
    public static void main(String[] args) throws LifecycleException {
        long start = System.currentTimeMillis();
        // load args
        ArgDefIterator.iterate(argumentDefine -> {
            EnvSettingsGetter.addArgumentDef(
                argumentDefine.getOption(),
                argumentDefine.getLongOption(),
                argumentDefine.getDescription()
            );
        });
        EnvSettingsGetter.parseArgs(args);
        // version
        if (EnvSettingsGetter.hasArgument("version")) {
            System.out.println(Launcher.class.getPackage().getImplementationVersion());
            return;
        }

        // log
        LogbackConfigurator.load();
        // address
        String address = EnvSettingsGetter.getValue(ARG_SERVER_ADDRESS);
        // port
        int port = 8080;
        try {
            port = Integer.parseInt(EnvSettingsGetter.getValue(ARG_SERVER_PORT, String.valueOf(port)));
        } catch (NumberFormatException e) {
            log.warn("Illegal {}, use default port: {}", ARG_SERVER_PORT, port);
        }
        // base dir
        String baseDir = EnvSettingsGetter.getValue(ARG_SERVER_BASE_DIR);
        Tomcat tomcat = new Tomcat();
        if (baseDir != null && Files.isDirectory(Paths.get(baseDir))) {
            tomcat.setBaseDir(baseDir);
        }
        tomcat.setPort(port);

        // context path
        String contextPath = EnvSettingsGetter.getValue(ARG_SERVER_CONTEXT_PATH, "");
        if (!contextPath.equals("") && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        Context context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());
        context.setDisplayName("filepod");

        // custom error
        ErrorPage errorPage404 = new ErrorPage();
        errorPage404.setErrorCode(HttpServletResponse.SC_NOT_FOUND);
        errorPage404.setLocation("/" + CommonErrorServlet.class.getSimpleName());
        context.addErrorPage(errorPage404);
        ErrorPage errorPage500 = new ErrorPage();
        errorPage500.setErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        errorPage500.setLocation(errorPage404.getLocation());
        context.addErrorPage(errorPage500);

        // load filter
        ServiceHelper.iterateProvider(Filter.class, filter -> {
            FilterLoader.load(context, filter);
        });

        // load servlet
        ServiceHelper.iterateProvider(HttpServlet.class, httpServlet -> {
            ServletLoader.load(context, httpServlet);
        });

//        if (args.length > 1) {
//            log.debug("production mode");
//            File webapp = new File(args[1]);
//            if (webapp.isDirectory()) {
//                tomcat.setBaseDir(webapp.getParent());
//                tomcat.addWebapp("", webapp.getAbsolutePath());
//            } else {
//                throw new RuntimeException("Illegal webapp: " + args[1]);
//            }
//        } else {
//            log.debug("debug mode");
//            tomcat.setBaseDir(new File("boot/target").getAbsolutePath());
//            Context context = tomcat.addWebapp("", new File("boot/src/main/webapp/").getAbsolutePath());
//            WebResourceRoot resourceRoot = new StandardRoot(context);
//            resourceRoot.addPreResources(new DirResourceSet(resourceRoot, "/WEB-INF/classes/",
//                    new File("webapp/target/classes").getAbsolutePath(), "/"));
//            context.setResources(resourceRoot);
//        }

        Connector connector = tomcat.getConnector();
        if (address != null) {
            connector.setProperty("address", address);
        }
        tomcat.start();
        log.info("service on {}:{}, context path: [{}]", address != null ? address : "", port, contextPath);
        log.info("server startup in {} ms", System.currentTimeMillis() - start);
        tomcat.getServer().await();
    }
}
