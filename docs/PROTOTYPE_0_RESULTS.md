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

RuneBoard 0.17.2 also requests its input view when Android starts a valid text connection. This was validated outside RuneBoard itself with Chrome on the upper display: before focus the IME was hidden; focusing the browser address field triggered `onStartInput`, assigned the IME token to display 4 and produced a visible, drawn RuneBoard surface on the lower display.

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

Left-stick navigation is now validated on the physical Thor. While the RuneBoard IME view is active on display 4, Android delivers the Xbox Wireless Controller's `ABS_X` / `ABS_Y` axes through the IME generic-motion path.

Physical testing found that Thor/Android also emits a matching synthetic `KEYCODE_DPAD_*` approximately 1 ms after analog deflection. RuneBoard now deduplicates that compatibility event. Controlled tests on the real controller input node verified: moderate analog deflection = 0 moves; held analog with jitter = 1 move; two deliberate flicks after a stable center = 2 moves; short center bounce = 1 move; all four cardinal directions work; diagonal input chooses one dominant axis. Physical D-pad tap still moves once and D-pad hold begins autorepeat after the initial delay.

The accessibility service remains key-filter-only; it is not used as the analog-axis source.

## Gate C - transparency

**PASS.**

RuneBoard 0.17.3 cycles the keyboard-background opacity through 255, 170, 85 and 0 (100%, 67%, 33% and 0%). The view clears its transparent buffer before every redraw, while key fills, labels and controller selection keep their independent opacity.

This was validated on the physical AYN Thor lower display (display 4, 1240 x 1080 logical). Display-targeted captures at a fixed background pixel measured RGB 0/0/0, 48/62/19, 95/124/37 and 143/186/56 across the four states, showing approximately even one-third steps in visibility of the application below. Fixed key pixels remained unchanged at 12/12/17 and 21/21/27 across every state.

The physical cycle was also verified as 255 -> 170 -> 85 -> 0 -> 255, with the final return to opaque reproducing the original key and background values. Gate C is therefore closed.

## Gate D — minimize / restore

**PASS.**
The original full prototype measured approximately `1240 × 595` px on the lower display. In 0.17.1, that artificial height cap was removed: RuneBoard now fills the height Android provides to the IME instead of subtracting the navigation-bar inset a second time. Android reports lower-display app bounds of approximately `1240 × 1025` px in landscape on the test Thor.

Minimized, AYN accepts the requested resize and RuneBoard measures approximately `1240 × 134` px.

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
