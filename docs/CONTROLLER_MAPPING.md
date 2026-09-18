# Physical controller mapping

RuneBoard treats the AYN Thor controller as a first-class keyboard input method.

## Default mapping

| Control | Action |
| --- | --- |
| D-pad | Move keyboard selection |
| A | Confirm / press selected key |
| B | Backspace |
| X | Space |
| Y | Shift / Caps Lock |
| L1 / R1 | Cursor left / right |
| L2 / R2 | Previous / next word |
| Start | Enter |
| Select | Minimize |
| Touch | Direct key input |

D-pad navigation is intentionally fixed so a bad configuration cannot make the keyboard impossible to navigate.

## Remapping

The settings activity exposes ten remappable actions:

- Confirm
- Backspace
- Space
- Shift
- Cursor left
- Cursor right
- Previous word
- Next word
- Enter
- Minimize

Supported assignable controls are A/B/X/Y, L1/R1, L2/R2, Start/Select and L3/R3.

Selecting an action and pressing a controller button assigns it immediately.

If that button already belongs to another action, RuneBoard swaps the two assignments. This guarantees that every action remains reachable and no two actions share one physical button.

RESET CONTROLS removes all custom mappings and restores the default Thor profile.

## Persistence

Mappings are stored through RunePreferences as binding_* values.

The IME builds its ControllerMapper from persisted ControllerBindings. Changing or resetting a mapping requests an IME input-view refresh so the new mapping becomes active without restarting RuneBoard.

Repeat behavior follows the action rather than the original physical key. For example, if Backspace is moved from B to X, holding X becomes the repeatable delete control.

## Word navigation

L2/R2 use WordNavigator and the active Android InputConnection.

They move the selection directly to the previous or next word boundary instead of synthesizing controller/D-pad input.

Word characters currently include letters, digits, underscore and apostrophe.

## Minimized mode

While RuneBoard is minimized, navigation and editing actions are allowed to pass through to the application/game below.

Only explicit restore-capable actions remain captured.

BUTTON_A and Android's synthetic DPAD_CENTER are deliberately distinct. Some Android devices emit DPAD_CENTER as a compatibility fallback after another gamepad button; treating it separately prevents accidental restore.

## Real AYN Thor validation

Validated on AYN Thor / Android 13 / firmware .377.

With RuneBoard active on mCurTokenDisplayId=4, the hardware produced:

- A -> Confirm
- B -> Backspace
- X -> Space
- Y -> Shift
- L1 / R1 -> cursor left / right
- L2 / R2 -> previous / next word
- Start -> Enter
- Select -> minimize

Minimized behavior was also verified: X did not restore the keyboard, while A restored it.

A live remap was then tested on the physical device:

- Confirm changed from A to X
- Space automatically swapped from X to A
- the active lower-display IME resolved X as Confirm and A as Space

After validation, RESET CONTROLS restored the defaults and the Thor system setting ime_fixed was returned to its original value (0).


## Shift and Caps Lock

Shift uses a deterministic three-state cycle: Off -> one-shot Shift -> Caps Lock -> Off.

One-shot Shift is consumed after the next text key. Caps Lock remains active until Shift is pressed again. The on-screen Shift key changes to CAPS while locked.

Action keys also render the currently mapped physical-button label, and the minimized bar shows the actual Confirm and Minimize buttons used to restore RuneBoard.
