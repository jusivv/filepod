package org.coodex.filepod.tomcat;

import org.apache.catalina.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import java.util.HashSet;
import java.util.Set;

public class FilterLoader {
    private static Logger log = LoggerFactory.getLogger(FilterLoader.class);
    public static WebFilter getConfig(Filter filter) {
        Class<?> clazz = filter.getClass();
        WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
        if (webFilter == null) {
            return null;
        }
        if (clazz.getAnnotation(Deprecated.class) != null) {
            return null;
        }
        return webFilter;
    }

    public static void load(Context context, Filter filter) {
        WebFilter webFilter = getConfig(filter);
        if (webFilter != null) {
            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(filter);
            filterDef.setFilterClass(filter.getClass().getName());
            filterDef.setFilterName(StringUtils.isNotBlank(webFilter.filterName()) ? webFilter.filterName() :
                filterDef.getFilterClass());
            filterDef.setDisplayName(StringUtils.isNotBlank(webFilter.displayName()) ? webFilter.displayName() :
                filter.getClass().getSimpleName());
            filterDef.setAsyncSupported(webFilter.asyncSupported() ? "true" : "false");
            filterDef.setDescription(webFilter.description());
            filterDef.setLargeIcon(webFilter.largeIcon());
            filterDef.setSmallIcon(webFilter.smallIcon());
            YamlInitParamReader.read(new FilterInitParamLoader(filterDef), filter.getClass());
            context.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filterDef.getFilterName());
            Set<String> urlPatterns = new HashSet<>();
            for (String urlPattern : webFilter.urlPatterns()) {
                urlPatterns.add(urlPattern);
            }
            for (String urlPattern : webFilter.value()) {
                urlPatterns.add(urlPattern);
            }
            for (String urlPattern : urlPatterns) {
                filterMap.addURLPattern(urlPattern);
            }
            for (String servletName : webFilter.servletNames()) {
                filterMap.addServletName(servletName);
            }
            for (DispatcherType dispatcherType : webFilter.dispatcherTypes()) {
                filterMap.setDispatcher(dispatcherType.name());
            }
            context.addFilterMap(filterMap);
            log.info("add filter, url patterns: {}, filter name: {}", String.join(" ", urlPatterns),
                filterDef.getFilterName());
        }
    }

}
