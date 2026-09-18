package io.github.joelmomo.runeboard.controller;

import android.view.KeyEvent;

import java.util.EnumMap;
import java.util.Map;

public final class ControllerBindings {

    private final EnumMap<BindableAction, Integer> keyCodes;

    public ControllerBindings() {
        keyCodes = new EnumMap<>(BindableAction.class);
        for (BindableAction action : BindableAction.values()) {
            keyCodes.put(action, action.defaultKeyCode);
        }
    }

    public ControllerBindings(Map<BindableAction, Integer> source) {
        this();
        if (source == null) {
            return;
        }
        for (BindableAction action : BindableAction.values()) {
            Integer keyCode = source.get(action);
            if (keyCode != null && isBindableKeyCode(keyCode)) {
                assign(action, keyCode);
            }
        }
    }

    public int getKeyCode(BindableAction action) {
        Integer value = keyCodes.get(action);
        return value == null ? action.defaultKeyCode : value;
    }

    public BindableAction getBindableAction(int keyCode) {
        for (BindableAction action : BindableAction.values()) {
            if (getKeyCode(action) == keyCode) {
                return action;
            }
        }
        return null;
    }

    public ControllerAction getControllerAction(int keyCode) {
        BindableAction bindable = getBindableAction(keyCode);
        return bindable == null ? ControllerAction.NONE : bindable.action;
    }

    public void assign(BindableAction target, int newKeyCode) {
        if (!isBindableKeyCode(newKeyCode)) {
            throw new IllegalArgumentException(
                    "Unsupported controller key: " + newKeyCode);
        }

        int oldKeyCode = getKeyCode(target);
        BindableAction displaced = getBindableAction(newKeyCode);

        keyCodes.put(target, newKeyCode);

        if (displaced != null && displaced != target) {
            keyCodes.put(displaced, oldKeyCode);
        }
    }

    public void reset() {
        keyCodes.clear();
        for (BindableAction action : BindableAction.values()) {
            keyCodes.put(action, action.defaultKeyCode);
        }
    }

    public EnumMap<BindableAction, Integer> copyValues() {
        return new EnumMap<>(keyCodes);
    }

    public static boolean isBindableKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_BUTTON_L2:
            case KeyEvent.KEYCODE_BUTTON_R2:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                return true;
            default:
                return false;
        }
    }
}
