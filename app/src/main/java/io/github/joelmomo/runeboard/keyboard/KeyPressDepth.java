package io.github.joelmomo.runeboard.keyboard;

/**
 * How far the pressed key has sunk, from 0 (resting) to 1 (fully pressed).
 *
 * <p>The key sinks quickly while held and springs back after release. A quick tap still reaches
 * the bottom and rests there briefly, so taps from the touchscreen or a controller button are as
 * visible as long presses.
 */
public final class KeyPressDepth {

    public static final long PRESS_MS = 45L;
    public static final long MIN_HOLD_MS = 70L;
    public static final long RELEASE_MS = 120L;

    private static final long NOT_RELEASED = -1L;

    private int row = -1;
    private int col = -1;
    private long pressedAt;
    private long releasedAt = NOT_RELEASED;

    public void press(int row, int col, long now) {
        this.row = row;
        this.col = col;
        pressedAt = now;
        releasedAt = NOT_RELEASED;
    }

    public void release(long now) {
        if (row >= 0 && releasedAt == NOT_RELEASED) {
            releasedAt = now;
        }
    }

    public void clear() {
        row = -1;
        col = -1;
        releasedAt = NOT_RELEASED;
    }

    public float depthOf(int row, int col, long now) {
        if (row != this.row || col != this.col || row < 0) {
            return 0f;
        }
        float sunk = progress(now - pressedAt, PRESS_MS);
        if (releasedAt == NOT_RELEASED) {
            return sunk;
        }

        long riseStart = Math.max(releasedAt, pressedAt + PRESS_MS + MIN_HOLD_MS);
        if (now < riseStart) {
            return sunk;
        }
        float depthAtRise = progress(riseStart - pressedAt, PRESS_MS);
        return depthAtRise * (1f - progress(now - riseStart, RELEASE_MS));
    }

    /** Whether a frame drawn at {@code now} would differ from one drawn later. */
    public boolean isAnimating(long now) {
        if (row < 0) {
            return false;
        }
        if (releasedAt == NOT_RELEASED) {
            return now - pressedAt < PRESS_MS;
        }
        return depthOf(row, col, now) > 0f;
    }

    private static float progress(long elapsed, long duration) {
        if (elapsed <= 0L) {
            return 0f;
        }
        return Math.min(1f, elapsed / (float) duration);
    }
}
