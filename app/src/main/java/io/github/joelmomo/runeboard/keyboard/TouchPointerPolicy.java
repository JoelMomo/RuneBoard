package io.github.joelmomo.runeboard.keyboard;

/**
 * Which finger owns the key being pressed on the touchscreen.
 *
 * <p>Fast typing overlaps touches: the next finger lands before the previous one lifts. Only one
 * key is in progress at a time, so a new finger first finishes the previous finger's key as a
 * normal tap, then takes over. A finger that already handed its key over is ignored when it lifts.
 */
public final class TouchPointerPolicy {

  public static final int NONE = -1;

  /** How far outside every key a touch may land and still go to the nearest one, in dp. */
  public static final float GAP_SNAP_DP = 12f;

  private int activePointerId = NONE;

  /**
   * Starts tracking {@code pointerId}.
   *
   * @return the pointer whose key must be finished first, or {@link #NONE}
   */
  public int begin(int pointerId) {
    int previous = activePointerId;
    activePointerId = pointerId;
    return previous == pointerId ? NONE : previous;
  }

  public int activePointerId() {
    return activePointerId;
  }

  /** Whether lifting {@code pointerId} should release the key in progress. */
  public boolean isActive(int pointerId) {
    return pointerId != NONE && pointerId == activePointerId;
  }

  public void end() {
    activePointerId = NONE;
  }

  /** Distance from a point to a rectangle; 0 inside it. */
  public static float distanceToRect(
      float x, float y, float left, float top, float right, float bottom) {
    float dx = x < left ? left - x : x > right ? x - right : 0f;
    float dy = y < top ? top - y : y > bottom ? y - bottom : 0f;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }
}
