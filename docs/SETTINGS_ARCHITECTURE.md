# RuneBoard settings architecture

RuneBoard keeps product settings separate from the keyboard engine.

## RunePreferences

`settings/RunePreferences` is a thin Android `SharedPreferences` adapter.

Current persisted values:

- `theme_id`
- `background_opacity`

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
