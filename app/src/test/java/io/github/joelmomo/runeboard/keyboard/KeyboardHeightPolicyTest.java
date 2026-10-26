package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.joelmomo.runeboard.device.UiProfile;
import org.junit.Test;

public final class KeyboardHeightPolicyTest {

  private static final float THOR_DENSITY = 2f;
  private static final float PHONE_DENSITY = 2.625f;

  @Test
  public void thorFillsTheOfferedHeightExactlyAsBefore() {
    for (int available : new int[] {0, 400, 1080, 2400}) {
      assertEquals(
          available,
          KeyboardHeightPolicy.heightPx(
              UiProfile.THOR, false, available, 540, true, 0, THOR_DENSITY));
      // Screen size, orientation and insets must never change the Thor panel.
      assertEquals(
          available,
          KeyboardHeightPolicy.heightPx(
              UiProfile.THOR, false, available, 900, false, 120, THOR_DENSITY));
    }
  }

  @Test
  public void minimizedStripIsTheSameOnBothProfiles() {
    int expected = Math.round(58f * THOR_DENSITY);
    assertEquals(
        expected,
        KeyboardHeightPolicy.heightPx(UiProfile.THOR, true, 1080, 540, true, 0, THOR_DENSITY));
    assertEquals(
        expected,
        KeyboardHeightPolicy.heightPx(UiProfile.MOBILE, true, 1080, 540, true, 0, THOR_DENSITY));
  }

  @Test
  public void mobilePortraitTakesAShareOfTheScreenWithinBounds() {
    // Nothing Phone class screen: ~915 dp tall, so the share is capped at the maximum.
    assertEquals(320f, KeyboardHeightPolicy.mobileKeyboardHeightDp(915, false), 0.01f);
    // Small phone: the share drops below the minimum, which keeps keys usable.
    assertEquals(260f, KeyboardHeightPolicy.mobileKeyboardHeightDp(640, false), 0.01f);
    assertEquals(288f, KeyboardHeightPolicy.mobileKeyboardHeightDp(800, false), 0.01f);
  }

  @Test
  public void mobileLandscapeIsShorterThanPortrait() {
    float landscape = KeyboardHeightPolicy.mobileKeyboardHeightDp(392, true);
    assertEquals(392 * 0.55f, landscape, 0.01f);
    assertTrue(landscape < KeyboardHeightPolicy.mobileKeyboardHeightDp(915, false));
    assertEquals(170f, KeyboardHeightPolicy.mobileKeyboardHeightDp(280, true), 0.01f);
    assertEquals(230f, KeyboardHeightPolicy.mobileKeyboardHeightDp(600, true), 0.01f);
  }

  @Test
  public void mobileAddsBottomInsetAndNeverExceedsTheOfferedHeight() {
    int keys = Math.round(320f * PHONE_DENSITY);
    assertEquals(
        keys + 63,
        KeyboardHeightPolicy.heightPx(
            UiProfile.MOBILE, false, 2400, 915, false, 63, PHONE_DENSITY));
    assertEquals(
        500,
        KeyboardHeightPolicy.heightPx(
            UiProfile.MOBILE, false, 500, 915, false, 63, PHONE_DENSITY));
    // Unbounded measure: use the policy height as is.
    assertEquals(
        keys,
        KeyboardHeightPolicy.heightPx(UiProfile.MOBILE, false, 0, 915, false, 0, PHONE_DENSITY));
  }

  @Test
  public void mobileKeepsMostOfAPortraitPhoneScreenForTheEditor() {
    int screenDp = 915;
    float keyboardDp = KeyboardHeightPolicy.mobileKeyboardHeightDp(screenDp, false);
    assertTrue(keyboardDp / screenDp < 0.4f);
  }

  @Test
  public void unknownScreenHeightFallsBackToMinimum() {
    assertEquals(260f, KeyboardHeightPolicy.mobileKeyboardHeightDp(0, false), 0.01f);
    assertEquals(170f, KeyboardHeightPolicy.mobileKeyboardHeightDp(-1, true), 0.01f);
  }
}
