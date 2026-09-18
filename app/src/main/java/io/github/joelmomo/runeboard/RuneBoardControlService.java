package io.github.joelmomo.runeboard;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public final class RuneBoardControlService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // RuneBoard does not inspect accessibility content.
    }

    @Override
    public void onInterrupt() {
        // No persistent accessibility feedback to interrupt.
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        RuneBoardImeService ime = RuneBoardImeService.getActiveInstance();
        if (ime == null
                || !ime.isControllerCaptureAvailable()
                || !ime.shouldCaptureControllerKey(event.getKeyCode())) {
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        if (event.getRepeatCount() > 0
                && !isRepeatableKey(event.getKeyCode())) {
            return true;
        }

        ime.handleControllerKey(event.getKeyCode());
        return true;
    }

    private boolean isRepeatableKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_BUTTON_B;
    }
}
