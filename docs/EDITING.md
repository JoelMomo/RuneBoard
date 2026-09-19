# Editing mode

RuneBoard 0.13.0 introduced a controller-first editing panel without consuming another physical controller binding. RuneBoard 0.14.0 extends that panel with controller-first text selection by character or word. RuneBoard 0.15.0 makes the shared Enter key follow the active Android editor action.

## Entering and leaving

ABC and SYM both expose an EDIT key in the utility row.

Selecting EDIT switches the keyboard to a dedicated editing layout. The keyboard selection automatically lands on the ABC key in the editing panel so returning to normal typing is predictable with D-pad + A.

The mode header changes to EDIT while this panel is active.

## Commands

The editing panel exposes:

- Select All
- Cut
- Copy
- Paste
- Undo
- Redo
- Home
- End
- Cursor Left
- Cursor Right
- Word Left
- Word Right
- Select Left
- Select Right
- Select Word Left
- Select Word Right
- Forward Delete
- Backspace
- Space
- Enter

All keys use the same geometry-aware D-pad navigation and touch hit testing as the normal keyboard.

## Android integration

EditorCommand is a keyboard-domain enum. KeyboardEngine only emits abstract commands.

RuneBoardImeService converts them into the active Android InputConnection:

- Select All / Cut / Copy / Paste / Undo / Redo use performContextMenuAction.
- Home / End send Android MOVE_HOME / MOVE_END key events.
- Cursor and word movement reuse RuneBoard's existing navigation methods.
- Select Left / Right and Select Word Left / Right use `SelectionController` to preserve a selection anchor and call `InputConnection.setSelection`.
- Character selection advances by Unicode code point so surrogate pairs are not split.
- Word selection uses the same boundaries as `WordNavigator`.
- Forward Delete uses deleteSurroundingText(0, 1).
- Enter resolves the active `EditorInfo`: Go, Search, Send, Next, Done, Previous and custom action labels call `performEditorAction`; ordinary/no-action fields insert a newline.
- `IME_FLAG_NO_ENTER_ACTION` always keeps literal newline behavior even when an action code is present.

Commands that can modify text request a fresh suggestion pass afterwards.

## Clipboard privacy

RuneBoard does not maintain a private clipboard history.

Copy, Cut and Paste delegate to the editor/Android context-menu mechanism. RuneBoard does not store copied text in its own SharedPreferences or files.

## Suggestions

Entering EDIT hides any visible suggestion candidates. Editing commands clear stale candidates before operating so an old spelling suggestion cannot be applied after a selection or clipboard operation.

## Layout state

KeyboardState now has three explicit modes:

- ALPHABET
- SYMBOLS
- EDIT

Shift is only active in ALPHABET mode. Switching to SYM or EDIT clears one-shot/Caps state.

ABC/SYM/EDIT switches return KeyboardEngine.Update.GEOMETRY: the IME window size is unchanged, only key hit targets are rebuilt.

## Validation

Pure JVM coverage verifies:

- editor layout command mapping;
- explicit ABC return key;
- EDIT mode entry/exit;
- Shift clearing when entering EDIT;
- EditorCommand dispatch through KeyboardEngine;
- character and word selection extension in both directions;
- direction reversal across the anchor;
- extension of an existing editor selection from the requested edge;
- Unicode surrogate-pair safety for character selection.

Android emulator validation completed against a real EditText using controller input:

- Select All + Cut cleared `alpha beta`;
- Paste restored `alpha beta`;
- Copy + End + Paste duplicated the selected text without RuneBoard storing clipboard contents;
- Undo/Redo changed and restored editor content through the Android editor stack;
- Home/End moved insertion to the exact text boundaries;
- Cursor Left/Right and Word Left/Right moved insertion to the expected character/word positions;
- Forward Delete removed the character immediately after the caret;
- Select Left produced range `10 -> 9`; cutting it changed `alpha beta` to `alpha bet`;
- Select Word Left produced range `10 -> 6`; cutting it changed `alpha beta` to `alpha `;
- Select Right from Home produced range `0 -> 1`; cutting it changed `alpha beta` to `lpha beta`;
- Select Word Right from Home produced range `0 -> 6`; cutting it changed `alpha beta` to `beta`;
- all four selection commands were reached through D-pad navigation and A on the six-row EDIT layout;
- exiting EDIT returned to the alphabet layout and controller navigation resumed from the EDIT key position.

RuneBoard 0.15 editor-action validation on the same emulator also confirmed:

- Start/Enter in RuneBoard's multiline test field inserted a literal newline (`alpha` -> `alpha\n`);
- Android Settings search exposed `imeOptions=0x10000003`, which RuneBoard resolved as `SEARCH` / action ID 3;
- pressing the remappable physical Enter action dispatched `performEditorAction(3)` and Android returned `handled=true`.

Undo/Redo grouping remains editor-defined: RuneBoard delegates to Android `performContextMenuAction` and does not implement its own undo history.
