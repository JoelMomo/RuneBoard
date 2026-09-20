# Prototype 0 — Thor hardware test plan

The first APK is a feasibility build. Visual polish is deliberately out of scope.

## Gate A — IME on lower display

- Install the debug APK.
- Open RuneBoard.
- Enable RuneBoard under Android input-method settings.
- Select RuneBoard as the active keyboard.
- Use the AYN Thor setting that pins the software keyboard to the lower display.
- Focus a text field on the upper display.
- Confirm RuneBoard stays on the lower display and enters text into the upper-display app.

**Pass:** typing works across displays without RuneBoard launching a separate overlay/activity.

## Gate B — physical controls

Test while a text field is focused:

- D-pad: selection moves in all four directions.
- Left stick: selection moves without uncontrolled repeats.
- A: selected key is entered.
- B: deletes one character.
- X: inserts a space.
- Y: toggles Shift.
- L1 / R1: moves the text cursor left / right.
- Start: Enter/editor action.
- Select: minimizes RuneBoard.
- A while minimized: restores RuneBoard.

Record any Thor key codes that do not match the provisional Android gamepad mapping.

## Gate C — transparency

Tap the **BG** key repeatedly.

Expected states are 100%, 67%, 33%, and 0% keyboard-background opacity.

**Pass:** content from the application behind the IME is visibly readable at reduced opacity, while RuneBoard remains usable.

## Gate D — minimize / restore

Tap **Min** or press Select.

**Pass criteria:**

- RuneBoard collapses to a compact bar.
- The IME remains active.
- The lower-display app becomes substantially more visible.
- Tapping the bar or pressing A restores the full keyboard.
- Text focus on the original editor is preserved.

If the AYN pinned-IME window refuses to shrink, this gate is marked **firmware-limited** and we test an alternate implementation before proceeding.

## Decision

RuneBoard moves from Prototype 0 to visual/product development only when Gates A–D are either passed or have a clean workaround.
