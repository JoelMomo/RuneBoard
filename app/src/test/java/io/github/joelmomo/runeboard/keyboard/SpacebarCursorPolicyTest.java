package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class SpacebarCursorPolicyTest {

  @Test
  public void movementBelowOneStepDoesNotMoveCursor() {
    assertEquals(0, SpacebarCursorPolicy.cursorSteps(17f, 18f));
    assertEquals(0, SpacebarCursorPolicy.cursorSteps(-17f, 18f));
  }

  @Test
  public void horizontalMovementProducesSignedCursorSteps() {
    assertEquals(1, SpacebarCursorPolicy.cursorSteps(18f, 18f));
    assertEquals(2, SpacebarCursorPolicy.cursorSteps(37f, 18f));
    assertEquals(-1, SpacebarCursorPolicy.cursorSteps(-18f, 18f));
    assertEquals(-2, SpacebarCursorPolicy.cursorSteps(-37f, 18f));
  }

  @Test
  public void invalidStepDistanceDoesNotMoveCursor() {
    assertEquals(0, SpacebarCursorPolicy.cursorSteps(100f, 0f));
  }
}