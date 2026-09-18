package io.github.joelmomo.runeboard.suggestion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public final class SuggestionResultTest {

    @Test
    public void primaryReturnsFirstCandidate() {
        SuggestionResult result = new SuggestionResult(
                "helo",
                List.of("hello", "help"),
                false,
                true,
                true);

        assertEquals("hello", result.primary());
        assertTrue(result.recommended);
    }

    @Test
    public void emptyHasNoPrimaryCandidate() {
        assertNull(SuggestionResult.empty("hello").primary());
    }
}
