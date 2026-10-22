package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class RuneThemesTest {

    @Test
    public void unknownThemeFallsBackToDefault() {
        assertEquals(
                RuneThemes.ID_DEFAULT,
                RuneThemes.byId("does-not-exist").id);
        assertEquals(
                RuneThemes.ID_DEFAULT,
                RuneThemes.normalizeId("does-not-exist"));
    }

    @Test
    public void builtInThemesExposeDistinctProfiles() {
        KeyboardTheme normal =
                RuneThemes.byId(RuneThemes.ID_DEFAULT);
        KeyboardTheme oled =
                RuneThemes.byId(RuneThemes.ID_OLED);
        KeyboardTheme transparent =
                RuneThemes.byId(RuneThemes.ID_TRANSPARENT);

        assertEquals(255, normal.defaultBackgroundOpacity);
        assertEquals(255, oled.defaultBackgroundOpacity);
        assertEquals(0, transparent.defaultBackgroundOpacity);

        assertNotEquals(normal.backgroundTop, oled.backgroundTop);
        assertNotEquals(normal.keyAlpha, transparent.keyAlpha);
    }

    @Test
    public void customThemeUsesPersistedVisualInputs() {
        CustomThemeConfig config = new CustomThemeConfig(
                0xFF22D3EE,
                0xFF252B36,
                0xFF07121A,
                0xFF123044,
                18f,
                3f);

        KeyboardTheme theme = RuneThemes.customTheme(config);

        assertEquals(RuneThemes.ID_CUSTOM, theme.id);
        assertEquals(0xFF22D3EE, theme.accent);
        assertEquals(0xFF22D3EE, theme.selectedFill);
        assertEquals(0xFF252B36, theme.keyFill);
        assertEquals(0xFF07121A, theme.backgroundTop);
        assertEquals(0xFF123044, theme.backgroundBottom);
        assertEquals(18f, theme.keyRadiusDp, 0f);
        assertEquals(3f, theme.keyGapDp, 0f);
        assertNotEquals(theme.keyFill, theme.utilityKeyFill);
        assertNotEquals(theme.selectedFill, theme.selectedStroke);
    }

    @Test
    public void selectedContentIsReadableAcrossBuiltInThemes() {
        assertSelectedContentReadable(RuneThemes.defaultTheme());
        assertSelectedContentReadable(RuneThemes.oledTheme());
        assertSelectedContentReadable(RuneThemes.transparentTheme());
        assertSelectedContentReadable(RuneThemes.fosforoTheme());
    }

    @Test
    public void fosforoIdIsRecognized() {
        assertEquals(
                RuneThemes.ID_FOSFORO,
                RuneThemes.normalizeId(RuneThemes.ID_FOSFORO));
        assertEquals(
                RuneThemes.ID_FOSFORO,
                RuneThemes.byId(RuneThemes.ID_FOSFORO).id);
    }

    @Test
    public void fosforoSharesOtiumPhosphorTerminalLook() {
        KeyboardTheme theme = RuneThemes.fosforoTheme();

        assertEquals(0xFF000000, theme.backgroundTop);
        assertEquals(0xFF000000, theme.backgroundBottom);
        assertEquals(0xFF7CFFB2, theme.selectedFill);
        assertEquals(0xFF7CFFB2, theme.accent);
        assertEquals(0xFF7CFFB2, theme.keyOutline);
        assertEquals(0xFF3A8FB7, theme.utilityKeyOutline);
        assertEquals(0xFF0F1A28, theme.backgroundDot);
        assertTrue(theme.selectedGlow);
        assertEquals(255, theme.defaultBackgroundOpacity);
        assertTrue(theme.keyRadiusDp <= 4f);
    }

    @Test
    public void fosforoKeyLabelsStayReadableOnTheirKeys() {
        KeyboardTheme theme = RuneThemes.fosforoTheme();

        assertTrue(contrastRatio(theme.textPrimary, theme.keyFill) >= 4.5d);
        assertTrue(contrastRatio(theme.textPrimary, theme.utilityKeyFill) >= 4.5d);
        assertTrue(contrastRatio(theme.textSecondary, theme.utilityKeyFill) >= 4.5d);
    }

    @Test
    public void earlierThemesKeepTheirSoftStyle() {
        KeyboardTheme[] themes = {
                RuneThemes.defaultTheme(),
                RuneThemes.oledTheme(),
                RuneThemes.transparentTheme(),
                RuneThemes.customTheme(CustomThemeConfig.defaults())
        };

        for (KeyboardTheme theme : themes) {
            assertEquals(theme.id, 0, theme.keyOutline);
            assertEquals(theme.id, 0, theme.utilityKeyOutline);
            assertEquals(theme.id, 0, theme.backgroundDot);
            assertFalse(theme.id, theme.selectedGlow);
        }
    }

    @Test
    public void selectedContentAdaptsAcrossCustomAccentPresets() {
        int[] accents = {
                0xFFA78BFA,
                0xFF22D3EE,
                0xFF34D399,
                0xFFFBBF24,
                0xFFF472B6
        };

        for (int accent : accents) {
            CustomThemeConfig defaults = CustomThemeConfig.defaults();
            KeyboardTheme theme = RuneThemes.customTheme(
                    new CustomThemeConfig(
                            accent,
                            defaults.keyFill,
                            defaults.backgroundTop,
                            defaults.backgroundBottom,
                            defaults.keyRadiusDp,
                            defaults.keyGapDp));
            assertSelectedContentReadable(theme);
        }
    }

    @Test
    public void customIdIsRecognized() {
        assertEquals(
                RuneThemes.ID_CUSTOM,
                RuneThemes.normalizeId(RuneThemes.ID_CUSTOM));
        assertEquals(
                RuneThemes.ID_CUSTOM,
                RuneThemes.byId(RuneThemes.ID_CUSTOM).id);
    }

    private static void assertSelectedContentReadable(
            KeyboardTheme theme) {
        assertTrue(
                "Selected content contrast for " + theme.id,
                contrastRatio(
                        theme.selectedContent,
                        theme.selectedFill) >= 4.5d);
    }

    private static double contrastRatio(int first, int second) {
        double firstLuminance = relativeLuminance(first);
        double secondLuminance = relativeLuminance(second);
        double lighter = Math.max(firstLuminance, secondLuminance);
        double darker = Math.min(firstLuminance, secondLuminance);
        return (lighter + 0.05d) / (darker + 0.05d);
    }

    private static double relativeLuminance(int color) {
        return 0.2126d * linearize((color >>> 16) & 0xFF)
                + 0.7152d * linearize((color >>> 8) & 0xFF)
                + 0.0722d * linearize(color & 0xFF);
    }

    private static double linearize(int component) {
        double value = component / 255d;
        return value <= 0.04045d
                ? value / 12.92d
                : Math.pow((value + 0.055d) / 1.055d, 2.4d);
    }
}
