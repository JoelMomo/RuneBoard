# Custom appearance

RuneBoard 0.0.12 adds a persistent Custom theme on top of the existing theme model.

## Theme profiles

RuneBoard now exposes four profiles:

- Default
- OLED Black
- Transparent
- Custom

Custom is still a normal KeyboardTheme. The renderer does not contain special-case styling logic.

## Persisted custom values

RunePreferences stores:

- custom_accent
- custom_key_fill
- custom_background_top
- custom_background_bottom
- custom_key_radius
- custom_key_gap

The Custom theme is reconstructed from those values before RuneKeyboardView is created.

## Presets

### Accent

- Purple
- Cyan
- Green
- Amber
- Pink

### Key fill

- Graphite
- Black
- Navy
- Violet

### Background

- Rune
- Black
- Navy
- Violet

Background presets use two-color gradients.

### Key shape

- Square: 4 dp
- Round: 12 dp
- Soft: 20 dp

### Spacing

- Tight: 3 dp
- Normal: 6 dp
- Wide: 9 dp

CustomThemeConfig clamps external geometry values to safe bounds:

- radius: 2-24 dp
- gap: 2-10 dp

## Activation

Changing any Custom control immediately:

1. persists the changed value;
2. selects theme_id=custom;
3. refreshes the settings preview;
4. requests an IME appearance refresh.

The Custom theme card preview is rebuilt from the current persisted values.

## Reset

RESET CUSTOM STYLE removes all custom_* overrides but intentionally keeps theme_id=custom.

This returns Custom to its RuneBoard-derived defaults without unexpectedly switching the user to another theme.

## Validation

Validated on the dedicated 1240x1080 Android emulator:

- Custom controls are visible in settings;
- changing Cyan / Navy / Violet / Soft / Tight persisted the expected values;
- theme_id changed to custom automatically;
- values survived force-stop and process recreation;
- RuneBoard recreated its IME with the saved Custom configuration;
- RESET CUSTOM STYLE removed the overrides and retained theme_id=custom.

The pure theme layer is also covered by JVM tests for custom colors, geometry and clamp behavior.
