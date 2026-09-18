# RuneBoard architecture - Prototype 0.0.3

## Principle

RuneBoard is a standard Android IME. AYN Thor firmware is responsible for placing the IME on the lower display.

The application separates keyboard behavior from Android rendering so the product UI can evolve without destabilizing text input or physical controls.

## Layers

### keyboard/KeyboardKey

Immutable description of one key:

- key type;
- optional text value;
- relative width/weight.

### keyboard/KeyboardLayout

Immutable collection of keyboard rows and keys.

The view does not own the layout definition.

### keyboard/KeyboardLayouts

Factory for built-in layouts.

Prototype 0 currently exposes QWERTY with an always-visible number row.

### keyboard/KeyboardState

Owns mutable keyboard state:

- selected row/column;
- shift;
- opacity;
- minimized state.

Vertical D-pad movement follows the physical center of weighted keys rather than assuming equal column counts.

### keyboard/KeyboardEngine

Pure keyboard behavior.

It receives abstract `ControllerAction` values or touch selection changes and produces output callbacks:

- commit text;
- backspace;
- space;
- enter;
- cursor movement;
- minimize/restore.

This layer has no Android View dependency and is unit tested.

### controller/ControllerAction

Platform-independent actions such as:

- MOVE_LEFT;
- PRESS_SELECTED;
- BACKSPACE;
- CURSOR_RIGHT;
- TOGGLE_MINIMIZE.

### controller/ControllerMapper

Maps Android `KeyEvent` codes to `ControllerAction`.

`BUTTON_A` and `DPAD_CENTER` intentionally map to different actions. Some Android devices synthesize DPAD_CENTER after other gamepad buttons; keeping them separate prevents accidental restore while RuneBoard is minimized.

### RuneKeyboardView

Custom Android View responsible for:

- rendering;
- cached key geometry;
- touch hit testing;
- converting joystick/hat motion into controller actions;
- applying visual/layout invalidations.

Keyboard logic no longer lives in the renderer.

### RuneBoardImeService

Android `InputMethodService` adapter.

It owns the active `InputConnection` and converts engine output into:

- text commits;
- deletion;
- editor actions;
- cursor selection changes.

### RuneBoardControlService

Accessibility service used only for physical key filtering.

It does not retrieve window content.

While RuneBoard is expanded, mapped controller buttons are captured for keyboard operation.

While RuneBoard is minimized, navigation/editing buttons are allowed to pass through. Only explicit restore actions remain captured.

### MainActivity

Setup and local test screen:

- enable RuneBoard;
- select the active IME;
- open Physical Controls accessibility settings;
- test text field.

## Rendering

Key rectangles are calculated when the View size/layout changes and cached for drawing and touch hit testing.

Normal redraws for selection, shift or opacity do not allocate new key geometry.

## Validation

### Unit tests

Current JVM tests cover:

- initial selection;
- horizontal wrapping;
- weighted vertical navigation;
- opacity cycling;
- minimized movement policy;
- one-shot shift;
- text/editing output callbacks;
- minimize/restore capture policy;
- controller mapping;
- BUTTON_A vs DPAD_CENTER behavior.

### Emulator

Dedicated AVD:

- Android 15 / API 35;
- 1240x1080;
- 320 dpi.

Useful for general IME/controller regressions.

A simulated secondary display works for Activities, but stock Android keeps the IME on display 0. It cannot reproduce AYN's lower-display IME policy.

### AYN Thor

Real-hardware validation remains authoritative for:

- cross-display IME placement;
- physical controller behavior specific to Thor firmware;
- lower-panel transparency;
- final minimize/restore behavior.

The Thor must only be used after explicit user permission.
