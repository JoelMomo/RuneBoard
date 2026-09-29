package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;
import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import org.junit.Test;

public final class KeyFeedbackPolicyTest {

  @Test
  public void navigationActionsDoNotProvideFeedback() {
    assertFalse(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.NONE));
    assertFalse(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.MOVE_LEFT));
    assertFalse(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.MOVE_RIGHT));
    assertFalse(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.MOVE_UP));
    assertFalse(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.MOVE_DOWN));
  }

  @Test
  public void faceButtonsProvideFeedbackWithDefaultBindings() {
    ControllerBindings bindings = new ControllerBindings();

    assertTrue(
        KeyFeedbackPolicy.shouldProvideFeedback(
            bindings.getControllerAction(KeyEvent.KEYCODE_BUTTON_A)));
    assertTrue(
        KeyFeedbackPolicy.shouldProvideFeedback(
            bindings.getControllerAction(KeyEvent.KEYCODE_BUTTON_B)));
    assertTrue(
        KeyFeedbackPolicy.shouldProvideFeedback(
            bindings.getControllerAction(KeyEvent.KEYCODE_BUTTON_X)));
    assertTrue(
        KeyFeedbackPolicy.shouldProvideFeedback(
            bindings.getControllerAction(KeyEvent.KEYCODE_BUTTON_Y)));
  }

  @Test
  public void nonNavigationActionsProvideFeedback() {
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.PRESS_SELECTED));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.BACKSPACE));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.SPACE));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.SHIFT));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.ENTER));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.LANGUAGE_NEXT));
    assertTrue(KeyFeedbackPolicy.shouldProvideFeedback(ControllerAction.TOGGLE_MINIMIZE));
  }
}
