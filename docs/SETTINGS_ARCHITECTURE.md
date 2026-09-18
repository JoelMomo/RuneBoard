# RuneBoard settings architecture

RuneBoard keeps product settings separate from the keyboard engine.

## RunePreferences

`settings/RunePreferences` is a thin Android `SharedPreferences` adapter.

Current persisted values:

- `theme_id`
- `background_opacity`
- `binding_*` controller assignments

The keyboard engine does not read Android preferences directly.

## Background opacity

Supported background values are defined in `theme/BackgroundOpacity`:

- 255 (100%)
- 180 (71%)
- 90 (35%)
- 0 (0%)

Arbitrary stored values are normalized to the nearest supported level.

When the user activates the `BG` key:

1. `KeyboardState` advances to the next supported level.
2. `KeyboardEngine` emits `onBackgroundOpacityChanged`.
3. `RuneBoardImeService` stores the value through `RunePreferences`.
4. A newly created IME reads the saved value before constructing `RuneKeyboardView`.

This path has been validated on the dedicated Android emulator by changing BG to 35%, killing the RuneBoard process, recreating it, and confirming the next BG step starts from the persisted value.

## Themes

Built-in profile IDs:

- `default`
- `oled`
- `transparent`

Each profile owns:

- background palette;
- key palette;
- selection palette;
- key alpha;
- utility-key alpha;
- geometry metrics;
- default background opacity.

An unknown theme ID falls back to `default`.

The UI selector is intentionally separate from this storage/model layer.

## Privacy and backup

RuneBoard does not back up app data through Android cloud backup or device transfer.

This avoids moving keyboard configuration or future learned typing data implicitly between devices.

Explicit export/import can be added later for user-controlled backups.

## Settings UI

MainActivity now provides a custom RuneBoard settings surface instead of stock-looking setup buttons.

Current controls:

- Android keyboard enable shortcut;
- active IME picker;
- Physical Controls accessibility shortcut;
- visual theme cards for Default, OLED Black and Transparent;
- background opacity chips for 100%, 71%, 35% and 0%;
- built-in test text field.

Theme and opacity changes persist immediately. If the RuneBoard IME service is alive, the activity also requests an input-view appearance refresh so the next visible keyboard uses the new profile without restarting the app.

The settings UI was validated on the dedicated 1240x1080 emulator.


## Physical controller mappings

RunePreferences persists the ten remappable editing actions as `binding_*` integer key codes.

Changing a mapping loads the current ControllerBindings, swaps conflicts if necessary, persists the complete unique mapping, and refreshes the active IME view.

The settings UI captures the next supported physical button after the user taps an action. D-pad navigation is deliberately fixed. RESET CONTROLS removes all binding overrides and restores the Thor defaults.

The mapping flow, L2/R2 word navigation, minimized pass-through and a live A/X swap were validated on the physical AYN Thor with RuneBoard running on display 4.
