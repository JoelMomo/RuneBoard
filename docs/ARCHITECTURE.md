# RuneBoard architecture - 0.17.0 prototype

## Principle

RuneBoard is a standard Android IME. AYN Thor firmware is responsible for placing the IME on the lower display.

The application separates keyboard behavior from Android rendering so the product UI can evolve without destabilizing text input or physical controls.

## Layers

### keyboard/KeyboardKey

Immutable description of one key:

- key type;
- optional text value;
- optional `EditorCommand`;
- relative width/weight.

### keyboard/KeyboardLayout

Immutable collection of keyboard rows and keys.

The view does not own the layout definition.

### keyboard/KeyboardLayouts

Factory for built-in layouts.

RuneBoard exposes language-specific alphabet layouts, secondary symbol layouts and one shared controller-first editing layout. Alphabet and symbol layouts keep the always-visible number row; EDIT replaces typing rows with editing commands while preserving the same renderer/navigation model.

### keyboard/EditorCommand

Platform-independent editing commands for selection, clipboard actions, undo/redo, document navigation and forward deletion. The keyboard engine emits these without depending on Android APIs.

### keyboard/SelectionController

Maintains the anchor/caret pair for controller-first extended selection. Character selection advances by Unicode code point, word selection reuses `WordNavigator`, and an existing editor selection is extended from the edge matching the requested direction.

### editor/EditorActionResolver

Converts Android `EditorInfo` action metadata into a small `EditorActionSpec`. Standard Go/Search/Send/Next/Done/Previous actions, custom labels/action IDs and `IME_FLAG_NO_ENTER_ACTION` are resolved in one place so rendering and execution stay consistent.

### editor/EditorInputPolicy

Classifies Android input fields for contextual typing behavior. It centralizes which fields may request spelling suggestions and which capitalization modes RuneBoard should ask the active `InputConnection` to evaluate. Normal/message text defaults to sentence capitalization, names/addresses to word capitalization, explicit Android capitalization flags are honored, and email/URI/password/non-text fields disable automatic capitalization.

### keyboard/KeyboardState

Owns mutable keyboard state:

- selected row/column;
- shift mode (off / automatic / one-shot / Caps Lock);
- opacity;
- minimized state;
- current mode: `ALPHABET`, `SYMBOLS` or `EDIT`.

Vertical D-pad movement follows the physical center of weighted keys rather than assuming equal column counts.

### keyboard/KeyboardEngine

Pure keyboard behavior.

It receives abstract `ControllerAction` values or touch selection changes and produces output callbacks:

- commit text;
- backspace;
- space;
- enter;
- cursor movement;
- word movement;
- abstract `EditorCommand` dispatch;
- minimize/restore.

This layer has no Android View dependency and is unit tested. Automatic Shift is represented separately from manual one-shot Shift and Caps Lock, so editor context never overrides a deliberate manual state.

### controller/ControllerAction

Platform-independent actions such as:

- MOVE_LEFT;
- PRESS_SELECTED;
- BACKSPACE;
- CURSOR_RIGHT;
- WORD_LEFT / WORD_RIGHT;
- TOGGLE_MINIMIZE.

### controller/ControllerMapper

Maps Android `KeyEvent` codes to `ControllerAction`. D-pad navigation remains fixed; editing actions resolve through persisted `ControllerBindings`.

`BUTTON_A` and `DPAD_CENTER` intentionally map to different actions. Some Android devices synthesize DPAD_CENTER after other gamepad buttons; keeping them separate prevents accidental restore while RuneBoard is minimized.

### controller/ControllerBindings

Owns the remappable A/B/X/Y, L1/R1, L2/R2, Start/Select and L3/R3 assignments.

Assigning an occupied button swaps the two actions, guaranteeing unique and reachable bindings. Repeatability follows the resolved action, so remapped Backspace, cursor and word-navigation controls still repeat.

### keyboard/KeyRepeatPolicy

Centralizes which controller actions and touch keys are safe to repeat. Navigation, deletion and selection movement may repeat; one-shot actions such as Enter, Shift, Space, language switching and suggestion acceptance never do. Touch repeat uses a delayed start and a faster steady cadence.

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
- cursor selection changes;
- previous/next word cursor movement through `WordNavigator`;
- character/word selection extension through `SelectionController` and `InputConnection.setSelection`;
- Android context-menu editing actions such as Select All, Cut, Copy, Paste, Undo and Redo;
- Home/End key events and forward deletion;
- `InputConnection.getCursorCapsMode(...)` queries using `EditorInputPolicy`, refreshed after typing, deletion, cursor movement and editor selection updates;
- context-aware Enter/editor-action dispatch using the same resolved action shown on the key.

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
- select visual themes and background opacity;
- remap physical controller actions;
- reset mappings to the Thor defaults;
- test text field.

## Rendering

Key rectangles are calculated when the View size/layout changes and cached for drawing and touch hit testing.

Normal redraws for selection, shift or opacity do not allocate new key geometry.

ABC/SYM/EDIT switching uses `KeyboardEngine.Update.GEOMETRY`: RuneKeyboardView rebuilds hit targets and redraws without requesting a new IME window layout. `Update.LAYOUT` remains reserved for real size changes such as minimize/restore.

The view also subtracts the bottom navigation-bar inset before distributing row height so the utility row remains above Android system navigation.

## Validation

### Unit tests

Current JVM tests cover:

- initial selection;
- horizontal wrapping;
- weighted vertical navigation;
- opacity cycling;
- minimized movement policy;
- manual one-shot/Caps shift and editor-driven automatic Shift priority;
- input-field capitalization policy for prose, names/addresses, explicit `CAP_*`, sensitive/address-like and non-text fields;
- text/editing output callbacks;
- editor-layout command mapping and EDIT mode entry/exit;
- minimize/restore capture policy;
- controller mapping and occupied-button swapping;
- repeatability after remapping;
- word-boundary navigation;
- Android editor-action resolution, including standard actions, custom labels and `IME_FLAG_NO_ENTER_ACTION`;
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


### suggestion/

Contains the text-service integration without coupling it to rendering or controller input:

- WordContext extracts the word before the cursor;
- AndroidSpellSuggestionSource wraps Android SpellCheckerSession;
- SuggestionResult is the provider-independent result model;
- SuggestionText owns autocorrect eligibility and case adaptation.
