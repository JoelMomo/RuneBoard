# RuneBoard

[![Android CI](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml/badge.svg)](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml) [![Apps & tools](https://img.shields.io/badge/Apps%20%26%20tools-Browse-6F8F72?style=flat-square)](https://joelmomo.github.io/)

RuneBoard is an experimental dual-screen Android keyboard designed primarily for the **AYN Thor**.

The project is currently in **Prototype 1**. Prototype 0 closed all Thor hardware gates; the current phase focuses on product-level visual hierarchy, setup/settings UX and regression-safe polish on top of that validated architecture.

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

The DS-style controls are validated on AYN Thor hardware. Twelve editing actions can be remapped; D-pad navigation remains fixed. Left-stick keyboard navigation is also validated on the Thor while the IME view is active: each deliberate stick gesture advances one key, while D-pad hold uses delayed autorepeat.

## Current implementation

Prototype 1 starts from the validated Prototype 0 foundation:

- a real Android `InputMethodService`;
- a touch QWERTY keyboard with a compact command row, an always-visible number row directly above the letters and a thumb-oriented bottom action row;\n- Prototype 1 keyboard chrome with a dedicated header surface, quieter command controls, stronger typing-key hierarchy and a more explicit selected-key halo;
- a narrowly scoped accessibility service for physical controller buttons;
- a separate keyboard model, state engine and controller mapper;
- geometry-aware D-pad navigation across rows with different key widths;
- L1/R1 cursor movement and L2/R2 word navigation through the active `InputConnection`;
- hold-to-repeat for D-pad navigation, Backspace, cursor movement and word movement, with touch autorepeat on repeatable editing keys;
- controller-first EDIT mode with Select All, Cut, Copy, Paste, Undo, Redo, Home, End, cursor/word navigation and character/word selection extension;
- a dedicated on-screen ↵ key that always inserts a newline, while the remappable physical Enter action (Start by default) keeps Android's contextual Go/Search/Send/Next/Done/Previous behavior;
- field-aware capitalization: sentence starts for prose, word starts for names/addresses, explicit Android cap flags when present, and no automatic caps for email/URL/password/non-text editors;
- persistent typing profiles for English QWERTY, Spanish QWERTY, French AZERTY and Russian JCUKEN;
- quick language cycling from L3 or the keyboard header;
- ABC/SYM mode switching without closing the IME;
- direct comma/period keys plus language-specific accent and punctuation pages;
- lower-display geometry that fills Android's available IME bounds without double-applying the navigation-bar inset;
- automatic IME presentation when an external text field gains focus, including a Thor-specific delayed show pass for the secondary display;
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

Current development version: **0.18.0-prototype**

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
- D-pad and A/B/X/Y controller typing, including delayed D-pad autorepeat while held;
- one-step analog-stick navigation with Android's synthetic D-pad compatibility events deduplicated;
- L1/R1 text-cursor movement and L2/R2 word navigation;
- EN/ES/FR/RU layout switching, persisted across IME recreation;
- L3 quick-cycle language switching;
- R3 primary-suggestion acceptance and touch-selectable candidate chips;
- conservative Space autocorrection for recommended typos;
- live physical-button remapping, including conflict swapping and reset;
- Shift/Caps state cycling and compact-mode Start pass-through;
- compact minimize/restore with controller pass-through;
- tuned background-opacity switching at 100%, 67%, 33% and 0%;
- controller/touch EDIT panel with clipboard and document-navigation actions.

On the test Thor, Android reports lower-display app bounds of approximately `1240x1025` px in landscape. The 0.17.1 full keyboard now fills the IME-provided height instead of using the earlier approximately `1240x595` px cap. Compact mode remains approximately `1240x134` px.

The lower panel is `1080x1240` native and `1240x1080` in landscape.

See:

- `docs/PROTOTYPE_0_TEST_PLAN.md`
- `docs/PROTOTYPE_0_RESULTS.md`\n- `docs/PROTOTYPE_1_RESULTS.md`
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

## Prototype 1 scope

Prototype 1 is the product-polish phase: keyboard visual hierarchy, setup/settings UX, theme consistency and regression-safe daily-use testing. It intentionally does not reopen the validated IME/controller architecture unless a regression requires it.

Swipe typing, cloud prediction, accounts, telemetry, Japanese composition and broad general-Android support remain out of scope for this phase.

See `docs/PROTOTYPE_1_PLAN.md` for the current product baseline and acceptance rules.

RuneBoard remains Thor-first. General Android support can be evaluated later.

## Status

**Build, unit tests and lint: passing.**

**Core Thor architecture: validated.**

**Prototype 0 hardware gates: complete.**

**Physical controller remapping, Shift/Caps, transparency and compact capture policy: validated on the real Thor.**

**Prototype 1 product/visual polish: in progress.**

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
