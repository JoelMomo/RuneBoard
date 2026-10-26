package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.device.UiProfile;

/**
 * How tall the keyboard view asks to be.
 *
 * <p>THOR fills whatever the lower display offers, exactly as before profiles existed. MOBILE asks
 * for a share of the screen in dp, clamped so the focused editor stays visible above it, plus the
 * system navigation area it has to keep clear at the bottom.
 */
public final class KeyboardHeightPolicy {

  public static final float MINIMIZED_HEIGHT_DP = 58f;

  static final float PORTRAIT_SCREEN_SHARE = 0.36f;
  static final float PORTRAIT_MIN_DP = 260f;
  static final float PORTRAIT_MAX_DP = 320f;
  static final float LANDSCAPE_SCREEN_SHARE = 0.55f;
  static final float LANDSCAPE_MIN_DP = 170f;
  static final float LANDSCAPE_MAX_DP = 230f;

  private KeyboardHeightPolicy() {}

  /**
   * @param availableHeightPx the height the IME window offers; 0 or less when unbounded
   * @param screenHeightDp the current screen height in dp, as the configuration reports it
   * @param bottomInsetPx system bars the MOBILE keyboard must leave clear below its keys
   */
  public static int heightPx(
      UiProfile profile,
      boolean minimized,
      int availableHeightPx,
      int screenHeightDp,
      boolean landscape,
      int bottomInsetPx,
      float density) {
    if (minimized) {
      return px(MINIMIZED_HEIGHT_DP, density);
    }
    if (profile != UiProfile.MOBILE) {
      return availableHeightPx;
    }

    int desired =
        px(mobileKeyboardHeightDp(screenHeightDp, landscape), density)
            + Math.max(0, bottomInsetPx);
    return availableHeightPx > 0 ? Math.min(desired, availableHeightPx) : desired;
  }

  static float mobileKeyboardHeightDp(int screenHeightDp, boolean landscape) {
    float share = landscape ? LANDSCAPE_SCREEN_SHARE : PORTRAIT_SCREEN_SHARE;
    float min = landscape ? LANDSCAPE_MIN_DP : PORTRAIT_MIN_DP;
    float max = landscape ? LANDSCAPE_MAX_DP : PORTRAIT_MAX_DP;
    if (screenHeightDp <= 0) {
      return min;
    }
    return Math.max(min, Math.min(max, screenHeightDp * share));
  }

  private static int px(float dp, float density) {
    return Math.round(dp * density);
  }
}
