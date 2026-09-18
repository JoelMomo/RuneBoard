package io.github.joelmomo.runeboard.controller;

import android.view.KeyEvent;

public final class ControllerMapper {

    private final ControllerBindings bindings;

    public ControllerMapper(ControllerBindings bindings) {
        this.bindings = bindings;
    }

    public ControllerAction fromKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return ControllerAction.MOVE_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return ControllerAction.MOVE_RIGHT;
            case KeyEvent.KEYCODE_DPAD_UP:
                return ControllerAction.MOVE_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return ControllerAction.MOVE_DOWN;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return ControllerAction.PRESS_CENTER;
            case KeyEvent.KEYCODE_ENTER:
                return ControllerAction.ENTER;
            default:
                return bindings.getControllerAction(keyCode);
        }
    }

    public boolean isRepeatable(int keyCode) {
        ControllerAction action = fromKeyCode(keyCode);
        return action == ControllerAction.MOVE_LEFT
                || action == ControllerAction.MOVE_RIGHT
                || action == ControllerAction.MOVE_UP
                || action == ControllerAction.MOVE_DOWN
                || action == ControllerAction.BACKSPACE;
    }

    public ControllerBindings getBindings() {
        return bindings;
    }
}
