package org.coodex.filepod.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.HashSet;
import java.util.Set;

public class ServletLoader {
    private static Logger log = LoggerFactory.getLogger(ServletLoader.class);

    public static WebServlet getConfig(HttpServlet servlet) {
        Class<?> clazz = servlet.getClass();
        return clazz.getAnnotation(WebServlet.class);
    }

    public static void load(Context context, HttpServlet servlet) {
        WebServlet webServlet = getConfig(servlet);
        if (webServlet != null) {
            String servletName = StringUtils.isNotBlank(webServlet.name()) ? webServlet.name() :
                servlet.getClass().getName();
            Wrapper wrapper = Tomcat.addServlet(context, servletName, servlet);
            wrapper.setAsyncSupported(webServlet.asyncSupported());
            wrapper.setLoadOnStartup(webServlet.loadOnStartup());
            YamlInitParamReader.read(new ServletInitParamLoader(wrapper), servlet.getClass());
            Set<String> urlPatterns = new HashSet<>();
            for (String urlPattern : webServlet.urlPatterns()) {
                urlPatterns.add(urlPattern);
            }
            for (String urlPattern : webServlet.value()) {
                urlPatterns.add(urlPattern);
            }
            for (String urlPattern : urlPatterns) {
                context.addServletMappingDecoded(urlPattern, servletName);
            }
            log.info("add servlet url patterns: {}, servlet name: {}", String.join(" ", urlPatterns),
                servletName);
        }
    }
}
