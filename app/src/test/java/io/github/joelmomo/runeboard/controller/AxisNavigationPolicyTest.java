package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AxisNavigationPolicyTest {

    @Test
    public void moderateDeflectionDoesNotMove() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertNull(policy.onSample(0.55f, 0f, 0L));
    }

    @Test
    public void heldDeflectionMovesOnlyOnce() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 0L));
        assertNull(policy.onSample(0.92f, 0f, 40L));
        assertNull(policy.onSample(0.88f, 0f, 80L));
        assertNull(policy.onSample(0.95f, 0f, 160L));
    }

    @Test
    public void shortCenterBounceDoesNotRearm() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 0L));
        assertNull(policy.onSample(0f, 0f, 50L));
        assertNull(policy.onSample(0.9f, 0f, 120L));
    }

    @Test
    public void stableCenterRearmsNextFlick() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 0L));
        assertNull(policy.onSample(0f, 0f, 50L));
        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 160L));
    }

    @Test
    public void fastRepeatedFlicksWorkAfterStableCenter() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 0L));
        assertNull(policy.onSample(0f, 0f, 40L));
        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 150L));
        assertNull(policy.onSample(0f, 0f, 190L));
        assertEquals(
                ControllerAction.MOVE_RIGHT,
                policy.onSample(0.9f, 0f, 300L));
    }

    @Test
    public void dominantAxisChoosesDirection() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_DOWN,
                policy.onSample(0.72f, 0.91f, 0L));
    }

    @Test
    public void analogDeflectionSuppressesImmediateSyntheticDpad() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertNull(policy.onSample(0.55f, 0f, 100L));
        assertTrue(policy.shouldSuppressSyntheticDpad(101L));
        assertFalse(policy.shouldSuppressSyntheticDpad(141L));
    }

    @Test
    public void centeredSampleDoesNotSuppressDpad() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertNull(policy.onSample(0f, 0f, 100L));
        assertFalse(policy.shouldSuppressSyntheticDpad(101L));
    }

    @Test
    public void resetArmsImmediately() {
        AxisNavigationPolicy policy = new AxisNavigationPolicy();

        assertEquals(
                ControllerAction.MOVE_LEFT,
                policy.onSample(-0.9f, 0f, 0L));
        policy.reset();
        assertEquals(
                ControllerAction.MOVE_LEFT,
                policy.onSample(-0.9f, 0f, 1L));
    }
}
