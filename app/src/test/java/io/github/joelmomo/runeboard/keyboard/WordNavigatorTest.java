package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WordNavigatorTest {

    @Test
    public void movesRightToNextWordStart() {
        String text = "alpha  beta, gamma";

        assertEquals(7, WordNavigator.move(text, 0, 1));
        assertEquals(13, WordNavigator.move(text, 7, 1));
        assertEquals(text.length(), WordNavigator.move(text, 13, 1));
    }

    @Test
    public void movesLeftToPreviousWordStart() {
        String text = "alpha  beta, gamma";

        assertEquals(13, WordNavigator.move(text, text.length(), -1));
        assertEquals(7, WordNavigator.move(text, 13, -1));
        assertEquals(0, WordNavigator.move(text, 7, -1));
    }

    @Test
    public void apostropheStaysInsideWord() {
        String text = "don't stop";

        assertEquals(6, WordNavigator.move(text, 0, 1));
        assertEquals(0, WordNavigator.move(text, 6, -1));
    }
}
