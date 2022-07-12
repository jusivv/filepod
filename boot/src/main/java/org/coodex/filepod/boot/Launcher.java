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
        String basePath = new File("webapp/target/classes").getAbsolutePath();
//        String basePath = Launcher.class.getClassLoader().getResource("").getPath();
        log.debug("base path: {}", basePath);
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8090);
        tomcat.setBaseDir(basePath);

        Context context = tomcat.addWebapp("", new File("webapp/src/main/webapp/").getAbsolutePath());
//        Wrapper wrapper = Tomcat.addServlet(context, ServletSample.class.getSimpleName(), new ServletSample());
//        wrapper.addMapping("/sample");
        WebResourceRoot resourceRoot = new StandardRoot(context);
        resourceRoot.addPreResources(new DirResourceSet(resourceRoot, "/WEB-INF/classes/",
                basePath, "/"));
        context.setResources(resourceRoot);

        tomcat.getConnector();
        tomcat.start();
        log.info("server startup in {} ms", System.currentTimeMillis() - start);
        tomcat.getServer().await();
    }
}
