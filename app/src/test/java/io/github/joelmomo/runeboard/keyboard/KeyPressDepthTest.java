package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyPressDepthTest {

    private static final float EPSILON = 0.0001f;

    private final KeyPressDepth depth = new KeyPressDepth();

    @Test
    public void restingKeysHaveNoDepth() {
        assertEquals(0f, depth.depthOf(1, 2, 1_000L), EPSILON);
        assertFalse(depth.isAnimating(1_000L));
    }

    @Test
    public void heldKeySinksToFullDepthAndStays() {
        depth.press(1, 2, 1_000L);

        assertEquals(0f, depth.depthOf(1, 2, 1_000L), EPSILON);
        assertEquals(0.5f, depth.depthOf(1, 2, 1_000L + KeyPressDepth.PRESS_MS / 2), 0.02f);
        assertEquals(1f, depth.depthOf(1, 2, 1_000L + KeyPressDepth.PRESS_MS), EPSILON);
        assertEquals(1f, depth.depthOf(1, 2, 5_000L), EPSILON);
    }

    @Test
    public void onlyThePressedKeySinks() {
        depth.press(1, 2, 1_000L);

        assertEquals(0f, depth.depthOf(1, 3, 2_000L), EPSILON);
        assertEquals(0f, depth.depthOf(0, 2, 2_000L), EPSILON);
    }

    @Test
    public void releasedKeySpringsBackOverReleaseTime() {
        depth.press(1, 2, 1_000L);
        depth.release(2_000L);

        assertEquals(1f, depth.depthOf(1, 2, 2_000L), EPSILON);
        assertEquals(0.5f, depth.depthOf(1, 2, 2_000L + KeyPressDepth.RELEASE_MS / 2), EPSILON);
        assertEquals(0f, depth.depthOf(1, 2, 2_000L + KeyPressDepth.RELEASE_MS), EPSILON);
    }

    @Test
    public void quickTapStillReachesTheBottomAndRests() {
        depth.press(1, 2, 1_000L);
        depth.release(1_010L);

        long bottom = 1_000L + KeyPressDepth.PRESS_MS;
        long riseStart = bottom + KeyPressDepth.MIN_HOLD_MS;
        assertEquals(1f, depth.depthOf(1, 2, bottom), EPSILON);
        assertEquals(1f, depth.depthOf(1, 2, riseStart), EPSILON);
        assertEquals(0f, depth.depthOf(1, 2, riseStart + KeyPressDepth.RELEASE_MS), EPSILON);
    }

    @Test
    public void newPressMovesTheDepthToTheNewKey() {
        depth.press(1, 2, 1_000L);
        depth.press(3, 0, 1_020L);

        assertEquals(0f, depth.depthOf(1, 2, 1_100L), EPSILON);
        assertEquals(1f, depth.depthOf(3, 0, 1_100L), EPSILON);
    }

    @Test
    public void repeatedReleaseKeepsTheFirstReleaseTime() {
        depth.press(1, 2, 1_000L);
        depth.release(2_000L);
        depth.release(2_100L);

        assertEquals(0f, depth.depthOf(1, 2, 2_000L + KeyPressDepth.RELEASE_MS), EPSILON);
    }

    @Test
    public void releaseWithoutPressIsIgnored() {
        depth.release(1_000L);

        assertEquals(0f, depth.depthOf(-1, -1, 1_000L), EPSILON);
        assertFalse(depth.isAnimating(1_000L));
    }

    @Test
    public void clearReturnsEveryKeyToRest() {
        depth.press(1, 2, 1_000L);

        depth.clear();

        assertEquals(0f, depth.depthOf(1, 2, 1_100L), EPSILON);
        assertFalse(depth.isAnimating(1_100L));
    }

    @Test
    public void animatesOnlyWhileTheDepthIsChanging() {
        depth.press(1, 2, 1_000L);
        assertTrue(depth.isAnimating(1_010L));
        assertFalse(depth.isAnimating(1_000L + KeyPressDepth.PRESS_MS + 500L));

        depth.release(3_000L);
        assertTrue(depth.isAnimating(3_000L));
        assertTrue(depth.isAnimating(3_000L + KeyPressDepth.RELEASE_MS - 1));
        assertFalse(depth.isAnimating(3_000L + KeyPressDepth.RELEASE_MS));
    }
}
