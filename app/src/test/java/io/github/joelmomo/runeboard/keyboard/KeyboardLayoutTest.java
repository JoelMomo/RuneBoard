package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardLayoutTest {

    @Test
    public void qwertyUsesThorStaggeredRows() {
        KeyboardLayout layout = KeyboardLayouts.qwerty();

        assertEquals(6, layout.getRowCount());
        assertEquals(9, layout.getRow(0).size());
        assertEquals(10, layout.getRow(1).size());
        assertEquals(10, layout.getRow(2).size());
        assertEquals(9, layout.getRow(3).size());
        assertEquals(7, layout.getRow(4).size());
        assertEquals(7, layout.getRow(5).size());

        assertTrue(layout.getRow(3).getLeftInsetWeight() > 0f);
        assertTrue(layout.getRow(4).getLeftInsetWeight()
                > layout.getRow(3).getLeftInsetWeight());
    }

    @Test
    public void quickEditRowExposesCommonEditingActions() {
        KeyboardLayout layout = KeyboardLayouts.qwerty();

        assertEquals(EditorCommand.CURSOR_LEFT,
                layout.getKey(0, 0).getCommand());
        assertEquals(EditorCommand.CURSOR_RIGHT,
                layout.getKey(0, 1).getCommand());
        assertEquals(EditorCommand.COPY,
                layout.getKey(0, 2).getCommand());
        assertEquals(EditorCommand.PASTE,
                layout.getKey(0, 3).getCommand());
        assertEquals(EditorCommand.UNDO,
                layout.getKey(0, 4).getCommand());
        assertEquals(EditorCommand.REDO,
                layout.getKey(0, 5).getCommand());
        assertEquals(KeyboardKey.Type.OPACITY,
                layout.getKey(0, 6).getType());
        assertEquals(KeyboardKey.Type.MINIMIZE,
                layout.getKey(0, 7).getType());
        assertEquals(KeyboardKey.Type.EDIT,
                layout.getKey(0, 8).getType());
    }

    @Test
    public void numberRowIsShorterThanLetterRows() {
        KeyboardLayout layout = KeyboardLayouts.qwerty();

        assertTrue(layout.getRow(1).getHeightWeight()
                < layout.getRow(2).getHeightWeight());
        assertTrue(layout.getRow(5).getHeightWeight()
                < layout.getRow(2).getHeightWeight());
    }
}
