package io.github.joelmomo.runeboard.controller;

import android.view.KeyEvent;

public final class ControllerMapper {

    private ControllerMapper() {
    }

    public static ControllerAction fromKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return ControllerAction.MOVE_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return ControllerAction.MOVE_RIGHT;
            case KeyEvent.KEYCODE_DPAD_UP:
                return ControllerAction.MOVE_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return ControllerAction.MOVE_DOWN;
            case KeyEvent.KEYCODE_BUTTON_A:
                return ControllerAction.PRESS_SELECTED;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return ControllerAction.PRESS_CENTER;
            case KeyEvent.KEYCODE_BUTTON_B:
                return ControllerAction.BACKSPACE;
            case KeyEvent.KEYCODE_BUTTON_X:
                return ControllerAction.SPACE;
            case KeyEvent.KEYCODE_BUTTON_Y:
                return ControllerAction.SHIFT;
            case KeyEvent.KEYCODE_BUTTON_L1:
                return ControllerAction.CURSOR_LEFT;
            case KeyEvent.KEYCODE_BUTTON_R1:
                return ControllerAction.CURSOR_RIGHT;
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_ENTER:
                return ControllerAction.ENTER;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return ControllerAction.TOGGLE_MINIMIZE;
            default:
                return ControllerAction.NONE;
        }
    }
}
