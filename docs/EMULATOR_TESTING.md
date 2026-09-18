# RuneBoard emulator testing

RuneBoard has a dedicated local AVD named `runeboard-test`.

## Configuration

- Android 15 / API 35
- Google APIs x86_64
- 1240 × 1080
- 320 dpi
- hardware keyboard enabled
- `show_ime_with_hard_keyboard=1`

This matches the AYN Thor lower display's landscape working resolution closely enough for layout and controller regression testing.

## What the emulator can validate

- Android IME lifecycle
- touch input
- D-pad/button mappings
- cursor movement
- minimize/restore
- opacity state changes
- keyboard layout geometry
- unit/integration regressions

## Simulated second display

Android's overlay-display feature can create a second 1240 × 1080 display:

```
settings put global overlay_display_devices 1240x1080/320
```

The resulting overlay is currently `displayId=2`.

RuneBoard activities can run there and Android correctly reports editors on `targetDisplayId=2`.

However, stock Android 15 still creates the IME on `imeDisplayId=0`. Therefore the emulator does **not** reproduce the AYN Thor firmware policy that places the IME on the lower display while the focused editor remains on the upper display.

Thor-specific display placement must continue to be validated on real hardware, with explicit permission before using the device.
