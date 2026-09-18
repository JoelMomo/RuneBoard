package io.github.joelmomo.runeboard.theme;

import android.graphics.Color;

public final class RuneThemes {

    private RuneThemes() {
    }

    public static KeyboardTheme defaultTheme() {
        return new KeyboardTheme(
                Color.rgb(13, 14, 20),
                Color.rgb(24, 19, 33),
                Color.rgb(43, 43, 56),
                Color.rgb(32, 32, 43),
                Color.rgb(139, 92, 246),
                Color.rgb(224, 214, 255),
                Color.rgb(248, 248, 252),
                Color.rgb(159, 160, 180),
                Color.rgb(167, 139, 250),
                232,
                220,
                10f,
                6f,
                12f,
                38f);
    }
}
