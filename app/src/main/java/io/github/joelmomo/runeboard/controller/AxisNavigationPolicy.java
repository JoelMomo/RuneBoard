package io.github.joelmomo.runeboard.controller;

public final class AxisNavigationPolicy {

    static final float ACTIVATION_THRESHOLD = 0.68f;
    static final float RELEASE_THRESHOLD = 0.30f;
    static final long CENTER_REARM_MS = 100L;
    static final long SYNTHETIC_DPAD_WINDOW_MS = 40L;

    private boolean latched;
    private long centeredSince = -1L;
    private long lastAnalogDeflectionAt = Long.MIN_VALUE;

    public ControllerAction onSample(float x, float y, long eventTimeMs) {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        if (absX > RELEASE_THRESHOLD || absY > RELEASE_THRESHOLD) {
            lastAnalogDeflectionAt = eventTimeMs;
        }

        boolean centered = absX <= RELEASE_THRESHOLD
                && absY <= RELEASE_THRESHOLD;

        if (centered) {
            if (latched && centeredSince < 0L) {
                centeredSince = eventTimeMs;
            }
            return null;
        }

        if (latched) {
            if (centeredSince >= 0L
                    && eventTimeMs - centeredSince >= CENTER_REARM_MS) {
                latched = false;
            } else {
                centeredSince = -1L;
                return null;
            }
        }

        centeredSince = -1L;
        if (absX < ACTIVATION_THRESHOLD
                && absY < ACTIVATION_THRESHOLD) {
            return null;
        }

        latched = true;
        if (absX >= absY) {
            return x < 0f
                    ? ControllerAction.MOVE_LEFT
                    : ControllerAction.MOVE_RIGHT;
        }
        return y < 0f
                ? ControllerAction.MOVE_UP
                : ControllerAction.MOVE_DOWN;
    }

    public boolean shouldSuppressSyntheticDpad(long keyEventTimeMs) {
        if (lastAnalogDeflectionAt == Long.MIN_VALUE) {
            return false;
        }
        long delta = keyEventTimeMs - lastAnalogDeflectionAt;
        return delta >= 0L && delta <= SYNTHETIC_DPAD_WINDOW_MS;
    }

    public void reset() {
        latched = false;
        centeredSince = -1L;
        lastAnalogDeflectionAt = Long.MIN_VALUE;
    }
}
