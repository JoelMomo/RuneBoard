package io.github.joelmomo.runeboard.controller;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps every controller press with whoever received its first key-down.
 *
 * <p>Capture can start or stop while a button is held (the keyboard hides after Enter, or a game
 * opens a text field). Repeats and the release must follow the original press, otherwise the game
 * sees a button that never lets go, or RuneBoard keeps a long-press pending on a hidden keyboard.
 */
public final class ControllerPressTracker {

  private final Set<Integer> capturedKeyCodes = new HashSet<>();

  /**
   * Returns whether RuneBoard owns this key-down. A new press is owned when capture is active;
   * repeats stay with the owner of the first press regardless of the current capture state.
   */
  public boolean claimDown(int keyCode, int repeatCount, boolean captureActive) {
    if (repeatCount > 0) {
      return capturedKeyCodes.contains(keyCode);
    }
    if (captureActive) {
      capturedKeyCodes.add(keyCode);
      return true;
    }
    // A fresh press the app receives also forgets a press whose release never reached us.
    capturedKeyCodes.remove(keyCode);
    return false;
  }

  /** Returns whether RuneBoard owns this key-up, which is true only if it owned the press. */
  public boolean claimUp(int keyCode) {
    return capturedKeyCodes.remove(keyCode);
  }

  public void clear() {
    capturedKeyCodes.clear();
  }
}
