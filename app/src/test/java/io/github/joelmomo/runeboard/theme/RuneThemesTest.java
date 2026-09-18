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
}
