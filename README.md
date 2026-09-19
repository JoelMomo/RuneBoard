# RuneBoard

[![Android CI](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml/badge.svg)](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml) [![Apps & tools](https://img.shields.io/badge/Apps%20%26%20tools-Browse-6F8F72?style=flat-square)](https://joelmomo.github.io/)

RuneBoard is an experimental dual-screen Android keyboard designed primarily for the **AYN Thor**.

The project is currently in **Prototype 0**. The Thor-specific architecture has been validated on physical hardware and is now being refactored into a modular product foundation.

## Prototype controls

- D-pad: move keyboard selection
- A: press selected key
- B: backspace
- X: space
- Y: Shift -> Caps Lock -> off
- L1 / R1: move text cursor left / right
- L2 / R2: previous / next word
- Start: enter / active editor action
- Select: minimize
- L3: next language
- R3: accept primary suggestion
- Touch: direct key input

The DS-style controls are validated on AYN Thor hardware. Twelve editing actions can be remapped; D-pad navigation remains fixed. Analog-stick navigation remains experimental.

## Current implementation

Prototype 0 contains:

- a real Android `InputMethodService`;
- a touch QWERTY keyboard with an always-visible number row;
- a narrowly scoped accessibility service for physical controller buttons;
- a separate keyboard model, state engine and controller mapper;
- geometry-aware D-pad navigation across rows with different key widths;
- L1/R1 cursor movement and L2/R2 word navigation through the active `InputConnection`;
- hold-to-repeat for D-pad navigation, Backspace, cursor movement and word movement, with touch autorepeat on repeatable editing keys;
- controller-first EDIT mode with Select All, Cut, Copy, Paste, Undo, Redo, Home, End, cursor/word navigation and character/word selection extension;
- context-aware Enter key behavior and labels for Go, Search, Send, Next, Done, Previous and custom Android editor actions;
- field-aware capitalization: sentence starts for prose, word starts for names/addresses, explicit Android cap flags when present, and no automatic caps for email/URL/password/non-text editors;
- persistent typing profiles for English QWERTY, Spanish QWERTY, French AZERTY and Russian JCUKEN;
- quick language cycling from L3 or the keyboard header;
- ABC/SYM mode switching without closing the IME;
- direct comma/period keys plus language-specific accent and punctuation pages;
- navigation-bar inset handling so the utility row stays tappable;
- system spell-check suggestions with up to three header candidates;
- touch or remappable R3 acceptance of the primary suggestion;
- conservative autocorrect on Space for strong typo recommendations;
- Suggestions and Autocorrect toggles, both enabled by default;
- persistent physical-button remapping with automatic conflict swapping;
- context-aware auto-capitalization plus manual one-shot Shift and persistent Caps Lock;
- action keys show the currently mapped physical button;
- four keyboard opacity levels;
- persistent background opacity across IME recreation;
- theme profiles: Default, OLED Black, Transparent and persistent Custom;
- custom settings UI with live theme/background controls, palette presets and key geometry;
- compact/minimized mode;
- standard Android clipboard actions without maintaining a private clipboard history;
- a setup/test activity;
- unit tests for state, controller mappings and minimized capture policy;
- GitHub Actions running unit tests, lint and APK assembly;
- a dedicated Android emulator for non-Thor regression testing.

There are no third-party runtime dependencies.

## Architecture

The product code is split into independent layers:

- `keyboard/`: layout, key model, ABC/SYM/EDIT state and keyboard engine;
- `controller/`: controller actions and Android key-code mapping;
- `theme/`: visual profiles, persistent custom-theme data and supported background-opacity levels;
- `language/`: typing-language profiles and layout metadata;
- `editor/`: editor-action resolution and input-field policy;
- `suggestion/`: word extraction, Android spell-check adapter and correction policy;
- `settings/`: persistent user preferences;
- `RuneKeyboardView`: rendering, touch hit testing and motion-event adapter;
- `RuneBoardImeService`: Android IME and `InputConnection` adapter;
- `RuneBoardControlService`: physical-button filtering while RuneBoard is active.

See `docs/ARCHITECTURE.md`.

## Build

Current development version: **0.17.0-prototype**

Current minimum Android version: **Android 13 / API 33**.

GitHub Actions runs:

1. unit tests;
2. Android lint;
3. debug APK assembly.

The successful workflow exposes `RuneBoard-prototype-debug` as an artifact.

## Hardware validation

Prototype 0 was tested on an AYN Thor running Android 13 / firmware `.377`.

Validated on the physical device:

- AYN pins RuneBoard to the lower display while the editor remains on the upper display;
- touch input on the lower display writes into the upper-display editor;
- D-pad and A/B/X/Y controller typing;
- L1/R1 text-cursor movement and L2/R2 word navigation;
- EN/ES/FR/RU layout switching, persisted across IME recreation;
- L3 quick-cycle language switching;
- R3 primary-suggestion acceptance and touch-selectable candidate chips;
- conservative Space autocorrection for recommended typos;
- live physical-button remapping, including conflict swapping and reset;
- Shift/Caps state cycling and compact-mode Start pass-through;
- compact minimize/restore with controller pass-through;
- opacity-state switching;
- controller/touch EDIT panel with clipboard and document-navigation actions.

Measured lower-display keyboard sizes during the prototype:

- full: approximately `1240x595` px;
- compact: approximately `1240x134` px.

The lower panel is `1080x1240` native and `1240x1080` in landscape.

See:

- `docs/PROTOTYPE_0_TEST_PLAN.md`
- `docs/PROTOTYPE_0_RESULTS.md`
- `docs/EMULATOR_TESTING.md`
- `docs/CONTROLLER_MAPPING.md`
- `docs/LANGUAGES.md`
- `docs/SUGGESTIONS.md`
- `docs/SYMBOLS.md`
- `docs/CUSTOMIZATION.md`
- `docs/EDITING.md`

## Emulator limitation

A dedicated Android 15 emulator can simulate a second `1240x1080` display and RuneBoard activities can run on it.

Stock Android still places the IME on display 0 even when the focused editor is on display 2. The emulator therefore cannot reproduce AYN's special cross-display IME policy.

Thor-specific display placement remains a real-hardware test.

## Scope

Prototype 0 intentionally does not include swipe typing, cloud prediction, accounts or telemetry. RuneBoard now integrates Android system spelling for suggestions and conservative autocorrection. Japanese is intentionally deferred until RuneBoard has a real kana/kanji composition layer.

RuneBoard is Thor-first. General Android support can be evaluated later.

## Status

**Build, unit tests and lint: passing.**

**Core Thor architecture: validated.**

**RuneBoard Default visual system: implemented in emulator.**

**Physical controller remapping, Shift/Caps and compact capture policy: validated on the real Thor.**

**Final visual tuning on the physical Thor panel: pending.**

Experimental - not ready for daily use.

## Contributing

Bug reports, feature ideas and focused pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) before opening one.

For security or privacy-sensitive reports, follow [SECURITY.md](SECURITY.md) instead of posting details publicly.

<div align="center">

## Support development

These projects are free to use and developed in my spare time. If they've been useful to you, you can help support future development.

<p>
  <a href="https://github.com/sponsors/JoelMomo">
    <img src="https://img.shields.io/badge/GitHub%20Sponsors-Sponsor-EA4AAA?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="Sponsor on GitHub">
  </a>
  <a href="https://ko-fi.com/joelmomodev">
    <img src="https://img.shields.io/badge/Ko--fi-One--time%20tip-FF5E5B?style=for-the-badge&logo=kofi&logoColor=white" alt="Leave a tip on Ko-fi">
  </a>
</p>

<sub>All projects remain free regardless of support.</sub>

</div>
