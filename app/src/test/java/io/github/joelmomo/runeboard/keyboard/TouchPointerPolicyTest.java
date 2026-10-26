package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TouchPointerPolicyTest {

  @Test
  public void singleFingerTapOwnsItsKeyUntilItLifts() {
    TouchPointerPolicy policy = new TouchPointerPolicy();
    assertEquals(TouchPointerPolicy.NONE, policy.begin(0));
    assertTrue(policy.isActive(0));
    policy.end();
    assertFalse(policy.isActive(0));
  }

  @Test
  public void overlappingFingerFinishesThePreviousKeyAndTakesOver() {
    TouchPointerPolicy policy = new TouchPointerPolicy();
    policy.begin(0);

    // Second finger lands while the first is still down: the first key must be finished.
    assertEquals(0, policy.begin(1));
    assertEquals(1, policy.activePointerId());

    // The first finger lifting later must not release the second finger's key.
    assertFalse(policy.isActive(0));
    assertTrue(policy.isActive(1));
  }

  @Test
  public void threeFingerRollHandsOverEachTime() {
    TouchPointerPolicy policy = new TouchPointerPolicy();
    policy.begin(0);
    assertEquals(0, policy.begin(1));
    assertEquals(1, policy.begin(2));
    assertTrue(policy.isActive(2));
    assertFalse(policy.isActive(1));
  }

  @Test
  public void nothingIsActiveAfterEndOrForUnknownPointers() {
    TouchPointerPolicy policy = new TouchPointerPolicy();
    assertFalse(policy.isActive(TouchPointerPolicy.NONE));
    policy.begin(3);
    policy.end();
    assertEquals(TouchPointerPolicy.NONE, policy.activePointerId());
    assertEquals(TouchPointerPolicy.NONE, policy.begin(4));
  }

  @Test
  public void distanceIsZeroInsideAndEuclideanOutside() {
    assertEquals(0f, TouchPointerPolicy.distanceToRect(5, 5, 0, 0, 10, 10), 0f);
    assertEquals(3f, TouchPointerPolicy.distanceToRect(13, 5, 0, 0, 10, 10), 0.001f);
    assertEquals(4f, TouchPointerPolicy.distanceToRect(5, -4, 0, 0, 10, 10), 0.001f);
    assertEquals(5f, TouchPointerPolicy.distanceToRect(13, 14, 0, 0, 10, 10), 0.001f);
  }
}
