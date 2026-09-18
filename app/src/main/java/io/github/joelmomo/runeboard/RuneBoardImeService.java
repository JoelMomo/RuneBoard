package io.github.joelmomo.runeboard;

import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.keyboard.WordNavigator;
import io.github.joelmomo.runeboard.settings.RunePreferences;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;

public final class RuneBoardImeService extends InputMethodService
        implements RuneKeyboardView.Listener {

    private static volatile RuneBoardImeService activeInstance;

    private RuneKeyboardView keyboardView;
    private RunePreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new RunePreferences(this);
        activeInstance = this;
    }

    @Override
    public void onDestroy() {
        if (activeInstance == this) {
            activeInstance = null;
        }
        super.onDestroy();
    }

    public static RuneBoardImeService getActiveInstance() {
        return activeInstance;
    }

    public static void requestAppearanceRefresh() {
        RuneBoardImeService instance = activeInstance;
        if (instance != null) {
            instance.getMainExecutor().execute(instance::refreshAppearance);
        }
    }

    public boolean isControllerCaptureAvailable() {
        return keyboardView != null && isInputViewShown();
    }

    public boolean shouldCaptureControllerKey(int keyCode) {
        return keyboardView != null
                && keyboardView.shouldCaptureKeyCode(keyCode);
    }

    public boolean handleControllerKey(int keyCode) {
        return keyboardView != null
                && keyboardView.handleKeyCode(keyCode);
    }

    public boolean isRepeatableControllerKey(int keyCode) {
        return keyboardView != null
                && keyboardView.isRepeatableKeyCode(keyCode);
    }

    @Override
    public View onCreateInputView() {
        keyboardView = createKeyboardView();
        return keyboardView;
    }

    private RuneKeyboardView createKeyboardView() {
        KeyboardTheme theme = preferences.getTheme();
        int initialOpacity = preferences.getBackgroundOpacity(theme);

        RuneKeyboardView view = new RuneKeyboardView(
                this,
                theme,
                initialOpacity,
                new ControllerMapper(preferences.getControllerBindings()));
        view.setListener(this);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        return view;
    }

    private void refreshAppearance() {
        RuneKeyboardView refreshed = createKeyboardView();
        keyboardView = refreshed;
        setInputView(refreshed);
        if (isInputViewShown()) {
            refreshed.requestFocus();
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyboardView != null
                && event.getRepeatCount() == 0
                && keyboardView.handleKeyCode(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (keyboardView != null
                && keyboardView.handleMotionEvent(event)) {
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

        ExtractedText extracted =
                connection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted != null
                && extracted.text != null
                && extracted.selectionStart >= 0) {
            int next = Math.max(
                    0,
                    Math.min(
                            extracted.text.length(),
                            extracted.selectionStart
                                    + (direction < 0 ? -1 : 1)));
            connection.setSelection(next, next);
            return;
        }

        int keyCode = direction < 0
                ? KeyEvent.KEYCODE_DPAD_LEFT
                : KeyEvent.KEYCODE_DPAD_RIGHT;
        long now = SystemClock.uptimeMillis();
        connection.sendKeyEvent(new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0));
        connection.sendKeyEvent(new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_UP,
                keyCode,
                0));
    }

    @Override
    public void onMoveWord(int direction) {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        ExtractedText extracted =
                connection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted == null
                || extracted.text == null
                || extracted.selectionStart < 0) {
            return;
        }

        String text = extracted.text.toString();
        int localSelection = extracted.selectionStart - extracted.startOffset;
        if (localSelection < 0 || localSelection > text.length()) {
            return;
        }

        int localNext = WordNavigator.move(
                text,
                localSelection,
                direction);
        int next = extracted.startOffset + localNext;
        connection.setSelection(next, next);
    }

    @Override
    public void onMinimizedChanged(boolean minimized) {
        // AYN Thor firmware .377 honors the requested input-view resize.
    }

    @Override
    public void onBackgroundOpacityChanged(int opacity) {
        preferences.setBackgroundOpacity(opacity);
    }
}
