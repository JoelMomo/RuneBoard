# Mobile interface profile

RuneBoard is one keyboard with two presentations that share the same engine, languages, suggestions, themes and settings:

- **THOR**: the original dual-screen panel. It fills the lower display, shows controller hints and keeps the Thor-specific show workaround. Nothing about it changes with this profile.
- **MOBILE**: a compact touch keyboard for single-screen Android phones.

## Choosing the profile

Settings → **INTERFACE** offers Automatic, AYN Thor and Phone. The choice is stored as `ui_profile` (`auto`, `thor`, `mobile`); the default is Automatic.

Automatic (`UiProfileResolver`) only looks at what the device is, never at orientation or window width, so a rotated phone stays a phone and a Thor can't fall into the phone layout. Any one of these keeps THOR:

- manufacturer or brand `AYN`;
- model or build display id starting with `AYN`/`Thor` (Thor firmware reports `Thor_V1.0.0.377_…`);
- a gamepad built into the device (paired Bluetooth/USB pads are ignored);
- the IME window running on a non-default display, which is how the Thor firmware places it on the lower screen.

A device with none of them resolves to MOBILE. If detection is wrong, pick AYN Thor or Phone manually.

## What MOBILE changes

| Area | THOR | MOBILE |
|---|---|---|
| Height | All the height Android offers the IME | 31 % of the screen in portrait (220–280 dp), 48 % in landscape (140–190 dp), plus the navigation-bar inset, never more than offered |
| Layout | Command row, number row, 3 letter rows, action row | Compact editing bar (COPY PASTE UNDO REDO EDIT; drag the space bar to move the cursor) + 3 letter rows + action row |
| Numbers/symbols | Number row always visible; SYM page | `?123` page with digits, symbols and the language extras (¿ ¡ € …) |
| Accents | Long press | Long press; Spanish keeps a dedicated Ñ key |
| Header | Title, profile, controller names, BG %, suggestions from 48 % width | None. Up to three suggestions replace the editing bar while there are any; the language is chosen in Settings → TYPING |
| Controller hints (A/B/X/Y…) | Shown on action keys | Hidden |
| On-screen Enter | Always types a newline | Shows and runs the editor action (Send, Search, Go, Next, Done…), newline otherwise |
| Opacity / minimize keys | In the command row | Not shown |
| Forced show on `onStartInput` (`requestShowSelf` + 140 ms `showWindow`) | Yes | No |
| Hardware keyboard attached | On-screen keyboard always shown | Android's own rule |
| Accessibility service | Setup step required | Marked optional; only needed to type with a game controller |

A game controller connected to a phone still navigates the keyboard while it is visible, through the same capture policy as the Thor: nothing is captured while the keyboard is hidden.

## Testing

## Nothing Ndot typeface

Settings → APPEARANCE → Typeface offers **Nothing Ndot** when the device ships `/system/fonts/Ndot-57.otf` (Nothing OS). The font is loaded from the system at runtime and is never bundled in the APK; on other devices the option is hidden and a stored choice falls back to the system font. It works on both profiles.

Unit tests: `UiProfileResolverTest`, `KeyboardHeightPolicyTest`, `MobileLayoutTest`.

Emulator: a phone AVD such as Pixel 7 / API 35 (1080 × 2400, 420 dpi). The `runeboard-test` AVD from `EMULATOR_TESTING.md` resolves to MOBILE under Automatic (it has no AYN identity or built-in gamepad); choose AYN Thor in settings to test the Thor presentation there.
