package io.github.joelmomo.runeboard;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
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
        if (ime == null || !ime.isControllerCaptureAvailable()
                || !isRuneBoardKey(event.getKeyCode())) {
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        Log.d("RuneBoard", "control keyCode=" + event.getKeyCode()
                + " repeat=" + event.getRepeatCount());
        if (event.getRepeatCount() > 0
                && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_LEFT
                && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_RIGHT
                && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_UP
                && event.getKeyCode() != KeyEvent.KEYCODE_DPAD_DOWN
                && event.getKeyCode() != KeyEvent.KEYCODE_BUTTON_B) {
            return true;
        }

        ime.handleControllerKey(event.getKeyCode());
        return true;
    }

    private boolean isRuneBoardKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_ENTER:
                return true;
            default:
                return false;
        }
    }
}
