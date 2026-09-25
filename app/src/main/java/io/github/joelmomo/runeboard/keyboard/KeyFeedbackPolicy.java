package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.controller.ControllerAction;

public final class KeyFeedbackPolicy {

  private KeyFeedbackPolicy() {}

  public static boolean shouldProvideFeedback(ControllerAction action) {
    if (action == null) {
      return false;
    }
    switch (action) {
      case NONE:
      case MOVE_LEFT:
      case MOVE_RIGHT:
      case MOVE_UP:
      case MOVE_DOWN:
        return false;
      default:
        return true;
    }
  }
}
