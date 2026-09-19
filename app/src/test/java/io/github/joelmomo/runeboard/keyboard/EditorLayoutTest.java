package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class EditorLayoutTest {

    @Test
    public void editorLayoutExposesClipboardAndNavigationCommands() {
        KeyboardLayout layout = KeyboardLayouts.editorLayout();

        assertEquals(EditorCommand.SELECT_ALL,
                layout.getKey(0, 0).getCommand());
        assertEquals(EditorCommand.CUT,
                layout.getKey(0, 1).getCommand());
        assertEquals(EditorCommand.COPY,
                layout.getKey(0, 2).getCommand());
        assertEquals(EditorCommand.PASTE,
                layout.getKey(0, 3).getCommand());
        assertEquals(EditorCommand.UNDO,
                layout.getKey(1, 0).getCommand());
        assertEquals(EditorCommand.REDO,
                layout.getKey(1, 1).getCommand());
        assertEquals(EditorCommand.HOME,
                layout.getKey(1, 2).getCommand());
        assertEquals(EditorCommand.END,
                layout.getKey(1, 3).getCommand());
        assertEquals(EditorCommand.DELETE_FORWARD,
                layout.getKey(3, 1).getCommand());
    }

    @Test
    public void editorLayoutHasExplicitReturnToAlphabetKey() {
        KeyboardLayout layout = KeyboardLayouts.editorLayout();

        assertEquals(
                KeyboardKey.Type.EDIT,
                layout.getKey(4, 0).getType());
    }
}
