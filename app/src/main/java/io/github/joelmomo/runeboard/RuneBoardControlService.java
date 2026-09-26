package io.github.joelmomo.runeboard;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.github.joelmomo.runeboard.keyboard.KeyRepeatPolicy;

public final class RuneBoardControlService extends AccessibilityService {

    private final Handler repeatHandler = new Handler(Looper.getMainLooper());
    private int repeatingDpadKeyCode = KeyEvent.KEYCODE_UNKNOWN;

    private final Runnable dpadRepeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (repeatingDpadKeyCode == KeyEvent.KEYCODE_UNKNOWN) {
                return;
            }

            RuneBoardImeService ime = RuneBoardImeService.getActiveInstance();
            if (ime == null
                    || !ime.isControllerCaptureAvailable()
                    || !ime.shouldCaptureControllerKey(repeatingDpadKeyCode)) {
                stopDpadRepeat();
                return;
            }

            ime.handleControllerKey(repeatingDpadKeyCode);
            repeatHandler.postDelayed(
                    this,
                    KeyRepeatPolicy.DPAD_INTERVAL_MS);
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // RuneBoard does not inspect accessibility content.
    }

    @Override
    public void onInterrupt() {
        stopDpadRepeat();
    }

    @Override
    public void onDestroy() {
        stopDpadRepeat();
        super.onDestroy();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        RuneBoardImeService ime = RuneBoardImeService.getActiveInstance();
        if (ime == null
                || !ime.isControllerCaptureAvailable()
                || !ime.shouldCaptureControllerKey(keyCode)) {
            if (isDpadNavigationKey(keyCode)) {
                stopDpadRepeat();
            }
            return false;
        }

        if (isDpadNavigationKey(keyCode)) {
            return handleDpadKeyEvent(ime, event);
        }

        if (event.getAction() == KeyEvent.ACTION_UP) {
            return ime.handleControllerKeyUp(keyCode);
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        return ime.handleControllerKeyDown(keyCode, event.getRepeatCount());
    }

    private boolean handleDpadKeyEvent(
            RuneBoardImeService ime,
            KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (repeatingDpadKeyCode == keyCode) {
                stopDpadRepeat();
            }
            return true;
        }

        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        // Android may emit its own repeat ACTION_DOWN events. RuneBoard owns
        // D-pad repeat timing so these are consumed to avoid double movement.
        if (event.getRepeatCount() > 0) {
            return true;
        }

        ime.handleControllerKey(keyCode);
        startDpadRepeat(keyCode);
        return true;
    }

    private void startDpadRepeat(int keyCode) {
        stopDpadRepeat();
        repeatingDpadKeyCode = keyCode;
        repeatHandler.postDelayed(
                dpadRepeatRunnable,
                KeyRepeatPolicy.DPAD_INITIAL_DELAY_MS);
    }

    private void stopDpadRepeat() {
        repeatingDpadKeyCode = KeyEvent.KEYCODE_UNKNOWN;
        repeatHandler.removeCallbacks(dpadRepeatRunnable);
    }

    private static boolean isDpadNavigationKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;
            default:
                return false;
        }
    }
}
