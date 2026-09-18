package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardStateTest {

    @Test
    public void startsOnQ() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());

        assertEquals(1, state.getSelectedRow());
        assertEquals(0, state.getSelectedCol());
        assertEquals("q", state.getSelectedKey().getText());
    }

    @Test
    public void horizontalMovementWrapsWithinRow() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());

        assertTrue(state.move(-1, 0));
        assertEquals(9, state.getSelectedCol());
        assertEquals("p", state.getSelectedKey().getText());

        assertTrue(state.move(1, 0));
        assertEquals(0, state.getSelectedCol());
        assertEquals("q", state.getSelectedKey().getText());
    }

    @Test
    public void verticalMovementTracksPhysicalKeyCenter() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());
        state.select(1, 9);

        assertTrue(state.move(0, 1));
        assertEquals(2, state.getSelectedRow());
        assertEquals(8, state.getSelectedCol());
        assertEquals("l", state.getSelectedKey().getText());

        assertTrue(state.move(0, 1));
        assertEquals(3, state.getSelectedRow());
        assertEquals(6, state.getSelectedCol());
        assertEquals("m", state.getSelectedKey().getText());
    }

    @Test
    public void opacityCyclesAndReturnsToOpaque() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());

        assertEquals(255, state.getOpacity());
        state.cycleOpacity();
        assertEquals(180, state.getOpacity());
        state.cycleOpacity();
        assertEquals(90, state.getOpacity());
        state.cycleOpacity();
        assertEquals(0, state.getOpacity());
        state.cycleOpacity();
        assertEquals(255, state.getOpacity());
    }

    @Test
    public void initialOpacitySnapsToNearestSupportedLevel() {
        KeyboardState state =
                new KeyboardState(
                        KeyboardLayouts.qwerty(),
                        170);

        assertEquals(180, state.getOpacity());
    }

    @Test
    public void movementStopsWhileMinimized() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());
        state.setMinimized(true);

        assertFalse(state.move(1, 0));
        assertEquals(0, state.getSelectedCol());
    }
}
