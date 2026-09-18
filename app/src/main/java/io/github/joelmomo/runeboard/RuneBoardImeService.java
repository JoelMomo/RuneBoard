package io.github.joelmomo.runeboard;

import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.View;

public final class RuneBoardImeService extends InputMethodService
        implements RuneKeyboardView.Listener {

    private RuneKeyboardView keyboardView;

    @Override
    public View onCreateInputView() {
        keyboardView = new RuneKeyboardView(this);
        keyboardView.setListener(this);
        keyboardView.setFocusable(true);
        keyboardView.setFocusableInTouchMode(true);
        keyboardView.requestFocus();
        return keyboardView;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyboardView != null && event.getRepeatCount() == 0
                && keyboardView.handleKeyCode(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (keyboardView != null && keyboardView.handleMotionEvent(event)) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onText(String text) {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            connection.commitText(text, 1);
        }
    }

    @Override
    public void onBackspace() {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            connection.deleteSurroundingText(1, 0);
        }
    }

    @Override
    public void onSpace() {
        onText(" ");
    }

    @Override
    public void onEnter() {
        EditorInfo info = getCurrentInputEditorInfo();
        int action = info == null
                ? EditorInfo.IME_ACTION_NONE
                : info.imeOptions & EditorInfo.IME_MASK_ACTION;

        if (action != EditorInfo.IME_ACTION_NONE
                && action != EditorInfo.IME_ACTION_UNSPECIFIED
                && sendDefaultEditorAction(false)) {
            return;
        }

        onText("\n");
    }

    @Override
    public void onMoveCursor(int direction) {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        int keyCode = direction < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
        long now = SystemClock.uptimeMillis();
        connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0));
        connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0));
    }

    @Override
    public void onMinimizedChanged(boolean minimized) {
        // The custom view requests a new measured height. Hardware testing on the Thor
        // will tell us whether AYN's pinned-IME window honors that resize directly.
    }
}
