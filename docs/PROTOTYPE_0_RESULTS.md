# Prototype 0 — AYN Thor hardware results

Test device:

- AYN Thor
- Android 13
- Firmware `Thor_V1.0.0.377_20260206_165408_user`
- Upper display: logical display 0
- Lower display: logical display 4
- Lower panel: 1080 × 1240 native, 1240 × 1080 in landscape

## Gate A — lower-display IME

**PASS.**

AYN settings already pin RuneBoard to the lower display. Android reports RuneBoard on `imeDisplayId=4` while the target editor remains on `targetDisplayId=0`.

Touch input on the lower display successfully inserts text into the editor on the upper display.

## Gate B — physical controls

**PASS for the DS-style control scheme.**

A companion accessibility service filters controller key events only while RuneBoard is visible. It requests no window-content access.

Validated:
- D-pad navigation
- A: selected key
- B: backspace
- X: space
- Y: Shift
- L1 / R1: cursor left / right
- Select: minimize / restore
- touch input on the lower panel

L1/R1 use `InputConnection.setSelection()` because synthetic DPAD cursor events were not reliable on the Thor.

Analog-stick navigation is not yet validated. Android 13 accessibility can filter controller key events but does not provide an equivalent global joystick-axis stream.

## Gate C — transparency

**FUNCTIONAL, visual tuning still required.**

RuneBoard cycles through opacity values 255, 205, 145 and 85 while remaining active. The view now clears its transparent buffer before every redraw.

ADB screenshots do not reliably preserve the visible differences between all semi-transparent levels, so final appearance must be tuned visually on the physical lower panel during the design phase.

## Gate D — minimize / restore

**PASS.**
The full prototype measured approximately `1240 × 595` px on the lower display.

Minimized, AYN accepted the requested resize and RuneBoard measured approximately `1240 × 134` px.

The IME remains selected and can be restored with the controller.

## Architecture decision

Prototype 0 validates the project architecture:

- standard Android `InputMethodService` for text entry;
- AYN firmware for lower-display IME placement;
- touch handled directly by the IME view;
- narrowly scoped accessibility service for physical button filtering;
- no root requirement;
- no overlay permission required for the core keyboard.

The project can proceed to product/UI development.
