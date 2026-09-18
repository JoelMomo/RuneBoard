package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class BackgroundOpacityTest {

    @Test
    public void normalizationUsesNearestSupportedLevel() {
        assertEquals(255, BackgroundOpacity.normalize(250));
        assertEquals(180, BackgroundOpacity.normalize(170));
        assertEquals(90, BackgroundOpacity.normalize(100));
        assertEquals(0, BackgroundOpacity.normalize(12));
    }

    @Test
    public void levelsCycleBackToOpaque() {
        assertEquals(180, BackgroundOpacity.next(255));
        assertEquals(90, BackgroundOpacity.next(180));
        assertEquals(0, BackgroundOpacity.next(90));
        assertEquals(255, BackgroundOpacity.next(0));
    }
}
