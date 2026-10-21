package io.github.joelmomo.runeboard.controller;

import android.text.InputType;

public final class ControllerCapturePolicy {

  public static final String TEST_PREVIEW_IME_OPTION = "runeboard.test-preview";

  private ControllerCapturePolicy() {}

  public static boolean shouldCaptureGlobally(boolean testPreviewEditor) {
    return !testPreviewEditor;
  }

  /**
   * Controller input belongs to RuneBoard only while its keyboard is on screen for a real text
   * editor. Games often keep a TYPE_NULL view focused, which still routes every key and motion
   * event through the IME first; those must reach the game while the keyboard is hidden.
   */
  public static boolean shouldCaptureForEditor(boolean inputViewShown, int inputType) {
    return inputViewShown && inputType != InputType.TYPE_NULL;
  }
}
