package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public final class ControllerBindingsTest {

    @Test
    public void defaultsUseUniqueKeys() {
        ControllerBindings bindings = new ControllerBindings();
        Set<Integer> keys = new HashSet<>();

        for (BindableAction action : BindableAction.values()) {
            assertTrue(keys.add(bindings.getKeyCode(action)));
        }
    }

    @Test
    public void assigningOccupiedKeySwapsActions() {
        ControllerBindings bindings = new ControllerBindings();

        bindings.assign(
                BindableAction.SPACE,
                KeyEvent.KEYCODE_BUTTON_A);

        assertEquals(
                KeyEvent.KEYCODE_BUTTON_A,
                bindings.getKeyCode(BindableAction.SPACE));
        assertEquals(
                KeyEvent.KEYCODE_BUTTON_X,
                bindings.getKeyCode(BindableAction.CONFIRM));
        assertNotEquals(
                bindings.getKeyCode(BindableAction.SPACE),
                bindings.getKeyCode(BindableAction.CONFIRM));
    }

    @Test
    public void resetRestoresPrototypeMapping() {
        ControllerBindings bindings = new ControllerBindings();
        bindings.assign(
                BindableAction.MINIMIZE,
                KeyEvent.KEYCODE_BUTTON_THUMBR);

        bindings.reset();

        assertEquals(
                KeyEvent.KEYCODE_BUTTON_SELECT,
                bindings.getKeyCode(BindableAction.MINIMIZE));
    }
}
