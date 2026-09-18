package io.github.joelmomo.runeboard.theme;

public final class KeyboardTheme {

    public final String id;
    public final int defaultBackgroundOpacity;
    public final int backgroundTop;
    public final int backgroundBottom;
    public final int keyFill;
    public final int utilityKeyFill;
    public final int selectedFill;
    public final int selectedStroke;
    public final int textPrimary;
    public final int textSecondary;
    public final int accent;
    public final int keyAlpha;
    public final int utilityKeyAlpha;
    public final float outerMarginDp;
    public final float keyGapDp;
    public final float keyRadiusDp;
    public final float headerHeightDp;

    public KeyboardTheme(
            String id,
            int defaultBackgroundOpacity,
            int backgroundTop,
            int backgroundBottom,
            int keyFill,
            int utilityKeyFill,
            int selectedFill,
            int selectedStroke,
            int textPrimary,
            int textSecondary,
            int accent,
            int keyAlpha,
            int utilityKeyAlpha,
            float outerMarginDp,
            float keyGapDp,
            float keyRadiusDp,
            float headerHeightDp) {
        this.id = id;
        this.defaultBackgroundOpacity =
                BackgroundOpacity.normalize(defaultBackgroundOpacity);
        this.backgroundTop = backgroundTop;
        this.backgroundBottom = backgroundBottom;
        this.keyFill = keyFill;
        this.utilityKeyFill = utilityKeyFill;
        this.selectedFill = selectedFill;
        this.selectedStroke = selectedStroke;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.accent = accent;
        this.keyAlpha = keyAlpha;
        this.utilityKeyAlpha = utilityKeyAlpha;
        this.outerMarginDp = outerMarginDp;
        this.keyGapDp = keyGapDp;
        this.keyRadiusDp = keyRadiusDp;
        this.headerHeightDp = headerHeightDp;
    }
}
