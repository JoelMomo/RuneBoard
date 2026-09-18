package io.github.joelmomo.runeboard.controller;

import android.view.KeyEvent;

public final class ControllerKeyNames {

    private ControllerKeyNames() {
    }

    public static String nameFor(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                return "A";
            case KeyEvent.KEYCODE_BUTTON_B:
                return "B";
            case KeyEvent.KEYCODE_BUTTON_X:
                return "X";
            case KeyEvent.KEYCODE_BUTTON_Y:
                return "Y";
            case KeyEvent.KEYCODE_BUTTON_L1:
                return "L1";
            case KeyEvent.KEYCODE_BUTTON_R1:
                return "R1";
            case KeyEvent.KEYCODE_BUTTON_L2:
                return "L2";
            case KeyEvent.KEYCODE_BUTTON_R2:
                return "R2";
            case KeyEvent.KEYCODE_BUTTON_START:
                return "START";
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return "SELECT";
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                return "L3";
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                return "R3";
            default:
                return "?";
        }
    }
}
