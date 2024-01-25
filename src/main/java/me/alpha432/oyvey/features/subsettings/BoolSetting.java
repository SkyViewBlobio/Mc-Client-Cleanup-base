package me.alpha432.oyvey.features.subsettings;

import me.alpha432.oyvey.features.setting.Setting;

import java.util.function.Predicate;

public final class BoolSetting extends Setting {
    private final boolean defaultValue;
    private boolean parent;
    private boolean value;

    public BoolSetting(final String name, final boolean defaultValue, final boolean parent) {
        super(name, defaultValue);
        this.parent = parent;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BoolSetting(final String name, final boolean defaultValue, final boolean parent, final Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
        this.parent = parent;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void setValue(final boolean value) {
        this.value = value;
    }

    public boolean isParent() {
        return this.parent;
    }

    public void toggle() {
        this.value = !this.value;
    }
}
