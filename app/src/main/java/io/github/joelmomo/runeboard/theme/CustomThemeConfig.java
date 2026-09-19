package io.github.joelmomo.runeboard.theme;

public final class CustomThemeConfig {

    public static final int DEFAULT_ACCENT = 0xFFA78BFA;
    public static final int DEFAULT_KEY_FILL = 0xFF2B2B38;
    public static final int DEFAULT_BACKGROUND_TOP = 0xFF0D0E14;
    public static final int DEFAULT_BACKGROUND_BOTTOM = 0xFF181321;
    public static final float DEFAULT_RADIUS_DP = 12f;
    public static final float DEFAULT_GAP_DP = 6f;

    public final int accent;
    public final int keyFill;
    public final int backgroundTop;
    public final int backgroundBottom;
    public final float keyRadiusDp;
    public final float keyGapDp;

    public CustomThemeConfig(
            int accent,
            int keyFill,
            int backgroundTop,
            int backgroundBottom,
            float keyRadiusDp,
            float keyGapDp) {
        this.accent = accent;
        this.keyFill = keyFill;
        this.backgroundTop = backgroundTop;
        this.backgroundBottom = backgroundBottom;
        this.keyRadiusDp = clamp(keyRadiusDp, 2f, 24f);
        this.keyGapDp = clamp(keyGapDp, 2f, 10f);
    }

    public static CustomThemeConfig defaults() {
        return new CustomThemeConfig(
                DEFAULT_ACCENT,
                DEFAULT_KEY_FILL,
                DEFAULT_BACKGROUND_TOP,
                DEFAULT_BACKGROUND_BOTTOM,
                DEFAULT_RADIUS_DP,
                DEFAULT_GAP_DP);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
