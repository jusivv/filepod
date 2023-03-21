package org.coodex.filepod.boot;

public class ArgumentDefine {
    private String option;
    private String longOption;
    private String description;

    public ArgumentDefine(String option, String longOption, String description) {
        this.option = option;
        this.longOption = longOption;
        this.description = description;
    }

    public String getOption() {
        return option;
    }

    public String getLongOption() {
        return longOption;
    }

    public String getDescription() {
        return description;
    }
}
