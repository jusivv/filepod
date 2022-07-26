package org.coodex.filepod.boot;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Launcher {
    private static Logger log = LoggerFactory.getLogger(Launcher.class);
    
    public static void main(String[] args) throws LifecycleException {
        long start = System.currentTimeMillis();
        // port
        int port = 8090;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("Illegal number of port, use defalut port: " + port);
            }
        }
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);


        if (args.length > 1) {
            log.debug("production mode");
            File webapp = new File(args[1]);
            if (webapp.isDirectory()) {
                tomcat.setBaseDir(webapp.getParent());
                tomcat.addWebapp("", webapp.getAbsolutePath());
            } else {
                throw new RuntimeException("Illegal webapp: " + args[1]);
            }
        } else {
            log.debug("debug mode");
            tomcat.setBaseDir(new File("webapp/target").getAbsolutePath());
            Context context = tomcat.addWebapp("", new File("webapp/src/main/webapp/").getAbsolutePath());
            WebResourceRoot resourceRoot = new StandardRoot(context);
            resourceRoot.addPreResources(new DirResourceSet(resourceRoot, "/WEB-INF/classes/",
                    new File("webapp/target/classes").getAbsolutePath(), "/"));
            context.setResources(resourceRoot);
        }

        tomcat.getConnector();
        tomcat.start();
        log.info("listening on {}", port);
        log.info("server startup in {} ms", System.currentTimeMillis() - start);
        tomcat.getServer().await();
    }
}
