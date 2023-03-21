package org.coodex.filepod.filter;

import org.apache.catalina.filters.CorsFilter;

import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {"/*"}, asyncSupported = true)
public class ApacheCorsFilterFacade extends CorsFilter {
}
