package io.github.joelmomo.runeboard.controller;

public final class ControllerCapturePolicy {

  public static final String TEST_PREVIEW_IME_OPTION = "runeboard.test-preview";

  private ControllerCapturePolicy() {}

  public static boolean shouldCaptureGlobally(boolean testPreviewEditor) {
    return !testPreviewEditor;
  }
}