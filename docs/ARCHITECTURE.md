# RuneBoard architecture — Prototype 0

## Principle

RuneBoard is a normal Android IME. AYN Thor firmware remains responsible for pinning the IME to the lower display.

## Components

### `MainActivity`

Small setup/test screen used to enable RuneBoard, open the input-method picker, and provide a local text field.

### `RuneBoardImeService`

Android `InputMethodService` implementation. It owns the active `InputConnection` and converts RuneBoard actions into text, deletion, editor actions, and cursor movement.

### `RuneKeyboardView`

Dependency-free custom View responsible for:

- drawing the prototype keyboard;
- touch hit-testing;
- keyboard selection;
- physical key mapping;
- joystick / hat-axis navigation;
- opacity switching;
- compact/minimized state.

## Deliberate Prototype 0 constraints

- Android 13+ / Thor-first.
- Java/platform APIs only.
- No prediction engine yet.
- No persistent settings yet.
- No final layout or visual system yet.
- Physical mappings are provisional until captured on real Thor hardware.

## Risk gates

The architecture is accepted only after real-device validation of:

1. lower-display IME pinning;
2. controller input reaching the IME reliably;
3. translucency over the lower-screen application;
4. IME window resizing cleanly between full and compact states.
