package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertEquals;

import android.view.KeyEvent;

import org.junit.Test;

public final class ControllerMapperTest {

    @Test
    public void aAndDpadCenterAreDistinctActions() {
        assertEquals(
                ControllerAction.PRESS_SELECTED,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_A));
        assertEquals(
                ControllerAction.PRESS_CENTER,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_DPAD_CENTER));
    }

    @Test
    public void thorPrototypeButtonsMapToExpectedActions() {
        assertEquals(
                ControllerAction.BACKSPACE,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_B));
        assertEquals(
                ControllerAction.SPACE,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_X));
        assertEquals(
                ControllerAction.SHIFT,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_Y));
        assertEquals(
                ControllerAction.CURSOR_LEFT,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_L1));
        assertEquals(
                ControllerAction.CURSOR_RIGHT,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_R1));
        assertEquals(
                ControllerAction.TOGGLE_MINIMIZE,
                ControllerMapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_SELECT));
    }
}
