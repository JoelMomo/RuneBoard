package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import org.junit.Test;

public final class ControllerMapperTest {

    private final ControllerMapper mapper =
            new ControllerMapper(new ControllerBindings());

    @Test
    public void aAndDpadCenterAreDistinctActions() {
        assertEquals(
                ControllerAction.PRESS_SELECTED,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_A));
        assertEquals(
                ControllerAction.PRESS_CENTER,
                mapper.fromKeyCode(KeyEvent.KEYCODE_DPAD_CENTER));
    }

    @Test
    public void defaultThorButtonsMapToExpectedActions() {
        assertEquals(
                ControllerAction.BACKSPACE,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_B));
        assertEquals(
                ControllerAction.SPACE,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_X));
        assertEquals(
                ControllerAction.SHIFT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_Y));
        assertEquals(
                ControllerAction.CURSOR_LEFT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_L1));
        assertEquals(
                ControllerAction.CURSOR_RIGHT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_R1));
        assertEquals(
                ControllerAction.WORD_LEFT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_L2));
        assertEquals(
                ControllerAction.WORD_RIGHT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_R2));
        assertEquals(
                ControllerAction.ENTER,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_START));
        assertEquals(
                ControllerAction.LANGUAGE_NEXT,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_THUMBL));
        assertEquals(
                ControllerAction.ACCEPT_SUGGESTION,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_THUMBR));
        assertEquals(
                ControllerAction.TOGGLE_MINIMIZE,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_SELECT));
    }

    @Test
    public void remappedButtonChangesResolvedAction() {
        mapper.getBindings().assign(
                BindableAction.SPACE,
                KeyEvent.KEYCODE_BUTTON_A);

        assertEquals(
                ControllerAction.SPACE,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_A));
        assertEquals(
                ControllerAction.PRESS_SELECTED,
                mapper.fromKeyCode(KeyEvent.KEYCODE_BUTTON_X));
    }

    @Test
    public void repeatabilityFollowsMappedAction() {
        assertTrue(mapper.isRepeatable(KeyEvent.KEYCODE_DPAD_LEFT));
        assertTrue(mapper.isRepeatable(KeyEvent.KEYCODE_BUTTON_B));
        assertFalse(mapper.isRepeatable(KeyEvent.KEYCODE_BUTTON_X));

        mapper.getBindings().assign(
                BindableAction.BACKSPACE,
                KeyEvent.KEYCODE_BUTTON_X);

        assertTrue(mapper.isRepeatable(KeyEvent.KEYCODE_BUTTON_X));
        assertFalse(mapper.isRepeatable(KeyEvent.KEYCODE_BUTTON_B));
    }
}
