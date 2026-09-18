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
    public void shiftCyclesOffOnceCapsAndBackOff() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());

        assertEquals(
                KeyboardState.ShiftMode.OFF,
                state.getShiftMode());

        state.advanceShiftMode();
        assertEquals(
                KeyboardState.ShiftMode.ONCE,
                state.getShiftMode());
        assertTrue(state.isShifted());
        assertFalse(state.isCapsLocked());

        state.advanceShiftMode();
        assertEquals(
                KeyboardState.ShiftMode.CAPS_LOCK,
                state.getShiftMode());
        assertTrue(state.isCapsLocked());
        assertFalse(state.consumeOneShotShift());

        state.advanceShiftMode();
        assertEquals(
                KeyboardState.ShiftMode.OFF,
                state.getShiftMode());
    }

    @Test
    public void oneShotShiftIsConsumedButCapsIsNot() {
        KeyboardState state =
                new KeyboardState(KeyboardLayouts.qwerty());

        state.advanceShiftMode();
        assertTrue(state.consumeOneShotShift());
        assertEquals(
                KeyboardState.ShiftMode.OFF,
                state.getShiftMode());

        state.advanceShiftMode();
        state.advanceShiftMode();
        assertFalse(state.consumeOneShotShift());
        assertEquals(
                KeyboardState.ShiftMode.CAPS_LOCK,
                state.getShiftMode());
    }

    @Test
    public void symbolModeSwitchesLayoutsAndClearsShift() {
        KeyboardState state = new KeyboardState(
                KeyboardLayouts.englishQwerty(),
                KeyboardLayouts.englishSymbols(),
                255);

        state.advanceShiftMode();
        state.select(4, 8);
        assertTrue(state.toggleSymbols());

        assertTrue(state.isSymbols());
        assertFalse(state.isShifted());
        assertEquals("!", state.getLayout().getKey(1, 0).getText());
        assertEquals(4, state.getSelectedRow());
        assertEquals(0, state.getSelectedCol());
        assertEquals(KeyboardKey.Type.MODE,
                state.getSelectedKey().getType());

        assertTrue(state.toggleSymbols());
        assertFalse(state.isSymbols());
        assertEquals(1, state.getSelectedCol());
        assertEquals(KeyboardKey.Type.MODE,
                state.getSelectedKey().getType());
        assertEquals("q", state.getLayout().getKey(1, 0).getText());
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
