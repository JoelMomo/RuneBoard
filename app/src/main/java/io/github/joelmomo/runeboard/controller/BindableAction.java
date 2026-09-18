package io.github.joelmomo.runeboard.controller;

import android.view.KeyEvent;

public enum BindableAction {
    CONFIRM(
            "confirm",
            ControllerAction.PRESS_SELECTED,
            KeyEvent.KEYCODE_BUTTON_A),
    BACKSPACE(
            "backspace",
            ControllerAction.BACKSPACE,
            KeyEvent.KEYCODE_BUTTON_B),
    SPACE(
            "space",
            ControllerAction.SPACE,
            KeyEvent.KEYCODE_BUTTON_X),
    SHIFT(
            "shift",
            ControllerAction.SHIFT,
            KeyEvent.KEYCODE_BUTTON_Y),
    CURSOR_LEFT(
            "cursor_left",
            ControllerAction.CURSOR_LEFT,
            KeyEvent.KEYCODE_BUTTON_L1),
    CURSOR_RIGHT(
            "cursor_right",
            ControllerAction.CURSOR_RIGHT,
            KeyEvent.KEYCODE_BUTTON_R1),
    WORD_LEFT(
            "word_left",
            ControllerAction.WORD_LEFT,
            KeyEvent.KEYCODE_BUTTON_L2),
    WORD_RIGHT(
            "word_right",
            ControllerAction.WORD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_R2),
    ENTER(
            "enter",
            ControllerAction.ENTER,
            KeyEvent.KEYCODE_BUTTON_START),
    LANGUAGE_NEXT(
            "language_next",
            ControllerAction.LANGUAGE_NEXT,
            KeyEvent.KEYCODE_BUTTON_THUMBL),
    MINIMIZE(
            "minimize",
            ControllerAction.TOGGLE_MINIMIZE,
            KeyEvent.KEYCODE_BUTTON_SELECT);

    public final String preferenceKey;
    public final ControllerAction action;
    public final int defaultKeyCode;

    BindableAction(
            String preferenceKey,
            ControllerAction action,
            int defaultKeyCode) {
        this.preferenceKey = preferenceKey;
        this.action = action;
        this.defaultKeyCode = defaultKeyCode;
    }
}
