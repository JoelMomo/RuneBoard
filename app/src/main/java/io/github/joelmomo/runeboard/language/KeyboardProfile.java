package io.github.joelmomo.runeboard.language;

import io.github.joelmomo.runeboard.keyboard.KeyboardLayout;

import java.util.Locale;

public final class KeyboardProfile {

    public final String id;
    public final String localeTag;
    public final String displayName;
    public final String layoutName;
    public final String shortLabel;
    public final KeyboardLayout layout;
    public final Locale locale;

    public KeyboardProfile(
            String id,
            String localeTag,
            String displayName,
            String layoutName,
            String shortLabel,
            KeyboardLayout layout) {
        this.id = id;
        this.localeTag = localeTag;
        this.displayName = displayName;
        this.layoutName = layoutName;
        this.shortLabel = shortLabel;
        this.layout = layout;
        this.locale = Locale.forLanguageTag(localeTag);
    }
}
