package io.github.joelmomo.runeboard.keyboard;

public final class SpacebarCursorPolicy {

  public static final float ACTIVATION_DISTANCE_DP = 12f;
  public static final float STEP_DISTANCE_DP = 18f;

  private SpacebarCursorPolicy() {}

  public static int cursorSteps(float deltaPx, float stepPx) {
    if (stepPx <= 0f) {
      return 0;
    }

    int magnitude = (int) (Math.abs(deltaPx) / stepPx);
    if (magnitude == 0) {
      return 0;
    }
    return deltaPx < 0f ? -magnitude : magnitude;
  }
}