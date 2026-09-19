package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.github.joelmomo.runeboard.controller.ControllerAction;

import org.junit.Test;

public final class KeyRepeatPolicyTest {

    @Test
    public void controllerRepeatIsLimitedToNavigationAndDeletion() {
        assertTrue(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.MOVE_LEFT));
        assertTrue(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.BACKSPACE));
        assertTrue(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.CURSOR_RIGHT));
        assertTrue(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.WORD_LEFT));
        assertFalse(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.SPACE));
        assertFalse(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.ENTER));
        assertFalse(KeyRepeatPolicy.isControllerActionRepeatable(
                ControllerAction.SHIFT));
    }

    @Test
    public void touchBackspaceAndEditingMovementRepeat() {
        assertTrue(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1f)));
        assertTrue(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.command(
                        EditorCommand.CURSOR_LEFT,
                        "LEFT",
                        1f)));
        assertTrue(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.command(
                        EditorCommand.SELECT_WORD_RIGHT,
                        "SEL WORD R",
                        1f)));
        assertTrue(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.command(
                        EditorCommand.DELETE_FORWARD,
                        "DELETE",
                        1f)));
    }

    @Test
    public void touchTypingAndOneShotActionsDoNotRepeat() {
        assertFalse(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.text("a")));
        assertFalse(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.action(KeyboardKey.Type.SPACE, 1f)));
        assertFalse(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.action(KeyboardKey.Type.ENTER, 1f)));
        assertFalse(KeyRepeatPolicy.isTouchKeyRepeatable(
                KeyboardKey.command(
                        EditorCommand.SELECT_ALL,
                        "SELECT ALL",
                        1f)));
    }

    @Test
    public void touchRepeatStartsSlowerThanItsSteadyCadence() {
        assertTrue(KeyRepeatPolicy.TOUCH_INITIAL_DELAY_MS
                > KeyRepeatPolicy.TOUCH_INTERVAL_MS);
        assertTrue(KeyRepeatPolicy.TOUCH_INTERVAL_MS > 0L);
    }

    @Test
    public void dpadRepeatWaitsBeforeContinuousNavigation() {
        assertTrue(KeyRepeatPolicy.DPAD_INITIAL_DELAY_MS
                > KeyRepeatPolicy.DPAD_INTERVAL_MS);
        assertTrue(KeyRepeatPolicy.DPAD_INTERVAL_MS > 0L);
    }
}
