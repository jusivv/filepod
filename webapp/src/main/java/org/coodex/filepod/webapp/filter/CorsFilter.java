package org.coodex.filepod.webapp.filter;

import org.apache.commons.lang3.StringUtils;
import org.coodex.filepod.webapp.config.CorsFilterSettings;
import org.coodex.filepod.webapp.config.EnvSettingsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

@WebFilter(urlPatterns = {"/*"}, asyncSupported = true)
public class CorsFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(CorsFilter.class);
    private CorsFilterSettings settings;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(EnvSettingsGetter.configurationPath() + "cors-settings.yml");
            settings = yaml.loadAs(inputStream, CorsFilterSettings.class);
        } catch (FileNotFoundException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                printRequest(req, settings.isDebug());
                String origin = req.getHeader("Origin");
                for (String o : settings.getAllowedOrigins()) {
                    if (o.equalsIgnoreCase("*") || o.equalsIgnoreCase(origin)) {
                        resp.addHeader("Access-Control-Allow-Origin", origin);
                        break;
                    }
                }
                resp.addHeader("Access-Control-Allow-Methods",
                        StringUtils.join(settings.getAllowedMethods(), ","));
                resp.addHeader("Access-Control-Allow-Headers",
                        StringUtils.join(settings.getAllowedHeaders(), ","));
                resp.addHeader("Access-Control-Expose-Headers",
                        StringUtils.join(settings.getExposedHeaders(), ","));
                resp.addHeader("Access-Control-Allow-Credentials", "true");
                resp.addHeader("Access-Control-Max-Age", Long.toString(settings.getMaxAge()));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private void printRequest(HttpServletRequest request, boolean debug) {
        if (debug) {
            log.debug("CORS filter request ======");
            log.debug("Request URI: {}", request.getRequestURI());
            log.debug("Request method: {}", request.getMethod());
            for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.debug("Header: {} = {}", headerName, headerValue);
            }
            log.debug("==========================");
        }
    }
}
