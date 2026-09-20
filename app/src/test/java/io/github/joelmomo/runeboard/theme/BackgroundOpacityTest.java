package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class BackgroundOpacityTest {

    @Test
    public void normalizationUsesNearestSupportedLevel() {
        assertEquals(255, BackgroundOpacity.normalize(250));
        assertEquals(170, BackgroundOpacity.normalize(180));
        assertEquals(85, BackgroundOpacity.normalize(90));
        assertEquals(0, BackgroundOpacity.normalize(12));
    }

    @Test
    public void percentagesMatchDisplayedLevels() {
        assertEquals(100, BackgroundOpacity.percent(255));
        assertEquals(67, BackgroundOpacity.percent(170));
        assertEquals(33, BackgroundOpacity.percent(85));
        assertEquals(0, BackgroundOpacity.percent(0));
    }

    @Test
    public void levelsCycleBackToOpaque() {
        assertEquals(170, BackgroundOpacity.next(255));
        assertEquals(85, BackgroundOpacity.next(170));
        assertEquals(0, BackgroundOpacity.next(85));
        assertEquals(255, BackgroundOpacity.next(0));
    }
}
