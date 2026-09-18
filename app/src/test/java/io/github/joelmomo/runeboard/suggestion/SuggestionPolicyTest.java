package io.github.joelmomo.runeboard.suggestion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

public final class SuggestionPolicyTest {

    @Test
    public void acceptsNormalAndMultilineText() {
        assertTrue(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT));
        assertTrue(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
    }

    @Test
    public void rejectsNoSuggestionsFlag() {
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
    }

    @Test
    public void rejectsPasswordVariations() {
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD));
    }

    @Test
    public void rejectsEmailAndUri() {
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_URI));
    }

    @Test
    public void rejectsNonTextEditors() {
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_NUMBER));
        assertFalse(SuggestionPolicy.supportsInputType(
                InputType.TYPE_CLASS_PHONE));
    }
}
