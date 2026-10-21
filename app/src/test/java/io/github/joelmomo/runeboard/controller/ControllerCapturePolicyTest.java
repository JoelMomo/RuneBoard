package io.github.joelmomo.runeboard.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

public final class ControllerCapturePolicyTest {

  @Test
  public void externalEditorUsesGlobalControllerCapture() {
    assertTrue(ControllerCapturePolicy.shouldCaptureGlobally(false));
  }

  @Test
  public void internalPreviewNeverUsesGlobalControllerCapture() {
    assertFalse(ControllerCapturePolicy.shouldCaptureGlobally(true));
  }

  @Test
  public void visibleKeyboardOnTextEditorCapturesController() {
    assertTrue(
        ControllerCapturePolicy.shouldCaptureForEditor(
            true, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL));
  }

  @Test
  public void hiddenKeyboardNeverCapturesController() {
    assertFalse(
        ControllerCapturePolicy.shouldCaptureForEditor(false, InputType.TYPE_CLASS_TEXT));
  }

  @Test
  public void nullInputTypeGameSurfaceNeverCapturesController() {
    assertFalse(ControllerCapturePolicy.shouldCaptureForEditor(false, InputType.TYPE_NULL));
    assertFalse(ControllerCapturePolicy.shouldCaptureForEditor(true, InputType.TYPE_NULL));
  }
}
