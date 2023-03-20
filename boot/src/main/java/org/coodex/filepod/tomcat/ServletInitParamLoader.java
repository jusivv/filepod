package org.coodex.filepod.tomcat;

import org.apache.catalina.Wrapper;

public class ServletInitParamLoader implements IInitParamLoader {
    private Wrapper wrapper;

    public ServletInitParamLoader(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void load(String name, String value) {
        wrapper.addInitParameter(name, value);
    }
}
