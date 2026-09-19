package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    public void customIdIsRecognized() {
        assertEquals(
                RuneThemes.ID_CUSTOM,
                RuneThemes.normalizeId(RuneThemes.ID_CUSTOM));
        assertEquals(
                RuneThemes.ID_CUSTOM,
                RuneThemes.byId(RuneThemes.ID_CUSTOM).id);
    }
}
