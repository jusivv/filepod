package org.coodex.filepod.tomcat;

import org.apache.tomcat.util.descriptor.web.FilterDef;

public class FilterInitParamLoader implements IInitParamLoader {
    private FilterDef filterDef;

    public FilterInitParamLoader(FilterDef filterDef) {
        this.filterDef = filterDef;
    }

    @Override
    public void load(String name, String value) {
        filterDef.addInitParameter(name, value);
    }
}
