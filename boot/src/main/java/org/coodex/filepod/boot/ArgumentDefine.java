package org.coodex.filepod.boot;

public class ArgumentDefine {
    private String option;
    private String longOption;
    private String description;
    private boolean valueRequired;

    public ArgumentDefine(String option, String longOption, String description) {
        this(option, longOption, true, description);
    }

    public ArgumentDefine(String option, String longOption, boolean valueRequired, String description) {
        this.option = option;
        this.longOption = longOption;
        this.valueRequired = valueRequired;
        this.description = description;
    }

    public String getOption() {
        return option;
    }

    public String getLongOption() {
        return longOption;
    }

    public boolean isValueRequired() {
        return valueRequired;
    }

    public String getDescription() {
        return description;
    }
}
