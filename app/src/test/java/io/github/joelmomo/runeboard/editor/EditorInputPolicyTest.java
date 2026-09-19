package io.github.joelmomo.runeboard.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.text.TextUtils;

import org.junit.Test;

public final class EditorInputPolicyTest {

    @Test
    public void proseDefaultsToSentenceCapitalization() {
        assertEquals(
                TextUtils.CAP_MODE_SENTENCES,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT));
        assertEquals(
                TextUtils.CAP_MODE_SENTENCES,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE));
    }

    @Test
    public void namesAndAddressesDefaultToWordCapitalization() {
        assertEquals(
                TextUtils.CAP_MODE_WORDS,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PERSON_NAME));
        assertEquals(
                TextUtils.CAP_MODE_WORDS,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS));
    }

    @Test
    public void explicitCapitalizationFlagsWin() {
        assertEquals(
                TextUtils.CAP_MODE_CHARACTERS,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
        assertEquals(
                TextUtils.CAP_MODE_WORDS,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
        assertEquals(
                TextUtils.CAP_MODE_SENTENCES,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES));
    }

    @Test
    public void addressLikeAndSensitiveEditorsDisableCapitalization() {
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS));
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_URI));
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    @Test
    public void literalNoSuggestionFieldsAvoidImplicitCaps() {
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        assertEquals(
                TextUtils.CAP_MODE_WORDS,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS));
    }

    @Test
    public void nonTextEditorsDisableCapitalization() {
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_NUMBER));
        assertEquals(
                0,
                EditorInputPolicy.capitalizationModes(
                        InputType.TYPE_CLASS_PHONE));
    }

    @Test
    public void suggestionPolicyMatchesFieldSemantics() {
        assertTrue(EditorInputPolicy.supportsSuggestions(
                InputType.TYPE_CLASS_TEXT));
        assertTrue(EditorInputPolicy.supportsSuggestions(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PERSON_NAME));
        assertFalse(EditorInputPolicy.supportsSuggestions(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS));
        assertFalse(EditorInputPolicy.supportsSuggestions(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        assertFalse(EditorInputPolicy.supportsSuggestions(
                InputType.TYPE_CLASS_NUMBER));
    }
}
