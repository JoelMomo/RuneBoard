package io.github.joelmomo.runeboard.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public final class EditorActionResolverTest {

    @Test
    public void noneAndUnspecifiedUseEnter() {
        assertEnter(EditorInfo.IME_ACTION_NONE);
        assertEnter(EditorInfo.IME_ACTION_UNSPECIFIED);
    }

    @Test
    public void resolvesStandardActions() {
        assertStandard(
                EditorInfo.IME_ACTION_GO,
                EditorActionSpec.Kind.GO);
        assertStandard(
                EditorInfo.IME_ACTION_SEARCH,
                EditorActionSpec.Kind.SEARCH);
        assertStandard(
                EditorInfo.IME_ACTION_SEND,
                EditorActionSpec.Kind.SEND);
        assertStandard(
                EditorInfo.IME_ACTION_NEXT,
                EditorActionSpec.Kind.NEXT);
        assertStandard(
                EditorInfo.IME_ACTION_DONE,
                EditorActionSpec.Kind.DONE);
        assertStandard(
                EditorInfo.IME_ACTION_PREVIOUS,
                EditorActionSpec.Kind.PREVIOUS);
    }

    @Test
    public void noEnterActionFlagForcesLiteralEnter() {
        EditorActionSpec spec = EditorActionResolver.resolve(
                EditorInfo.IME_ACTION_SEND
                        | EditorInfo.IME_FLAG_NO_ENTER_ACTION,
                0,
                null);

        assertEquals(EditorActionSpec.Kind.ENTER, spec.kind());
        assertFalse(spec.performsEditorAction());
    }

    @Test
    public void customActionUsesProvidedLabelAndId() {
        EditorActionSpec spec = EditorActionResolver.resolve(
                EditorInfo.IME_ACTION_UNSPECIFIED,
                42,
                " Publish ");

        assertEquals(EditorActionSpec.Kind.CUSTOM, spec.kind());
        assertEquals(42, spec.actionId());
        assertEquals("Publish", spec.customLabel());
        assertTrue(spec.performsEditorAction());
    }

    @Test
    public void blankCustomLabelFallsBackToStandardAction() {
        EditorActionSpec spec = EditorActionResolver.resolve(
                EditorInfo.IME_ACTION_DONE,
                42,
                "   ");

        assertEquals(EditorActionSpec.Kind.DONE, spec.kind());
        assertEquals(EditorInfo.IME_ACTION_DONE, spec.actionId());
    }

    private static void assertEnter(int imeOptions) {
        EditorActionSpec spec =
                EditorActionResolver.resolve(imeOptions, 0, null);

        assertEquals(EditorActionSpec.Kind.ENTER, spec.kind());
        assertEquals(0, spec.actionId());
        assertFalse(spec.performsEditorAction());
    }

    private static void assertStandard(
            int action,
            EditorActionSpec.Kind expectedKind) {
        EditorActionSpec spec =
                EditorActionResolver.resolve(action, 0, null);

        assertEquals(expectedKind, spec.kind());
        assertEquals(action, spec.actionId());
        assertTrue(spec.performsEditorAction());
    }
}
