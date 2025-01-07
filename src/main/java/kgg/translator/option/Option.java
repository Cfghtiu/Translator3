package kgg.translator.option;

import java.util.ArrayList;
import java.util.List;

public class Option {
    private static final List<Option> options = new ArrayList<>();

    public static List<Option> getOptions() {
        return options;
    }

    public final String name;
    private boolean enable;
    public final boolean defaultValue;

    public Option(String name, boolean defaultValue) {
        this.name = name;
        this.enable = defaultValue;
        this.defaultValue = defaultValue;
        options.add(this);
    }
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
