package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardLayoutTest {

    @Test
    public void qwertyUsesThorStaggeredRows() {
        KeyboardLayout layout = KeyboardLayouts.qwerty();

        assertEquals(5, layout.getRowCount());
        assertEquals(10, layout.getRow(0).size());
        assertEquals(10, layout.getRow(1).size());
        assertEquals(9, layout.getRow(2).size());
        assertEquals(7, layout.getRow(3).size());
        assertEquals(10, layout.getRow(4).size());

        assertTrue(layout.getRow(2).getLeftInsetWeight() > 0f);
        assertTrue(layout.getRow(3).getLeftInsetWeight()
                > layout.getRow(2).getLeftInsetWeight());
    }

    @Test
    public void numberRowIsShorterThanLetterRows() {
        KeyboardLayout layout = KeyboardLayouts.qwerty();

        assertTrue(layout.getRow(0).getHeightWeight()
                < layout.getRow(1).getHeightWeight());
        assertTrue(layout.getRow(4).getHeightWeight()
                > layout.getRow(1).getHeightWeight());
    }
}
