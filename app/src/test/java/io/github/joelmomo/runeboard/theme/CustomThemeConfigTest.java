package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CustomThemeConfigTest {

    @Test
    public void geometryIsClampedToSafeRange() {
        CustomThemeConfig low = new CustomThemeConfig(
                0xFF000001,
                0xFF000002,
                0xFF000003,
                0xFF000004,
                -10f,
                0f);
        assertEquals(2f, low.keyRadiusDp, 0f);
        assertEquals(2f, low.keyGapDp, 0f);

        CustomThemeConfig high = new CustomThemeConfig(
                0xFF000001,
                0xFF000002,
                0xFF000003,
                0xFF000004,
                99f,
                99f);
        assertEquals(24f, high.keyRadiusDp, 0f);
        assertEquals(10f, high.keyGapDp, 0f);
    }

    @Test
    public void defaultsMatchRuneBoardBaseGeometry() {
        CustomThemeConfig config = CustomThemeConfig.defaults();

        assertEquals(12f, config.keyRadiusDp, 0f);
        assertEquals(6f, config.keyGapDp, 0f);
        assertEquals(
                CustomThemeConfig.DEFAULT_ACCENT,
                config.accent);
    }
}
