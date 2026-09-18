package io.github.joelmomo.runeboard.theme;

public final class RuneThemes {

    public static final String ID_DEFAULT = "default";
    public static final String ID_OLED = "oled";
    public static final String ID_TRANSPARENT = "transparent";

    private RuneThemes() {
    }

    public static KeyboardTheme defaultTheme() {
        return new KeyboardTheme(
                ID_DEFAULT,
                255,
                0xFF0D0E14,
                0xFF181321,
                0xFF2B2B38,
                0xFF20202B,
                0xFF8B5CF6,
                0xFFE0D6FF,
                0xFFF8F8FC,
                0xFF9FA0B4,
                0xFFA78BFA,
                232,
                220,
                10f,
                6f,
                12f,
                38f);
    }

    public static KeyboardTheme oledTheme() {
        return new KeyboardTheme(
                ID_OLED,
                255,
                0xFF000000,
                0xFF000000,
                0xFF17171D,
                0xFF0E0E13,
                0xFF7C3AED,
                0xFFE9DDFF,
                0xFFFFFFFF,
                0xFF9A9AA8,
                0xFF9F67FF,
                238,
                225,
                10f,
                6f,
                12f,
                38f);
    }

    public static KeyboardTheme transparentTheme() {
        return new KeyboardTheme(
                ID_TRANSPARENT,
                0,
                0xFF0D0E14,
                0xFF181321,
                0xFF30303D,
                0xFF242430,
                0xFF8B5CF6,
                0xFFF0E9FF,
                0xFFFFFFFF,
                0xFFB3B3C2,
                0xFFB197FC,
                205,
                195,
                10f,
                6f,
                12f,
                38f);
    }

    public static KeyboardTheme byId(String id) {
        if (ID_OLED.equals(id)) {
            return oledTheme();
        }
        if (ID_TRANSPARENT.equals(id)) {
            return transparentTheme();
        }
        return defaultTheme();
    }
}
