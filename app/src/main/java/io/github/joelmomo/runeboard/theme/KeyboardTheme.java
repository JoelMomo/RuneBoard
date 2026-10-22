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
    public final int selectedContent;
    public final int textPrimary;
    public final int textSecondary;
    public final int accent;
    public final int keyAlpha;
    public final int utilityKeyAlpha;
    public final float outerMarginDp;
    public final float keyGapDp;
    public final float keyRadiusDp;
    public final float headerHeightDp;
    /** Neon outline drawn around resting typing keys; 0 keeps the soft default edge. */
    public final int keyOutline;
    /** Neon outline drawn around resting utility keys; 0 keeps the soft default edge. */
    public final int utilityKeyOutline;
    /** Color of a sparse dot matrix over the background; 0 draws none. */
    public final int backgroundDot;
    /** Whether the selected key casts a soft glow of its fill color. */
    public final boolean selectedGlow;

    public KeyboardTheme(
            String id,
            int defaultBackgroundOpacity,
            int backgroundTop,
            int backgroundBottom,
            int keyFill,
            int utilityKeyFill,
            int selectedFill,
            int selectedStroke,
            int selectedContent,
            int textPrimary,
            int textSecondary,
            int accent,
            int keyAlpha,
            int utilityKeyAlpha,
            float outerMarginDp,
            float keyGapDp,
            float keyRadiusDp,
            float headerHeightDp) {
        this(
                id,
                defaultBackgroundOpacity,
                backgroundTop,
                backgroundBottom,
                keyFill,
                utilityKeyFill,
                selectedFill,
                selectedStroke,
                selectedContent,
                textPrimary,
                textSecondary,
                accent,
                keyAlpha,
                utilityKeyAlpha,
                outerMarginDp,
                keyGapDp,
                keyRadiusDp,
                headerHeightDp,
                0,
                0,
                0,
                false);
    }

    public KeyboardTheme(
            String id,
            int defaultBackgroundOpacity,
            int backgroundTop,
            int backgroundBottom,
            int keyFill,
            int utilityKeyFill,
            int selectedFill,
            int selectedStroke,
            int selectedContent,
            int textPrimary,
            int textSecondary,
            int accent,
            int keyAlpha,
            int utilityKeyAlpha,
            float outerMarginDp,
            float keyGapDp,
            float keyRadiusDp,
            float headerHeightDp,
            int keyOutline,
            int utilityKeyOutline,
            int backgroundDot,
            boolean selectedGlow) {
        this.id = id;
        this.defaultBackgroundOpacity =
                BackgroundOpacity.normalize(defaultBackgroundOpacity);
        this.backgroundTop = backgroundTop;
        this.backgroundBottom = backgroundBottom;
        this.keyFill = keyFill;
        this.utilityKeyFill = utilityKeyFill;
        this.selectedFill = selectedFill;
        this.selectedStroke = selectedStroke;
        this.selectedContent = selectedContent;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.accent = accent;
        this.keyAlpha = keyAlpha;
        this.utilityKeyAlpha = utilityKeyAlpha;
        this.outerMarginDp = outerMarginDp;
        this.keyGapDp = keyGapDp;
        this.keyRadiusDp = keyRadiusDp;
        this.headerHeightDp = headerHeightDp;
        this.keyOutline = keyOutline;
        this.utilityKeyOutline = utilityKeyOutline;
        this.backgroundDot = backgroundDot;
        this.selectedGlow = selectedGlow;
    }
}
