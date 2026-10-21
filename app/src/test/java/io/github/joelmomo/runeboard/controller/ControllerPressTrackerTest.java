package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ControllerPressTrackerTest {

  private static final int BUTTON_A = 96;
  private static final int BUTTON_B = 97;
  private static final int DPAD_LEFT = 21;

  private final ControllerPressTracker tracker = new ControllerPressTracker();

  @Test
  public void capturedPressOwnsItsRelease() {
    assertTrue(tracker.claimDown(BUTTON_A, 0, true));
    assertTrue(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void releaseIsClaimedOnlyOnce() {
    tracker.claimDown(BUTTON_A, 0, true);
    tracker.claimUp(BUTTON_A);

    assertFalse(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void pressReceivedByAppKeepsItsReleaseInTheApp() {
    assertFalse(tracker.claimDown(BUTTON_A, 0, false));
    assertFalse(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void releaseWithoutPressIsNeverClaimed() {
    assertFalse(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void capturedPressKeepsItsReleaseAfterKeyboardHides() {
    // Start -> Enter closes the editor before the button is released.
    tracker.claimDown(BUTTON_A, 0, true);

    assertTrue(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void repeatsFollowCapturedFirstPressEvenAfterCaptureEnds() {
    tracker.claimDown(DPAD_LEFT, 0, true);

    assertTrue(tracker.claimDown(DPAD_LEFT, 1, false));
    assertTrue(tracker.claimDown(DPAD_LEFT, 2, true));
  }

  @Test
  public void repeatsFollowAppFirstPressEvenIfCaptureStarts() {
    // A game button opens a text field while the button is still held.
    tracker.claimDown(BUTTON_A, 0, false);

    assertFalse(tracker.claimDown(BUTTON_A, 1, true));
    assertFalse(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void repeatWithoutKnownPressIsNotClaimed() {
    assertFalse(tracker.claimDown(BUTTON_A, 3, true));
  }

  @Test
  public void newAppPressForgetsCapturedPressWhoseReleaseWasLost() {
    tracker.claimDown(BUTTON_A, 0, true);

    assertFalse(tracker.claimDown(BUTTON_A, 0, false));
    assertFalse(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void keysAreTrackedIndependently() {
    tracker.claimDown(BUTTON_A, 0, true);
    tracker.claimDown(BUTTON_B, 0, false);

    assertFalse(tracker.claimUp(BUTTON_B));
    assertTrue(tracker.claimUp(BUTTON_A));
  }

  @Test
  public void clearDropsEveryCapturedPress() {
    tracker.claimDown(BUTTON_A, 0, true);
    tracker.claimDown(DPAD_LEFT, 0, true);

    tracker.clear();

    assertFalse(tracker.claimUp(BUTTON_A));
    assertFalse(tracker.claimDown(DPAD_LEFT, 1, true));
  }
}
