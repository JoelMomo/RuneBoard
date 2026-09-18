package io.github.joelmomo.runeboard.suggestion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

public final class SuggestionTextTest {

    @Test
    public void autocorrectRequiresRecommendedTypo() {
        SuggestionResult recommended = new SuggestionResult(
                "helo", List.of("hello"), false, true, true);
        SuggestionResult weak = new SuggestionResult(
                "helo", List.of("hello"), false, true, false);
        SuggestionResult valid = new SuggestionResult(
                "helo", List.of("hello"), true, false, false);

        assertTrue(SuggestionText.shouldAutoCorrect("helo", recommended));
        assertFalse(SuggestionText.shouldAutoCorrect("helo", weak));
        assertFalse(SuggestionText.shouldAutoCorrect("helo", valid));
        assertFalse(SuggestionText.shouldAutoCorrect("other", recommended));
    }

    @Test
    public void adaptsSuggestionCase() {
        assertEquals("Hello", SuggestionText.adaptCase(
                "hello", "Helo", Locale.US));
        assertEquals("HELLO", SuggestionText.adaptCase(
                "hello", "HELO", Locale.US));
        assertEquals("hello", SuggestionText.adaptCase(
                "hello", "helo", Locale.US));
    }
}
