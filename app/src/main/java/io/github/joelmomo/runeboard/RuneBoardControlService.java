package io.github.joelmomo.runeboard;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.github.joelmomo.runeboard.controller.ControllerPressTracker;
import io.github.joelmomo.runeboard.keyboard.KeyRepeatPolicy;

public final class RuneBoardControlService extends AccessibilityService {

    private final Handler repeatHandler = new Handler(Looper.getMainLooper());
    private final ControllerPressTracker pressTracker = new ControllerPressTracker();
    private int repeatingDpadKeyCode = KeyEvent.KEYCODE_UNKNOWN;

    private final Runnable dpadRepeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (repeatingDpadKeyCode == KeyEvent.KEYCODE_UNKNOWN) {
                return;
            }

            RuneBoardImeService ime = RuneBoardImeService.getActiveInstance();
            if (ime == null || !ime.shouldCaptureGlobalControllerKey(repeatingDpadKeyCode)) {
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
        RuneBoardImeService ime = RuneBoardImeService.getActiveInstance();
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                return handleKeyDown(ime, event);
            case KeyEvent.ACTION_UP:
                return handleKeyUp(ime, event.getKeyCode());
            default:
                return false;
        }
    }

    private boolean handleKeyDown(RuneBoardImeService ime, KeyEvent event) {
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        boolean captureActive = ime != null && ime.shouldCaptureGlobalControllerKey(keyCode);

        if (!pressTracker.claimDown(keyCode, repeatCount, captureActive)) {
            if (isDpadNavigationKey(keyCode)) {
                stopDpadRepeat();
            }
            return false;
        }

        // A held press keeps its repeats after capture ends, but they no longer type.
        if (!captureActive) {
            return true;
        }

        if (isDpadNavigationKey(keyCode)) {
            // Android may emit its own repeat ACTION_DOWN events. RuneBoard owns
            // D-pad repeat timing so these are consumed to avoid double movement.
            if (repeatCount == 0) {
                ime.handleControllerKey(keyCode);
                startDpadRepeat(keyCode);
            }
            return true;
        }

        return ime.handleControllerKeyDown(keyCode, repeatCount);
    }

    private boolean handleKeyUp(RuneBoardImeService ime, int keyCode) {
        // Releases follow their press, so a game never loses the release of a
        // button it received and RuneBoard always finishes the presses it took.
        if (!pressTracker.claimUp(keyCode)) {
            if (isDpadNavigationKey(keyCode)) {
                stopDpadRepeat();
            }
            return false;
        }

        if (isDpadNavigationKey(keyCode)) {
            if (repeatingDpadKeyCode == keyCode) {
                stopDpadRepeat();
            }
            return true;
        }

        if (ime != null) {
            ime.handleControllerKeyUp(keyCode);
        }
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
