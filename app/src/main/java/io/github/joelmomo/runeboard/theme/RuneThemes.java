package io.github.joelmomo.runeboard.theme;

public final class RuneThemes {

    public static final String ID_DEFAULT = "default";
    public static final String ID_OLED = "oled";
    public static final String ID_TRANSPARENT = "transparent";
    public static final String ID_CUSTOM = "custom";

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

    public static KeyboardTheme customTheme(CustomThemeConfig config) {
        CustomThemeConfig value =
                config == null ? CustomThemeConfig.defaults() : config;
        return new KeyboardTheme(
                ID_CUSTOM,
                255,
                value.backgroundTop,
                value.backgroundBottom,
                value.keyFill,
                darken(value.keyFill, 0.72f),
                value.accent,
                lighten(value.accent, 0.35f),
                0xFFFFFFFF,
                0xFFB3B3C2,
                value.accent,
                235,
                220,
                10f,
                value.keyGapDp,
                value.keyRadiusDp,
                38f);
    }

    public static KeyboardTheme byId(String id) {
        if (ID_OLED.equals(id)) {
            return oledTheme();
        }
        if (ID_TRANSPARENT.equals(id)) {
            return transparentTheme();
        }
        if (ID_CUSTOM.equals(id)) {
            return customTheme(CustomThemeConfig.defaults());
        }
        return defaultTheme();
    }

    public static String normalizeId(String id) {
        if (ID_OLED.equals(id)
                || ID_TRANSPARENT.equals(id)
                || ID_CUSTOM.equals(id)) {
            return id;
        }
        return ID_DEFAULT;
    }

    private static int darken(int color, float factor) {
        return argb(
                alpha(color),
                Math.round(red(color) * factor),
                Math.round(green(color) * factor),
                Math.round(blue(color) * factor));
    }

    private static int lighten(int color, float amount) {
        return argb(
                alpha(color),
                mix(red(color), 255, amount),
                mix(green(color), 255, amount),
                mix(blue(color), 255, amount));
    }

    private static int mix(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }

    private static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    private static int red(int color) {
        return (color >>> 16) & 0xFF;
    }

    private static int green(int color) {
        return (color >>> 8) & 0xFF;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }
}
