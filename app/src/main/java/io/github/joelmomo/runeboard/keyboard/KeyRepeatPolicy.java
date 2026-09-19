package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.controller.ControllerAction;

public final class KeyRepeatPolicy {

    public static final long TOUCH_INITIAL_DELAY_MS = 380L;
    public static final long TOUCH_INTERVAL_MS = 55L;
    public static final long DPAD_INITIAL_DELAY_MS = 380L;
    public static final long DPAD_INTERVAL_MS = 85L;

    private KeyRepeatPolicy() {
    }

    public static boolean isControllerActionRepeatable(
            ControllerAction action) {
        if (action == null) {
            return false;
        }
        switch (action) {
            case MOVE_LEFT:
            case MOVE_RIGHT:
            case MOVE_UP:
            case MOVE_DOWN:
            case BACKSPACE:
            case CURSOR_LEFT:
            case CURSOR_RIGHT:
            case WORD_LEFT:
            case WORD_RIGHT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isTouchKeyRepeatable(KeyboardKey key) {
        if (key == null) {
            return false;
        }
        if (key.getType() == KeyboardKey.Type.BACKSPACE) {
            return true;
        }
        if (key.getType() != KeyboardKey.Type.COMMAND
                || key.getCommand() == null) {
            return false;
        }

        switch (key.getCommand()) {
            case CURSOR_LEFT:
            case CURSOR_RIGHT:
            case WORD_LEFT:
            case WORD_RIGHT:
            case SELECT_LEFT:
            case SELECT_RIGHT:
            case SELECT_WORD_LEFT:
            case SELECT_WORD_RIGHT:
            case DELETE_FORWARD:
                return true;
            default:
                return false;
        }
    }
}
